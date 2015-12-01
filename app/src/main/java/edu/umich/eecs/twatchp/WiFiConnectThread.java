package edu.umich.eecs.twatchp;

import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * Wifi connector thread. To stream the phone audio data.
 */
public class WiFiConnectThread extends Thread {
    String TAG = "ConnectThread";
    MainActivity myactivity;

    //final String SERVERNAME = "ibrad.eecs.umich.edu";
    //ibrad.eecs.umich.edu";
    int SERVERPORT = 3000;

    Socket socket;

    public WiFiConnectThread(MainActivity myactivity) {
        this.myactivity = myactivity;
   }

    public void connect () {
        boolean notconnected = true;
        while (notconnected) {
            try {
                myactivity.addInfo("Connecting to " + C.SERVERNAME + ":" + SERVERPORT);

                InetAddress serverAddr = InetAddress.getByName(C.SERVERNAME);
                socket = new Socket(serverAddr, SERVERPORT);
                notconnected = false;

                myactivity.runOnUiThread(new Runnable () {
                    public void run () {
                        myactivity.setWiFiSocket(socket);
                    }
                });

                Log.v(TAG, "Starting connection.");
            } catch (UnknownHostException e1) {
                e1.printStackTrace();

                Log.e(TAG, "Recieved exception - " + e1.getLocalizedMessage());
                //myactivity.addInfo("Failed connection: " + e.getLocalizedMessage());

                WiFiAddressDialog dialog = new WiFiAddressDialog();
                dialog.show(myactivity.getFragmentManager(), "AddressDialogFragment");


                //shutdown();
            } catch (IOException e1) {
                e1.printStackTrace();

                Log.e(TAG, "Received exception - " + e1.getLocalizedMessage());
                //myactivity.addInfo("Failed connection: " + e.getLocalizedMessage());

                WiFiAddressDialog dialog = new WiFiAddressDialog();
                dialog.show(myactivity.getFragmentManager(), "AddressDialogFragment");

                /*
                myactivity.addInfo("WiFi failed, retrying in 5 seconds.", 250);
                try { Thread.sleep(1000); } catch (Exception ee) {}
                myactivity.addInfo("WiFi failed, retrying in 4 seconds.", 0);
                try { Thread.sleep(1000); } catch (Exception ee) {}
                myactivity.addInfo("WiFi failed, retrying in 3 seconds.", 0);
                try { Thread.sleep(1000); } catch (Exception ee) {}
                myactivity.addInfo("WiFi failed, retrying in 2 seconds.", 0);
                try { Thread.sleep(1000); } catch (Exception ee) {}
                myactivity.addInfo("WiFi failed, retrying in 1 seconds.", 0);
                try { Thread.sleep(1000); } catch (Exception ee) {}
                //shutdown();
                */
            }
        }
    }



    public void run () {
        connect();
    }

    public void cancel () {
        try {
            socket.close();
        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }


}
