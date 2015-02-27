package edu.umich.eecs.twatchp;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Arun on 8/14/2014.
 */
public class CountdownBuffer {
    //private ArrayList<Byte> buffer = new ArrayList<Byte>();
    private List<Byte> buffer = Collections.synchronizedList(new ArrayList<Byte>());
    private String TAG = "BufferManager";
    private int recordAmount = 0;
    private final static int FS = 44100;

    public CountdownBuffer() {}

    public synchronized ArrayList<Byte> buffer_handle (String action, byte[] array, int length) {
        //Log.e(TAG, "Buffer handling with " + action + " current length is " + buffer.size());
        ArrayList<Byte> tmpBuffer = null;
        if (action.equals("add")) {
            for (int i = 0; i < length; i++) {
                buffer.add(array[i]);
                if (--recordAmount <= 0) break;
            }
        } else if (action.equals("get")) {
            tmpBuffer = new ArrayList<Byte>(buffer);
            buffer.clear();
        }
        return tmpBuffer;
    }

    public int howMany () { return buffer.size(); }
    public boolean doYouWant () { return (recordAmount > 0); }
    public ArrayList<Byte> getAll() { return buffer_handle("get", null, 0); }
    public void addByteArray (byte[] array, int len) { buffer_handle("add", array, len); }
    public void storeData (double seconds) { buffer.clear(); recordAmount = (int)(seconds*FS*4); } // 4 bytes per sound sample, 2 channels
}
