
package edu.umich.eecs.twatchp;

import android.util.Log;


/**
 * Created by Arun on 4/6/2015.
 */
public class ExperimentManager {
    Player player;
    SocketThread bsocket;
    MainActivity mainActivity;
    String TAG = "ExpMan";

    int exp_count = 0;
    public ExperimentManager (Player player, SocketThread bsocket, MainActivity mainActivity) {
        this.player = player;
        this.bsocket = bsocket;
        this.mainActivity = mainActivity;
    }

    public void do_16_points () {
        new Thread () {
            @Override
            public void run () {
                for (int i = 0; i < 16; i++) {
                    bsocket.tellWatch(SocketThread.DO_DRAW);
                    try {
                        Thread.sleep(1500);
                    } catch (Exception e) {
                    }
                    bsocket.tellWatch(SocketThread.DO_DRAW);
                    try {
                        Thread.sleep(2000);
                    } catch (Exception e) {
                    }
                }
            }

        }.start();
    }

    public void do_border () {
        new Thread () {
            @Override
            public void run () {
                for (int i = 0; i < 6; i++) {
                    bsocket.tellWatch(SocketThread.DO_DRAW);
                    try {
                        Thread.sleep(4000);
                    } catch (Exception e) {
                    }
                    bsocket.tellWatch(SocketThread.DO_DRAW);
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {
                    }
                }
            }

        }.start();
    }
}
