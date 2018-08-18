package jp.gr.java_conf.ka_ka_xyz.util;

import jp.gr.java_conf.ka_ka_xyz.util.Log;

public class PreferenceUtils {

	private PreferenceUtils() {
	}

	public static final String PMDPATH = "PMDPATH";
	public static final String TEXTURE_ENABLED = "TEXTURE_ENABLED";
	public static final String VMDPATH = "VMDPATH";
	public static final String SCALE_PROGRESS = "SCALE_PROGRESS";
	public static final String SCALE_ENABLED = "SCALE_ENABLED";
	public static final String VBO_ENABLED = "VBO_ENABLED";
	
	public static final String PREVIEW_WIDTH = "PREVIEW_WIDTH";
	public static final String PREVIEW_HEIGHT = "PREVIEW_HEIGHT";
	public static final String PREVIEW_INDEX = "PREVIEW_INDEX";
	
	public static final int RESULT_STOP = Integer.MAX_VALUE;
	
	public static final int MAX_PROGRESS = 20;

	/**Minimum XMX*/
	public static final int XMX_THRESHOLD = 15*1024*1024;
	
	public static boolean isTextureEnabled;

	/**
	 * プログレスバーの値（0～10、整数型）を倍率へ変換
	 * */
	public static double convertProgressToScale(int progress) {
		progress = progress + 1;
		double rtn;
		if (progress < MAX_PROGRESS / 2) {
			rtn = (double) progress / 10;
		} else {
			rtn = (double) (progress - 9);
		}
		Log.d("PREFERENCE", progress + ", " + MAX_PROGRESS / 2);
		return rtn;
	}
}
