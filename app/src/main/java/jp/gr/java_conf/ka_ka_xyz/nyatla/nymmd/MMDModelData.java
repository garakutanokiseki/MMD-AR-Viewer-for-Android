package jp.gr.java_conf.ka_ka_xyz.nyatla.nymmd;

import java.io.InputStream;

import javax.microedition.khronos.opengles.GL11;

import android.content.res.AssetManager;
import android.os.SystemClock;
import jp.gr.java_conf.ka_ka_xyz.util.Log;

import jp.nyatla.kGLModel.IModelData;
import jp.nyatla.kGLModel.KGLException;
import jp.nyatla.kGLModel.KGLTextures;
import jp.nyatla.nymmd.IMmdDataIo;
import jp.nyatla.nymmd.IMmdPmdRender;
import jp.nyatla.nymmd.MmdException;
import jp.nyatla.nymmd.MmdMotionPlayer;
import jp.nyatla.nymmd.MmdPmdModel;
import jp.nyatla.nymmd.MmdVmdMotion;

public class MMDModelData implements IModelData {

	private MmdPmdModel _pmd;

	private MmdVmdMotion _vmd;

	private MmdMotionPlayer _player;

	private IMmdPmdRender _render = null;

	private IMmdDataIo _data_io;

	private boolean enableScale;

	private float scale;

	/**
	 * ファイル名の拡張子を見て読み込みクラスを作成する。<br>
	 * →MQOファイルしか作ってないけどね！<br>
	 * 
	 * @param in_gl
	 *            OpenGLコマンド群をカプセル化したクラス
	 * @param in_texPool
	 *            テクスチャ管理クラス（nullならこのクラス内部に作成）
	 * @param i_file_provider
	 *            ファイル提供オブジェクト
	 * @param pmdfilename
	 *            pmdファイル文字列
	 * @param vmdfilename
	 *            vmdファイル文字列
	 * @param scale
	 *            モデルの倍率
	 * @param in_isUseVBO
	 *            頂点配列バッファを使用するかどうか
	 * @return モデルデータクラス
	 * @throws MmdException
	 */
	static public IModelData createGLModel(GL11 gl, KGLTextures in_texPool,
			AssetManager am, MMDModelInfo modelInfo, boolean enableScale,
			float scale, boolean useVBO) throws KGLException, MmdException {

		InputStream pmdIS = modelInfo.getPMDIS();
		InputStream vmdIS = modelInfo.getVMDIS();

		MmdPmdModel pmd = new MmdPmdModel(pmdIS);
		MmdVmdMotion vmd = new MmdVmdMotion(vmdIS);

		return new MMDModelData(gl, pmd, vmd, modelInfo, enableScale, scale, useVBO);
	}

	private MMDModelData(GL11 gl, MmdPmdModel pmd, MmdVmdMotion vmd,
			IMmdDataIo dataIO, boolean enableScale, float scale, boolean useVBO)
			throws MmdException {
		// テクスチャ用のIO
		this._data_io = dataIO;
		this._pmd = pmd;
		this._vmd = vmd;

		this.scale = scale;
		this.enableScale = enableScale;

		if(useVBO){
			this._render = new MmdPmdRenderGL11VBO();
		} else {
			this._render = new MmdPmdRenderGL11();
		}
		this._render.setPmd(gl, this._pmd, this._data_io);
		this._player = new MmdMotionPlayer(this._pmd, this._vmd);
		this._player.setLoop(true);// ループするよ。

	}

	@Override
	public void Clear(GL11 gl) {
		gl.glDisable(GL11.GL_BLEND);
		gl.glDisable(GL11.GL_TEXTURE_2D);
		gl.glDisable(GL11.GL_NORMALIZE);
		gl.glDisable(GL11.GL_ALPHA_TEST);
		gl.glDisable(GL11.GL_DEPTH_TEST);
		gl.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		gl.glDisableClientState(GL11.GL_COLOR_ARRAY);
		_render.dispose(gl);

	}

	@Override
	public void objectVisible(String objectName, boolean isVisible) {
		// TODO Auto-generated method stub
	}

	@Override
	public void materialVisible(String materialtName, boolean isVisible) {
		// TODO Auto-generated method stub

	}

	@Override
	public void materialVisible(String objectName, String materialtName,
			boolean isVisible) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enables(GL11 gl, float scale) {

		gl.glEnable(GL11.GL_CULL_FACE);
		gl.glEnable(GL11.GL_ALPHA_TEST);
		gl.glEnable(GL11.GL_BLEND);
		gl.glEnable(GL11.GL_SMOOTH);
		gl.glEnable(GL11.GL_DEPTH_TEST);
		gl.glEnable(GL11.GL_TEXTURE_2D);
	}

	@Override
	public void disables(GL11 gl) {

		gl.glDisable(GL11.GL_CULL_FACE);
		gl.glDisable(GL11.GL_ALPHA_TEST);
		gl.glDisable(GL11.GL_BLEND);
		gl.glDisable(GL11.GL_SMOOTH);
		gl.glDisable(GL11.GL_DEPTH_TEST);

	}

	private long prevtime = SystemClock.uptimeMillis();
	private int fps = 30;

	@Override
	public void draw(GL11 gl) {
		try {

			if (enableScale) {
				gl.glScalef(scale, scale, scale);
			}

			// // 位置調整
			gl.glTranslatef(0.0f, 0.0f, 0.0f);
			// // OpenGL座標系→ARToolkit座標系
			gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);

			long time = SystemClock.uptimeMillis();
			float elapsedframe = (float) (time - prevtime)
					* (1.0f / (float) fps);
			Log.d("FRAMERATE", "elapsedframe: " + elapsedframe + " for "
					+ (time - prevtime) + " ms");
			prevtime = time;
			// レンダリング
			this._player.updateMotion(elapsedframe);
			this._render.updateSkinning(this._player.refSkinningMatrix());
			this._render.render(gl);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void draw(GL11 gl, float alpha) {
		draw(gl);

	}

	@Override
	public void reloadTexture(GL11 gl) {
		// TODO Auto-generated method stub

	}

	@Override
	public void resetTexture() {
		// TODO Auto-generated method stub

	}
}
