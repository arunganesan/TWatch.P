package edu.umich.eecs.twatchp;

/**
 * Created by Arun on 4/6/2015.
 */
public class ExperimentManager {
    Player player;
    SocketThread bsocket;
    MainActivity mainActivity;
    FileSaver fsaver;

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
                player.contMode = false;
                bsocket.tellWatch(SocketThread.PLAY100);
                bsocket.tellWatch(SocketThread.DO_DRAW);
                try { Thread.sleep(10000); } catch (Exception e) {}
                bsocket.tellWatch(SocketThread.DO_DRAW);

                try { Thread.sleep(10000); }  catch (Exception e) {}

                // Both play, phone 100 step
                player.contMode = true;
                bsocket.tellWatch(SocketThread.PLAYCONT);
                bsocket.tellWatch(SocketThread.DO_DRAW);
                try { Thread.sleep(10000); } catch (Exception e) {}
                bsocket.tellWatch(SocketThread.DO_DRAW);

                try { Thread.sleep(10000); }  catch (Exception e) {}

                // Only phone plays
                player.contMode = true;
                bsocket.tellWatch(SocketThread.SILENCE);
                bsocket.tellWatch(SocketThread.DO_DRAW);
                try { Thread.sleep(10000); }  catch (Exception e) {}
                bsocket.tellWatch(SocketThread.DO_DRAW);

                try { Thread.sleep(10000); }  catch (Exception e) {}

                // Only watch plays
                player.setSoftwareVolume(0.0);
                bsocket.tellWatch(SocketThread.UNSILENCE);
                bsocket.tellWatch(SocketThread.PLAY100);
                bsocket.tellWatch(SocketThread.DO_DRAW);
                try { Thread.sleep(10000); }  catch (Exception e) {}
                bsocket.tellWatch(SocketThread.DO_DRAW);

                try { Thread.sleep(10000); }  catch (Exception e) {}
            }

        }.start();
    }
}
