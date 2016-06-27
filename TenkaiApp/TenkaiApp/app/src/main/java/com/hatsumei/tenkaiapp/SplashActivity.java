package com.hatsumei.tenkaiapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Owner on 2016/06/27.
 */
public class SplashActivity extends Activity {

	Intent intent = new Intent();

	String ADDRESS = "7C:B7:33:06:1E:D0";
	public static String EXTRA_DEVICE_ADDRESS = "device_address";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_splash);

		setResult(Activity.RESULT_CANCELED, intent);

		String address = ADDRESS;

		Intent intent = new Intent();
		Bundle data = new Bundle();
		data.putString(EXTRA_DEVICE_ADDRESS, address);
		intent.putExtras(data);

		setResult(Activity.RESULT_OK, intent);
		finish();
	}

}
