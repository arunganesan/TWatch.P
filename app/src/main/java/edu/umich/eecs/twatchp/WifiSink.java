package edu.umich.eecs.twatchp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaRecorder;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class WifiSink extends Thread {
	MainActivity mainActivity;
	//ArrayList<Byte> btData, recData;
	boolean running = true;
	byte [] tmpBuffer = new byte [44100];

    /**
     * File saving debug parameters
     */
    FileOutputStream phone_tmp;
    public static String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    public static String AUDIO_RECORDER_FOLDER = "twatch";
    String AUDIO_RECORDER_PHONE_TMP;
    File last_phone;
    long now;
	boolean closeRECwhenDone = false;

	String TAG = "ClientThread";
	MainActivity context;

    /*
     * Network related variables
     */
    Socket phoneSocket;
    BufferedInputStream phone_in;
    BufferedOutputStream phone_out;
    TapBuffer recTap;


	public WifiSink (final MainActivity context, Socket phoneSocket, TapBuffer recTap) {
		this.context = context;
        this.mainActivity = context;

        this.phoneSocket = phoneSocket;

        try {
            this.phone_in=  new BufferedInputStream(phoneSocket.getInputStream());
            this.phone_out= new BufferedOutputStream(phoneSocket.getOutputStream());
        } catch (Exception e) {
            Log.e(TAG, "Could not open wifi streams");
        }

        this.recTap = recTap;

        String filepath = Environment.getExternalStorageDirectory().getPath();
        File tmpPhone = new File(filepath, "phone_temp.raw");
        AUDIO_RECORDER_PHONE_TMP = tmpPhone.getAbsolutePath();
    }

    /**
     * Keeps reading in data from the bluetooth tap and the record tap
     * Then sends to server once buffer fills (default buffer = 0)
     * It prefaces each transmission with whether it is BT or REC
     * Then includes teh size of the transmission
     */
    public void run () {
        Log.v(TAG, "Running filesaver in thread " + currentThread().getName());


        while (running) {
            if (!recTap.isTapOpen())
                try {
                    //Log.v(TAG, "Both taps closed, sleeping thread " + currentThread().getName());
                    this.sleep(150);
                } catch (Exception e) {
                }
            else {
                //Log.v(TAG, "Opening taps in FSaver");
                if (recTap.isTapOpen() && recTap.howMany() != 0) {
                    int got = recTap.getSome(tmpBuffer, tmpBuffer.length);
                    try {
                        phone_out.write(tmpBuffer, 0, got);
                        phone_tmp.write(tmpBuffer, 0, got);
                    } catch (Exception e) {
                    }
                }

                if (closeRECwhenDone && recTap.howMany() == 0) {
                    recTap.closeTap();
                }

                if (closeRECwhenDone && recTap.howMany() == 0) {
                    recTap.closeTap();
                    try {
                        phone_tmp.close();
                    } catch (Exception e) {
                    }

                    mainActivity.player.turnOffSound();

                    String filepath = Environment.getExternalStorageDirectory().getPath();
                    File file = new File(filepath, AUDIO_RECORDER_FOLDER);

                    String phone_filename = file.getAbsolutePath() + "/phone." + now + AUDIO_RECORDER_FILE_EXT_WAV;

                    last_phone = new File(phone_filename);

                    AudioFile.CopyWaveFile(AUDIO_RECORDER_PHONE_TMP, phone_filename, mainActivity.recorder.bufferSize);
                    //AudioFile.SaveFromBuffer(btData, watch_filename, mainActivity.recorder.bufferSize);
                    //AudioFile.SaveFromBuffer(recData, phone_filename, mainActivity.recorder.bufferSize);

                    Log.v(TAG, "Tap size is rec=" + recTap.howMany());
                    Log.v(TAG, "Saved data under name " + now);
                    mainActivity.addInfo("Done!");

                    recTap.emptyBuffer();
                    System.gc();


                    try {
                        //Thread.sleep(1000);
                        mainActivity.ready();
                    } catch (Exception e) {

                    }

                    closeRECwhenDone = false;
                }
            }
        }
    }

	public void shutdown () {
		try {
			if (phone_out != null) phone_out.close();
			if (phone_in != null) phone_in.close();
			if (phoneSocket != null) phoneSocket.close();

		} catch (IOException e) {
			Log.e(TAG, "Could not shut down.");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


    public void stopRecording () {
        closeRECwhenDone = true;
    }

    public void startNewFile () {
        File tmpPhone = new File(AUDIO_RECORDER_PHONE_TMP);

        if (tmpPhone.exists()) tmpPhone.delete();

        try {
            phone_tmp = new FileOutputStream(tmpPhone);
        } catch (FileNotFoundException e) { }


        //btData.clear();
        //recData.clear();
        //btData.trimToSize();
        //recData.trimToSize();
    }

}
