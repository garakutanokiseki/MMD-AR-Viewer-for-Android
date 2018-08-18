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

import java.io.*;
import java.util.*;
import java.nio.*;
import java.lang.reflect.*;

import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import javax.microedition.khronos.opengles.*;

import android.content.res.*;
import jp.gr.java_conf.ka_ka_xyz.util.Log;

/**
 * MQOファイルの読み込みと描画<br>
 * （描画は親クラスで実装済み）<br>
 * <br>
 * インスタンス化も親クラスのKGLModelData::createGLModelを使用する<br>
 * メタセコファイルフォーマットは<br>
 * <a href="http://www.metaseq.net/">http://www.metaseq.net/</a><br>
 * 参照。
 * 
 * @author kei
 * 
 */
public class KGLMetaseq extends KGLModelData {
	/**
	 * 「"」で囲まれた文字を取り出す
	 * 
	 * @param st
	 *            操作対象文字列
	 * @return 前後の「"」を抜いた文字列
	 */
	protected String getDoubleQuoatString(String st) {
		String ret;
		ret = st.trim();
		if (ret.charAt(0) != '\"') {
			return ret;
		}
		int p;
		p = ret.indexOf("\"", 1);
		return ret.substring(1, p);
	}

	/**
	 * 法線を求める
	 * 
	 * @param V
	 *            頂点配列
	 * @param A
	 *            頂点の位置
	 * @param B
	 *            頂点の位置
	 * @param C
	 *            頂点の位置
	 * @return 法線ベクトル
	 */
	protected KGLPoint calcNormal(KGLPoint[] V, int A, int B, int C) {
		KGLPoint ret = null;
		KGLPoint BA = null;
		KGLPoint BC = null;
		// ベクトルB->A
		BA = KGLPoint.vector(V[B], V[A]);
		// ベクトルB->C
		BC = KGLPoint.vector(V[B], V[C]);
		// 法線の計算
		ret = KGLPoint.createXYZ(BA.Y() * BC.Z() - BA.Z() * BC.Y(),
				BA.Z() * BC.X() - BA.X() * BC.Z(), BA.X() * BC.Y() - BA.Y()
						* BC.X());
		ret.normalize();// 正規化
		return ret;
	}

	/**
	 * 頂点法線を求める
	 * 
	 * @param mqoObj
	 *            読み込んだMQOデータ
	 * @return 頂点法線
	 */
	protected KGLPoint[] vNormal(objects mqoObj) {
		KGLPoint[] ret = null;
		KGLPoint sn = null;
		// 頂点に接している面の法線を頂点法線に足し込んでいく
		ret = new KGLPoint[mqoObj.vertex.length];
		for (int f = 0; f < mqoObj.face.length; f++) {
			sn = calcNormal(mqoObj.vertex, mqoObj.face[f].V[0],
					mqoObj.face[f].V[1], mqoObj.face[f].V[2]);
			if (sn == null)
				continue;
			for (int i = 0; i < 3; i++) {
				if (ret[mqoObj.face[f].V[i]] == null) {
					ret[mqoObj.face[f].V[i]] = KGLPoint.createXYZ(0, 0, 0);
				}
				ret[mqoObj.face[f].V[i]].add(sn);
			}
		}
		// 正規化（長さを求めて、ソレで割って０～１の値にする！）
		for (int v = 0; v < ret.length; v++) {
			if (ret[v] == null)
				continue;
			ret[v].normalize();
		}
		return ret;
	}

	/**
	 * オブジェクトのフィールドにデータを設定する<br>
	 * Integer、Float、Float[]、Stringにしか対応していない
	 * 
	 * @param obj
	 *            設定対象オブジェクト
	 * @param fl
	 *            設定対象フィールド
	 * @param ty
	 *            設定対象フィールドの型
	 * @param data
	 *            設定データ
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	protected void dataSetter(Object obj, Field fl, Class ty, String data)
			throws IllegalArgumentException, IllegalAccessException {
		// Integer型のデータを設定
		if (ty.toString().equals("class java.lang.Integer")) {
			fl.set(obj, Integer.parseInt(data));
		}
		// Float型のデータを設定
		if (ty.toString().equals("class java.lang.Float")) {
			fl.set(obj, Float.parseFloat(data));
		}
		// String型のデータを設定（""の内側）
		if (ty.toString().equals("class java.lang.String")) {
			fl.set(obj, getDoubleQuoatString(data));
		}
		// Float[]型のデータを設定
		if (ty.toString().equals("class [Ljava.lang.Float;")) {
			String[] s;
			s = data.split(" ");
			Float[] f = new Float[s.length];
			for (int i = 0; i < s.length; i++) {
				f[i] = Float.parseFloat(s[i]);
			}
			fl.set(obj, f);
		}

	}

	/**
	 * MQOファイルのMaterial情報読み込み＆データ保持クラス
	 * 
	 */
	private class material {
		/**
		 * マテリアル名
		 */
		String name = null;

		class material_data {
			Integer shader = null;

			Float[] col = null;

			Float dif = null;

			Float amb = null;

			Float emi = null;

			Float spc = null;

			Float pow = null;

			String tex = null;

			String aplane = null;
		}

		material_data data = null;

		String[] fls_names = null;

		Field[] fls = null;

		Class[] tys = null;

		public material() {
			// データフィールドをとりだす
			data = new material_data();
			fls = data.getClass().getDeclaredFields();
			int len;
			len = fls.length;
			fls_names = new String[len];
			tys = new Class[len];
			for (int i = 0; i < len; i++) {
				// フィールド名とフィールド型を取り出す
				fls_names[i] = fls[i].getName();
				tys[i] = fls[i].getType();
			}
		}

		public void set(String line) throws Exception {
			name = getDoubleQuoatString(line);
			int p;
			int pe;

			for (int fn = 0; fn < fls_names.length; fn++) {
				p = line.indexOf(fls_names[fn] + "(");
				if (p == -1)
					continue;
				pe = line.indexOf(")", p);
				dataSetter(data, fls[fn], tys[fn],
						line.substring(p + fls_names[fn].length() + 1, pe));
			}
			// data.texとdata.alphaにファイル識別子を書き出している？
			// これはファイルプロバイダの機能にまかせる。
			// File wf;
			// if (data.tex != null){
			// wf = new File(data.tex);
			// if (!wf.isAbsolute()) {// 相対パス
			// if (targetMQO.getParent() != null) {
			// data.tex = targetMQO.getParent() + File.separator
			// + data.tex;
			// }
			// }
			// }
			// if (data.aplane != null) {
			// wf = new File(data.aplane);
			// if (!wf.isAbsolute()) {// 相対パス
			// if (targetMQO.getParent() != null) {
			// data.aplane = targetMQO.getParent() + File.separator
			// + data.aplane;
			// }
			// }
			// }
		}

		public String toString() {
			String ret;
			ret = name;
			if (data.shader != null)
				ret += " shader(" + data.shader + ")";
			if (data.col != null) {
				ret += " col(";
				for (int i = 0; i < data.col.length; i++) {
					if (i != 0)
						ret += ", ";
					ret += data.col[i];
				}
				ret += ")";
			}
			if (data.dif != null)
				ret += " dif(" + data.dif + ")";
			if (data.amb != null)
				ret += " amb(" + data.amb + ")";
			if (data.emi != null)
				ret += " emi(" + data.emi + ")";
			if (data.spc != null)
				ret += " spc(" + data.spc + ")";
			if (data.pow != null)
				ret += " pow(" + data.pow + ")";
			if (data.tex != null)
				ret += " tex(" + data.tex + ")";
			if (data.aplane != null)
				ret += " aplane(" + data.aplane + ")";
			return ret;
		}
	}

	/**
	 * MQOファイルのObject情報読み込みクラス
	 * 
	 */
	private class objects {
		String name = null;

		class objects_data {
			Integer depth = null;

			Integer folding = null;

			Float[] scale = null;

			Float[] rotation = null;

			Float[] translation = null;

			Integer patch = null;

			Integer segment = null;

			Integer visible = null;

			Integer locking = null;

			Integer shading = null;

			Float facet = null;

			Float[] color = null;

			Integer mirror = null;

			Integer color_type = null;

			Integer mirror_axis = null;

			Float mirror_dis = null;
		}

		objects_data data = null;;

		String[] fls_names = null;

		Field[] fls = null;

		Class[] tys = null;

		KGLPoint[] vertex = null;

		class Face {
			Integer[] V = null;

			Integer M = null;

			Float[] UV = null;

			Float[] COL = null;
		}

		Face[] face;

		public objects() {
			// データフィールドをとりだす
			data = new objects_data();
			fls = data.getClass().getDeclaredFields();
			int len;
			len = fls.length;
			fls_names = new String[len];
			tys = new Class[len];
			// フィールド名とフィールド型を取り出す
			for (int i = 0; i < len; i++) {
				fls_names[i] = fls[i].getName();
				tys[i] = fls[i].getType();
			}
		}

		/**
		 * vertexチャンクの読み込み
		 * 
		 * @param num
		 *            チャンクにあるデータ数
		 * @param br
		 *            読み込みストリーム
		 * @param scale
		 *            モデルの倍率
		 * @return 頂点配列
		 * @throws Exception
		 */
		private KGLPoint[] readVertex(int num, multiInput br, float scale)
				throws Exception {
			KGLPoint[] ret = null;
			String line = null;
			String[] s;
			int cnt;
			ret = new KGLPoint[num];
			cnt = 0;
			try {
				while ((line = br.readLine()) != null) {
					if (line.length() <= 0)
						continue;
					line = line.trim();
					if (line.equals("}"))
						break;
					s = line.split(" ", 3);
					ret[cnt] = KGLPoint.createXYZ(Float.parseFloat(s[0])
							* scale, Float.parseFloat(s[1]) * scale,
							Float.parseFloat(s[2]) * scale);
					cnt++;
				}
			} catch (Exception e) {
				Log.e("KGLMetaseq", "MQOファイル　フォーマットエラー（Object>vertex）[" + line
						+ "]");
				throw e;
			}

			if (cnt != num)
				return null;
			return ret;
		}

		/**
		 * BVertexチャンクの読み込み
		 * 
		 * @param br
		 *            読み込みストリーム
		 * @return 頂点配列
		 * @param scale
		 *            モデルの倍率
		 * @throws Exception
		 */
		private KGLPoint[] readBvertex(multiInput br, float scale)
				throws Exception {
			KGLPoint[] ret = null;
			String line = null;
			String[] s;
			int p;
			int pe;
			int datasize = 0;
			try {
				while ((line = br.readLine().trim()) != null) {
					if (line.length() <= 0)
						continue;
					s = line.split(" ");
					if (s[0].equals("Vector")) {
						if (s.length != 3) {
							line = null;
							break;
						}
						p = s[2].indexOf("[");
						pe = s[2].indexOf("]");
						datasize = Integer.parseInt(s[2].substring(p + 1, pe));
						break;
					}
				}
			} catch (Exception e) {
				Log.e("KGLMetaseq", "MQOファイル　フォーマットエラー（Object>Bvertex）[" + line
						+ "]");
				throw new KGLException(e);
			}
			if (line == null) {
				return null;
			}
			if (datasize == 0)
				return null;
			byte[] bbuf = new byte[datasize];
			if (datasize != br.read(bbuf))
				return null;
			ByteBuffer bb;
			bb = ByteBuffer.wrap(bbuf);
			bb.order(ByteOrder.LITTLE_ENDIAN);// MQOファイルのエンディアンはIntel形式
			FloatBuffer fb = bb.asFloatBuffer();
			ret = new KGLPoint[fb.limit() / 3];
			fb.position(0);
			float[] wf = new float[3];
			for (int i = 0; i < ret.length; i++) {
				fb.get(wf);
				ret[i] = KGLPoint.create(wf).scale(scale);
			}
			while ((line = br.readLine().trim()) != null) {
				if (line.equals("}"))
					break;
			}
			return ret;
		}

		/**
		 * faceチャンクの読み込み
		 * 
		 * @param br
		 *            読み込みストリーム
		 * @return 面配列
		 * @throws Exception
		 */
		private Face[] readFace(multiInput br) throws Exception {
			ArrayList<Face> qf;
			String line = null;
			String[] s;
			Integer Mn;
			Face[] wface = null;
			int p;
			int pe;
			qf = new ArrayList<Face>();
			try {
				while ((line = br.readLine()) != null) {
					if (line.length() <= 0)
						continue;
					line = line.trim();
					if (line.equals("}"))
						break;
					wface = null;
					Mn = null;
					p = line.indexOf("M(");
					if (p != -1) {
						pe = line.indexOf(")", p);
						Mn = Integer.parseInt(line.substring(p + 2, pe));
					}
					p = line.indexOf("V(");
					if (p == -1)
						continue;
					pe = line.indexOf(")", p);
					s = line.substring(p + 2, pe).split(" ");
					if (s.length == 3) {
						wface = new Face[1];
						wface[0] = new Face();
						wface[0].V = new Integer[3];
						wface[0].V[0] = Integer.parseInt(s[0]);
						wface[0].V[1] = Integer.parseInt(s[1]);
						wface[0].V[2] = Integer.parseInt(s[2]);
						wface[0].M = Mn;
						p = line.indexOf("UV(");
						if (p != -1) {
							pe = line.indexOf(")", p);
							s = line.substring(p + 3, pe).split(" ");
							if (s.length != 2 * 3)
								throw new Exception("UVの数が不正");
							wface[0].UV = new Float[2 * 3];
							for (int i = 0; i < s.length; i++) {
								wface[0].UV[i] = Float.parseFloat(s[i]);
							}
						}
						p = line.indexOf("COL(");
						if (p != -1) {
							pe = line.indexOf(")", p);
							s = line.substring(p + 4, pe).split(" ");
							if (s.length != 3)
								throw new Exception("COLの数が不正");
							wface[0].COL = new Float[4 * 3];
							long wl;
							float wf;
							for (int i = 0; i < s.length; i++) {
								wl = Long.parseLong(s[i]);
								wf = (wl >>> 0) & 0x000000ff;
								wface[0].COL[i * 4 + 0] = wf / 255f;
								wf = (wl >>> 8) & 0x000000ff;
								wface[0].COL[i * 4 + 1] = wf / 255f;
								wf = (wl >>> 16) & 0x000000ff;
								wface[0].COL[i * 4 + 2] = wf / 255f;
								wf = (wl >>> 24) & 0x000000ff;
								wface[0].COL[i * 4 + 3] = wf / 255f;
							}
						}
					}
					// 頂点配列はすべて三角にするので、四角は三角ｘ２に分割
					// 0 3 0 0 3
					// □ → △ ▽
					// 1 2 1 2 2
					if (s.length == 4) {
						wface = new Face[2];
						wface[0] = new Face();
						wface[1] = new Face();
						wface[0].V = new Integer[3];
						wface[0].V[0] = Integer.parseInt(s[0]);
						wface[0].V[1] = Integer.parseInt(s[1]);
						wface[0].V[2] = Integer.parseInt(s[2]);
						wface[0].M = Mn;
						wface[1].V = new Integer[3];
						wface[1].V[0] = Integer.parseInt(s[0]);
						wface[1].V[1] = Integer.parseInt(s[2]);
						wface[1].V[2] = Integer.parseInt(s[3]);
						wface[1].M = Mn;
						p = line.indexOf("UV(");
						if (p != -1) {
							int uv_p;
							pe = line.indexOf(")", p);
							s = line.substring(p + 3, pe).split(" ");
							if (s.length != 2 * 4)
								throw new Exception("UVの数が不正");
							wface[0].UV = new Float[2 * 3];
							wface[1].UV = new Float[2 * 3];
							for (int i = 0; i < 2; i++) {
								uv_p = 0;
								for (int j = 0; j < 4; j++) {
									if (i == 0 && j == 3)
										continue;
									if (i == 1 && j == 1)
										continue;
									wface[i].UV[uv_p++] = Float
											.parseFloat(s[j * 2 + 0]);
									wface[i].UV[uv_p++] = Float
											.parseFloat(s[j * 2 + 1]);
								}
							}
						}
						p = line.indexOf("COL(");
						if (p != -1) {
							pe = line.indexOf(")", p);
							s = line.substring(p + 4, pe).split(" ");
							if (s.length != 4)
								throw new Exception("COLの数が不正");
							wface[0].COL = new Float[4 * 3];
							wface[1].COL = new Float[4 * 3];
							long wl;
							float wf;
							int col_p;
							for (int i = 0; i < 2; i++) {
								col_p = 0;
								for (int j = 0; j < s.length; j++) {
									if (i == 0 && j == 3)
										continue;
									if (i == 1 && j == 1)
										continue;
									wl = Long.parseLong(s[j]);
									wf = (wl >>> 0) & 0x000000ff;
									wface[i].COL[col_p * 4 + 0] = wf / 255f;
									wf = (wl >>> 8) & 0x000000ff;
									wface[i].COL[col_p * 4 + 1] = wf / 255f;
									wf = (wl >>> 16) & 0x000000ff;
									wface[i].COL[col_p * 4 + 2] = wf / 255f;
									wf = (wl >>> 24) & 0x000000ff;
									wface[i].COL[col_p * 4 + 3] = wf / 255f;
									col_p++;
								}
							}
						}

					}
					if (wface != null) {
						for (int i = 0; i < wface.length; i++) {
							qf.add(wface[i]);
						}
					}
				}
			} catch (Exception e) {
				Log.e("KGLMetaseq",
						"MQOファイル　フォーマットエラー（Object>face）" + e.getMessage() + "["
								+ line + "]");
				throw e;
			}
			if (qf.size() == 0)
				return null;
			return qf.toArray(new Face[0]);
		}

		private void set(String in_name, multiInput br, float scale)
				throws Exception {
			String line[];
			name = getDoubleQuoatString(in_name);
			while ((line = Chank(br, true)) != null) {
				if (line[0].equals("}"))
					break;
				if (line[line.length - 1].equals("{")) { // 内部チャンク
					if (line[0].equals("vertex")) {
						vertex = readVertex(Integer.parseInt(line[1]), br,
								scale);
						continue;
					}
					if (line[0].equals("BVertex")) {
						vertex = readBvertex(br, scale);
						continue;
					}
					if (line[0].equals("face")) {
						face = readFace(br);
						continue;
					}
					String single = null;
					while ((single = br.readLine()) != null) {
						if (single.trim().equals("}"))
							break;
					}
					continue;
				}
				for (int i = 0; i < fls_names.length; i++) {
					if (fls_names[i].equals(line[0])) {
						dataSetter(data, fls[i], tys[i], line[1]);
					}
				}

			}
		}

		public String toString() {
			String ret;
			ret = name;
			if (data.visible != null)
				ret += " visible(" + data.visible + ")";
			if (data.color != null) {
				ret += " color(";
				for (int i = 0; i < data.color.length; i++) {
					if (i != 0)
						ret += ", ";
					ret += data.color[i];
				}
				ret += ")";
			}
			if (data.facet != null)
				ret += " facet(" + data.facet + ")";
			return ret;

		}
	}

	/**
	 * 描画用マテリアル情報をMQOデータから作成
	 * 
	 * @param mqomat
	 *            MQOファイルから読み込んだマテリアル情報
	 * @param i_mqomat
	 *            MQOファイルのマテリアル番号
	 * @param mqoObjs
	 *            MQOファイルのオブジェクト情報
	 * @param vn
	 *            頂点法線配列
	 * @return 描画用マテリアル情報
	 */
	private GLMaterial makeMats(GL11 gl, material mqomat, int i_mqomat,
			objects mqoObjs, KGLPoint[] vn) {
		GLMaterial ret = new GLMaterial();
		ArrayList<KGLPoint> apv = new ArrayList<KGLPoint>();
		ArrayList<KGLPoint> apn = new ArrayList<KGLPoint>();
		ArrayList<KGLPoint> apuv = new ArrayList<KGLPoint>();
		ArrayList<KGLPoint> apc = new ArrayList<KGLPoint>();
		KGLPoint wpoint = null;
		boolean uvValid = false;
		boolean colValid = false;
		KGLPoint fn;
		float s;
		for (int f = 0; f < mqoObjs.face.length; f++) {
			if (mqoObjs.face[f].M == null) {
				continue;
			}
			if (mqoObjs.face[f].M != i_mqomat)
				continue;

			fn = calcNormal(mqoObjs.vertex, mqoObjs.face[f].V[0],
					mqoObjs.face[f].V[1], mqoObjs.face[f].V[2]);
			for (int v = 0; v < 3; v++) {
				apv.add(mqoObjs.vertex[mqoObjs.face[f].V[v]]);
				// apv.add(new KGLPoint(mqoObjs.vertex[mqoObjs.face[f].V[v]])) ;
				s = (float) Math.acos(fn.X() * vn[mqoObjs.face[f].V[v]].X()
						+ fn.Y() * vn[mqoObjs.face[f].V[v]].Y() + fn.Z()
						* vn[mqoObjs.face[f].V[v]].Z());
				if (mqoObjs.data.facet < s) {
					apn.add(fn);
				} else {
					apn.add(vn[mqoObjs.face[f].V[v]]);
				}
				wpoint = new KGLPoint(2);
				if (mqoObjs.face[f].UV == null) {
					wpoint.set_UV(0, 0);
				} else {
					wpoint.set_UV(mqoObjs.face[f].UV[v * 2 + 0],
							mqoObjs.face[f].UV[v * 2 + 1]);
					uvValid = true;
				}
				apuv.add(wpoint);
				wpoint = new KGLPoint(4);
				if (mqoObjs.face[f].COL == null) {
					if (mqomat.data.col == null) {
						wpoint.set_COLOR(1.0f, 1.0f, 1.0f, 1.0f);
					} else {
						wpoint.set_COLOR(mqomat.data.col[0],
								mqomat.data.col[1], mqomat.data.col[2],
								mqomat.data.col[3]);
					}
				} else {
					wpoint.set_COLOR(mqoObjs.face[f].COL[v * 4 + 0],
							mqoObjs.face[f].COL[v * 4 + 1],
							mqoObjs.face[f].COL[v * 4 + 2],
							mqoObjs.face[f].COL[v * 4 + 3]);
					colValid = true;
				}
				apc.add(wpoint);
			}
		}
		ret.texID = texPool.getGLTexture(gl, mqomat.data.tex,
				mqomat.data.aplane, false);
		// @@@ reload 用
		if (ret.texID != 0) {
			ret.texName = mqomat.data.tex;
			ret.alphaTexName = mqomat.data.aplane;
		} else {
			ret.texName = null;
			ret.alphaTexName = null;
		}

		if (apv.size() == 0)
			return null;
		uvValid &= (ret.texID != 0);
		ret.name = mqomat.name;
		// uvValid = false ;
		KGLPoint[] wfv = null;
		KGLPoint[] wfn = null;
		KGLPoint[] wft = null;
		KGLPoint[] wfc = null;
		wfv = apv.toArray(new KGLPoint[0]);
		wfn = apn.toArray(new KGLPoint[0]);
		wft = apuv.toArray(new KGLPoint[0]);
		wfc = apc.toArray(new KGLPoint[0]);
		ret.vertex_num = wfv.length;

		// @@@ interleaveFormat は無いので分ける
		ret.uvValid = uvValid;
		ret.colValid = colValid;

		ret.vertexBuffer = ByteBuffer.allocateDirect(ret.vertex_num * 3 * 4);
		ret.vertexBuffer.order(ByteOrder.nativeOrder());
		ret.vertexBuffer.position(0);

		ret.normalBuffer = ByteBuffer.allocateDirect(ret.vertex_num * 3 * 4);
		ret.normalBuffer.order(ByteOrder.nativeOrder());
		ret.normalBuffer.position(0);

		if (uvValid) {
			ret.uvBuffer = ByteBuffer.allocateDirect(ret.vertex_num * 2 * 4);
			ret.uvBuffer.order(ByteOrder.nativeOrder());
			ret.uvBuffer.position(0);
		}
		if (colValid) {
			ret.colBuffer = ByteBuffer.allocateDirect(ret.vertex_num * 4 * 4);
			ret.colBuffer.order(ByteOrder.nativeOrder());
			ret.colBuffer.position(0);
		}

		// Log.i("KGLMetaseq", "vertex_num: "+ ret.vertex_num);

		for (int v = 0; v < ret.vertex_num; v++) {
			ret.vertexBuffer.putFloat(wfv[v].X());
			ret.vertexBuffer.putFloat(wfv[v].Y());
			ret.vertexBuffer.putFloat(wfv[v].Z());

			ret.normalBuffer.putFloat(wfn[v].X());
			ret.normalBuffer.putFloat(wfn[v].Y());
			ret.normalBuffer.putFloat(wfn[v].Z());

			if (uvValid) {
				ret.uvBuffer.putFloat(wft[v].U());
				ret.uvBuffer.putFloat(wft[v].V());
			}
			if (colValid) {
				ret.colBuffer.putFloat(wfc[v].R());
				ret.colBuffer.putFloat(wfc[v].G());
				ret.colBuffer.putFloat(wfc[v].B());
				ret.colBuffer.putFloat(wfc[v].A());
			}
		}
		if (mqomat.data.col != null) {
			ret.color = new float[mqomat.data.col.length];
			for (int c = 0; c < mqomat.data.col.length; c++) {
				ret.color[c] = mqomat.data.col[c];
			}
			if (mqomat.data.dif != null) {
				ret.dif = new float[mqomat.data.col.length];
				for (int c = 0; c < mqomat.data.col.length; c++) {
					ret.dif[c] = mqomat.data.dif * mqomat.data.col[c];
				}
				// KEICHECK difでアルファ値を１未満にすると透明度が変化する？
				ret.dif[3] = mqomat.data.col[3];
			}
			if (mqomat.data.amb != null) {
				ret.amb = new float[mqomat.data.col.length];
				for (int c = 0; c < mqomat.data.col.length; c++) {
					ret.amb[c] = mqomat.data.amb * mqomat.data.col[c];
				}
			}
			if (mqomat.data.emi != null) {
				ret.emi = new float[mqomat.data.col.length];
				for (int c = 0; c < mqomat.data.col.length; c++) {
					ret.emi[c] = mqomat.data.emi * mqomat.data.col[c];
				}
			}
			if (mqomat.data.spc != null) {
				ret.spc = new float[mqomat.data.col.length];
				for (int c = 0; c < mqomat.data.col.length; c++) {
					ret.spc[c] = mqomat.data.spc * mqomat.data.col[c];
				}
			}
		}
		if (mqomat.data.pow != null) {
			ret.power = new float[1];
			ret.power[0] = mqomat.data.pow;
		}
		ret.shadeMode_IsSmooth = true;// defaultはtrue
		if (mqoObjs.data.shading == 0)
			ret.shadeMode_IsSmooth = false;

		return ret;
	}

	/**
	 * 描画用オブジェクト情報を作成する
	 * 
	 * @param mqoMats
	 *            MQOファイルから読み込んだマテリアル情報配列
	 * @param mqoObjs
	 *            MQOファイルのオブジェクト情報
	 * @return 描画用オブジェクト情報
	 */
	private GLObject makeObjs(GL11 gl, material mqoMats[], objects mqoObjs) {
		GLObject ret = null;
		ArrayList<GLMaterial> mats = new ArrayList<GLMaterial>();
		GLMaterial mr;
		KGLPoint[] vn = null;
		vn = vNormal(mqoObjs);
		for (int m = 0; m < mqoMats.length; m++) {
			mr = makeMats(gl, mqoMats[m], m, mqoObjs, vn);
			if (mr != null) {
				mats.add(mr);
			}
		}
		if (mats.size() == 0)
			return null;
		ret = new GLObject();
		ret.name = mqoObjs.name;
		ret.mat = mats.toArray(new GLMaterial[0]);
		ret.isVisible = (mqoObjs.data.visible != 0);

		return ret;
	}

	// 後で復帰させる
	// protected KGLMetaseq(GL in_gl, KGLTextures in_texPool, File mqoFile,float
	// scale, boolean isUseVBO)
	// {
	//
	// }
	/**
	 * コンストラクタ ここでファイルからデータを読み込んでいる
	 * 
	 * @param in_gl
	 *            OpenGLコマンド群をカプセル化したクラス
	 * @param in_texPool
	 *            テクスチャ管理クラス
	 * @param i_provider
	 *            ファイルデータプロバイダ
	 * @param mqoFile
	 *            読み込みファイル
	 * @param scale
	 *            モデルの倍率
	 * @param isUseVBO
	 *            頂点配列バッファを使用するかどうか
	 */
	protected KGLMetaseq(GL11 gl, KGLTextures in_texPool, AssetManager am,
			String msqname, float scale) {
		super(in_texPool, am, scale);
		// targetMQO = in_moq;
		material mats[] = null;
		InputStream fis = null;
		// InputStreamReader isr = null ;
		// BufferedReader br = null;
		multiInput br = null;
		String chankName[] = null;
		GLObject glo = null;
		ArrayList<GLObject> globjs = new ArrayList<GLObject>();
		try {
			fis = am.open(msqname);
			// isr = new InputStreamReader(fis) ;
			// br = new BufferedReader(isr);
			br = new multiInput(fis);
			while ((chankName = Chank(br, false)) != null) {
				/*
				 * for( int i = 0 ; i < chankName.length ; i++ ) {
				 * System.out.print(chankName[i]+" ") ; } System.out.println() ;
				 */
				if (chankName[0].trim().toUpperCase().equals("MATERIAL")) {
					try {
						mats = new material[Integer.parseInt(chankName[1])];
						for (int m = 0; m < mats.length; m++) {
							mats[m] = new material();
							mats[m].set(br.readLine().trim());
							// Log.i("KGLMetaseq", "Material(" + m+") :" +
							// mats[m].toString());
						}
					} catch (Exception mat_e) {
						Log.e("KGLMetaseq", "MQOファイル　Materialチャンク読み込み例外発生 "
								+ mat_e.getMessage());
						throw new KGLException(mat_e);
					}
				}
				try {
					if (chankName[0].trim().toUpperCase().equals("OBJECT")) {
						objects object = new objects();
						object.set(chankName[1], br, scale);

						// System.out.println(object.toString()) ;
						if (object.face == null) {
							continue;// 面情報のないオブジェクトは飛ばす
						}
						glo = makeObjs(gl, mats, object);
						if (glo != null) {
							globjs.add(glo);
						}
					}
				} catch (Exception obj_e) {
					Log.e("KGLMetaseq", "MQOファイル　Object[" + chankName[1]
							+ "]チャンク読み込み例外発生 " + obj_e.toString());
					throw new KGLException(obj_e);
				}
			}
			br.close();// 読み込み終了
			br = null;
			glObj = globjs.toArray(new GLObject[0]);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * チャンクサーチ
	 * 
	 * @param br
	 *            読み込みストリーム
	 * @param isInner
	 *            true:チャンク内部のチャンクサーチ false:一番外のチャンクサーチ
	 * @return チャンク文字列
	 * @throws IOException
	 */
	private String[] Chank(multiInput br, boolean isInner) throws IOException {
		String ret[] = null;
		String ws[] = null;
		String read = null;
		char c;
		while ((read = br.readLine()) != null) {
			if (read.length() <= 0)
				continue;
			c = read.charAt(read.length() - 1);
			if (c == '{') {
				ws = read.split(" ");
				if (ws.length == 2) {
					ret = new String[2];
					ret[0] = ws[0].trim();
					ret[1] = "{";
					break;
				}
				ret = new String[3];
				ret[0] = ws[0].trim();
				ret[1] = read.substring(ws[0].length(), read.length() - 2)
						.trim();
				ret[2] = "{";
				break;
			}
			if (isInner) {
				ws = read.split(" ", 2);
				ret = new String[ws.length];
				for (int i = 0; i < ws.length; i++)
					ret[i] = ws[i].trim();
				break;
			}
		}

		return ret;
	}
}
