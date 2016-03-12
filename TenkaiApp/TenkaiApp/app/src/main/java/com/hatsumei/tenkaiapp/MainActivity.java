package com.hatsumei.tenkaiapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.DialogFragment;
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
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
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
	private ToggleButton toggleButton;
	private ToggleButton toggleButton1;


	private Activity thisActivity;





	// MediaRecorderの初期設定
	private MediaRecorder myRecorder;
	private boolean isRecording;


	private int scale;
	private int level;

	String cpu = "";
	String memo = "";
	String tmpr = "";
	String batt = "";
	String lat = "1";
	String alt = "2";
	String hei = "3";
	String gabX = "";
	String gabY = "";
	String gabZ = "";
	float mLaptime = 0.0f;
	MyTimerTask timerTask = null;
	Timer mTimer = null;
	Handler mHandler = new Handler();

	MyTimerTask2 timerTask2 = null;
	Timer mTimer2 = null;
	Handler mHandler2 = new Handler();

	int icount = 0;
	//String scount = "";
	//double lat, hei, alt;
	//byte[] bcount;
	SurfaceHolder v_holder;

	Calendar calendar;

	private boolean running;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

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
		mySurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
		SurfaceHolder holder = mySurfaceView.getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		myRecorder = new MediaRecorder();


		toggleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (toggleButton.isChecked()) {
					running = true;
					Btsend mbtsend = new Btsend(cpu, memo ,String.valueOf(temp), batt,
							lat, alt, hei, gabX, gabY, gabZ, address);
					mbtsend.start();
				} else if (toggleButton.isChecked() == false) {
					running = false;
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

		timerTask2 = new MyTimerTask2();
		mTimer2 = new Timer(true);
		mTimer2.scheduleAtFixedRate(timerTask2, 0, 1000);

		thisActivity = this;

		btCreate();

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


		super.onResume();
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
		String sendMsg = "";
		byte[] sendByte;

		calendar = Calendar.getInstance();
		try {
			sendMsg = calendar.get(Calendar.YEAR) + "," + calendar.get(Calendar.MONTH) + calendar.get(Calendar.DATE) + calendar.get(Calendar.HOUR_OF_DAY)
					+ calendar.get(Calendar.MINUTE) + calendar.get(Calendar.SECOND) + calendar.get(Calendar.MILLISECOND) + ","
					+ cpu + "," + memo + "," + temp + "," + batt + ","
					+ lat + "," + alt + "," + hei + "," + gabX + "," + gabY + "," + gabZ + "\n";
			//scount = " " + String.valueOf(icount) + ", " + String.valueOf(lat) + ", " + String.valueOf(alt) + ", "+ String.valueOf(hei);
			sendByte = sendMsg.getBytes();
			//bcount = scount.getBytes();
			//textview.setText(String.valueOf(scount) + "\n" + textview.getText());
			//mOutput.write(sendByte);
		} //catch (IOException e) {
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


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
		} else if (2 >= usage) {
			result.setText(String.valueOf((usage * 100 / 2)));
			cpu = String.valueOf((usage * 100 / 2));
		} else if (3 >= usage) {
			result.setText(String.valueOf((usage * 100 / 3)));
			cpu = String.valueOf((usage * 100 / 3));
		} else {
			result.setText(String.valueOf((usage * 100 / 4)));
			cpu = String.valueOf((usage * 100 / 4));
		}
		try {
			ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
			ActivityManager am = ((ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE));
			am.getMemoryInfo(info);

			textView12.setText(String.valueOf(100 * (info.totalMem / 1024.00 - info.availMem / 1024.00) / (info.totalMem / 1024.00)));
			memo = String.valueOf(100 * (info.totalMem / 1024.00 - info.availMem / 1024.00) / (info.totalMem / 1024.00));
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
					Transmission();
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
				temp = intent.getIntExtra("temperature", 0) / 10;
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
		myRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT); // 骭ｲ逕ｻ縺ｮ蜈･蜉帙た繝ｼ繧ｹ繧呈欠螳�
		myRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // 繝輔ぃ繧､繝ｫ繝輔か繝ｼ繝槭ャ繝医ｒ謖�ｮ�
		myRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP); // 繝薙ョ繧ｪ繧ｨ繝ｳ繧ｳ繝ｼ繝繧呈欠螳�

		myRecorder.setOutputFile("/sdcard/sample.mp4"); // 蜍慕判縺ｮ蜃ｺ蜉帛�縺ｨ縺ｪ繧九ヵ繧｡繧､繝ｫ繝代せ繧呈欠螳�
		myRecorder.setVideoFrameRate(30); // 蜍慕判縺ｮ繝輔Ξ繝ｼ繝�繝ｬ繝ｼ繝医ｒ謖�ｮ�
		myRecorder.setVideoSize(1920, 1080); // 蜍慕判縺ｮ繧ｵ繧､繧ｺ繧呈欠螳�
		myRecorder.setPreviewDisplay(v_holder.getSurface()); // 骭ｲ逕ｻ荳ｭ縺ｮ繝励Ξ繝薙Η繝ｼ縺ｫ蛻ｩ逕ｨ縺吶ｋ繧ｵ繝ｼ繝輔ぉ繧､繧ｹ繧呈欠螳壹☆繧�

		try {
			myRecorder.prepare();
		} catch (Exception e) {
			Log.e("recMovie", e.getMessage());
		}
	}


	//-----Bluetooth----------------------------------------------------------------------------
	private static final int REQUEST_CONNECT_DEVICE = 1000;
	private static final int REQUEST_ENABLE_BLUETOOTH = 2000;
	private static final int REQUEST_SEND = 3000;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice mBtDevice;
	private BluetoothSocket mBtSocket;
	private OutputStream mOutput;
	private String address;

	private ProgressDialog connectingProgressDialog;
	private boolean connected = false;
	private boolean bt_error_pending = false;

	private Handler btcHandler;


	private BTCommunicator myBTCommunicator = null;
	private Toast mLongToast;
	private Toast mShortToast;

	private Menu myMenu;

	public static final int MENU_TOGGLE_CONNECT = Menu.FIRST;
	public static final int MENU_QUIT = Menu.FIRST + 1;
	public static final int MENU_HOME = Menu.FIRST + 2;

	boolean newDevice;

	public void btCreate(){
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		boolean btEnable = mBluetoothAdapter.isEnabled();
		if(btEnable == true){
			//BluetoothがONだった場合の処理
			selectDevice();
		}else{
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

					// 接続した
					showToastLong(getResources().getString(R.string.connected));
					//BTsend();
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
	protected void onActivityResult(int requestCode, int ResultCode, Intent data){
		switch (requestCode) {
			case REQUEST_CONNECT_DEVICE:
				if (ResultCode == Activity.RESULT_OK) {
					// Get the device MAC address
					address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
					startBTCommunicator(address);
				}else{
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
		}
	}

}


