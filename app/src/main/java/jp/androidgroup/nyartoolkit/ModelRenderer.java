package jp.androidgroup.nyartoolkit;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.opengl.GLSurfaceView;
import android.os.Handler;

public interface ModelRenderer extends GLSurfaceView.Renderer {

	public abstract void setMainHandler(Handler handler);

	public abstract void initModel(GL10 _gl);

	public abstract void objectClear();

	public abstract void cameraReset();

	public abstract void cameraRotate(float rot, float x, float y, float z,
			float[] sMtx);

	public abstract void cameraZoom(float z);

	public abstract void cameraMove(float x, float y, float z);

	public abstract void objectPointChanged(int found_markers,
			int[] ar_code_index, float[][] resultf, float[] cameraRHf);

	public abstract float getFramerate();

	public abstract float getStartTime();
	
	public abstract Throwable getError();

}