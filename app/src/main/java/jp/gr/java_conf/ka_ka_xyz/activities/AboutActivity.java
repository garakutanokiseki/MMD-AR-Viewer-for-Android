package jp.gr.java_conf.ka_ka_xyz.activities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import jp.gr.java_conf.ka_ka_xyz.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

public class AboutActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
	}

	@Override
	public void onStart() {
		super.onStart();
		initAbout();
		initCloseBtn();
		initLicenseBtn();
	}

	private void initAbout() {
		WebView about = (WebView) findViewById(R.id.about);
		InputStream is = getResources().openRawResource(R.raw.about);
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
			sb.append("No document found!");
		}
		// about.loadData(sb.toString(),"text/html", "UTF-8");
		about.loadDataWithBaseURL(null, sb.toString(), "text/html", "UTF-8",
				null);

		try {
			is.close();
		} catch (IOException e) {
		}

	}

	private void initCloseBtn() {
		Button closeBtn = (Button) findViewById(R.id.about_closeBtn);
		closeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	private void initLicenseBtn() {
		Button closeBtn = (Button) findViewById(R.id.about_showlicBtn);
		closeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getBaseContext(),
						LicenseActivity.class);
				startActivity(intent);
			}
		});
	}

}
