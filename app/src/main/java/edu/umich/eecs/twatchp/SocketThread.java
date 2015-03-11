package edu.umich.eecs.twatchp;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class SocketThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private MainActivity myactivity;
    private final String TAG = "ListenerClient";
    TapBuffer tap;
    public boolean monitor = false;

    public static byte START_AUTOTUNE = 0;
    public static byte STOP_AUTOTUNE = 1;
    public static byte START_BORDER= 2;
    public static byte START_NORMAL = 3;

    public static byte DO_TAP = 4;
    public static byte DO_DRAW = 5;

    public SocketThread(BluetoothSocket socket, MainActivity myactivity, TapBuffer tap) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        this.myactivity = myactivity;
        this.tap = tap;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }


    public void run() {
        final byte[] buffer = new byte[4410];  // buffer store for the stream
        int bytes; // bytes returned from read()

        Log.v(TAG, "Started socket thread");
        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream

                int bytesAvailable = mmInStream.available();
                if (bytesAvailable > 0) {
                    byte[] curBuf = new byte[bytesAvailable];
                    Log.v(TAG, "Blocking for read");
                    long start = System.currentTimeMillis();
                    bytes = mmInStream.read(curBuf);
                    long end = System.currentTimeMillis();
                    Log.v(TAG, "Received " + bytes + " in " + (end - start) + "ms");
                    if (monitor) myactivity.reportReceipt(bytes);
                    if (tap.isTapOpen()) tap.addByteArrayLen(curBuf, bytes);
                }
            } catch (IOException e) {
                break;
            }
        }
    }

    public void tellWatch (byte COMMAND) {
        // The input loop above might be locked on the inputstream.read
        try {
            mmOutStream.write(COMMAND);
        } catch (Exception e) {}
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}