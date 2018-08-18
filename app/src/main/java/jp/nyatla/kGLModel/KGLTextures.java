/* 
 * PROJECT: NyARMqoView
 * --------------------------------------------------------------------------------
 * これはMetaseqファイル（.MQO）ファイルをｊａｖａに読み込み＆描画するクラスです。
 * Copyright (C)2008 kei
 * 
 * 
 * オリジナルファイルの著作権はkeiさんにあります。
 * オリジナルのファイルは以下のURLから入手できます。
 * http://www.sainet.or.jp/~kkoni/OpenGL/reader.html
 * 
 * このファイルは、http://www.sainet.or.jp/~kkoni/OpenGL/20080408.zipにあるファイルを
 * ベースに、NyARMqoView用にカスタマイズしたものです。
 *
 * For further information please contact.
 * 	A虎＠nyatla.jp
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp>
 * 
 */
package jp.nyatla.kGLModel;

import java.util.*;
import java.nio.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.io.*; // For File
import java.io.InputStream;
//import java.awt.image.*;    // For BufferedImage
//import javax.imageio.*;     // For ImageIO

import javax.microedition.khronos.opengles.*;

import android.content.res.*;
import android.graphics.*;
//import android.opengl.GLU;
import android.opengl.GLUtils;

/**
 * テクスチャの生成と管理<br>
 * 一度読み込んだテクスチャは再利用する。<br>
 * 使用後はClear()を呼んでください<br>
 * ＯｐｅｎＧＬへ登録したリソースの解放をします。<br>
 * 
 * @author kei
 * 
 */
public class KGLTextures {
	public AssetManager am;
	/**
	 * texture nameの保存コンテナ<br>
	 * テクスチャ名＋アルファファイル名＋透明度<br>
	 * をキーにOpenGLのtexture name（int）を保存しているコンテナ<br>
	 */
	private HashMap<String, Integer> texPool = null;

	/**
	 * コンストラクタ
	 * 
	 */
	public KGLTextures(AssetManager am) {
		this.am = am;
		texPool = new HashMap<String, Integer>();
	}

	/**
	 * ＯｐｅｎＧＬへ登録したリソースを解放する
	 * 
	 */
	public void Clear(GL11 gl) {
		Collection<Integer> collection = texPool.values();
		Integer[] ciarray = collection.toArray(new Integer[0]);
		if (ciarray.length == 0)
			return;
		int[] iarray = new int[ciarray.length];
		for (int i = 0; i < iarray.length; i++) {
			iarray[i] = ciarray[i];
		}
		gl.glDeleteTextures(iarray.length, iarray, 0);
		texPool.clear();
	}

	/**
	 * テクスチャの登録<br>
	 * 
	 * @param texname
	 *            テクスチャファイル名
	 * @param alpname
	 *            アルファファイル名
	 * @param reload
	 *            true:登録してあってもファイルから読み直しする
	 * @return OpenGLのtexture name（int）
	 */
	public int getGLTexture(GL11 gl, String texname, String alpname,
			boolean reload) {
		if (texname == null && alpname == null)
			return 0;
		Integer ret = 0;
		ret = texPool.get(texname + alpname);
		if (!reload) {// 再読込しない＆登録済みなら、登録してあったものを返す
			if (ret != null)
				return ret;
		} else {// 再読込＆登録済みなら、登録していたものを削除する
			if (ret != null) {
				gl.glBindTexture(GL11.GL_TEXTURE_2D, 0);
				glDeleteTexture(gl, ret);
				texPool.remove(texname + alpname);
				ret = 0;
			}
		}
		/*
		 * コレだと上下反転している画像の対応とか透明度を別ファイルで指定している場合とかがメンドイ if( alpname == null ) {
		 * try { Texture gltex = TextureIO.newTexture(new File(texname),false) ;
		 * boolean bbb = gltex.getMustFlipVertically() ;
		 * gltex.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER,GL.GL_LINEAR) ;
		 * gltex.setTexParameteri(GL.GL_TEXTURE_MIN_FILTER,GL.GL_LINEAR) ; ret =
		 * gltex.getTextureObject() ; texPool.put(texname+alpname+alpha,ret) ; }
		 * catch(Exception e) { e.printStackTrace() ; } if( ret != 0 ) return
		 * ret ; }
		 */
		Bitmap bitmap;
		bitmap = loadTexture(texname, alpname);
		if (bitmap == null)
			return 0;
		ret = glGenTexture(gl);
		if (ret == 0)
			return 0;
		gl.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
		gl.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
		gl.glBindTexture(GL11.GL_TEXTURE_2D, ret);
		gl.glTexParameterx(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
				GL11.GL_LINEAR);
		gl.glTexParameterx(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
				GL11.GL_LINEAR);
		GLUtils.texImage2D(GL11.GL_TEXTURE_2D, 0, bitmap, 0);
		gl.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		texPool.put(texname + alpname, ret);
		return ret;
	}

	/**
	 * OpenGLにテクスチャを登録する（１個）
	 * 
	 * @return 登録したtexture name(int)
	 */
	private int glGenTexture(GL11 gl) {
		int texs[] = new int[1];
		gl.glGenTextures(1, texs, 0);
		return texs[0];
	}

	/**
	 * 登録されたテクスチャを削除する（１個）
	 * 
	 * @param tex
	 *            登録済みtexture name(int)
	 */
	private void glDeleteTexture(GL11 gl, int tex) {
		int texs[] = new int[1];
		texs[0] = tex;
		gl.glDeleteTextures(1, texs, 0);
		return;
	}

	/**
	 * イメージファイルからデータ列を読み込む
	 * 
	 * @param texname
	 *            テクスチャファイル名
	 * @param alpname
	 *            アルファファイル名
	 * @return 読み込んだデータ列
	 */
	protected Bitmap loadTexture(String texname, String alpname) {
		try {
			InputStream is = am.open(texname);
			return BitmapFactory.decodeStream(is);
		} catch (Throwable e) {
		}
		return null;
	}

	//
	public void reset(GL11 gl, String texname, String alpname) {
		Integer ret = 0;
		ret = texPool.get(texname + alpname);
		if (ret != null) {
			if (gl != null) {
				gl.glBindTexture(GL11.GL_TEXTURE_2D, 0);
				glDeleteTexture(gl, ret);
			}
			texPool.remove(texname + alpname);
		}
	}
}
