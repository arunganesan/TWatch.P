package edu.umich.eecs.twatchp;

/**
 * Created by Arun on 10/24/2014.
 */

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


// Code heavily borrowed from
// http://krvarma-android-samples.googlecode.com/svn/trunk/AudioRecorder.2/src/com/varma/samples/audiorecorder/RecorderActivity.java

public class Recorder {
    static final String TAG = "Recorder";

    public static byte RECORDER_BPP = 16;
    public static int RECORDER_SAMPLERATE = 44100;
    public static int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    public static int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static int RECORDER_SOURCE = MediaRecorder.AudioSource.CAMCORDER;

    public AudioRecord recorder = null;
    private AudioManager aManager = null;
    public int bufferSize = 0;
    public boolean isRecording = false;
    CountdownBuffer countdownBuffer;

    MainActivity context;
    TapBuffer tap;

    public Recorder(MainActivity context, TapBuffer tap, CountdownBuffer countdownBuffer) {
        this.context = context;
        this.tap = tap;
        this.countdownBuffer = countdownBuffer;
        this.aManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING);
    }

    pelic void startRecording(){
        recorder = new AudioRecord(RECORDER_SOURCE, RECORDER_SAMPLERATE, RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, bufferSize);
        recorder.startRecording();
        isRecording = true;
        new Thread(new Runnable() {

            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY);
                writeAudioDataToFile();
            }
        },"AudioRecorder Thread").start();
    }




    private void writeAudioDataToFile(){
        byte data[] = new byte[bufferSize];
        int read = 0;

        while(isRecording){
            read = recorder.read(data, 0, bufferSize);
            if(read != AudioRecord.ERROR_INVALID_OPERATION){
                if (tap.isTapOpen()) tap.addByteArrayLen(data, read);
                if (countdownBuffer.doYouWant()) countdownBuffer.addByteArray(data, read);
            }
        }
    }

    public void stopRecording() {
        Log.v(TAG, "Stopping recorder");
        Log.v(TAG, "State on complete: " + recorder.getState());
        if (recorder != null) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
        }
        Log.v(TAG, "Stopped recording.");
    }
}