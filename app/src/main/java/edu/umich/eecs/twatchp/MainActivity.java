package edu.umich.eecs.twatchp;

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
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class MainActivity extends Activity {
    TextView statusText;
    SharedPreferences sp;
    BluetoothAdapter mBluetoothAdapter;
    SocketThread bsocket;
    Button autotuneButton;

    Player player;
    Recorder recorder;
    TapBuffer btTap, recTap;
    CountdownBuffer atBuff;
    FileSaver fsaver;
    AutoTuner autotuner;
    String nextMessage = "";

    final static String TAG = "MainActivity";

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
    }


    public void wireUI () {
        statusText = (TextView) findViewById(R.id.statusText);
        String fontPath = "fonts/CaviarDreams.ttf";
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        statusText.setTypeface(tf);

        autotuneButton = new Button(this);
        autotuneButton.setText("Initiate Autotune");
        autotuneButton.setBackgroundColor(0xffcacaca);
        autotuneButton.setAlpha((float) 0.0);

        final RelativeLayout rl = (RelativeLayout)findViewById(R.id.parentView);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lp.addRule(RelativeLayout.BELOW, R.id.statusText);
        lp.setMargins(0,100,0,0);
        rl.addView(autotuneButton, lp);


        autotuneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAutotune();
                autotuneButton.setAlpha(0);
                //rl.removeView(autotuneButton);
            }
        });
    }

    public void initializeTWatch() {
        player = new Player(this);
        player.setSoftwareVolume(0.05);
        player.setSpace((int)(0.05*44100));
        player.turnOffSound(true);
        player.startPlaying();

        btTap = new SpiralBuffer("BTap");
        recTap = new SpiralBuffer("Rectap");
        atBuff = new CountdownBuffer();

        recorder = new Recorder(this, recTap, atBuff);
        fsaver = new FileSaver(this, btTap, recTap);
        autotuner = new AutoTuner(this, atBuff, recorder, player);

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
                fadeAnim.setDuration(250);
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
            fadeAnim.setDuration(250);
            fadeAnim.start();
        }
    };


    public void startAutotune () {
        Log.v(TAG, "Starting auto tuner");
        player.changeSound(Player.CHIRP);
        bsocket.tellWatch(SocketThread.START_AUTOTUNE);
        player.turnOffSound(false);
        autotuner.start();
    }

    public void doneAutotune (boolean success) {
        bsocket.tellWatch(SocketThread.STOP_AUTOTUNE);
        player.turnOffSound(true);
        if (success) {
            bsocket.tellWatch(SocketThread.START_NORMAL);
            player.changeSound(Player.WN);
            bsocket.monitor = true;
            if (!fsaver.isAlive()) fsaver.start();
            ready();
        } else {
            addInfo("Autotuning failed :(");
        }
    }

    public void reportReceipt (int numbytes) {
        if (numbytes == 0) return;
        if (btTap.isTapOpen() == false) {
            addInfo("~~running~~");
            player.turnOffSound(false);
            fsaver.startNewFile();
            btTap.openTap();
            recTap.openTap();
        }
    }

    public void ready () {
        addInfo("Ready!", fadeInButton);

    }


    public void setBTSocket (BluetoothSocket socket) {
        // Even if another one exists, we update it
        initializeTWatch();
        sp.edit().putString("watch address", socket.getRemoteDevice().getAddress());
        bsocket = new SocketThread(socket, this, btTap);
        bsocket.start();

        showAutotuneStep();
        //startAutotune();
        //doneAutotune(true);
    }

    AnimatorListenerAdapter fadeInButton = new AnimatorListenerAdapter () {
        @Override
        public void onAnimationEnd (Animator animation) {
            ValueAnimator fadeAnim = ObjectAnimator.ofFloat(autotuneButton, "alpha", 0f, 1f);
            fadeAnim.setDuration(250);
            fadeAnim.start();
        }
    };

    public void showAutotuneStep () {
        addInfo("Connected. Ready for auto tuning?", fadeInButton);
    }

    public void setupBluetooth () {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        int REQUEST_ENABLE_BT = 1;
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Map<Integer, short[]> map = new HashMap<Integer, short[]>();

        map.put(R.id.chirpSound, Player.CHIRP);
        /*
        map.put(R.id.pnSound, Player.PN);
        map.put(R.id.goldSound, Player.GOLD);
        */
        map.put(R.id.whitenoiseSound, Player.WN);


        map.put(R.id.highWhitenoiseSound, Player.WNHIGH);
        map.put(R.id.highWhitenoiseHannSound, Player.WNHIGHHANN);
        map.put(R.id.highChirpSound, Player.CHIRPHIGH);
        map.put(R.id.highChirpHannSound, Player.CHIRPHIGHHANN);


        int id = item.getItemId();
        player.changeSound(map.get(id));
        Toast.makeText(this, "Changed sound", Toast.LENGTH_LONG).show();
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
        fsaver.shutdown();
    }
}
