package jp.gr.java_conf.ka_ka_xyz.activities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import jp.gr.java_conf.ka_ka_xyz.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LicenseActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.license);
	}

	@Override
	public void onStart() {
		super.onStart();
		initLicenseText();
		initCloseBtn();
	}

	private void initLicenseText() {
		EditText license = (EditText) findViewById(R.id.license);
		InputStream is = getResources().openRawResource(R.raw.gnu_gpl_v3);
		StringBuffer sb = new StringBuffer();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "UTF-8"));
			String s;
			while ((s = reader.readLine()) != null) {
				sb.append(s);
				sb.append("\n");
			}

		} catch (UnsupportedEncodingException e) {

		} catch (IOException e) {
			sb.append("No licensefile found!");
		}
		license.setText(sb.toString());
	}

	private void initCloseBtn() {
		Button closeBtn = (Button) findViewById(R.id.license_closeBtn);
		closeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
