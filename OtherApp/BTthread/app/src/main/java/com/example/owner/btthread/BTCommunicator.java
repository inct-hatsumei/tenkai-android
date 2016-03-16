package com.example.owner.btthread;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class BTCommunicator extends Thread {

    public static final int DISCONNECT = 99;

    public static final int DISPLAY_TOAST = 1000;
    public static final int STATE_CONNECTED = 1001;
    public static final int STATE_CONNECTERROR = 1002;
    public static final int STATE_RECEIVEERROR = 1004;
    public static final int STATE_SENDERROR = 1005;
    public static final int NO_DELAY = 0;

    private static final UUID SERIAL_PORT_SERVICE_CLASS_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    BluetoothAdapter btAdapter;
    private BluetoothSocket BTsocket = null;
    private final BluetoothServerSocket tmp = null;
    private DataOutputStream Dos = null;
    private boolean connected = false;

    private Handler uiHandler;
    private String mMACaddress;
    //private Activity_BT myBT;

    //private MainActivity mAT;
    private MainActivity mAT;

    //public BTCommunicator(MainActivity myGesNxt, Handler uiHandler, BluetoothAdapter btAdapter) {
    public BTCommunicator(MainActivity myGesNxt, Handler uiHandler, BluetoothAdapter btAdapter) {
        this.mAT = myGesNxt;
        this.uiHandler = uiHandler;
        this.btAdapter = btAdapter;
    }

    public Handler getHandler() {
        return myHandler;
    }

    public boolean isBTAdapterEnabled() {
        return (btAdapter == null) ? false : btAdapter.isEnabled();
    }

    @Override
    public void run() {

        createNXTconnection();

        while (connected) {
        }

    }

    private void createNXTconnection() {
        try {

            BluetoothSocket BTsocketTEMPORARY;
            BluetoothDevice btDevice = null;
            // Get a BluetoothDevice object for the given Bluetooth hardware address.
            btDevice = btAdapter.getRemoteDevice(mMACaddress);

            if (btDevice == null) {
                sendToast(mAT.getResources().getString(R.string.no_paired_nxt));
                sendState(STATE_CONNECTERROR);
                return;
            }

            // Create an RFCOMM BluetoothSocket ready to start a secure outgoing connection to this remote device using SDP lookup of uuid.

            BTsocketTEMPORARY = btDevice.createRfcommSocketToServiceRecord(SERIAL_PORT_SERVICE_CLASS_UUID);
            BTsocketTEMPORARY.connect(); // Attempt to connect to a remote device.
            BTsocket = BTsocketTEMPORARY;


            Dos = new DataOutputStream(BTsocket.getOutputStream());

            connected = true;

        } catch (IOException e) {
            Log.d("BTCommuicator", "error createNXTConnection()", e);
            if (mAT.newDevice) {
                sendToast(mAT.getResources().getString(R.string.pairing_message));
                sendState(STATE_CONNECTERROR);

            } else {
                sendState(STATE_CONNECTERROR);
            }

            return;
        }

        sendState(STATE_CONNECTED);
    }

    private void destroyNXTconnection() {
        try {
            if (BTsocket != null) {
                // send stop messages before closing
                //changeMotorSpeed(MOTOR_B, 0);
                //changeMotorSpeed(MOTOR_C, 0);
                waitSomeTime(1000);
                connected = false;
                BTsocket.close();
                BTsocket = null;
            }

            Dos = null;

        } catch (IOException e) {
            sendToast(mAT.getResources().getString(R.string.problem_at_closing));
        }
    }


    private void waitSomeTime(int millis) {
        try {
            Thread.sleep(millis);

        } catch (InterruptedException e) {
        }
    }

    private void sendToast(String toastText) {
        Bundle myBundle = new Bundle();
        myBundle.putInt("message", DISPLAY_TOAST);
        myBundle.putString("toastText", toastText);
        sendBundle(myBundle);
    }

    private void sendState(int message) {
        Bundle myBundle = new Bundle();
        myBundle.putInt("message", message);
        sendBundle(myBundle);
    }

    private void sendBundle(Bundle myBundle) {
        Message myMessage = myHandler.obtainMessage();  // Returns a new Message from the global message pool.
        myMessage.setData(myBundle);
        uiHandler.sendMessage(myMessage);
    }

    // receive messages from the UI
    final Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message myMessage) {

            int message;

            switch (message = myMessage.getData().getInt("message")) {

                case DISCONNECT:
                    destroyNXTconnection();
                    break;
            }
        }
    };

    public void setMACAddress(String mMACaddress) {
        this.mMACaddress = mMACaddress;
    }
}
