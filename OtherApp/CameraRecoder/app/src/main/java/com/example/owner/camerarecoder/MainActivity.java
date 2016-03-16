package com.example.owner.camerarecoder;


		import android.app.Activity;
		import android.media.MediaRecorder;
		import android.os.Bundle;
		import android.util.Log;
		import android.view.MotionEvent;
		import android.view.SurfaceHolder;
		import android.view.SurfaceView;

public class MainActivity extends Activity implements SurfaceHolder.Callback {
	private MediaRecorder myRecorder;
	private boolean isRecording;
	SurfaceHolder v_holder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SurfaceView mySurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
		SurfaceHolder holder = mySurfaceView.getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		myRecorder = new MediaRecorder(); // MediaRecorder縺ｮ繧､繝ｳ繧ｹ繧ｿ繝ｳ繧ｹ繧剃ｽ懈�
	}

	public void surfaceCreated(SurfaceHolder holder) {
		//
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		v_holder = holder; // SurfaceHolder繧剃ｿ晏ｭ�
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		//
	}

	public void initializeVideoSettings() {
		myRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT); // 骭ｲ逕ｻ縺ｮ蜈･蜉帙た繝ｼ繧ｹ繧呈欠螳�
		myRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // 繝輔ぃ繧､繝ｫ繝輔か繝ｼ繝槭ャ繝医ｒ謖�ｮ�
		myRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP); // 繝薙ョ繧ｪ繧ｨ繝ｳ繧ｳ繝ｼ繝繧呈欠螳�

		myRecorder.setOutputFile("/sdcard/sample.mp4"); // 蜍慕判縺ｮ蜃ｺ蜉帛�縺ｨ縺ｪ繧九ヵ繧｡繧､繝ｫ繝代せ繧呈欠螳�
		myRecorder.setVideoFrameRate(30); // 蜍慕判縺ｮ繝輔Ξ繝ｼ繝�繝ｬ繝ｼ繝医ｒ謖�ｮ�
		myRecorder.setVideoSize(320, 240); // 蜍慕判縺ｮ繧ｵ繧､繧ｺ繧呈欠螳�
		myRecorder.setPreviewDisplay(v_holder.getSurface()); // 骭ｲ逕ｻ荳ｭ縺ｮ繝励Ξ繝薙Η繝ｼ縺ｫ蛻ｩ逕ｨ縺吶ｋ繧ｵ繝ｼ繝輔ぉ繧､繧ｹ繧呈欠螳壹☆繧�

		try {
			myRecorder.prepare(); //
		} catch (Exception e) {
			Log.e("recMovie", e.getMessage());
		}
	}

	// 繧ｿ繝�メ繝代ロ繝ｫ縺梧款縺輔ｌ縺溘ｉ骭ｲ逕ｻ髢句ｧ�/骭ｲ逕ｻ蛛懈ｭ｢
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// 骭ｲ逕ｻ荳ｭ縺ｧ縺ｪ縺代ｌ縺ｰ骭ｲ逕ｻ繧帝幕蟋�
			if (!isRecording) {
				initializeVideoSettings(); // MediaRecorder縺ｮ險ｭ螳�
				myRecorder.start(); // 骭ｲ逕ｻ髢句ｧ�
				isRecording = true; // 骭ｲ逕ｻ荳ｭ縺ｮ繝輔Λ繧ｰ繧堤ｫ九※繧�

				// 骭ｲ逕ｻ荳ｭ縺ｧ縺ゅｌ縺ｰ骭ｲ逕ｻ繧貞●豁｢
			} else {
				myRecorder.stop(); // 骭ｲ逕ｻ蛛懈ｭ｢
				myRecorder.reset(); // 繧ｪ繝悶ず繧ｧ繧ｯ繝医�繝ｪ繧ｻ繝�ヨ
				isRecording = false; // 骭ｲ逕ｻ荳ｭ縺ｮ繝輔Λ繧ｰ繧貞､悶☆
			}
		}
		return true;
	}
}