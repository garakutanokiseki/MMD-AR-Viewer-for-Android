package jp.gr.java_conf.ka_ka_xyz.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public class Log {
	private static boolean isDebuggable;

	public Log(Context context) {
		try {
			ApplicationInfo info = context.getPackageManager()
					.getApplicationInfo(context.getPackageName(), 0);
			if ((info.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE) {
				isDebuggable = true;
			} else {
				isDebuggable = false;
			}
		} catch (NameNotFoundException e) {
			isDebuggable = false;
		}
	}

	public static void d(String tag, String msg) {
		if (isDebuggable)
			android.util.Log.d(tag, msg);
	}

	public static void d(String tag, String msg, Throwable t) {
		if (isDebuggable)
			android.util.Log.d(tag, msg, t);
	}

	public static void e(String tag, String msg) {
		android.util.Log.e(tag, msg);
	}

	public static void e(String tag, String msg, Throwable t) {
		android.util.Log.e(tag, msg, t);
	}

	public static void getStackTraceString(Throwable t) {
		android.util.Log.getStackTraceString(t);
	}

	public static void i(String tag, String msg) {
		if (isDebuggable)
			android.util.Log.i(tag, msg);
	}

	public static void i(String tag, String msg, Throwable t) {
		if (isDebuggable)
			android.util.Log.i(tag, msg, t);
	}

	public static void isLoggable(String tag, int i) {
		android.util.Log.isLoggable(tag, i);
	}

	public static void println(int i, String tag, String msg) {
		android.util.Log.println(i, tag, msg);
	}

	public static void v(String tag, String msg) {
		if (isDebuggable)
			android.util.Log.v(tag, msg);
	}

	public static void v(String tag, String msg, Throwable t) {
		if (isDebuggable)
			android.util.Log.v(tag, msg, t);
	}

	public static void w(String tag, String msg) {
		android.util.Log.w(tag, msg);
	}

	public static void w(String tag, String msg, Throwable t) {
		android.util.Log.w(tag, msg, t);
	}

	public static void w(String tag, Throwable t) {
		android.util.Log.w(tag, t);
	}

	public static void wtf(String tag, String msg) {
		android.util.Log.wtf(tag, msg);
	}

	public static void wtf(String tag, String msg, Throwable t) {
		android.util.Log.wtf(tag, msg, t);
	}

	public static void wtf(String tag, Throwable t) {
		android.util.Log.wtf(tag, t);
	}
}
