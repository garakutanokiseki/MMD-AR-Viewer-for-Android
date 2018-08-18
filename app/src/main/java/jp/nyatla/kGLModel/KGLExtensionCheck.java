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
 *	A虎＠nyatla.jp
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp>
 * 
 */
package jp.nyatla.kGLModel;

import javax.microedition.khronos.opengles.*;

/**
 * OpenGL拡張が実装されているかどうかチェックする<br>
 * 
 * @author kei
 * 
 */
public class KGLExtensionCheck {

	static public boolean IsExtensionSupported(GL11 gl, String targetExtension) {
		boolean ret = false;
		String supported = gl.glGetString(GL11.GL_EXTENSIONS);
		int p;
		String[] s;
		while ((p = supported.indexOf(targetExtension)) != -1) {
			supported = supported.substring(p, supported.length());
			s = supported.split(" ", 2);
			if (s[0].trim().equals(targetExtension))
				return true;
		}

		return ret;
	}
}
