package jp.gr.java_conf.ka_ka_xyz.nyatla.nymmd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import jp.gr.java_conf.ka_ka_xyz.exception.MMDARException;
import jp.gr.java_conf.ka_ka_xyz.util.Log;
import jp.gr.java_conf.ka_ka_xyz.util.PreferenceUtils;

import jp.nyatla.nymmd.IMmdDataIo;

public class MMDModelInfo implements IMmdDataIo {

	private String vmdId;
	private String pmdId;

	private InputStream vmdIs;
	private InputStream pmdIs;
		
	public MMDModelInfo(String pmdId, String vmdId) {
		this.vmdId = vmdId;
		this.pmdId = pmdId;

		File pmdFile = new File(pmdId);
		
		try {
			pmdIs = new FileInputStream(pmdFile);
		} catch (FileNotFoundException e) {
			throw new IllegalStateException(e);
		}

		File vmdFile = new File(vmdId);
		try {
			vmdIs = new FileInputStream(vmdFile);
		} catch (FileNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	public Bitmap getTextureBitmap(String i_name) {

		Bitmap bitmap = null;
		if(!PreferenceUtils.isTextureEnabled || i_name == null){
			return bitmap;
		}
		/*sphere map 簡易表示
		 * OpenGL ESがSphere Mapをサポートするまではコレで行く*/
		i_name = i_name.split("\\*")[0];
		File pmd = new File(pmdId);
		File texturePath = pmd.getParentFile();
		File texture = new File(texturePath, i_name);
		InputStream is;
		try {
			is = new FileInputStream(texture);
			bitmap = normalizeBitmap(BitmapFactory.decodeStream(is));
			is.close();
		} catch (FileNotFoundException e) {
			return null;
		} catch (Exception e) {
			throw new MMDARException(e);
		}
		return bitmap;
	}

	private Bitmap normalizeBitmap(Bitmap ori) {

		if (ori == null) {
			return null;
		}
		if (isNormarizedBitmap(ori)) {
			return ori;
		}
		int length = getNormarizedLength(ori.getWidth(), ori.getHeight());
		Log.d("MMDINFO", "length: " + length);
		Bitmap newBitmap = Bitmap.createScaledBitmap(ori, length, length, true);
		return newBitmap;
	}

	private static int getNormarizedLength(int x, int y) {

		int length = Math.max(x, y);
		int rtn = 1;
		final int two = 2;
		for (int pow = 2; pow < 15; pow++) {
			rtn = (int) Math.pow(two, pow);
			if (length < rtn + 1) {
				return rtn;
			}
		}
		return rtn;
	}

	private static boolean isPowerOfTwo(int x) {
		return x > 0 && (x & (x - 1)) == 0;
	}

	private static boolean isNormarizedBitmap(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		if (bitmap != null && width == height && isPowerOfTwo(width)) {
			return true;
		}
		return false;
	}

	@Override
	public InputStream getPMDIS() {
		return this.pmdIs;
	}

	@Override
	public InputStream getVMDIS() {
		return this.vmdIs;
	}

	@Override
	public String toString() {
		return "MMDModelInfo [vmdId=" + vmdId + ", pmdId=" + pmdId + "]";
	}

	@Override
	public void finalize() {
		try {
			super.finalize();
			this.pmdIs.close();
			this.vmdIs.close();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

}