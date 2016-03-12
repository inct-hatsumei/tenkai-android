package com.example.owner.btthread;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

	private static final int REQUEST_CONNECT_DEVICE = 1000;
	private static final int REQUEST_ENABLE_BLUETOOTH = 2000;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothAdapter mBluetoothAdapter2;
	private String address;
	private ProgressDialog connectingProgressDialog;
	private boolean connected = false;
	private boolean bt_error_pending = false;
	public static final int MENU_TOGGLE_CONNECT = Menu.FIRST;

	private Handler btcHandler;


	private BTCommunicator myBTCommunicator = null;
	private Toast mLongToast;
	private Toast mShortToast;


	private OutputStream mOutput;
	private BluetoothDevice mBtDevice;

	private BluetoothSocket mBtSocket;
	Menu myMenu;
	boolean newDevice;

	byte[] sendByte;
	String sendMsg = "";

	private ToggleButton btn1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		btn1 = (ToggleButton) findViewById(R.id.btn1);

		btn1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (btn1.isChecked()) {
					Btsend mbtsend = new Btsend(address);
					//mbtsend.start();
					send();
				} else if (btn1.isChecked() == false) {

				}
			}
		});

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		boolean btEnable = mBluetoothAdapter.isEnabled();
		if (btEnable == true) {
			//BluetoothがONだった場合の処理
			selectDevice();
		} else {
			//OFFだった場合、ONにすることを促すダイアログを表示する画面に遷移
			Intent btOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(btOn, REQUEST_ENABLE_BLUETOOTH);
		}
		mLongToast = Toast.makeText(this, "", Toast.LENGTH_LONG);
		mShortToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

	}

	void selectDevice() {
		Intent intent = new Intent(this, DeviceListActivity.class);
		startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
	}

	public void startBTCommunicator(String mac_address) {

		connectingProgressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.connecting_please_wait), true);

		if (myBTCommunicator == null) {
			createBTCommunicator();
		}

		switch (((Thread) myBTCommunicator).getState()) {
			/*
             * Thread.getState() Therad.isAlive()
        	 * スレッドの状態を返します。
        	 * 		NEW				false
        	 * 		RUNNABLE		true
        	 */
			case NEW:
				myBTCommunicator.setMACAddress(mac_address);
				myBTCommunicator.start();
				break;
			default:
				connected = false;
				myBTCommunicator = null;
				createBTCommunicator();
				myBTCommunicator.setMACAddress(mac_address);
				myBTCommunicator.start();
				break;
		}
		// optionMenu
		updateButtonsAndMenu();

	}

	public void createBTCommunicator() {
		// interestingly BT adapter needs to be obtained by the UI thread - so we pass it in in the constructor
		myBTCommunicator = new BTCommunicator(this, myHandler, BluetoothAdapter.getDefaultAdapter());
		btcHandler = myBTCommunicator.getHandler();
	}

	// receive messages from the BTCommunicator
	final Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message myMessage) {
			switch (myMessage.getData().getInt("message")) {
				case BTCommunicator.STATE_CONNECTED:
					connected = true;
					connectingProgressDialog.dismiss();
					// optionMenu
					updateButtonsAndMenu();
					BTsend();
					// 接続した
					showToastLong(getResources().getString(R.string.connected));

					break;
				case BTCommunicator.STATE_CONNECTERROR:
					connectingProgressDialog.dismiss();
				case BTCommunicator.STATE_RECEIVEERROR:
				case BTCommunicator.STATE_SENDERROR:
					destroyBTCommunicator();

					if (bt_error_pending == false) {
						bt_error_pending = true;
						// inform the user of the error with an AlertDialog
						DialogFragment newFragment = MyAlertDialogFragment.newInstance(
								R.string.bt_error_dialog_title, R.string.bt_error_dialog_message);
						newFragment.show(getFragmentManager(), "dialog");
					}

					break;
			}
		}
	};

	public void doPositiveClick() {
		bt_error_pending = false;
		selectDevice();
	}

	public void BTsend() {


		//mBtDevice = mBluetoothAdapter.getRemoteDevice("7C:B7:33:06:1E:D0");

		mBluetoothAdapter2 = BluetoothAdapter.getDefaultAdapter();
		mBtDevice = mBluetoothAdapter2.getRemoteDevice(address);
		Log.v("Mac", address);
		try {
			mBtSocket = mBtDevice.createRfcommSocketToServiceRecord(
					UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
		} catch (IOException e) {
			e.printStackTrace();
			Log.v("UUID", "miss");
		}
		mBluetoothAdapter2.cancelDiscovery();
		try {
			mBtSocket.connect();
			mOutput = mBtSocket.getOutputStream();
			Log.v("connect", "OK");
		} catch (IOException e) {
			e.printStackTrace();
			Log.v("connect", "miss");
		}

	}

	public void send() {

		Log.v("sen", "try");
		Log.v("trry", sendMsg);
		try {


			//mOutput.write(sendByte);
			mOutput.write('a');
			//Log.v("Output", sendMsg);

		} catch (IOException e) {
			//catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	void sendBTCmessage(int delay, int message) {
		Bundle myBundle = new Bundle();
		myBundle.putInt("message", message);
		Message myMessage = myHandler.obtainMessage();
		myMessage.setData(myBundle);

		if (delay == 0)
			btcHandler.sendMessage(myMessage);

		else
			btcHandler.sendMessageDelayed(myMessage, delay);
	}

	public void destroyBTCommunicator() {

		if (myBTCommunicator != null) {
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DISCONNECT/*, 0, 0*/);
			myBTCommunicator = null;
		}

		connected = false;
		// 追加
		updateButtonsAndMenu();
	}

	private void showToastShort(String textToShow) {
		mShortToast.setText(textToShow);
		mShortToast.show();
	}

	private void showToastLong(String textToShow) {
		mLongToast.setText(textToShow);
		mLongToast.show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		try {
			mBtSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void updateButtonsAndMenu() {
		if (myMenu == null) return;
		myMenu.removeItem(MENU_TOGGLE_CONNECT);

		if (connected) {
			myMenu.add(0, MENU_TOGGLE_CONNECT, 1, getResources().getString(R.string.disconnect));
		} else {
			myMenu.add(0, MENU_TOGGLE_CONNECT, 1, getResources().getString(R.string.connect));
		}
	}


	@Override
	protected void onActivityResult(int requestCode, int ResultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_CONNECT_DEVICE:
				if (ResultCode == Activity.RESULT_OK) {
					// Get the device MAC address
					address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
					startBTCommunicator(address);
				} else {
					//showToastLong("右上のメニューから次の動作を選択してください");

				}
				break;
			case REQUEST_ENABLE_BLUETOOTH:
				switch (ResultCode) {
					case Activity.RESULT_OK:
						selectDevice();
						break;
					case Activity.RESULT_CANCELED:
						Intent btOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
						startActivityForResult(btOn, REQUEST_ENABLE_BLUETOOTH);
						break;

				}
				break;
		}
	}


}