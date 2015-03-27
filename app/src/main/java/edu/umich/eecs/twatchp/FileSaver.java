package edu.umich.eecs.twatchp;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Created by Arun on 2/22/2015.
 */
public class FileSaver extends Thread {
    TapBuffer  btTap, recTap;
    MainActivity mainActivity;
    //ArrayList<Byte> btData, recData;
    boolean running = true;
    byte [] tmpBuffer = new byte [44100];

    int saved_count = 0;
    FileOutputStream phone_tmp, watch_tmp;

    String TAG = "FileSaver";

    public static String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    public static String AUDIO_RECORDER_FOLDER = "twatch";
    String AUDIO_RECORDER_PHONE_TMP;
    String AUDIO_RECORDER_WATCH_TMP;

    long now;

    boolean closeBTwhenDone = false, closeRECwhenDone = false;

    public FileSaver (MainActivity mainActivity, TapBuffer btTap, TapBuffer  recTap) {
        this.mainActivity = mainActivity;
        this.btTap = btTap;
        this.recTap = recTap;

        //btData = new ArrayList<Byte>();
        //recData = new ArrayList<Byte>();

        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);
        File tmpPhone = new File(filepath, "phone_temp.raw");
        File tmpWatch = new File(filepath, "watch_temp.raw");
        AUDIO_RECORDER_PHONE_TMP = tmpPhone.getAbsolutePath();
        AUDIO_RECORDER_WATCH_TMP = tmpWatch.getAbsolutePath();
    }

    public void run () {
        Log.v(TAG, "Running filesaver in thread " + currentThread().getName());

        while (running) {
            now = System.currentTimeMillis();
            if (!btTap.isTapOpen() && !recTap.isTapOpen())
                try {
                    //Log.v(TAG, "Both taps closed, sleeping thread " + currentThread().getName());
                    this.sleep(150);
                } catch (Exception e)
                { }
            else {
                //Log.v(TAG, "Opening taps in FSaver");
                if (btTap.isTapOpen() && btTap.howMany() != 0) {
                    int got = btTap.getSome(tmpBuffer, tmpBuffer.length);
                    try {
                        watch_tmp.write(tmpBuffer, 0, got);
                    } catch (Exception e) {}
                }

                if (recTap.isTapOpen() && recTap.howMany() != 0) {
                    int got = recTap.getSome(tmpBuffer, tmpBuffer.length);
                    //for (int i = 0; i < got; i++) recData.add(tmpBuffer[i]);
                    try {
                        phone_tmp.write(tmpBuffer, 0, got);
                    } catch (Exception e) {}
                }

                if (closeRECwhenDone && recTap.howMany() == 0) {
                    recTap.closeTap();
                }

                if ((closeBTwhenDone && btTap.howMany() == 0) && (closeRECwhenDone && recTap.howMany() == 0)) {
                    btTap.closeTap();
                    recTap.closeTap();
                    try {
                        phone_tmp.close();
                        watch_tmp.close();
                    } catch (Exception e) {}

                    mainActivity.player.turnOffSound();

                    String filepath = Environment.getExternalStorageDirectory().getPath();
                    File file = new File(filepath, AUDIO_RECORDER_FOLDER);

                    String watch_filename = file.getAbsolutePath() + "/watch." + now + AUDIO_RECORDER_FILE_EXT_WAV;
                    String phone_filename = file.getAbsolutePath() + "/phone." + now + AUDIO_RECORDER_FILE_EXT_WAV;
                    AudioFile.CopyWaveFile(AUDIO_RECORDER_PHONE_TMP, phone_filename, mainActivity.recorder.bufferSize);
                    AudioFile.CopyWaveFile(AUDIO_RECORDER_WATCH_TMP, watch_filename, mainActivity.recorder.bufferSize);
                    //AudioFile.SaveFromBuffer(btData, watch_filename, mainActivity.recorder.bufferSize);
                    //AudioFile.SaveFromBuffer(recData, phone_filename, mainActivity.recorder.bufferSize);

                    Log.v(TAG, "Tap size is bt=" + btTap.howMany() + " rec=" + recTap.howMany());
                    Log.v(TAG, "Saved data under name " + now);
                    mainActivity.addInfo("Done!");

                    btTap.emptyBuffer();
                    recTap.emptyBuffer();
                    //btData.clear();
                    //recData.clear();
                    //btData.trimToSize();
                    //recData.trimToSize();
                    System.gc();



                    try {
                        //Thread.sleep(1000);
                        saved_count++;
                        mainActivity.ready();

                        if (saved_count >= 9) {
                            saved_count = 0;
                            //mainActivity.startAutotune();;
                        }

                    } catch (Exception e) {

                    }

                    closeBTwhenDone = false;
                    closeRECwhenDone = false;
                }
            }
        }
    }

    public void doneBTStream () {
        closeBTwhenDone = true;
    }

    public void stopRecording () {
        closeRECwhenDone = true;
    }

    public void startNewFile () {
        File tmpPhone = new File(AUDIO_RECORDER_PHONE_TMP);
        File tmpWatch = new File(AUDIO_RECORDER_WATCH_TMP);

        if (tmpPhone.exists()) tmpPhone.delete();
        if (tmpWatch.exists()) tmpWatch.delete();

        try {
           phone_tmp = new FileOutputStream(tmpPhone);
           watch_tmp = new FileOutputStream(tmpWatch);
        } catch (FileNotFoundException e) { }


        //btData.clear();
        //recData.clear();
        //btData.trimToSize();
        //recData.trimToSize();
    }

    public void shutdown () {
        running = false;
    }
}
