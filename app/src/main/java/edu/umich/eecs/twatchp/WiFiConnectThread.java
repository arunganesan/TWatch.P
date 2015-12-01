package edu.umich.eecs.twatchp;

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
 * Created by Arun on 11/17/2014.
 */
public class WiFiConnectThread extends Thread {
    String TAG = "ConnectThread";
    MainActivity myactivity;

    //final String SERVERNAME = "ibrad.eecs.umich.edu";
    final String SERVERNAME = "192.168.0.10";
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
                myactivity.addInfo("Connecting to " + SERVERNAME + ":" + SERVERPORT);

                InetAddress serverAddr = InetAddress.getByName(SERVERNAME);
                socket = new Socket(serverAddr, SERVERPORT);
                notconnected = false;

                Log.v(TAG, "Starting connection.");
                // XXX: If cannot connect, stop, and try again later in a few seconds.

            } catch (UnknownHostException e1) {
                e1.printStackTrace();

                Log.e(TAG, "Recieved exception - " + e1.getLocalizedMessage());
                //myactivity.addInfo("Failed connection: " + e.getLocalizedMessage());

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
            } catch (IOException e1) {
                e1.printStackTrace();

                Log.e(TAG, "Recieved exception - " + e1.getLocalizedMessage());
                //myactivity.addInfo("Failed connection: " + e.getLocalizedMessage());

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
            }
        }
    }

    public void run () {
        connect();
        myactivity.runOnUiThread(new Runnable () {
            public void run () {
                myactivity.setWiFiSocket(socket);
            }
        });
    }

    public void cancel () {
        try {
            socket.close();
        } catch (IOException e) {}
    }
}
