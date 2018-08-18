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
 *  noritsuna
 */

package jp.androidgroup.nyartoolkit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11;

import jp.gr.java_conf.ka_ka_xyz.activities.NyARToolkitAndroidActivity;
import jp.nyatla.kGLModel.IModelData;
import jp.nyatla.kGLModel.KGLException;
import jp.nyatla.kGLModel.KGLModelData;

import android.content.res.AssetManager;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.SystemClock;
import jp.gr.java_conf.ka_ka_xyz.util.Log;

/**
 * Rendering 3DModels.
 * 
 * @author noritsuna
 * 
 */
public class ModelRendererImpl implements ModelRenderer {

	private static final int PATT_MAX = 1;
	private static final int MARKER_MAX = 1;

	private int found_markers;
	private int[] ar_code_index = new int[MARKER_MAX];
	private float[][] resultf = new float[MARKER_MAX][16];
	private float[] cameraRHf = new float[16];
	private boolean useRHfp = false;

	private boolean drawp = false;

	// metaseq
	private IModelData[] model = new IModelData[PATT_MAX];
	private AssetManager am;
	private String[] modelName = new String[PATT_MAX];
	private float[] modelScale = new float[PATT_MAX];

	public int mWidth;
	public int mHeight;

	public ModelRendererImpl() {
	}

	public ModelRendererImpl(AssetManager am, String[] modelName,
			float[] modelScale) {
		this.am = am;
		cameraReset();
		for (int i = 0; i < PATT_MAX; i++) {
			this.modelName[i] = modelName[i];
			this.modelScale[i] = modelScale[i];
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jp.androidgroup.nyartoolkit.ModelRenderer#onSurfaceCreated(javax.microedition
	 * .khronos.opengles.GL11, javax.microedition.khronos.egl.EGLConfig)
	 */
	@Override
	public void onSurfaceCreated(GL10 _gl, EGLConfig config) {
		GL11 gl = (GL11) _gl;
		/*
		 * By default, OpenGL enables features that improve quality but reduce
		 * performance. One might want to tweak that especially on software
		 * renderer.
		 */
		gl.glDisable(GL11.GL_DITHER);

		/*
		 * Some one-time OpenGL initialization can be made here probably based
		 * on features of this particular context
		 */
		gl.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_FASTEST);

		gl.glClearColor(0, 0, 0, 0);

		initModel(gl);

		cameraChangep = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jp.androidgroup.nyartoolkit.ModelRenderer#onSurfaceChanged(javax.microedition
	 * .khronos.opengles.GL11, int, int)
	 */
	@Override
	public void onSurfaceChanged(GL10 _gl, int width, int height) {
		GL11 gl = (GL11) _gl;
		mWidth = width;
		mHeight = height;

		gl.glViewport(0, 0, width, height);

		/*
		 * Set our projection matrix. This doesn't have to be done each time we
		 * draw, but usually a new projection needs to be set when the viewport
		 * is resized.
		 */
		ratio = (float) width / height;

		cameraChangep = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jp.androidgroup.nyartoolkit.ModelRenderer#onDrawFrame(javax.microedition
	 * .khronos.opengles.GL11)
	 */
	@Override
	public void onDrawFrame(GL10 _gl) {
		GL11 gl = (GL11) _gl;
		/*
		 * Usually, the first thing one might want to do is to clear the screen.
		 * The most efficient way of doing this is to use glClear().
		 */

		// gl.glClearColor(bgColor[0], bgColor[1], bgColor[2], bgColor[3]);
		gl.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		if (drawp) {
			// camera
			if (useRHfp) {
				gl.glMatrixMode(GL11.GL_PROJECTION);
				gl.glLoadMatrixf(cameraRHf, 0);
			} else if (cameraChangep) {
				cameraSetup(gl);
			}

			// FIXME: Draw detected marker only.
			for (int i = 0; i < MARKER_MAX; i++) {
				gl.glMatrixMode(GL11.GL_MODELVIEW);
				if (useRHfp) {
					gl.glLoadMatrixf(resultf[i], 0);
					// 位置調整
					gl.glTranslatef(0.0f, 0.0f, 0.0f);
					// OpenGL座標系→ARToolkit座標系
					gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
				} else {
					gl.glLoadIdentity();
				}
				Log.d("ModelRenderer", "onDrawFrame: " + i + ",model: "
						+ ar_code_index[i]);
				if (lightp)
					lightSetup(gl);
				model[ar_code_index[i]].enables(gl, 1.0f);
				model[ar_code_index[i]].draw(gl);
				//model[ar_code_index[i]].disables(gl);
				if (lightp)
					lightCleanup(gl);
			}
		} else {
			gl.glMatrixMode(GL11.GL_PROJECTION);
			gl.glLoadMatrixf(cameraRHf, 0);
			gl.glEnable(GL11.GL_DEPTH_TEST);
		}
		makeFramerate();
	}

	private Handler mainHandler;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jp.androidgroup.nyartoolkit.ModelRenderer#setMainHandler(android.os.Handler
	 * )
	 */
	@Override
	public void setMainHandler(Handler handler) {
		mainHandler = handler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jp.androidgroup.nyartoolkit.ModelRenderer#initModel(javax.microedition
	 * .khronos.opengles.GL11)
	 */
	@Override
	public void initModel(GL10 _gl) {
		GL11 gl = (GL11) _gl;
		if (mainHandler != null) {
			mainHandler.sendMessage(mainHandler
					.obtainMessage(NyARToolkitAndroidActivity.SHOW_LOADING));
		}
		Log.d("ModelRenderer", "initModel");
		for (int i = 0; i < PATT_MAX; i++) {
			if (model[i] != null) {
				model[i].Clear(gl);
				model[i] = null;
			}
			if (modelName[i] != null) {
				try {
					model[i] = KGLModelData.createGLModel(gl, null, am,
							modelName[i], modelScale[i]);

				} catch (KGLException e) {
					Log.e("ModelRenderer", "KGLModelData error", e);
				}
			}
		}
		if (mainHandler != null) {
			mainHandler.sendMessage(mainHandler
					.obtainMessage(NyARToolkitAndroidActivity.HIDE_LOADING));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jp.androidgroup.nyartoolkit.ModelRenderer#objectClear()
	 */
	@Override
	public void objectClear() {
		drawp = false;
	}

	public float[] zoomV = new float[4];
	public float[] upOriV = { 0.0f, 1.0f, 0.0f, 0.0f };
	public float[] lookV = new float[4];
	public float[] camRmtx = new float[16];

	public float[] camV = new float[4];
	public float[] upV = new float[4];
	public float ratio;

	// Temporary
	private float[] mtx = new float[16];

	private boolean cameraChangep;

	private void cameraSetup(GL11 gl) {
		gl.glMatrixMode(GL11.GL_PROJECTION);
		gl.glLoadIdentity();
		// gl.glFrustumf(-ratio, ratio, -1, 1, 1, 1000);
		GLU.gluPerspective(gl, 45, ratio, 1.0f, 10000.0f);
		GLU.gluLookAt(gl, camV[0], camV[1], camV[2], lookV[0], lookV[1],
				lookV[2], upV[0], upV[1], upV[2]);
		cameraChangep = false;
	}

	private void cameraMake() {
		Matrix.setIdentityM(mtx, 0);
		Matrix.translateM(mtx, 0, lookV[0], lookV[1], lookV[2]);
		Matrix.multiplyMM(mtx, 0, camRmtx, 0, mtx, 0);
		Matrix.multiplyMV(camV, 0, mtx, 0, zoomV, 0);
		Matrix.multiplyMV(upV, 0, camRmtx, 0, upOriV, 0);
		cameraChangep = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jp.androidgroup.nyartoolkit.ModelRenderer#cameraReset()
	 */
	@Override
	public void cameraReset() {
		zoomV[0] = zoomV[1] = camV[0] = camV[1] = 0.0f;
		zoomV[2] = camV[2] = -500.0f;
		lookV[0] = lookV[1] = lookV[2] = 0.0f;
		upV[0] = upV[2] = 0.0f;
		upV[1] = 1.0f;
		Matrix.setIdentityM(camRmtx, 0);
		cameraChangep = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jp.androidgroup.nyartoolkit.ModelRenderer#cameraRotate(float, float,
	 * float, float, float[])
	 */
	@Override
	public void cameraRotate(float rot, float x, float y, float z, float[] sMtx) {
		float[] vec = { x, y, z, 0 };
		Matrix.setIdentityM(mtx, 0);
		Matrix.rotateM(mtx, 0, rot, vec[0], vec[1], vec[2]);
		Matrix.multiplyMM(camRmtx, 0, sMtx, 0, mtx, 0);
		cameraMake();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jp.androidgroup.nyartoolkit.ModelRenderer#cameraZoom(float)
	 */
	@Override
	public void cameraZoom(float z) {
		zoomV[2] += z;
		cameraMake();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jp.androidgroup.nyartoolkit.ModelRenderer#cameraMove(float, float,
	 * float)
	 */
	@Override
	public void cameraMove(float x, float y, float z) {
		float[] vec = { x, y, z, 0 };
		Matrix.multiplyMV(vec, 0, camRmtx, 0, vec, 0);
		for (int i = 0; i < 3; i++) {
			lookV[i] += vec[i];
		}
		cameraMake();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jp.androidgroup.nyartoolkit.ModelRenderer#objectPointChanged(int,
	 * int[], float[][], float[])
	 */
	@Override
	public void objectPointChanged(int found_markers, int[] ar_code_index,
			float[][] resultf, float[] cameraRHf) {
		synchronized (this) {
			this.found_markers = found_markers;
			for (int i = 0; i < MARKER_MAX; i++) {
				this.ar_code_index[i] = ar_code_index[i];
				System.arraycopy(resultf[i], 0, this.resultf[i], 0, 16);
			}
			System.arraycopy(cameraRHf, 0, this.cameraRHf, 0, 16);
		}
		useRHfp = true;
		drawp = true;
	}

	// Light
	public boolean lightCamp = false;
	public boolean lightp = true;
	public boolean speLightp = false;

	float[] lightPos0 = { 10, 10, 10, 0 };
	float[] lightPos1 = { 10, 10, 10, 0 };
	float[] lightPos2 = { 10, 10, 10, 0 };
	float[] lightDif = { 0.6f, 0.6f, 0.6f, 1 };
	float[] lightSpe = { 1.0f, 1.0f, 1.0f, 1 };
	float[] lightAmb = { 0.01f, 0.01f, 0.01f, 1 };

	private void lightSetup(GL11 gl) {
		if (lightCamp) {
			gl.glLightfv(GL11.GL_LIGHT0, GL11.GL_POSITION, camV, 0);
			gl.glLightfv(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, lightDif, 0);
			gl.glLightfv(GL11.GL_LIGHT1, GL11.GL_POSITION, camV, 0);
			gl.glLightfv(GL11.GL_LIGHT1, GL11.GL_AMBIENT, lightAmb, 0);
			if (speLightp) {
				gl.glLightfv(GL11.GL_LIGHT2, GL11.GL_POSITION, camV, 0);
				gl.glLightfv(GL11.GL_LIGHT2, GL11.GL_SPECULAR, lightSpe, 0);
			}
		} else {
			gl.glLightfv(GL11.GL_LIGHT0, GL11.GL_POSITION, lightPos0, 0);
			gl.glLightfv(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, lightDif, 0);
			gl.glLightfv(GL11.GL_LIGHT1, GL11.GL_POSITION, lightPos2, 0);
			gl.glLightfv(GL11.GL_LIGHT1, GL11.GL_AMBIENT, lightAmb, 0);
			if (speLightp) {
				gl.glLightfv(GL11.GL_LIGHT2, GL11.GL_POSITION, lightPos1, 0);
				gl.glLightfv(GL11.GL_LIGHT2, GL11.GL_SPECULAR, lightSpe, 0);
			}
		}
		gl.glEnable(GL11.GL_LIGHTING);
		gl.glEnable(GL11.GL_LIGHT0);
		gl.glEnable(GL11.GL_LIGHT1);
		if (speLightp)
			gl.glEnable(GL11.GL_LIGHT2);
	}

	private void lightCleanup(GL11 gl) {
		gl.glDisable(GL11.GL_LIGHTING);
		gl.glDisable(GL11.GL_LIGHT0);
		gl.glDisable(GL11.GL_LIGHT1);
		gl.glDisable(GL11.GL_LIGHT2);
	}

	private int mFrames = 0;
	private float mFramerate;
	private long mStartTime;

	/*
	 * (non-Javadoc)
	 * 
	 * @see jp.androidgroup.nyartoolkit.ModelRenderer#getFramerate()
	 */
	@Override
	public float getFramerate() {
		return mFramerate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jp.androidgroup.nyartoolkit.ModelRenderer#getStartTime()
	 */
	@Override
	public float getStartTime() {
		return mStartTime;
	}

	private void makeFramerate() {
		long time = SystemClock.uptimeMillis();

		synchronized (this) {
			mFrames++;
			if (mStartTime == 0) {
				mStartTime = time;
			}
			if (time - mStartTime >= 1) {
				mFramerate = (float) (1000 * mFrames)
						/ (float) (time - mStartTime);
				Log.d("ModelRenderer", "Framerate: " + mFramerate + " ("
						+ (time - mStartTime) + "ms)");
				mFrames = 0;
				mStartTime = time;
			}
		}
	}

	@Override
	public Throwable getError() {
		// TODO Auto-generated method stub
		return null;
	}
}
