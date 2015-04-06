
package edu.umich.eecs.twatchp;

import android.util.Log;


/**
 * Created by Arun on 4/6/2015.
 */
public class ExperimentManager {
    Player player;
    SocketThread bsocket;
    MainActivity mainActivity;
    FileSaver fsaver;
    String TAG = "ExpMan";

    int exp_count = 0;
    public ExperimentManager (Player player, SocketThread bsocket, MainActivity mainActivity, FileSaver fsaver) {
        this.player = player;
        this.bsocket = bsocket;
        this.mainActivity = mainActivity;
        this.fsaver = fsaver;
    }

    public void do_an_experiment () {
        new Thread () {
            @Override
            public void run () {
                exp_count ++;

                // XXX: ASSUMES ALREADY AUTO TUNED
                fsaver.setPrefix("" + exp_count);

                // Both play, watch 100 step
                Log.v(TAG, "Both play, watch 100...");
                player.countMode = false;
                bsocket.tellWatch(SocketThread.PLAY100);
                bsocket.tellWatch(SocketThread.DO_DRAW);
                try { Thread.sleep(12000); } catch (Exception e) {}
                Log.v(TAG, "Telling watch to stop.");
                bsocket.tellWatch(SocketThread.DO_DRAW);


                Log.v(TAG, "Done. Should receive file now.");
                try { Thread.sleep(12000); }  catch (Exception e) {}
                Log.v(TAG, "Woke up");

                // Both play, phone 100 step
                Log.v(TAG, "Experiment 2, both play, phone 100");
                player.countMode = true;
                bsocket.tellWatch(SocketThread.PLAYCONT);
                bsocket.tellWatch(SocketThread.DO_DRAW);
                mainActivity.startDelay = 4000;
                try { Thread.sleep(20000); } catch (Exception e) {}
                bsocket.tellWatch(SocketThread.DO_DRAW);

                Log.v(TAG, "Done, sleeping");
                try { Thread.sleep(22000); }  catch (Exception e) {}
                Log.v(TAG, "Woke up");


                // Only phone plays
                Log.v(TAG, "Experiment 3, Only phone plays");
                mainActivity.startDelay = 0;
                player.countMode = true;
                player.chirpPlayCount = 0;
                bsocket.tellWatch(SocketThread.SILENCE);
                bsocket.tellWatch(SocketThread.DO_DRAW);
                try { Thread.sleep(12000); }  catch (Exception e) {}
                bsocket.tellWatch(SocketThread.DO_DRAW);

                Log.v(TAG, "Done, sleeping");
                try { Thread.sleep(12000); }  catch (Exception e) {}
                Log.v(TAG, "Woke up");


                // Only watch plays
                player.setSoftwareVolume(0.0);
                Log.v(TAG, "Experiment 4, Only watch plays");
                bsocket.tellWatch(SocketThread.UNSILENCE);
                bsocket.tellWatch(SocketThread.PLAY100);
                bsocket.tellWatch(SocketThread.DO_DRAW);
                try { Thread.sleep(12000); }  catch (Exception e) {}
                bsocket.tellWatch(SocketThread.DO_DRAW);

                Log.v(TAG, "Done, sleeping");
                try { Thread.sleep(12000); }  catch (Exception e) {}
                Log.v(TAG, "Woke up");

                bsocket.tellWatch(SocketThread.PLAYCONT);
                player.setSoftwareVolume(0.4);
            }

        }.start();
    }
}