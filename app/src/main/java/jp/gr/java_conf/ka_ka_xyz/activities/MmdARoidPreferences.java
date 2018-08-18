package jp.gr.java_conf.ka_ka_xyz.activities;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.android.camera.CameraHardwareException;
import com.android.camera.CameraHolder;
import com.lamerman.FileDialog;

import jp.gr.java_conf.ka_ka_xyz.R;
import jp.gr.java_conf.ka_ka_xyz.util.PreferenceUtils;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

public class MmdARoidPreferences extends Activity {

	private SharedPreferences sp;

	private EditText pmdPath = null;
	private EditText vmdPath = null;

	private SeekBar scaleSeekBar = null;
	private TextView scaleText = null;
	
	private Spinner spinner = null;

	private Resolution resolution;
	
	private Button acceptBtn = null;
	private Button cancelBtn = null;

	private boolean enableScale;
	
	private static boolean enableVBO;

	private static final int REQUEST_FOR_PMDFILEPATH = 100;
	private static final int REQUEST_FOR_VMDFILEPATH = 200;
	private static final int REQUEST_FOR_MAIN = 1000;

	@Override
	public void onPause(){
		super.onPause();
		try{
			CameraHolder.instance().release();			
		} catch(AssertionError ae){
			try {
				CameraHolder.instance().open();
			} catch (CameraHardwareException e) {}
			CameraHolder.instance().release();
		}
		
	}
	
	@Override	public void onCreate(Bundle savedInstanceState) {
		System.gc();		
		setContentView(R.layout.settings);
		super.onCreate(savedInstanceState);
		sp = PreferenceManager.getDefaultSharedPreferences(this);
	}

	@Override
	public void onStart() {
		super.onStart();

		load();
		initPmdFileViews();
		initVmdFileViews();
		initLoadTextureChk();
		initVBOChk();
		initModelSchale();
		initResolutionSpinner();
		initAcceptBtn();
		initCancelBtn();
		setCancelBtnEneabled();
		setAcceptBtnEneabled();
	}

	private void initPmdFileViews() {
		final String initPmdPath;
		if (pmdPath == null) {
			pmdPath = (EditText) findViewById(R.id.pref_mmdfile_textedit);
			initPmdPath = sp.getString(PreferenceUtils.PMDPATH, "");
		} else {
			initPmdPath = pmdPath.getText().toString();
		}

		File pmd = new File(initPmdPath);
		final String initPmdDir;
		if (pmd.exists() && pmd.isFile()) {
			initPmdDir = pmd.getParentFile().getAbsolutePath();
		} else if (pmd.isDirectory()) {
			initPmdDir = pmd.getAbsolutePath();
		} else {
			initPmdDir = "/";
		}
		pmdPath.setText(initPmdPath);
		pmdPath.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MmdARoidPreferences.this,
						FileDialog.class);
				if (initPmdPath == null || "".equals(initPmdPath)) {
					intent.putExtra(FileDialog.START_PATH, "/");
				} else {
					intent.putExtra(FileDialog.START_PATH, initPmdDir);
				}
				intent.putExtra(FileDialog.EXTENTION_FILTER,
						new String[] { "pmd" });
				startActivityForResult(intent, REQUEST_FOR_PMDFILEPATH);
			}
		});
	}
	
	private void initLoadTextureChk(){
		CheckBox chk = (CheckBox) findViewById(R.id.pref_loadtexture_chk);
		PreferenceUtils.isTextureEnabled = sp.getBoolean(PreferenceUtils.TEXTURE_ENABLED, true);
		chk.setChecked(PreferenceUtils.isTextureEnabled);
		chk.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				PreferenceUtils.isTextureEnabled = isChecked;
			}
		});
	}

	private void initVmdFileViews() {
		final String initVmdPath;
		if (vmdPath == null) {
			vmdPath = (EditText) findViewById(R.id.pref_vmdfile_textedit);
			initVmdPath = sp.getString(PreferenceUtils.VMDPATH, "");
		} else {
			initVmdPath = vmdPath.getText().toString();
		}
		File vmd = new File(initVmdPath);
		final String initVmdDir;
		if (vmd.exists() && vmd.isFile()) {
			initVmdDir = vmd.getParentFile().getAbsolutePath();
		} else if (vmd.isDirectory()) {
			initVmdDir = vmd.getAbsolutePath();
		} else {
			initVmdDir = "/";
		}

		vmdPath.setText(initVmdPath);
		vmdPath.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MmdARoidPreferences.this,
						FileDialog.class);
				if (initVmdPath == null || "".equals(initVmdPath)) {
					intent.putExtra(FileDialog.START_PATH, "/");
				} else {
					intent.putExtra(FileDialog.START_PATH, initVmdDir);
				}
				intent.putExtra(FileDialog.EXTENTION_FILTER,
						new String[] { "vmd" });
				startActivityForResult(intent, REQUEST_FOR_VMDFILEPATH);
			}
		});
	}


	private void initVBOChk() {
		CheckBox chk = (CheckBox) findViewById(R.id.pref_vbo_chk);
		enableVBO = sp.getBoolean(PreferenceUtils.VBO_ENABLED, true);
		chk.setChecked(enableVBO);
		chk.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				enableVBO = isChecked;
			}
		});
	}
	
	private void initModelSchale() {

		CheckBox chk = (CheckBox) findViewById(R.id.pref_scale_chk);
		enableScale = sp.getBoolean(PreferenceUtils.SCALE_ENABLED, false);
		chk.setChecked(enableScale);
		chk.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				scaleSeekBar.setEnabled(isChecked);
				enableScale = isChecked;
			}
		});

		if (scaleSeekBar == null) {
			scaleSeekBar = (SeekBar) findViewById(R.id.pref_scale_seekbar);
		}
		scaleSeekBar.setEnabled(enableScale);

		final int initProgress = sp.getInt(PreferenceUtils.SCALE_PROGRESS, 10);
		scaleSeekBar.setMax(PreferenceUtils.MAX_PROGRESS);
		scaleSeekBar.setProgress(initProgress);

		scaleText = (TextView) findViewById(R.id.pref_scale_text);
		scaleText.setText(String.valueOf(PreferenceUtils
				.convertProgressToScale(scaleSeekBar.getProgress())));

		scaleSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				scaleText.setText(String.valueOf(PreferenceUtils
						.convertProgressToScale(progress)));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
	}
	

	private void initResolutionSpinner() {
		if(spinner != null){
			return;
		}
		spinner = (Spinner) findViewById(R.id.pref_select_resolution);
		 ArrayAdapter<Resolution> adapter = new ArrayAdapter<Resolution>(this,android.R.layout.simple_spinner_item);
		 adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		try {
			Camera camera = CameraHolder.instance().open();
			List<Size> sizes = camera.getParameters().getSupportedPreviewSizes();
			Collections.sort(sizes, new Comparator<Size>(){
				@Override
				public int compare(Size size1, Size size2) {
					int rtn = 0;
					int a = size1.width*size1.height;
					int b = size2.width*size2.height;
					if(b < a){
						rtn = 1;
					} else if(b < a){
						rtn = -1;
					}
					return rtn;
				}}
			);
			
			for(int i = 0; i < sizes.size(); i++){
				Size size = sizes.get(i);
				Resolution res = new Resolution(size.width, size.height, i);
				adapter.add(res);
			}
			
			spinner.setAdapter(adapter);
			spinner.setSelection(sp.getInt(PreferenceUtils.PREVIEW_INDEX, 0));
		} catch (CameraHardwareException e) {}
		 spinner.setOnItemSelectedListener(
				 new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				Spinner spinner = (Spinner) arg0;
				resolution = (Resolution)spinner.getSelectedItem();
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
	}

	public synchronized void onActivityResult(final int requestCode,
			int resultCode, final Intent data) {

		switch (requestCode){
		
			case REQUEST_FOR_PMDFILEPATH:
				if (pmdPath == null) {
					initPmdFileViews();
				}
				pmdPath.setText(data.getStringExtra(FileDialog.RESULT_PATH));
				break;
			case REQUEST_FOR_VMDFILEPATH:
				if (vmdPath != null) {
					initVmdFileViews();
				}
				vmdPath.setText(data.getStringExtra(FileDialog.RESULT_PATH));
				break;
			
			case REQUEST_FOR_MAIN:
				if(resultCode == PreferenceUtils.RESULT_STOP ){
					stopActivity();
				}
				break;
		}
	}

	private void initCancelBtn() {
		cancelBtn = (Button) findViewById(R.id.pref_cancel);
		cancelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				stopActivity();
			}
		});
	}

	private void initAcceptBtn() {
		acceptBtn = (Button) findViewById(R.id.pref_accept);
		acceptBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				save();
				Intent intent = new Intent(getApplicationContext(), NyARToolkitAndroidActivity.class);
				startActivityForResult(intent, REQUEST_FOR_MAIN);
			}
		});
	}
	
	private void stopActivity(){
		finish();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	private void save() {
		sp.edit()
				.putString(PreferenceUtils.PMDPATH,
						this.pmdPath.getText().toString()).commit();
		sp.edit().putBoolean(PreferenceUtils.TEXTURE_ENABLED, PreferenceUtils.isTextureEnabled)
			.commit();
		sp.edit()
				.putString(PreferenceUtils.VMDPATH,
						this.vmdPath.getText().toString()).commit();
		sp.edit().putBoolean(PreferenceUtils.VBO_ENABLED, enableVBO)
			.commit();
		sp.edit()
				.putInt(PreferenceUtils.SCALE_PROGRESS,
						this.scaleSeekBar.getProgress()).commit();
		sp.edit().putBoolean(PreferenceUtils.SCALE_ENABLED, this.enableScale)
				.commit();
		sp.edit()
			.putInt(PreferenceUtils.PREVIEW_WIDTH,
				this.resolution.width).commit();
		sp.edit()
			.putInt(PreferenceUtils.PREVIEW_HEIGHT,
				this.resolution.height).commit();
		sp.edit()
			.putInt(PreferenceUtils.PREVIEW_INDEX,
					this.resolution.index).commit();
	}

	private void load() {
	}

	private void setAcceptBtnEneabled() {
		System.gc();		if (this.acceptBtn != null) {
			boolean enabled = true;
			if (pmdPath == null || "".equals(pmdPath.getText().toString())
					|| vmdPath == null
					|| "".equals(vmdPath.getText().toString())) {
				enabled = false;
			}
			acceptBtn.setEnabled(enabled);
		}
	}

	private void setCancelBtnEneabled() {
		System.gc();
		if (this.cancelBtn != null) {
			boolean enabled = true;
			if (pmdPath == null || "".equals(pmdPath.getText().toString())
					|| vmdPath == null
					|| "".equals(vmdPath.getText().toString())) {
				enabled = false;
			}
			cancelBtn.setEnabled(enabled);
		}

	}
	
	private static class Resolution{
		int width;
		int height;
		int index;
		static int max;
		
		Resolution(int width, int height, int index){
			this.width = width;
			this.height = height;
			this.index = index;
			if(max < this.index){
				max = this.index;
			}
		}
		
		@Override
		public String toString(){
			String rtn = this.width + " X " + this.height;
			if(this.index < 1){
				rtn = rtn + " (most lightweight but law quality)";
			} else if(max == this.index){
				rtn = rtn + " (most heavyweight but high quality)";
			}
			return rtn;
		}
	}

}
