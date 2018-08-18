package jp.nyatla.kGLModel;

import javax.microedition.khronos.opengles.GL11;

public interface IModelData {

	/**
	 * ＯｐｅｎＧＬへ登録したリソースを解放する<br>
	 * 
	 */
	public abstract void Clear(GL11 gl);

	/**
	 * 描画有無を変更する<br>
	 * 
	 * @param objectName
	 *            オブジェクト名
	 * @param isVisible
	 *            描画有無
	 */
	public abstract void objectVisible(String objectName, boolean isVisible);

	/**
	 * 描画有無を変更する<br>
	 * 
	 * @param materialtName
	 *            マテリアル名
	 * @param isVisible
	 *            描画有無
	 */
	public abstract void materialVisible(String materialtName, boolean isVisible);

	/**
	 * 描画有無を変更する<br>
	 * 
	 * @param objectName
	 *            オブジェクト名
	 * @param materialtName
	 *            マテリアル名
	 * @param isVisible
	 *            描画有無
	 */
	public abstract void materialVisible(String objectName,
			String materialtName, boolean isVisible);

	/**
	 * 描画に必要なglEnable処理を一括して行う。<br>
	 * glEnableするものは<br>
	 * GL_DEPTH_TEST<br>
	 * GL_ALPHA_TEST<br>
	 * GL_NORMALIZE（scaleが1.0以外の場合のみ）<br>
	 * GL_TEXTURE_2D<br>
	 * GL_BLEND<br>
	 * これらが必要ないことがわかっているときは手動で設定するほうがよいと思います<br>
	 * 
	 * @param scale
	 *            描画するサイズ（１倍以外はＯｐｅｎＧＬに余計な処理が入る）
	 */
	public abstract void enables(GL11 gl, float scale);

	/**
	 * 描画で使ったフラグ（enables()で設定したもの）をおとす<br>
	 * glDsableするものは<br>
	 * GL_DEPTH_TEST<br>
	 * GL_ALPHA_TEST<br>
	 * GL_NORMALIZE<br>
	 * GL_TEXTURE_2D<br>
	 * GL_BLEND<br>
	 */
	public abstract void disables(GL11 gl);

	/**
	 * 描画<br>
	 * 内部に持っているデータを描画する
	 */
	public abstract void draw(GL11 gl);

	/**
	 * 描画<br>
	 * 内部に持っているデータを描画する
	 * 
	 * @param alpha
	 *            描画する透明度（０～１）
	 */
	public abstract void draw(GL11 gl, float alpha);

	// @@@ テクスチャリロード
	public abstract void reloadTexture(GL11 gl);

	public abstract void resetTexture();

}