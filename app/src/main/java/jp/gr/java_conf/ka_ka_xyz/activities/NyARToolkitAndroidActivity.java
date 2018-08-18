/*
 * PROJECT: NyARToolkit for Android SDK
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * NyARToolkit for Android SDK
 *   Copyright (C)2010 NyARToolkit for Android team
 *   Copyright (C)2010 R.Iizuka(nyatla)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * For further information please contact.
 *  http://sourceforge.jp/projects/nyartoolkit-and/
 *
 * This work is based on the NyARToolKit developed by
 *  R.Iizuka (nyatla)
 *    http://nyatla.jp/nyatoolkit/
 *
 * contributor(s)
 *  Atsuo Igarashi
 *  
 *  P.S.
 *  This program was modified by ka-ka_xyz for MMD_AR_Viewer
 */

package jp.gr.java_conf.ka_ka_xyz.activities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jp.androidgroup.nyartoolkit.ARToolkitDrawer;
import jp.androidgroup.nyartoolkit.MmdModelRenderer;
import jp.androidgroup.nyartoolkit.ModelRenderer;
import jp.gr.java_conf.ka_ka_xyz.R;
import jp.gr.java_conf.ka_ka_xyz.exception.TooHugeFileException;
import jp.gr.java_conf.ka_ka_xyz.exception.UnsupportedFileTypeException;
import jp.gr.java_conf.ka_ka_xyz.nyatla.nymmd.MMDModelInfo;
import jp.gr.java_conf.ka_ka_xyz.util.PreferenceUtils;
import jp.gr.java_conf.ka_ka_xyz.util.StringUtil;

import com.android.camera.CameraHardwareException;
import com.android.camera.CameraHolder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import jp.gr.java_conf.ka_ka_xyz.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class NyARToolkitAndroidActivity extends Activity {
	// public class NyARToolkitAndroidActivity extends Activity implements
	// View.OnClickListener, SurfaceHolder.Callback,
	// min3d.interfaces.ISceneController {

	public static final String TAG = "NyARToolkitAndroid";

	private static final int CROP_MSG = 1;
	private static final int FIRST_TIME_INIT = 2;
	//private static final int RESTART_PREVIEW = 3;
	private static final int CLEAR_SCREEN_DELAY = 4;
//	private static final int SET_CAMERA_PARAMETERS_WHEN_IDLE = 5;
	public static final int SHOW_LOADING = 6;
	public static final int HIDE_LOADING = 7;

	private static final int SCREEN_DELAY = 2 * 60 * 1000;

	private android.hardware.Camera.Parameters mParameters;

	private static final int IDLE = 1;

	private android.hardware.Camera mCameraDevice = null;
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder = null;
	private boolean isPreviewStarted = false;

	private GLSurfaceView mGLSurfaceView = null;
	// Renderer for metasequoia model
	private ModelRenderer mRenderer;

	// Renderer of min3d
	// private Renderer mRenderer;

	private boolean mPreviewing;
	private boolean mPausing;
	private boolean alreadyInitialized;

	private Handler mHandler = new MainHandler();;

	private PreviewCallback mPreviewCallback = null;

	private ARToolkitDrawer arToolkitDrawer = null;

	// private MediaPlayer mMediaPlayer = null;

	private ArrayList<InputStream> patt = new ArrayList<InputStream>();

	private InputStream camePara = null;

	private MMDModelInfo[] mmdModelInfo = null;
	
	private NyARToolkitAndroidActivity.SurfaceCallback callback = null;

	/**
	 * This Handler is used to post message back onto the main thread of the
	 * application
	 */
	private class MainHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if(!readyToStart)return;
			Log.i(TAG, "handle message: " + msg.toString());
			try {
				switch (msg.what) {
//				case RESTART_PREVIEW: {
//					//restartPreview();
//					break;
//				}

				case CLEAR_SCREEN_DELAY: {
					getWindow().clearFlags(
							WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
					break;
				}

				case FIRST_TIME_INIT: {
					initialize();
					break;
				}

				case SHOW_LOADING: {
					showDialog(DIALOG_LOADING);
					break;
				}
				case HIDE_LOADING: {
					try {
						dismissDialog(DIALOG_LOADING);
						removeDialog(DIALOG_LOADING);
					} catch (IllegalArgumentException e) {
					}
					break;
				}
				}
			} catch (Throwable t) {
				Log.i(TAG, "failed to handle Message", t);
			}
		}
	}

	private static final int DIALOG_LOADING = 0;

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_LOADING: {
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage("Loading ...");
			// dialog.setIndeterminate(true);
			dialog.setCancelable(false);
			dialog.getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
					WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
			return dialog;
		}
		default:
			return super.onCreateDialog(id);
		}
	}
	// Snapshots can only be taken after this is called. It should be called
	// once only. We could have done these things in onCreate() but we want to
	// make preview screen appear as soon as possible.
	private void initialize() {
		if (alreadyInitialized){
			initializeSecondTime();
			return;
		}
			

		Log.d(TAG, "start initializeFirstTime");
		alreadyInitialized = true;
		changeGLSurfaceViewState();
		Log.d(TAG, "finish initializeFirstTime");
	}

	// If the activity is paused and resumed, this method will be called in
	// onResume.
	private void initializeSecondTime() {
		Log.d(TAG, "start initializeSecondTime");
		try {
			ensureCameraDevice();
		} catch (CameraHardwareException e) {
			showCameraErrorAndFinish();
		}
		setCameraParameters();

		changeGLSurfaceViewState();
		Log.d(TAG, "finish initializeSecondTime");
	}

	/**
	 * Callback interface used to deliver copies of preview frames as they are
	 * displayed.
	 */
	private final class PreviewCallback implements
			android.hardware.Camera.PreviewCallback {

		private long previouse = 0;
		private static final int INTERVAL = 100;

		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			Log.d(TAG, "PreviewCallback.onPreviewFrame");

			if (mPausing) {
				return;
			}
			long current = SystemClock.currentThreadTimeMillis();
			if (data != null) {
				Log.d(TAG, "data exist");

				if (arToolkitDrawer != null)
					arToolkitDrawer.draw(data, previewWidth, previewHeight);

			} else {
				try {
					// The measure against over load.
					// MEMO original 500

					if (current - previouse < INTERVAL) {
						Thread.sleep(INTERVAL - (current - previouse));
					}
					previouse = current;

				} catch (InterruptedException e) {
				}

			}
			restartPreview();
		}
	}
	
	/**
	 * アプリケーション起動フラグ。
	 * */
	private static boolean readyToStart;
	/** Called with the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		new Log(this);
		
		if(!readyToStart && Runtime.getRuntime().maxMemory() < PreferenceUtils.XMX_THRESHOLD){
			final AlertDialog.Builder adb = new AlertDialog.Builder(this);
			adb.setTitle(getString(R.string.MaxHeapTitle));
			
			int maxHeap = (int)Runtime.getRuntime().maxMemory()/1000000; 
			
			String[] values = {(PreferenceUtils.XMX_THRESHOLD/1000000) + "", maxHeap + ""};
			String msg = StringUtil.replace(getString(R.string.MaxHeapDialog), 
					values);
			adb.setMessage(msg);
			adb.setPositiveButton("Continue", new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					readyToStart = true;
					Intent intent = new Intent(getBaseContext(), NyARToolkitAndroidActivity.class);
					startActivity(intent);
				}
			});
			adb.setNegativeButton("Stop", new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			adb.setCancelable(false);
			adb.create().show();
			
		} else {
			readyToStart = true;
		}
		requestWindowFeature(Window.FEATURE_PROGRESS);

		Window win = getWindow();
		win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		win.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.main);
	}

	private void changeGLSurfaceViewState() {
		// If the camera resumes behind the lock screen, the orientation
		// will be portrait. That causes OOM when we try to allocation GPU
		// memory for the GLSurfaceView again when the orientation changes. So,
		// we delayed initialization of GLSurfaceView until the orientation
		// becomes landscape.
		
		Log.d(TAG, "start changeGLSurfaceViewState");
		Configuration config = getResources().getConfiguration();
		if (config.orientation == Configuration.ORIENTATION_LANDSCAPE
				&& !mPausing && alreadyInitialized) {
			if (mGLSurfaceView == null)
				initializeGLSurfaceView();
		} 
		
		//TODO
		if(mGLSurfaceView != null){
			double ratio = getSurfaceViewEnlargeRatio();
			ViewGroup.LayoutParams gllayoutParams = mGLSurfaceView.getLayoutParams();
			gllayoutParams.width = (int)(previewWidth*ratio);
			gllayoutParams.height = (int)(previewHeight*ratio);
			mGLSurfaceView.setLayoutParams(gllayoutParams);
		}
		//TODO
		
		Log.d(TAG, "finish changeGLSurfaceViewState");
	}
	
	private double getSurfaceViewEnlargeRatio(){
		int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
		int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
		double ratio = (double)Math.min(screenWidth, screenHeight)
		/ (double)Math.min(previewHeight,previewWidth);
		return ratio;
	}

	private void initializeGLSurfaceView() {
		Log.d(TAG, "strat initializeGLSurfaceView");
		// init ARToolkit.
		if (arToolkitDrawer == null) {
			int[] width = new int[2];
			for (int i = 0; i < 2; i++) {
				width[i] = 80;
			}
			// patt.add(getResources().openRawResource(R.raw.pattmadr));
			// camePara = getResources().openRawResource(R.raw.camera_para);
			arToolkitDrawer = new ARToolkitDrawer(camePara, width, patt,
					mRenderer);

			Log.d(TAG, "finish initializeGLSurfaceView");
			// mMediaPlayer = MediaPlayer.create(this, R.raw.miku_voice);
			// mMediaPlayer.setLooping(true);
			// mMediaPlayer.setOnPreparedListener(new
			// MediaPlayer.OnPreparedListener() {
			// public void onPrepared(MediaPlayer mediaplayer) {
			// arToolkitDrawer.setMediaPlayer(mediaplayer);
			// }
			// });
		}

		FrameLayout frame = (FrameLayout) findViewById(R.id.frame);
		if(mGLSurfaceView == null){
			mGLSurfaceView = new GLSurfaceView(getApplicationContext());
		}
		mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		mGLSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		mGLSurfaceView.setZOrderOnTop(true);
		mGLSurfaceView.setRenderer(mRenderer);
		mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		frame.addView(mGLSurfaceView);
		Log.d(TAG, "finish initializeGLSurfaceView");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(!readyToStart)return;
	}

	@Override
	public void onStart() {
		super.onStart();
		if(!readyToStart)return;
	}

	@Override
	public void onStop() {
		super.onStop();
		FrameLayout frame = (FrameLayout) findViewById(R.id.frame);
		frame.removeView(mGLSurfaceView);
		mGLSurfaceView = null;
		
		if(mSurfaceHolder != null){
			mSurfaceHolder.removeCallback(callback);
			callback = null;
		}
		mSurfaceHolder = null;
	}
	
	@Override
	public void onRestart(){
		super.onRestart();
		showPreference();
	}

//    @Override
//	public void onConfigurationChanged(Configuration newConfig) {
//		super.onConfigurationChanged(newConfig);
//		setCameraParameters();
//		changeGLSurfaceViewState();
//		
//		
//	}

	private void loadRawResources() {
		closeRawResources();
		camePara = getResources().openRawResource(R.raw.camera_para);
		patt.clear();
		patt.add(getResources().openRawResource(R.raw.pattmadr));
	}
	
	private void closeRawResources(){
		try {
			if (camePara != null) {
				camePara.close();
			}
			if (1 < patt.size()) {
				for (InputStream is : patt) {
					if (is != null)
						is.close();
				}
			}
		} catch (IOException ioe) {
			throw new IllegalStateException(ioe);
		}

		if (mmdModelInfo != null) {
			for (MMDModelInfo mmi : mmdModelInfo) {
				if (mmi != null)
					mmi.finalize();
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if(!readyToStart)return;
		Log.d(TAG, "start onResume");

		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
		}
		if(mSurfaceView == null){
			mSurfaceView = (SurfaceView) findViewById(R.id.camera_preview);
		}
		mSurfaceView.setKeepScreenOn(true);
		SurfaceHolder holder = mSurfaceView.getHolder();
		
		loadRawResources();
		// from onCreate
		try {
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(this);
			
			this.previewWidth = sp.getInt(PreferenceUtils.PREVIEW_WIDTH, 320);
			this.previewHeight = sp.getInt(PreferenceUtils.PREVIEW_HEIGHT, 200);
			
			
			String pmdPath = sp.getString(PreferenceUtils.PMDPATH, "");
			String vmdPath = sp.getString(PreferenceUtils.VMDPATH, "");
			File pmd = new File(pmdPath);
			File vmd = new File(vmdPath);
			boolean enableScale = sp.getBoolean(PreferenceUtils.SCALE_ENABLED,
					false);
			boolean isVBOEnabled = sp.getBoolean(PreferenceUtils.VBO_ENABLED,
					true);
			
			int scale = sp.getInt(PreferenceUtils.SCALE_PROGRESS, 1);
			
			PreferenceUtils.isTextureEnabled = sp.getBoolean(PreferenceUtils.TEXTURE_ENABLED, true);

			if ("".equals(pmdPath) || !pmd.exists() || !pmd.isFile()
					|| "".equals(vmdPath) || !vmd.exists() || !vmd.isFile()) {
				showPreference();
			}
			mmdModelInfo = new MMDModelInfo[1];

			mmdModelInfo[0] = new MMDModelInfo(pmdPath, vmdPath);
			Log.d(TAG, "Load MMDModel: " + mmdModelInfo[0].toString());
			// TODO sizeを指定可能に
			float[] modelScale = new float[] { ((float) scale) * 0.1f };
			mRenderer = new MmdModelRenderer(getAssets(), mmdModelInfo,
					enableScale, modelScale, isVBOEnabled);
			mRenderer.setMainHandler(mHandler);
			Log.d(TAG, "renderer: " + mRenderer.hashCode());
			
		} catch (TooHugeFileException thfe) {
			Log.i("MMDINFO", "Too Huge", thfe);
			showFileErrorDialog("Too Huge Size File to load.", 
					thfe.getLocalizedMessage());
		} catch (Throwable t) {
			Log.i("MMDINFO", "mmd_throwable", t);
		}
		if(callback == null){
			callback = new NyARToolkitAndroidActivity.SurfaceCallback();
		}
		holder.addCallback(callback);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mPausing = false;

		if (mSurfaceHolder != null) {
			// If first time initialization is not finished, put it in the
			// message queue.
			if (!alreadyInitialized) {
				mHandler.sendEmptyMessage(FIRST_TIME_INIT);
			} else {
				initializeSecondTime();
			}
		}
		if(mGLSurfaceView != null){
			mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		} 
		
		keepScreenOnAwhile();
		Log.d(TAG, "finish onResume");
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(!readyToStart)return;
		Log.d(TAG, "onPause");
		mPausing = true;
		stopPreview();
		// Close the camera now because other activities may need to use it.
		closeCamera();
		closeRawResources();
		resetScreenOn();

		// Remove the messages in the event queue.
//		mHandler.removeMessages(RESTART_PREVIEW);
		mHandler.removeMessages(FIRST_TIME_INIT);
		mmdModelInfo[0] = null;
		mmdModelInfo = null;
		arToolkitDrawer = null;
		
		mRenderer = null;

		FrameLayout frame = (FrameLayout) findViewById(R.id.frame);
		
		if(mGLSurfaceView != null){
			frame.removeView(mGLSurfaceView);
			mGLSurfaceView = null;
		}
		
		if(mSurfaceHolder != null){
			mSurfaceHolder.removeCallback(callback);
			callback = null;
		}
		mSurfaceHolder = null;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case CROP_MSG: {
			Intent intent = new Intent();
			if (data != null) {
				Bundle extras = data.getExtras();
				if (extras != null) {
					intent.putExtras(extras);
				}
			}
			setResult(resultCode, intent);
			finish();
			break;
		}
		}
	}

	private int previewWidth;
	private int previewHeight;

	private void showFileErrorDialog(String title, String msg){
		final AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle(title);
		adb.setMessage(msg);
		adb.setCancelable(false);
		adb.setPositiveButton("OK", new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				showPreference();
				readyToStart = true;
			}
		});
		readyToStart = false;
		if(mGLSurfaceView != null){
			mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		}
		adb.create().show();
	}
	
	
	private void closeCamera() {
		Log.d(TAG, "start closeCamera");
		if (mCameraDevice != null) {
			Log.d(TAG, "execute closeCamera");
			CameraHolder.instance().keep();
			CameraHolder.instance().release();
			mCameraDevice = null;
			mPreviewing = false;
		}
		Log.d(TAG, "finish closeCamera");
	}

	private void ensureCameraDevice() throws CameraHardwareException {
		Log.d(TAG, "start ensureCameraDevice");
		if (mCameraDevice == null) {
			mCameraDevice = CameraHolder.instance().open();
			if (mCameraDevice == null) {
				Log.d(TAG, "openCamera failed");
				showCameraErrorAndFinish();
			}
			Log.d(TAG, "openCamera");
		}
		Log.d(TAG, "finish ensureCameraDevice");
	}

	private void showCameraErrorAndFinish() {
		Resources ress = getResources();
		com.android.camera.Util.showFatalErrorAndFinish(
				NyARToolkitAndroidActivity.this,
				ress.getString(R.string.camera_error_title),
				ress.getString(R.string.cannot_connect_camera));
	}

	
	private void restartPreview() {
		Log.d(TAG, "start restartPreview");
		if(!readyToStart)return;
		try {
			startPreview();
		} catch (CameraHardwareException e) {
			showCameraErrorAndFinish();
			return;
		}
		Log.d(TAG, "finish restartPreview");
	}
	
	private void startPreview() throws CameraHardwareException {
		Log.d(TAG, "start startPreview");
		if (mPausing || isFinishing() || !isPreviewStarted)
			return;

		
		Log.d(TAG, "startPreview renderer: " + mRenderer.hashCode());
		if(mRenderer.getError() != null && readyToStart){
			Log.d(TAG, "Inavlid pmd file.", mRenderer.getError());
			if(mRenderer.getError() instanceof UnsupportedFileTypeException){
				showFileErrorDialog("This pmd file is unavaliable", 
				"This pmd file refers unsupported *.shp or *.spa type texture file.");
			} else {
				Throwable t = mRenderer.getError();
				showFileErrorDialog("Failed to render", 
				"Error: " + t.getClass().getName() 
				+ " Message: " + t.getLocalizedMessage() );
			}
		}
		
		ensureCameraDevice();

		if (mCameraDevice == null) {
			throw new CameraHardwareException(new NullPointerException());
		}

		setPreviewDisplay(mSurfaceHolder);
		if (!mPreviewing)
			setCameraParameters();

		if(mPreviewCallback == null){
			mPreviewCallback = new PreviewCallback();
		}
		mCameraDevice.setOneShotPreviewCallback(mPreviewCallback);

		try {
			Log.v(TAG, "startPreview");
			mCameraDevice.startPreview();
		} catch (Throwable ex) {
			closeCamera();
			throw new RuntimeException("startPreview failed", ex);
		}
		mPreviewing = true;
		Log.d(TAG, "finish startPreview");
	}

	private void setPreviewDisplay(SurfaceHolder holder) {
		Log.d(TAG, "start setPreviewDisplay");
		try {
			mCameraDevice.setPreviewDisplay(holder);
		} catch (Throwable ex) {
			closeCamera();
			throw new RuntimeException("setPreviewDisplay failed", ex);
		}
		Log.d(TAG, "finish setPreviewDisplay");
	}

	private void stopPreview() {
		Log.d(TAG, "start stopPreview");
		if (mCameraDevice != null && mPreviewing) {
			Log.v(TAG, "stopPreview");
			mCameraDevice.setOneShotPreviewCallback(null);
			mCameraDevice.stopPreview();
			mPreviewCallback = null;
		}
		mPreviewing = false;
		isPreviewStarted = false;
		Log.d(TAG, "finish stopPreview");
	}

	private void setCameraParameters() {
		Log.d(TAG, "start setCameraParameters");
		if (mCameraDevice == null)
			return;
		mParameters = mCameraDevice.getParameters();
		Log.d("ASPECT", "width: " + previewWidth + ", height: " + previewHeight);
		mParameters.setPreviewSize(previewWidth, previewHeight);

		// Adjust SurfaceView size
		ViewGroup.LayoutParams layoutParams = mSurfaceView.getLayoutParams();
		double ratio = getSurfaceViewEnlargeRatio();
		layoutParams.width = (int)(previewWidth*ratio);
		layoutParams.height = (int)(previewHeight*ratio);
		mCameraDevice.setParameters(mParameters);
		Log.d(TAG, "finish setCameraParameters");
	}

	private void resetScreenOn() {
		Log.d(TAG, "start resetScreenOn");
		mHandler.removeMessages(CLEAR_SCREEN_DELAY);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		Log.d(TAG, "finish resetScreenOn");
	}

	private void keepScreenOnAwhile() {
		Log.d(TAG, "start keepScreenOnAwhile");
		mHandler.removeMessages(CLEAR_SCREEN_DELAY);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		mHandler.sendEmptyMessageDelayed(CLEAR_SCREEN_DELAY, SCREEN_DELAY);
		Log.d(TAG, "finish keepScreenOnAwhile");
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return alreadyInitialized;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean ret = super.onCreateOptionsMenu(menu);
		menu.add(0, Menu.FIRST, Menu.NONE, "About").setIcon(R.drawable.about);
		menu.add(0, Menu.FIRST + 1, Menu.NONE, "Settings").setIcon(
				R.drawable.settings);
		menu.add(0, Menu.FIRST + 2, Menu.NONE, "Stop").setIcon(
				R.drawable.stop);
		return ret;
	}


	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case (Menu.FIRST):
			showAbout();
			break;
		case (Menu.FIRST + 1):
			finish();
			break;
		case (Menu.FIRST + 2):
			setResult(PreferenceUtils.RESULT_STOP);
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showPreference() {
		Log.d(TAG, "start showPreference");
		ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.show();
		stopPreview();
		setResult(RESULT_OK);
		finish();
		Log.d(TAG, "finish showPreference");
	}

	private void showAbout() {
		Log.d(TAG, "start showAbout");
		ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.show();
		stopPreview();
		Intent intent = new Intent(this, AboutActivity.class);
		progressDialog.cancel();
		startActivity(intent);
		Log.d(TAG, "finish showPreference");
	}
	

	private class SurfaceCallback implements SurfaceHolder.Callback{

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			if(!readyToStart)return;
			Log.d(TAG, "start surfaceChanged");

			// Make sure we have a surface in the holder before proceeding.
			if (holder.getSurface() == null) {
				Log.d(TAG, "holder.getSurface() == null");
				return;
			}

			// We need to save the holder for later use, even when the mCameraDevice
			// is null. This could happen if onResume() is invoked after this
			// function.
			mSurfaceHolder = holder;

			// The mCameraDevice will be null if it fails to connect to the camera
			// hardware. In this case we will show a dialog and then finish the
			// activity, so it's OK to ignore it.
			if (mCameraDevice == null) {
				try {
					isPreviewStarted = true;
					startPreview();
				} catch (Exception e) {
					// In eng build, we throw the exception so that test tool
					// can detect it and report it
					if ("eng".equals(Build.TYPE)) {
						throw new RuntimeException(e);
					}
					isPreviewStarted = false;
				}
			}
			// Sometimes surfaceChanged is called after onPause.
			// Ignore it.
			if (mPausing || isFinishing())
				return;

			// If first time initialization is not finished, send a message to do
			// it later. We want to finish surfaceChanged as soon as possible to let
			// user see preview first.
			if (!alreadyInitialized) {
				mHandler.sendEmptyMessage(FIRST_TIME_INIT);
			} else {
				initializeSecondTime();
			}
			changeGLSurfaceViewState();
			Log.d(TAG, "finish surfaceChanged");
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			if(!readyToStart)return;
			
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if(!readyToStart)return;
			stopPreview();
		}
	}
	
}
