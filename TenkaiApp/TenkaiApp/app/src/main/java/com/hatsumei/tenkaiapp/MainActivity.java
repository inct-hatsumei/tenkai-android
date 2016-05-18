package com.hatsumei.tenkaiapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Calendar;
import java.util.List;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.view.View;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements SensorEventListener, SurfaceHolder.Callback, LocationListener {


	private LocationManager Lmanager = null;
	private SensorManager Smanager;
	private Camera cam;
	private SurfaceView mySurfaceView;
	private TextView textView;
	private TextView textView1;
	private TextView textView3;
	private TextView textView4;
	private TextView textView5;
	private TextView textView6;
	private TextView textView7;
	private TextView textView8;
	private ToggleButton toggleButton1;
	private ToggleButton toggleButton2;

	private MediaPlayer mediaPlayer = null;
	private Alarm mAlarm;


	private Toast mLongToast;
	private Toast mShortToast;

	// MediaRecorderの初期設定
	private MediaRecorder myRecorder;
	private boolean isRecording;

	private int scale;
	private int level;

	String cpu = "";
	String memo = "";
	String tmpr = "";
	String batt = "";
	String lat = "0";
	String alt = "0";
	String hei = "0";
	String gabX = "";
	String gabY = "";
	String gabZ = "";
	MyTimerTask timerTask = null;
	Timer mTimer = null;

	MyTimerTask2 timerTask2 = null;
	Timer mTimer2 = null;
	Handler mHandler2 = new Handler();

	SurfaceHolder v_holder;

	Calendar calendar;

	private boolean running;

	private BluetoothAdapter mBluetoothAdapter = null;

	private static final String TAG = "MainActivity";
	private static final int REQUEST_ENABLE_BT = 1;
	private static final int REQUEST_CONNECT_DEVICE = 2;

	private static final String ADDRESS = "7C:B7:33:06:1E:D0";

	private BluetoothChatService mChatService = null;

	private StringBuffer mOutStringBuffer;
	private EditText mOutEditText;
	private ToggleButton toggleButton;

	boolean bt_on = false;
	boolean bt_connected = false;
	boolean gps_got = false;
	boolean sensor_got = false;
	boolean cpu_got = false;
	boolean memory_got = false;
	boolean battery_got = false;
	boolean temp_got  =false;


	private String log = "";

	String address;

	int ringMaxVolume;
	int flags;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Log.v("file", Environment.getExternalStorageDirectory().getPath());

		Lmanager = (LocationManager) getSystemService(LOCATION_SERVICE);

		Smanager = (SensorManager) getSystemService(SENSOR_SERVICE);
		textView1 = (TextView) findViewById(R.id.textView1);
		textView3 = (TextView) findViewById(R.id.textView3);
		textView4 = (TextView) findViewById(R.id.textView4);
		textView5 = (TextView) findViewById(R.id.textView5);
		textView = (TextView) findViewById(R.id.textView);
		textView6 = (TextView) findViewById(R.id.textView6);
		textView7 = (TextView) findViewById(R.id.textView7);
		textView8 = (TextView) findViewById(R.id.textView8);
		toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
		toggleButton1 = (ToggleButton) findViewById(R.id.toggleButton1);
		toggleButton2 = (ToggleButton) findViewById(R.id.toggleButton2);
		mySurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
		SurfaceHolder holder = mySurfaceView.getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		myRecorder = new MediaRecorder();

		mLongToast = Toast.makeText(this, "", Toast.LENGTH_LONG);
		mShortToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

		mediaPlayer = MediaPlayer.create(this, R.raw.hangouts_video_call);

		toggleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (toggleButton.isChecked()) {
					running = true;
					if (running) {
						timerTask = new MyTimerTask();
						mTimer = new Timer(true);
						mTimer.scheduleAtFixedRate(timerTask, 0, 500);
					}


				} else if (toggleButton.isChecked() == false) {
					running = false;
					mTimer.cancel();
					mTimer = null;
					fileout(log.getBytes());
				}
			}
		});
/*

		toggleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (toggleButton.isChecked()) {
					if (mTimer == null) {

						timerTask = new MyTimerTask();
						mLaptime = 0.0f;
						mTimer = new Timer(true);
						mTimer.schedule(timerTask, 500, 500);
					}
				} else if (toggleButton.isChecked() == false) {
					if (mTimer != null) {
						mTimer.cancel();
						mTimer = null;
					}
				}
			}
		});
		*/
		toggleButton1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (toggleButton1.isChecked()) {
					if (!isRecording) {
						cam.release();
						initializeVideoSettings(); // MediaRecorderの設定
						myRecorder.start(); // 録画開始
						isRecording = true; // 録画中のフラグを立てる
					}
				} else if (toggleButton1.isChecked() == false) {
					myRecorder.stop(); // 録画停止
					myRecorder.reset(); // オブジェクトをリセット
					//myRecorder.release();
					isRecording = false;
				}
			}

		});

		toggleButton2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mAlarm = new Alarm(mediaPlayer);
				if (toggleButton2.isChecked()) {
					setVolume(true);
					mAlarm.readMessage("ON");
				} else if (toggleButton2.isChecked() == false) {
					setVolume(false);
					mAlarm.readMessage("OFF");
				}
			}
		});
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		if (!mBluetoothAdapter.isEnabled()) {
			showToastShort(getResources().getString(R.string.wait_till_bt_on));
			//Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			//startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			mBluetoothAdapter.enable();
			bt_on = true;
// Otherwise, setup the chat session
		} else {
			bt_on = true;
			//if (mChatService == null)
			//selectDevice();
		}


		timerTask2 = new MyTimerTask2();
		mTimer2 = new Timer(true);
		mTimer2.scheduleAtFixedRate(timerTask2, 0, 1000);

		Toast.makeText(this, "GPSを有効にしてください", Toast.LENGTH_LONG).show();
		screenlock(0);



	}

	@Override
	public void onStart() {
		super.onStart();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mChatService != null) {
			mChatService.stop();
		}
		mAlarm.readMessage("Destroy");

		screenlock(1);
	}


	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_ENABLE_BT:
				// When the request to enable Bluetooth returns
				if (resultCode == Activity.RESULT_OK) {
					// Bluetooth is now enabled, so set up a chat session
					//selectDevice();
				} else {
					// User did not enable Bluetooth or an error occured
					Log.d(TAG, "BT not enabled");
					showToastShort(getResources().getString(R.string.bt_needs_to_be_enabled));
					Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
					//finish();
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
		//address = ADDRESS;
		//BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		boolean secure = true;
		//mChatService.connect(device, secure);
	}

	private void sendMessage() {

		String sendMsg = "";
		byte[] sendByte;

		calendar = Calendar.getInstance();
		try {
			int tmp=calendar.get(Calendar.MONTH)+1;//calendar.get(Calendar.MONTH)で取得出来るのは今の月-1なので、一度intにして+1してStringに変換する
			sendMsg = calendar.get(Calendar.YEAR) + "/" + String.valueOf(tmp)+"/"+ + calendar.get(Calendar.DATE) +" "+ calendar.get(Calendar.HOUR_OF_DAY)+":"
					+ calendar.get(Calendar.MINUTE) +":"+ calendar.get(Calendar.SECOND) +":"+ calendar.get(Calendar.MILLISECOND) + ","
					+ cpu + "," + memo + "," + temp + "," + batt + ","
					+ lat + "," + alt + "," + hei + "," + gabX + "," + gabY + "," + gabZ + "\n";
			//scount = " " + String.valueOf(icount) + ", " + String.valueOf(lat) + ", " + String.valueOf(alt) + ", "+ String.valueOf(hei);
			sendByte = sendMsg.getBytes();
			//bcount = scount.getBytes();
			//textview.setText(String.valueOf(scount) + "\n" + textview.getText());
			//mOutput.write(sendByte);
			log += sendMsg;
			mChatService.write(sendByte);
		} //catch (IOException e) {
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			//Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
			return;
		}


		// Check that there's actually something to send
		/*
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = message.getBytes();
			//mChatService.write(send);



			// Reset out string buffer to zero and clear the edit text field
			//mOutStringBuffer.setLength(0);
			//mOutEditText.setText(mOutStringBuffer);
		}
		*/
	}

	private void setStatus(int resId) {
	}

	private void setStatus(CharSequence subTitle) {

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
							showToastLong(getResources().getString(R.string.connected));
							bt_connected = true;
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
					showToastLong(readMessage);
					readCommand(readMessage);
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

	private void readCommand(String message) {
		if (message.equals("senddata") || message.equals("1")) {
			sendMessage();
		} else if (message.equals("senddataon") || message.equals("2")) {
			running = true;
			if (running) {
				timerTask = new MyTimerTask();
				mTimer = new Timer(true);
				mTimer.scheduleAtFixedRate(timerTask, 0, 500);
			}
		} else if (message.equals("senddataoff") || message.equals("3")) {
			running = false;
			mTimer.cancel();
			mTimer = null;
		} else if (message.equals("videorecstart") || message.equals("4")) {
			if (!isRecording) {
				cam.release();
				initializeVideoSettings(); // MediaRecorderの設定
				myRecorder.start(); // 録画開始
				isRecording = true; // 録画中のフラグを立てる
			}
		} else if (message.equals("videorecstop") || message.equals("5")) {
			myRecorder.stop(); // 録画停止
			myRecorder.reset(); // オブジェクトをリセット
			//myRecorder.release();
			isRecording = false;
		} else if (message.equals("soundon") || message.equals("6")) {
			setVolume(true);
			mAlarm = new Alarm(mediaPlayer);
			mAlarm.readMessage("ON");

		} else if (message.equals("soundoff") || message.equals("7")) {
			setVolume(false);
			mAlarm = new Alarm(mediaPlayer);
			mAlarm.readMessage("OFF");
		} else if (message.equals("exit") || message.equals("0")) {
			Context context;
			int waitperiod;
			context = getApplicationContext();
			waitperiod = 5000;
			restart(context, waitperiod);

		}

	}


	//----camera---------------------------------------------------------------------------------------------------------------------------------------------------------------


	public void surfaceCreated(SurfaceHolder holder) {
		cam = Camera.open();
		try {
			cam.setDisplayOrientation(90);
			cam.setPreviewDisplay(holder);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		v_holder = holder; // SurfaceHolderを保存
		textView1.setText("CAM\nHeight:" + height + "\nWidth:" + width);
		Camera.Parameters parameters = cam.getParameters();
		cam.setParameters(parameters);
		cam.startPreview();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		cam.release();
		cam = null;
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Smanager.unregisterListener(this);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		if (Lmanager != null) {
			Lmanager.removeUpdates(this);
		}
		super.onPause();
	}

	//---sensor----

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		/*
		List<Sensor> sensorstem = Smanager.getSensorList(Sensor.TYPE_TEMPERATURE);
		if (sensorstem.size() > 0) {
			Sensor stem = sensorstem.get(0);
			Smanager.registerListener(this, stem, SensorManager.SENSOR_DELAY_FASTEST);
		}
		*/
		List<Sensor> sensors = Smanager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if (sensors.size() > 0) {
			Sensor s = sensors.get(0);
			Smanager.registerListener(this, s, SensorManager.SENSOR_DELAY_UI);
		}
		if (Lmanager != null) {
			Lmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		}


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

	@Override
	public void onLocationChanged(Location location) {
		//		// TODO Auto-generated method stub


		lat = String.valueOf(location.getLatitude());
		textView6.setText("北緯：" + lat);
		alt = String.valueOf(location.getLongitude());
		textView7.setText("東経：" + alt);
		hei = String.valueOf(location.getAltitude());
		textView8.setText("高度：" + hei);

		gps_got = true;
	}


	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			gabX = String.valueOf(event.values[SensorManager.DATA_X]);
			textView3.setText("X軸:" + gabX);
			gabY = String.valueOf(event.values[SensorManager.DATA_Y]);
			textView4.setText("Y軸:" + gabY);
			gabZ = String.valueOf(event.values[SensorManager.DATA_Z]);
			textView5.setText("Z軸:" + gabZ);
			sensor_got = true;
		}
		/*
		if (event.sensor.getType() == Sensor.TYPE_TEMPERATURE) {
			TextView textView14 = (TextView) findViewById(R.id.textView14);
			textView14.setText(String.valueOf(event.values[0]) + "℃");
			Log.v("TEMP", "TEMP");
		}
		*/
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
	}


	//送信
	private void Transmission() {


	}

	private static long old_time;
	private static double old_use;
	int i = 0;
	double usage;

	public void setPerform() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(myReceiver, filter);

		TextView result = (TextView) findViewById(R.id.textView11);
		TextView textView12 = (TextView) findViewById(R.id.textView12);
		textView12.setText("");


		i++;
		String str = "";
		str += String.valueOf(i) + "\n";
		try {
			BufferedReader reader = new BufferedReader(new FileReader("/proc/stat"));
			String line = reader.readLine().trim();
			String[] vals = line.split("\\s+");
			int usr = Integer.parseInt(vals[1]);
			int nice = Integer.parseInt(vals[2]);
			int sys = Integer.parseInt(vals[3]);

			while ((line = reader.readLine()) != null) {
				str += line + "\n";
			}
			reader.close();
			long now = System.currentTimeMillis() / 10;
			usage = (usr + nice + sys - old_use) / (now - old_time);
			old_use = usr + nice + sys;
			old_time = now;


			//textView1.setText(String.valueOf(usage));
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "読み込みエラー", Toast.LENGTH_SHORT).show();
		}
		result.setText("");
		if (1 >= usage) {
			result.setText(String.valueOf(usage * 100));
			cpu = String.valueOf(usage * 100);
			cpu_got = true;
		} else if (2 >= usage) {
			result.setText(String.valueOf((usage * 100 / 2)));
			cpu = String.valueOf((usage * 100 / 2));
			cpu_got = true;
		} else if (3 >= usage) {
			result.setText(String.valueOf((usage * 100 / 3)));
			cpu = String.valueOf((usage * 100 / 3));
			cpu_got = true;
		} else {
			result.setText(String.valueOf((usage * 100 / 4)));
			cpu = String.valueOf((usage * 100 / 4));
			cpu_got = true;
		}
		try {
			ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
			ActivityManager am = ((ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE));
			am.getMemoryInfo(info);

			textView12.setText(String.valueOf(100 * (info.totalMem / 1024.00 - info.availMem / 1024.00) / (info.totalMem / 1024.00)));
			memo = String.valueOf(100 * (info.totalMem / 1024.00 - info.availMem / 1024.00) / (info.totalMem / 1024.00));
			memory_got = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		/*
		if(running == true) {
			String mtemp = String.valueOf(temp);
			Btsend mBtsend = new Btsend(cpu, memo, mtemp, batt, lat, alt, hei, gabX, gabY, gabZ, mOutput);
			//mBtsend.start();
			try {
				mOutput.write('a');

			}catch (IOException e){

			}
		}
		*/

	}

	//タイマー処理
	class MyTimerTask extends TimerTask {
		@Override
		public void run() {
			mHandler.post(new Runnable() {
				public void run() {
					sendMessage();
				}
			});
		}
	}

	class MyTimerTask2 extends TimerTask {
		@Override
		public void run() {
			mHandler2.post(new Runnable() {
				public void run() {
					setPerform();
				}
			});
		}
	}

	int temp;
	public BroadcastReceiver myReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
				// 電池残量の最大値
				scale = intent.getIntExtra("scale", 0);
				// 電池残量
				level = intent.getIntExtra("level", 0);
				battery_got = true;
				temp = intent.getIntExtra("temperature", 0) / 10;
				temp_got = true;
			}

			//結果を描写
			TextView textView13 = (TextView) findViewById(R.id.textView13);
			textView13.setText("" + level + "/" + scale);
			batt = String.valueOf(level);
			//textView13.setTextSize(64);
			TextView textView14 = (TextView) findViewById(R.id.textView14);
			textView14.setText(String.valueOf(temp) + "℃");
			tmpr = String.valueOf(temp);
		}
	};


	public void initializeVideoSettings() {

		calendar = Calendar.getInstance();
		int tmp=calendar.get(Calendar.MONTH)+1;
		String filename = "/" + calendar.get(Calendar.YEAR) + String.valueOf(tmp) + calendar.get(Calendar.DATE) + calendar.get(Calendar.HOUR_OF_DAY)
				+ calendar.get(Calendar.MINUTE) + calendar.get(Calendar.SECOND) + calendar.get(Calendar.MILLISECOND) + ".mp4";

		myRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT); // 骭ｲ逕ｻ縺ｮ蜈･蜉帙た繝ｼ繧ｹ繧呈欠螳�
		myRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // 繝輔ぃ繧､繝ｫ繝輔か繝ｼ繝槭ャ繝医ｒ謖�ｮ�
		myRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP); // 繝薙ョ繧ｪ繧ｨ繝ｳ繧ｳ繝ｼ繝繧呈欠螳�

		myRecorder.setOutputFile(Environment.getExternalStorageDirectory().getPath()+filename/*"/sample.mp4"*/); // 蜍慕判縺ｮ蜃ｺ蜉帛�縺ｨ縺ｪ繧九ヵ繧｡繧､繝ｫ繝代せ繧呈欠螳�
		myRecorder.setVideoFrameRate(30); // 蜍慕判縺ｮ繝輔Ξ繝ｼ繝�繝ｬ繝ｼ繝医ｒ謖�ｮ�
		myRecorder.setVideoSize(1920, 1080); // 蜍慕判縺ｮ繧ｵ繧､繧ｺ繧呈欠螳�
		myRecorder.setPreviewDisplay(v_holder.getSurface()); // 骭ｲ逕ｻ荳ｭ縺ｮ繝励Ξ繝薙Η繝ｼ縺ｫ蛻ｩ逕ｨ縺吶ｋ繧ｵ繝ｼ繝輔ぉ繧､繧ｹ繧呈欠螳壹☆繧�

		try {
			myRecorder.prepare();
		} catch (Exception e) {
			Log.e("recMovie", e.getMessage());
		}
	}



	private void showToastShort(String textToShow) {
		mShortToast.setText(textToShow);
		mShortToast.show();
	}

	private void showToastLong(String textToShow) {
		mLongToast.setText(textToShow);
		mLongToast.show();
	}

	private void fileout(byte[] bytes) {

		calendar = Calendar.getInstance();
		int tmp=calendar.get(Calendar.MONTH)+1;
		String filename = "/" + calendar.get(Calendar.YEAR) + String.valueOf(tmp) + calendar.get(Calendar.DATE) + calendar.get(Calendar.HOUR_OF_DAY)
				+ calendar.get(Calendar.MINUTE) + calendar.get(Calendar.SECOND) + calendar.get(Calendar.MILLISECOND) + ".csv";

		Log.v("file", Environment.getExternalStorageDirectory().getPath());


		FileOutputStream fileOutputStream;
		try {
			fileOutputStream = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + filename, true);
			fileOutputStream.write(bytes);
			Log.v("output", "GO");
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}

	void restart(Context cnt, int period) {
		Intent mainActivity  = new Intent(cnt, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(cnt, 0, mainActivity, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarmManager = (AlarmManager)cnt.getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + period, pendingIntent);
		finish();
	}

	void setVolume(boolean volume) {
		AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		flags = AudioManager.FLAG_SHOW_UI;
		int ringvolume = 0;
		if(volume) {
			ringvolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
		}else {
			ringvolume = 0;
			audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
		}
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, ringvolume, flags);
	}

	void screenlock(int i) {
		PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock lock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "MY tag");

		switch(i) {
			case 0:
				lock.acquire();
				break;
			case 1:
				lock.release();
				break;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);

		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if(id == R.id.device_select) {
			selectDevice();

			return true;
		}
		else if (id == R.id.restart) {
			Context context;
			int waitperiod;
			context = getApplicationContext();
			waitperiod = 5000;
			restart(context, waitperiod);
		}
		else if (id == R.id.check) {

			Intent intent  = new Intent(MainActivity.this, Check.class);
			intent.putExtra("bt_on", bt_on);
			intent.putExtra("bt_connect", bt_connected);
			intent.putExtra("gps_got", gps_got);
			intent.putExtra("sensor_got", sensor_got);
			intent.putExtra("cpu_got", cpu_got);
			intent.putExtra("memory_got", memory_got);
			intent.putExtra("battery_got", battery_got);
			intent.putExtra("temp_got", temp_got);
			startActivity(intent);
		}
		return  super.onOptionsItemSelected(item);
	}

}

