package com.hatsumei.tenkaiapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

/**
 * Created by Owner on 2016/03/01.
 */
public class DeviceListActivity extends Activity {
	static final String PAIRING = "pairing";
	public static String EXTRA_DEVICE_ADDRESS = "device_address";
	public static String EXTRA_DEVICE_NAME = "device_name";

	private BluetoothAdapter mBtAdapter;
	private ArrayAdapter<String> mPairedDevicesArrayAdapter;

	Intent intent = new Intent();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);



		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.device_list);

		setResult(Activity.RESULT_CANCELED, intent);

		Button scanButton = (Button) findViewById(R.id.button_scan);
		scanButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				BTset();
			}
		});

		mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
		ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
		pairedListView.setAdapter(mPairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(mDeviceClickListener);

		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

		boolean DevicesFound = false;

		if (pairedDevices.size() > 0) {
			findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
			for (BluetoothDevice device : pairedDevices) {
				DevicesFound = true;
				mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
			}
		}

		if (DevicesFound == false) {
			String noDevices = getResources().getText(R.string.none_paired).toString();
			mPairedDevicesArrayAdapter.add(noDevices);
		}

	}

	private void BTset() {
		Intent intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
		startActivity(intent);
		finish();
	}

	private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

			// Cancel discovery because it's costly and we're about to connect
			mBtAdapter.cancelDiscovery();

			// Get the device MAC address, which is the last 17 chars in the View
			String info = ((TextView) v).getText().toString();
			String devicenemae  =info.substring(0, info.length() - 17);
			String address = info.substring(info.length() - 17);
			// Create the result Intent and include the MAC address
			Intent intent = new Intent();
			Bundle data = new Bundle();
			data.putString(EXTRA_DEVICE_NAME, devicenemae);
			data.putString(EXTRA_DEVICE_ADDRESS, address);
			//data.putBoolean(PAIRING,av.getId()==R.id.new_devices);
			intent.putExtras(data);

			// Set result and finish this Activity
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();

		//unregisterReceiver(mReceiver);
	}

}