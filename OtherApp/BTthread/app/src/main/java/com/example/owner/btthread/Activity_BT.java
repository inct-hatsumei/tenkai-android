package com.example.owner.btthread;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EdgeEffect;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * Created by Owner on 2016/03/15.
 */
public class Activity_BT extends AppCompatActivity {

	private BluetoothAdapter mBluetoothAdapter = null;

	private static final String TAG = "Activity_BT";
	private static final int REQUEST_ENABLE_BT = 1;
	private static final int REQUEST_CONNECT_DEVICE = 2;

	private BluetoothChatService mChatService = null;

	private StringBuffer mOutStringBuffer;
	private EditText mOutEditText;
	private ToggleButton toggleButton;

	String address;


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		toggleButton = (ToggleButton)findViewById(R.id.btn1);
		toggleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
// Otherwise, setup the chat session
		} else {
			//if (mChatService == null)
			selectDevice();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mChatService != null) {
			mChatService.stop();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_ENABLE_BT:
				// When the request to enable Bluetooth returns
				if (resultCode == Activity.RESULT_OK) {
					// Bluetooth is now enabled, so set up a chat session
					selectDevice();
				} else {
					// User did not enable Bluetooth or an error occured
					Log.d(TAG, "BT not enabled");
					Toast.makeText(this, "BT is OFF", Toast.LENGTH_SHORT).show();
					finish();
				}
				break;
			case REQUEST_CONNECT_DEVICE:
				if (resultCode == Activity.RESULT_OK) {
					// Get the device MAC address
					address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
					BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
					boolean secure = true;
					mChatService.connect(device, secure);
					//startBTCommunicator(address);
				} else {
					//showToastLong("右上のメニューから次の動作を選択してください");

				}
				break;

		}
	}

	void selectDevice() {
		mChatService = new BluetoothChatService(mHandler);
		Intent intent = new Intent(this, DeviceListActivity.class);
		startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
	}

	private void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			//Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = message.getBytes();
			mChatService.write(send);

			// Reset out string buffer to zero and clear the edit text field
			mOutStringBuffer.setLength(0);
			mOutEditText.setText(mOutStringBuffer);
		}
	}

	private void setStatus(int resId) {
	}
	private void setStatus(CharSequence subTitle){

	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			//FragmentActivity activity = getActivity();
			switch (msg.what) {
				case Constants.MESSAGE_STATE_CHANGE:
					switch (msg.arg1) {
						case BluetoothChatService.STATE_CONNECTED:
							//setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
							//mConversationArrayAdapter.clear();
							break;
						case BluetoothChatService.STATE_CONNECTING:
							//setStatus(R.string.title_connecting);
							break;
						case BluetoothChatService.STATE_LISTEN:
						case BluetoothChatService.STATE_NONE:
							//setStatus(R.string.title_not_connected);
							break;
					}
					break;
				case Constants.MESSAGE_WRITE:
					byte[] writeBuf = (byte[]) msg.obj;
					// construct a string from the buffer
					String writeMessage = new String(writeBuf);
					//mConversationArrayAdapter.add("Me:  " + writeMessage);
					break;
				case Constants.MESSAGE_READ:
					byte[] readBuf = (byte[]) msg.obj;
					// construct a string from the valid bytes in the buffer
					String readMessage = new String(readBuf, 0, msg.arg1);
					//mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
					break;
				case Constants.MESSAGE_DEVICE_NAME:
					// save the connected device's name
					//mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
					/*
					if (null != activity) {
						Toast.makeText(activity, "Connected to "
								+ mConnectedDeviceName, Toast.LENGTH_SHORT).show();
					}
					*/
					break;
				case Constants.MESSAGE_TOAST:
					/*
					if (null != activity) {
						Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
								Toast.LENGTH_SHORT).show();
					}
					*/
					break;
			}
		}
	};

}
