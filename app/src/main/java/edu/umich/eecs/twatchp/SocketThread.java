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


    public static byte START = 6;
    public static byte STOP = 7;
    public static byte STARTFILE = 8;

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
        final byte[] buffer = new byte[4410];  // buffer store for the stream
        int bytes; // bytes returned from read()
        boolean saveMode = false;
        ArrayList<Byte> sizeBuffer = new ArrayList<Byte>();
        long total_received = 0;
        long total_size = -1;
        int i;


        Log.v(TAG, "Started socket thread");
        // Keep listening to the InputStream until an exception occurs


        while (true) {
            try {
                // Read from the InputStream


                int bytesAvailable = mmInStream.available();
                if (bytesAvailable > 0) {
                    byte[] curBuf = new byte[bytesAvailable];
                    byte [] leftover = null;
                    bytes = mmInStream.read(curBuf);

                    if (!saveMode) {
                        for (i = 0; i < bytes; i++) {
                            if (curBuf[i] == START) myactivity.startChirping();
                            else if (curBuf[i] == STOP) myactivity.stopChirping();
                            else if (curBuf[i] == STARTFILE) {
                                tap.emptyBuffer();
                                tap.openTap();
                                sizeBuffer.clear();
                                saveMode = true;

                                Log.v(TAG, "Receiving file");

                                if (bytes - i < 8) Log.e(TAG, "Missing length! Total bytes size is " + bytes + " and we are at " + i);
                                sizeBuffer.add(curBuf[i+1]);
                                sizeBuffer.add(curBuf[i+2]);
                                sizeBuffer.add(curBuf[i+3]);
                                sizeBuffer.add(curBuf[i+4]);
                                sizeBuffer.add(curBuf[i+5]);
                                sizeBuffer.add(curBuf[i+6]);
                                sizeBuffer.add(curBuf[i+7]);
                                sizeBuffer.add(curBuf[i+8]);
                                total_size = bytesToLong(primArray(sizeBuffer));
                                total_received = 0;
                                Log.v(TAG, "Got file size length. Waiting to transfer total: " + total_size);

                                break;

                                //if ((i+1)+8 == bytes) break;
                                //else {
                                //    leftover = new byte[bytes-(i+1)-8];
                                //    System.arraycopy(curBuf, i+8+1, leftover, 0, bytes-(i+1)-8);
                                //    break;
                                //}
                            }
                        }
                    } else if (saveMode) {
                        if (total_size > total_received) {
                            //if (leftover != null && leftover.length != 0) {
                            //    tap.addByteArray(leftover);
                            //    total_received += leftover.length;
                            //    leftover = null;
                            //} else {
                                tap.addByteArrayLen(curBuf, bytes);
                                total_received += bytes;
                            //}

                            //Log.v(TAG, "So far got " + total_received);
                        }

                        if (total_received >= total_size) {
                            Log.v(TAG, "Successfully got the file! Total received " + total_received);
                            myactivity.doneFileReceive();
                            saveMode = false;
                        }
                    }


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