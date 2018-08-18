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

/**
 * float型の頂点関連情報を保持、計算する為のクラス<br>
 * 
 * @author kei
 * 
 */
class KGLPoint {
	/**
	 * データ保持配列
	 */
	private float[] data = null;

	/**
	 * カラデータ作成
	 * 
	 * @param in_num
	 *            データ数
	 */
	public KGLPoint(int in_num) {
		data = new float[in_num];
	}

	/**
	 * 指定データ配列と同じ内容のデータで作成
	 * 
	 * @param source
	 *            指定データ
	 */
	public KGLPoint(KGLPoint source) {
		data = new float[source.getData().length];
		set(source.getData());
	}

	/**
	 * 指定データ配列と同じ内容のデータで作成
	 * 
	 * @param in_data
	 *            指定データ
	 * @return 作成クラス
	 */
	static public KGLPoint create(KGLPoint in_data) {
		KGLPoint ret;
		ret = new KGLPoint(in_data.data.length);
		ret.set(in_data.getData());
		return ret;
	}

	/**
	 * 指定データ配列と同じ内容のデータで作成
	 * 
	 * @param in_data
	 *            指定データ
	 * @return 作成クラス
	 */
	static public KGLPoint create(float[] in_data) {
		KGLPoint ret;
		ret = new KGLPoint(in_data.length);
		ret.set(in_data);
		return ret;
	}

	/**
	 * 指定ＵＶデータでこのクラスを作成
	 * 
	 * @param U
	 * @param V
	 * @return 作成クラス
	 */
	static public KGLPoint createUV(float U, float V) {
		KGLPoint ret = new KGLPoint(2);
		ret.set_UV(U, V);
		return ret;
	}

	/**
	 * 指定頂点データでこのクラスを作成
	 * 
	 * @param X
	 * @param Y
	 * @param Z
	 * @return 作成クラス
	 */
	static public KGLPoint createXYZ(float X, float Y, float Z) {
		KGLPoint ret = new KGLPoint(3);
		ret.set_XYZ(X, Y, Z);
		return ret;
	}

	/**
	 * 指定カラーデータでこのクラスを作成
	 * 
	 * @param R
	 * @param G
	 * @param B
	 * @return 作成クラス
	 */
	static public KGLPoint createCOLOR(float R, float G, float B) {
		KGLPoint ret = new KGLPoint(3);
		ret.set_COLOR(R, G, B);
		return ret;
	}

	/**
	 * 指定カラーデータでこのクラスを作成
	 * 
	 * @param R
	 * @param G
	 * @param B
	 * @param A
	 * @return 作成クラス
	 */
	static public KGLPoint createCOLOR(float R, float G, float B, float A) {
		KGLPoint ret = new KGLPoint(4);
		ret.set_COLOR(R, G, B, A);
		return ret;
	}

	// 正規化（長さを求めて、ソレで割って０～１の値にする！）
	/**
	 * 正規化をする<br>
	 * 原点からの距離をもとめて、全要素を距離で割って０～１の値にする<br>
	 * 
	 * @return 自分自身
	 */
	public KGLPoint normalize() {
		double add;
		double len;
		add = 0;
		for (int i = 0; i < data.length; i++) {
			add += data[i] * data[i];
		}
		len = Math.sqrt(add);
		if (len == 0)
			return this;
		for (int i = 0; i < data.length; i++) {
			data[i] /= len;
		}
		return this;
	}

	/**
	 * from地点～to地点を原点からの点にする<br>
	 * 0 　　from--->to<br>
	 * 原点<br>
	 * ↓<br>
	 * 0--->returan値<br>
	 * 
	 * @param from
	 * @param to
	 * @return 自分自身
	 */
	static public KGLPoint vector(KGLPoint from, KGLPoint to) {
		KGLPoint ret = null;
		float[] d_from;
		float[] d_to;
		float[] d_ret;
		d_from = from.getData();
		d_to = to.getData();
		if (d_from.length != d_to.length)
			return null;
		d_ret = new float[d_from.length];
		for (int i = 0; i < d_ret.length; i++) {
			d_ret[i] = d_to[i] - d_from[i];
		}
		ret = new KGLPoint(d_ret.length);
		ret.set(d_ret);
		return ret;
	}

	/**
	 * 内部データの取り出し
	 * 
	 * @return 内部データ
	 */
	protected float[] getData() {
		return data;
	}

	/**
	 * データをこのクラスに設定する
	 * 
	 * @param src
	 *            データ
	 * @return 自分自身
	 */
	public KGLPoint set(KGLPoint src) {
		System.arraycopy(src.data, 0, data, 0,
				(data.length < src.data.length) ? data.length : src.data.length);
		return this;
	}

	/**
	 * データをこのクラスに設定する
	 * 
	 * @param src
	 *            データ
	 * @return 自分自身
	 */
	public KGLPoint set(float[] src) {
		System.arraycopy(src, 0, data, 0,
				(data.length < src.length) ? data.length : src.length);
		return this;
	}

	/**
	 * ３次元データをこのクラスに設定する
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return 自分自身
	 */
	public KGLPoint set_XYZ(float x, float y, float z) {
		data[0] = x;
		data[1] = y;
		data[2] = z;
		return this;
	}

	/**
	 * ２次元データをこのクラスに設定する
	 * 
	 * @param u
	 * @param v
	 * @return 自分自身
	 */
	public KGLPoint set_UV(float u, float v) {
		data[0] = u;
		data[1] = v;
		return this;
	}

	/**
	 * カラーデータをこのクラスに設定する
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @return 自分自身
	 */
	public KGLPoint set_COLOR(float r, float g, float b) {
		data[0] = r;
		data[1] = g;
		data[2] = b;
		return this;
	}

	/**
	 * カラーデータをこのクラスに設定する
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @param a
	 * @return 自分自身
	 */
	public KGLPoint set_COLOR(float r, float g, float b, float a) {
		data[0] = r;
		data[1] = g;
		data[2] = b;
		data[3] = a;
		return this;
	}

	/**
	 * このクラスの要素に追加データを足し込む
	 * 
	 * @param in
	 *            追加データ
	 * @return 自分自身
	 */
	public KGLPoint add(KGLPoint in) {
		for (int i = 0; i < in.data.length || i < data.length; i++) {
			data[i] += in.data[i];
		}
		return this;
	}

	/**
	 * このクラスの要素に追加データを足し込む
	 * 
	 * @param in
	 *            追加データ
	 * @return 自分自身
	 */
	public KGLPoint add(float[] in) {
		for (int i = 0; i < in.length || i < data.length; i++) {
			data[i] += in[i];
		}
		return this;
	}

	/**
	 * このクラスの要素を指定倍する
	 * 
	 * @param in
	 *            倍率
	 * @return 自分自身
	 */
	public KGLPoint scale(float in) {
		for (int i = 0; i < data.length; i++) {
			data[i] *= in;
		}
		return this;
	}

	/**
	 * 要素ｘを取り出す
	 * 
	 * @return 指定データ
	 */
	public float X() {
		return data[0];
	}

	/**
	 * 要素Ｙを取り出す
	 * 
	 * @return 指定データ
	 */
	public float Y() {
		return data[1];
	}

	/**
	 * 要素Ｚを取り出す
	 * 
	 * @return 指定データ
	 */
	public float Z() {
		return data[2];
	}

	/**
	 * 要素Ｕを取り出す
	 * 
	 * @return 指定データ
	 */
	public float U() {
		return data[0];
	}

	/**
	 * 要素Ｖを取り出す
	 * 
	 * @return 指定データ
	 */
	public float V() {
		return data[1];
	}

	/**
	 * 要素Ｒを取り出す
	 * 
	 * @return 指定データ
	 */
	public float R() {
		return data[0];
	}

	/**
	 * 要素Ｇを取り出す
	 * 
	 * @return 指定データ
	 */
	public float G() {
		return data[1];
	}

	/**
	 * 要素Ｂを取り出す
	 * 
	 * @return 指定データ
	 */
	public float B() {
		return data[2];
	}

	/**
	 * 要素Ａを取り出す
	 * 
	 * @return 指定データ
	 */
	public float A() {
		return data[3];
	}

	/**
	 * 内部データを文字列にする
	 */
	public String toString() {
		String ret = "[";
		for (int i = 0; i < data.length; i++) {
			ret += Float.toString(data[i]);
			if ((i + 1) < data.length)
				ret += ", ";
		}
		ret += "]";

		return ret;
	}
}
