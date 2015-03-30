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
import android.view.MotionEvent;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
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
    SeekBar phoneVolume;

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
        //fakeSetBTSocket();
    }


    public void wireUI () {
        statusText = (TextView) findViewById(R.id.statusText);
        String fontPath = "fonts/CaviarDreams.ttf";
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        statusText.setTypeface(tf);
        autotuneButton = (Button)findViewById(R.id.autotuneButton);

        autotuneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAutotune();
                //autotuneButton.setAlpha(0);
                //rl.removeView(autotuneButton);
            }
        });

        ((ImageView)findViewById(R.id.tapButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bsocket.tellWatch(SocketThread.DO_TAP);
            }
        });

        ((Button)findViewById(R.id.gotFileButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                doneFileReceive();
            }
        });

        //((ImageView)findViewById(R.id.drawButton)).setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View view) {
        //        bsocket.tellWatch(SocketThread.DO_DRAW);
        //    }
        //});


        ((ImageView)findViewById(R.id.drawButton)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                Log.v(TAG, "Got touch event: " + e.getAction());

                if (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_DOWN) {
                    bsocket.tellWatch(SocketThread.DO_DRAW);
                }

                return true;
            }
        });
    }

    public void initializeTWatch() {
        player = new Player(this);
        player.setSoftwareVolume(0.0); // XXX CHANGE
        player.setSpace((int)(0.1*44100));
        player.turnOffSound();
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


    public void startAutotune () {
        Log.v(TAG, "Starting auto tuner");
        player.changeSound(Player.CHIRP);
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
            player.changeSound(Player.CHIRP);
            bsocket.monitor = true;
            if (!fsaver.isAlive()) fsaver.start();
            player.setSoftwareVolume(0.1); /// XXX CHANGE
            ready();
        } else {
            addInfo("Autotuning failed :(");
        }
    }

    public void startChirping () {
        fsaver.startNewFile();
        player.turnOnSound();
        player.playAligner();
        recTap.openTap();
        btTap.openTap();

        addInfo("Beeping...");
    }

    public void stopChirping() {
        player.turnOffSound();
        fsaver.stopRecording();
    }

    public void doneFileReceive () {
        fsaver.doneBTStream();
    }


    public void ready () {
        addInfo("Ready!", fadeInButton);

    }


    public void fakeSetBTSocket () {
        initializeTWatch();
        //doneAutotune(true);
    }

    public void setBTSocket (BluetoothSocket socket) {
        // Even if another one exists, we update it
        initializeTWatch();
        sp.edit().putString("watch address", socket.getRemoteDevice().getAddress());
        bsocket = new SocketThread(socket, this, btTap);
        bsocket.start();

        //showAutotuneStep();
        //startAutotune();
        doneAutotune(true); // XXX: CHANGE TO ENABLE AUTO TUNE
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
            case R.id.volumeLowest: player.setSoftwareVolume(0.2); break;
            case R.id.volumeLow: player.setSoftwareVolume(0.4); break;
            case R.id.volumeMedium: player.setSoftwareVolume(0.6); break;
            case R.id.volumeHigh: player.setSoftwareVolume(0.8); break;
            case R.id.volumeHighest: player.setSoftwareVolume(1); break;
            case R.id.volumeSilence: player.setSoftwareVolume(0); break;
            //case R.id.highChirpSound: player.changeSound(Player.CHIRPHIGH); break;
            //case R.id.highWhitenoiseSound: player.changeSound(Player.WNHIGH); break;

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
        fsaver.shutdown();
    }
}
