package edu.umich.eecs.twatchp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Connects to the bluetooth watch server.
 */
public class ConnectThread extends Thread {
    private BluetoothSocket mmSocket;
    private BluetoothAdapter myAdapter;
    private UUID DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String TAG = "ConnectThread";
    MainActivity myactivity;
    BluetoothDevice device;

    String address;
    String address1 = "E4:92:FB:3F:2C:6C";
    String address2 = "D8:90:E8:9A:5B:83";

    public ConnectThread(BluetoothAdapter myAdapter, MainActivity myactivity) {
        this.myactivity = myactivity;
        this.myAdapter = myAdapter;
        this.address = address1;
        device = myAdapter.getRemoteDevice(address);
    }

    public void connect () {
        BluetoothSocket tmp = null;
        boolean notconnected = true;

        while (notconnected) {
            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(DEFAULT_UUID);
                tmp.connect();
                notconnected = !tmp.isConnected();
            } catch (Exception e) {
                Log.e(TAG, "Recieved exception - " + e.getLocalizedMessage());
                //myactivity.addInfo("Failed connection: " + e.getLocalizedMessage());

                if (address.equals(address1)) address = address2;
                else address = address1;
                device = myAdapter.getRemoteDevice(address);

                myactivity.addInfo("Now trying: " + address, 250);
                try { Thread.sleep(1000); } catch (Exception ee) { Log.e(TAG, e.getLocalizedMessage()); }


                myactivity.addInfo("BT Connection failed, retrying in 5 seconds.", 250);
                try { Thread.sleep(1000); } catch (Exception ee) { Log.e(TAG, e.getLocalizedMessage()); }
                myactivity.addInfo("BT Connection failed, retrying in 4 seconds.", 0);
                try { Thread.sleep(1000); } catch (Exception ee) { Log.e(TAG, e.getLocalizedMessage()); }
                myactivity.addInfo("BT Connection failed, retrying in 3 seconds.", 0);
                try { Thread.sleep(1000); } catch (Exception ee) { Log.e(TAG, e.getLocalizedMessage()); }
                myactivity.addInfo("BT Connection failed, retrying in 2 seconds.", 0);
                try { Thread.sleep(1000); } catch (Exception ee) { Log.e(TAG, e.getLocalizedMessage()); }
                myactivity.addInfo("BT Connection failed, retrying in 1 seconds.", 0);
                try { Thread.sleep(1000); } catch (Exception ee) { Log.e(TAG, e.getLocalizedMessage()); }
            }
        }
        mmSocket = tmp;
    }

    public void run () {
        connect();
        myAdapter.cancelDiscovery();
        myactivity.runOnUiThread(new Runnable () {
            public void run () {
                myactivity.setBTSocket(mmSocket);
            }
        });
    }

    public void cancel () {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
