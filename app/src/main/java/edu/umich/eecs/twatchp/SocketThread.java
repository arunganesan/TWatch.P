package edu.umich.eecs.twatchp;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

class SocketThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final OutputStream mmOutStream;
    private final InputStream mmInStream;

    private MainActivity myactivity;
    private final String TAG = "ListenerClient";
    public boolean monitor = false;

    public static byte START_AUTOTUNE = 0;
    public static byte STOP_AUTOTUNE = 1;
    public static byte START_BORDER= 2;
    public static byte START_NORMAL = 3;
    public static byte DO_TAP = 4;
    public static byte DO_DRAW = 5;
    public static byte START = 6;
    public static byte STOP = 7;
    public static byte FASTMODE = 9;
    public static byte SLOWMODE = 10;
    public static byte SETSPACE = 11;

    public SocketThread(BluetoothSocket socket, MainActivity myactivity) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        this.myactivity = myactivity;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    private byte [] primArray (ArrayList<Byte> array) {
        byte [] prim = new byte [array.size()];
        for (int i = 0; i < array.size(); i++)
            prim[i] = array.get(i).byteValue();
        return prim;
    }

    public byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(x);
        return buffer.array();
    }

    public long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(bytes);
        buffer.flip();//need flip
        return buffer.getLong();
    }

    public void run() {
        int bytes; // bytes returned from read()
        int i;

        Log.v(TAG, "Started socket thread");
        // Keep listening to the InputStream until an exception occurs

        while (true) {
            try {
                // Read from the InputStream
                int bytesAvailable = mmInStream.available();
                if (bytesAvailable > 0) {
                    byte[] curBuf = new byte[bytesAvailable];

                    bytes = mmInStream.read(curBuf);

                    for (i = 0; i < bytes; i++) {
                        if (curBuf[i] == START) myactivity.startChirping();
                        else if (curBuf[i] == STOP) myactivity.stopChirping();
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Exception");
                e.printStackTrace();
                break;
            }
        }
    }

    public void setWatchSpace (long space) {
        try {
            byte [] longbytes = longToBytes(space);
            byte [] fullmessage = new byte [9];
            fullmessage[0] = SETSPACE;
            for (int i = 0; i < 8; i++) fullmessage[i+1] = longbytes[i];
            mmOutStream.write(fullmessage);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
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