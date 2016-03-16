package com.example.owner.btthread;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.UUID;

/**
 * Created by Owner on 2016/03/02.
 */
public class Btsend extends Thread {

	Calendar calendar;

	//String cpu, memo, temp, batt, lat, alt, hei, gabX, gabY, gabZ, address;
	String mcpu;
	private BluetoothAdapter mBluetoothAdapter;
	private OutputStream mOutput;
	private BluetoothDevice mBtDevice;
	private BluetoothSocket mBtSocket;

	byte[] sendByte;
	String sendMsg = "";

	String address;

	//public Btsend(String mcpu, String mmemom, String mtemp, String mbatt, String mlat, String malt, String mhei, String mgabX, String mgabY, String mgabZ, String maddress) {
	public Btsend(String maddress) {

		/*
		cpu = mcpu;
		memo = mmemom;
		temp = mtemp;
		batt = mbatt;
		lat = mlat;
		alt = malt;
		hei = mhei;
		gabX = mgabX;
		gabY = mgabY;
		gabZ = mgabZ;
		address = maddress;
		*/

		address = maddress;
	}
@Override
	public void run() {
	Log.v("run", "go");
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


		calendar = Calendar.getInstance();

		/*
		sendMsg = calendar.get(Calendar.YEAR) + "," + calendar.get(Calendar.MONTH) + calendar.get(Calendar.DATE) + calendar.get(Calendar.HOUR_OF_DAY)
				+ calendar.get(Calendar.MINUTE) + calendar.get(Calendar.SECOND) + calendar.get(Calendar.MILLISECOND) + ","
				+ cpu + "," + memo + "," + temp + "," + batt + ","
				+ lat + "," + alt + "," + hei + "," + gabX + "," + gabY + "," + gabZ + "\n";
				*/
		sendMsg = "Hey";
		//scount = " " + String.valueOf(icount) + ", " + String.valueOf(lat) + ", " + String.valueOf(alt) + ", "+ String.valueOf(hei);
		sendByte = sendMsg.getBytes();
		//bcount = scount.getBytes();
		//textview.setText(String.valueOf(scount) + "\n" + textview.getText());

		BTsend();


	}

	public void BTsend() {

		//mBtDevice = mBluetoothAdapter.getRemoteDevice("7C:B7:33:06:1E:D0");

		mBtDevice = mBluetoothAdapter.getRemoteDevice(address);
		Log.v("Mac", address);
		try {
			mBtSocket = mBtDevice.createRfcommSocketToServiceRecord(
					UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			mBtSocket.connect();


		} catch (IOException e) {
			e.printStackTrace();
			Log.v("SocketConnect", "false");
		}
		try{
			mOutput = mBtSocket.getOutputStream();
			send();
		}catch (IOException e) {
			Log.v("mOutput", "null");
		}


	}

	public void send() {
		Log.v("sen", "try");
		try {


			Log.v("trry", sendMsg);
			//mOutput.write(sendByte);
			mOutput.write('a');
			//mOutput.write(sendByte);
			//Log.v("Output", sendMsg);

		} //catch (IOException e) {
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
