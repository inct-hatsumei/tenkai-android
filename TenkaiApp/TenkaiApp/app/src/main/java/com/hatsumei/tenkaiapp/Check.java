package com.hatsumei.tenkaiapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by Owner on 2016/05/18.
 */
public class Check extends Activity {

	private Intent intent;

	private TextView textView1;
	private TextView textView2;
	private TextView textView3;
	private TextView textView4;
	private TextView textView5;
	private TextView textView6;
	private TextView textView7;
	private TextView textView8;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_check);

		textView1 = (TextView)findViewById(R.id.bt_enable);
		textView2 = (TextView)findViewById(R.id.bt_connect);
		textView3 = (TextView)findViewById(R.id.gps_got);
		textView4 = (TextView)findViewById(R.id.sensor_got);
		textView5 = (TextView)findViewById(R.id.cpu_got);
		textView6 = (TextView)findViewById(R.id.memory_got);
		textView7 = (TextView)findViewById(R.id.battery_got);
		textView8 = (TextView)findViewById(R.id.temp_got);
		intent = getIntent();

		if(intent.getBooleanExtra("bt_on", false)) {
			textView1.setBackgroundColor(Color.RED);
		}
		if(intent.getBooleanExtra("bt_connect", false)) {
			textView2.setBackgroundColor(Color.RED);
		}
		if(intent.getBooleanExtra("gps_got", false)) {
			textView3.setBackgroundColor(Color.RED);
		}
		if(intent.getBooleanExtra("sensor_got", false)) {
			textView4.setBackgroundColor(Color.RED);
		}
		if(intent.getBooleanExtra("cpu_got", false)) {
			textView5.setBackgroundColor(Color.RED);
		}
		if(intent.getBooleanExtra("memory_got", false)) {
			textView6.setBackgroundColor(Color.RED);
		}
		if(intent.getBooleanExtra("battery_got", false)) {
			textView7.setBackgroundColor(Color.RED);
		}
		if(intent.getBooleanExtra("temp_got", false)) {
			textView8.setBackgroundColor(Color.RED);
		}

	}
}
