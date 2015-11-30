package edu.umich.eecs.twatchp;

import android.os.Vibrator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.MotionEvent;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class MainActivity extends Activity {
    TextView statusText;
    SharedPreferences sp;
    BluetoothAdapter mBluetoothAdapter;
    SocketThread bsocket;
    SeekBar phoneVolume;

    RelativeLayout parentView;

    ImageView holdButton, toggleButton;

    Player player;
    Recorder recorder;
    TapBuffer btTap, recTap;
    CountdownBuffer atBuff;
    FileSaver fsaver;
    AutoTuner autotuner;
    WifiSink wifisink;
    String nextMessage = "";

    BluetoothSocket btSocket;
    Socket phoneSocket, watchSocket;

    final static String TAG = "MainActivity";

    enum Mode {TOGGLE, HOLD}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        sp = getSharedPreferences("twatch", Context.MODE_PRIVATE);


        wireUI();

        /**
         * 1. If first time, show list of paired devices and ask user to choose.
         * 2. Start BT Client thread to watch device
         * 3. Once connected, use BTSocket. Connection established.
         *
         * --- Then can receive ChirpStream from watch
         * --- Use watch commands to start and stop chirping (for all taps)
         * --- Tell watch to autotune chirp, to border trace
         */

        setupBluetooth();
        //fakeSetBTSocket();
    }


    public void wireUI () {
        statusText = (TextView) findViewById(R.id.statusText);
        String fontPath = "fonts/CaviarDreams.ttf";
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        statusText.setTypeface(tf);
        parentView = (RelativeLayout)findViewById(R.id.parentView);

        holdButton = (ImageView)findViewById(R.id.drawButton);
        toggleButton = (ImageView)findViewById(R.id.toggleButton);

        holdButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent e) {
                Log.v(TAG, "Got touch event: " + e.getAction());

                if (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_DOWN) {
                    bsocket.tellWatch(SocketThread.DO_DRAW);
                }

                return true;
            }
        });


        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (player.isSoundOn()) toggleButton.setImageResource(R.drawable.start);
                else toggleButton.setImageResource(R.drawable.stop);
                bsocket.tellWatch(SocketThread.DO_DRAW);
            }
        });

        setMode(Mode.HOLD);
    }

    public void setMode (Mode mode) {
        //if (mode == C)
        parentView.removeView(holdButton);
        parentView.removeView(toggleButton);
        if (mode == Mode.TOGGLE) parentView.addView(toggleButton);
        if (mode == Mode.HOLD) parentView.addView(holdButton);
    }


    public void initializeTWatch() {
        player = new Player(this);
        player.setSoftwareVolume(0.1);
        player.setSpace((int) (0.5 * 44100));
        player.turnOffSound();
        player.startPlaying();

        btTap = new SpiralBuffer("BTap");
        recTap = new SpiralBuffer("Rectap");
        atBuff = new CountdownBuffer();

        recorder = new Recorder(this, recTap, atBuff);
        //fsaver = new FileSaver(this, btTap, recTap);
        autotuner = new AutoTuner(this, atBuff, recorder, player);
        wifisink = new WifiSink(this, phoneSocket, watchSocket, recTap, btTap);
        bsocket = new SocketThread(btSocket, this, btTap);

        bsocket.start();
        recorder.startRecording();



        /**
         * - Create player
         * - Create 2 Taps and 1 Countdown
         * - Give 1 tap to bluetooth socket
         * - Create recorder, give it 1 tap
         * - Create file saver, give it both taps (1 from BT and 1 from recorder)
         * - Create auto tuner, give it 1 countdown
         *
         * And then, do auto tuning routine:
         *  1. Tell watch to play chirp
         *  2. Go through auto tune procedure
         *  3. Once done, tell watch to stop
         *
         * And then, tell watch to resume normal operation mode
         *
         * Then! Anytime we get data through BT socket,
         *  1. recorder.start()
         *  2. recordTap.open()
         *  3. file.startNewFile()
         *  4. file saver keeps emptying the taps into two buffers (one for watch and phone)
         *  5. if it doesn't receive anything for ~0.5 seconds, it initiates shut down sequence
         *      a. stops recorder
         *      b. save files under the same name
         *      c. clears and closes recordTap
         *      - (eventually, we don't save and just process the stream in real time *somehow*)
         */
    }






    public void startAutotune () {
        Log.v(TAG, "Starting auto tuner");
        player.setSoftwareVolume(0.2);
        bsocket.tellWatch(SocketThread.START_AUTOTUNE);
        player.turnOnSound();
        autotuner.start();
    }

    public void doneAutotune (boolean success) {
        bsocket.tellWatch(SocketThread.STOP_AUTOTUNE);
        player.turnOffSound();

        if (success) {
            bsocket.tellWatch(SocketThread.START_NORMAL);
            bsocket.monitor = true;
            //if (!fsaver.isAlive()) fsaver.start();
            if (!wifisink.isAlive()) wifisink.start();
            player.setSoftwareVolume(0.4);
            ready();

        } else {
            addInfo("Autotuning failed :(");
        }
    }

    public void startChirping () {
        //fsaver.startNewFile();
        wifisink.startNewFile();
        player.turnOnSound();
        player.playAligner();
        recTap.openTap();
        btTap.openTap();

        addInfo("Beeping...");
    }

    public void stopChirping() {
        player.turnOffSound();
        wifisink.stopRecording();
        //fsaver.stopRecording();
    }

    public void doneFileReceive () {
        //fsaver.doneBTStream();
        wifisink.doneBTStream();
    }


    public void ready () {
        //Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        //v.vibrate(500);

        addInfo("Ready!");
    }


    public void fakeSetBTSocket () {
        initializeTWatch();
        //doneAutotune(true);
    }

    /**
     * Called by the blueooth connect thread after a connection has been esablished.
     *
     * @param socket The socket ued to perform any communication with BT
     */
    public void setBTSocket (BluetoothSocket socket) {
        // Even if another one exists, we update it
        sp.edit().putString("watch address", socket.getRemoteDevice().getAddress());
        this.btSocket = socket;
        addInfo("Connected to bluetooth.");
        setupNetwork("phone");
    }

    /**
     * Sets up the network. Simply try to connect to the server
     * and return the socket in a callback function once connection
     * has been established.
     *
     * Does this in a separate thread
     */
    public void setupNetwork (String name) {
        addInfo("Connecting to network...");
        new WiFiConnectThread("phone", this).start();
    }


    /**
     * Callback function from the wifi connect thread. After this, the initialization
     * progresses sequentially by calling doneNetworks()
     *
     * @param socket
     */
    public void setWiFiSocket (String name, Socket socket) {
        if (name == "phone") phoneSocket = socket;
        else if (name == "watch") watchSocket = socket;

        if (name == "phone") {
            addInfo("Connected phone. Trying watch.");
            new WiFiConnectThread("watch", this).start();
            return;
        }

        addInfo("Connected to both streams.");
        doneNetworks();
    }

    public void doneNetworks () {
        initializeTWatch();
        setSpeed("slow");
        // XXX. Enable autotune for actual usage.
        //showAutotuneStep();
        //startAutotune();
        doneAutotune(true);
    }

    public void showAutotuneStep() {
        addInfo("Connected. Please autotune");
    }

    /**
     * Sets up the bluetooth connection.
     */
    public void setupBluetooth () {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        int REQUEST_ENABLE_BT = 1;
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        //sp.edit().putString("watch address", "E4:92:FB:3F:2C:6C").commit();
        sp.edit().putString("watch address", "D8:90:E8:9A:5B:83").commit();

        if (!sp.contains("watch address")) {
            // XXX: This is a hack solution for now
            Log.v(TAG, "Looking for watches");

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            BluetoothDevice target = null;
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                        if (device.getName().contains("Gear")) {
                        sp.edit().putString("watch address", device.getAddress()).commit();
                        break;
                    }
                }
            }
        }

        if (!sp.contains("watch address")) {
            Log.e(TAG, "Could not find device! Exiting");
            statusText.setText("Watch not found");
        } else {
            String address = sp.getString("watch address", "0");
            assert !address.equals("0");
            Log.v(TAG, "Found previous connection " + address);
            new ConnectThread(address, mBluetoothAdapter, this).start();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void setSpeed(String mode) {
        if (mode.equals("slow")) {
            player.sound = Player.LONGCHIRP;
            //player.setSpace((int)(0.1*44100));
            player.setSpace((int)(0.5*44100));
            bsocket.tellWatch(bsocket.SLOWMODE);
            autotuner.sound = autotuner.longchirp;
            //startAutotune();
        } else {
            player.sound = Player.SHORTCHIRP;
            player.setSpace((int)(0.05*44100));
            bsocket.tellWatch(bsocket.FASTMODE);
            autotuner.sound = autotuner.shortchirp;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        /*
        Map<Integer, short[]> map = new HashMap<Integer, short[]>();

        map.put(R.id.chirpSound, Player.CHIRP);
        map.put(R.id.pnSound, Player.PN);
        map.put(R.id.goldSound, Player.GOLD);
        map.put(R.id.whitenoiseSound, Player.WN);
        map.put(R.id.highWhitenoiseSound, Player.WNHIGH);
        map.put(R.id.highWhitenoiseHannSound, Player.WNHIGHHANN);
        map.put(R.id.highChirpSound, Player.CHIRPHIGH);
        map.put(R.id.highChirpHannSound, Player.CHIRPHIGHHANN);
        int id = item.getItemId();
        player.changeSound(map.get(id));
        Toast.makeText(this, "Changed sound", Toast.LENGTH_LONG).show();
        */

        switch (item.getItemId()) {
            case R.id.initiateAutotune: startAutotune(); break;
            case R.id.cutOff: doneFileReceive(); break;
            case R.id.clearLast: fsaver.deleteLast(); break;
            case R.id.switchToSlow: setSpeed("slow"); break;
            case R.id.switchToFast: setSpeed("fast"); break;
            case R.id.setHoldMode: setMode(Mode.HOLD); break;
            case R.id.setToggleMode: setMode(Mode.TOGGLE); break;
        }

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("OnDestroy");
        player.stopPlaying();
        recorder.stopRecording();
        if (recorder.recorder != null) recorder.recorder.release();
        if (player.audioTrack != null) player.audioTrack.release();
        bsocket.cancel();
        wifisink.shutdown();
        //fsaver.shutdown();
    }



























    public void addInfo (final String message, final int time) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ValueAnimator fadeAnim = ObjectAnimator.ofFloat(statusText, "alpha", 1f, 0f);
                fadeAnim.setDuration(time);
                fadeAnim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        statusText.setText(nextMessage);
                        ValueAnimator fadeAnim = ObjectAnimator.ofFloat(statusText, "alpha", 0f, 1f);
                        fadeAnim.setDuration(time);
                        fadeAnim.start();
                    }
                });
                fadeAnim.start();
                nextMessage = message;
            }
        });
    }

    public void addInfo (final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ValueAnimator fadeAnim = ObjectAnimator.ofFloat(statusText, "alpha", 1f, 0f);
                fadeAnim.setDuration(500);
                fadeAnim.addListener(doneFadeOut);
                fadeAnim.start();

                nextMessage = message;
            }
        });
    }

    public void addInfo (final String message, final AnimatorListenerAdapter customAction) {
        Log.v(TAG, "In overridden add info");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ValueAnimator fadeAnim = ObjectAnimator.ofFloat(statusText, "alpha", 1f, 0f);
                fadeAnim.setDuration(250);
                fadeAnim.addListener(customAction);
                fadeAnim.addListener(doneFadeOut);
                fadeAnim.start();
                //statusText.animate().alpha(0.5f).setDuration(250).setListener(doneFadeOut).setListener(customAction);
                Log.v(TAG, "Setting message: " + nextMessage);
                nextMessage = message;
            }
        });
    }

    AnimatorListenerAdapter doneFadeOut = new AnimatorListenerAdapter () {
        @Override
        public void onAnimationEnd (Animator animation) {
            statusText.setText(nextMessage);
            ValueAnimator fadeAnim = ObjectAnimator.ofFloat(statusText, "alpha", 0f, 1f);
            fadeAnim.setDuration(500);
            fadeAnim.start();
        }
    };
}
