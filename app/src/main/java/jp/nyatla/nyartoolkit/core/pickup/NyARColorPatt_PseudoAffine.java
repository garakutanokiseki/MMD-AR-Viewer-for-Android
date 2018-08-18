/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2009 Ryo Iizuka
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
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core.pickup;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.rasterreader.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.*;

/**
 * このクラスは、疑似アフィン変換を使用して画像からパターンを取得します。 取得領域は、領域を定義する４頂点と、除外する枠線の幅（割合）から定義します。
 */
public class NyARColorPatt_PseudoAffine implements INyARColorPatt {
	private int[] _patdata;
	private NyARRgbPixelReader_INT1D_X8R8G8B8_32 _pixelreader;
	private NyARIntSize _size;
	private static final int BUFFER_FORMAT = NyARBufferType.INT1D_X8R8G8B8_32;

	/**
	 * この関数はラスタの幅を返します。
	 */
	public final int getWidth() {
		return this._size.w;
	}

	/**
	 * この関数はラスタの高さを返します。
	 */
	public final int getHeight() {
		return this._size.h;
	}

	/**
	 * この関数はラスタのサイズの参照値を返します。
	 */
	public final NyARIntSize getSize() {
		return this._size;
	}

	/**
	 * この関数は、ラスタの画素読み取りオブジェクトの参照値を返します。
	 */
	public final INyARRgbPixelReader getRgbPixelReader() {
		return this._pixelreader;
	}

	/**
	 * この関数は、ラスタ画像のバッファを返します。 バッファ形式は、{@link NyARBufferType#INT1D_X8R8G8B8_32}
	 * (int[])です。
	 */
	public Object getBuffer() {
		return this._patdata;
	}

	/**
	 * この関数は、インスタンスがバッファを所有しているかを返します。基本的にtrueです。
	 */
	public boolean hasBuffer() {
		return this._patdata != null;
	}

	/**
	 * この関数は使用不可能です。
	 */
	public void wrapBuffer(Object i_ref_buf) throws NyARException {
		NyARException.notImplement();
	}

	/**
	 * この関数は、バッファタイプの定数を返します。
	 */
	public final int getBufferType() {
		return BUFFER_FORMAT;
	}

	/**
	 * この関数は、インスタンスのバッファタイプが引数のものと一致しているか判定します。
	 */
	public final boolean isEqualBufferType(int i_type_value) {
		return BUFFER_FORMAT == i_type_value;
	}

	private NyARDoubleMatrix44 _invmat = new NyARDoubleMatrix44();

	/**
	 * コンストラクタです。
	 * 
	 * @param i_width
	 *            このラスタの幅
	 * @param i_height
	 *            このラスタの高さ
	 */
	public NyARColorPatt_PseudoAffine(int i_width, int i_height) {
		this._size = new NyARIntSize(i_width, i_height);
		this._patdata = new int[i_height * i_width];
		this._pixelreader = new NyARRgbPixelReader_INT1D_X8R8G8B8_32(
				this._patdata, this._size);
		// 疑似アフィン変換のパラメタマトリクスを計算します。
		// 長方形から計算すると、有効要素がm00,m01,m02,m03,m10,m11,m20,m23,m30になります。
		final NyARDoubleMatrix44 mat = this._invmat;
		mat.m00 = 0;
		mat.m01 = 0;
		mat.m02 = 0;
		mat.m03 = 1.0;
		mat.m10 = 0;
		mat.m11 = i_width - 1;
		mat.m12 = 0;
		mat.m13 = 1.0;
		mat.m20 = (i_width - 1) * (i_height - 1);
		mat.m21 = i_width - 1;
		mat.m22 = i_height - 1;
		mat.m23 = 1.0;
		mat.m30 = 0;
		mat.m31 = 0;
		mat.m32 = i_height - 1;
		mat.m33 = 1.0;
		mat.inverse(mat);
		return;
	}

	/**
	 * 変換行列と頂点座標から、パラメータを計算 o_paramの[0..3]にはXのパラメタ、[4..7]にはYのパラメタを格納する。
	 * 
	 * @param i_vertex
	 * @param pa
	 * @param pb
	 */
	private void calcPara(NyARIntPoint2d[] i_vertex, double[] o_cparam) {
		final NyARDoubleMatrix44 invmat = this._invmat;
		double v1, v2, v4;
		// 変換行列とベクトルの積から、変換パラメタを計算する。
		v1 = i_vertex[0].x;
		v2 = i_vertex[1].x;
		v4 = i_vertex[3].x;

		o_cparam[0] = invmat.m00 * v1 + invmat.m01 * v2 + invmat.m02
				* i_vertex[2].x + invmat.m03 * v4;
		o_cparam[1] = invmat.m10 * v1 + invmat.m11 * v2;// m12,m13は0;
		o_cparam[2] = invmat.m20 * v1 + invmat.m23 * v4;// m21,m22は0;
		o_cparam[3] = v1;// m30は1.0で、m31,m32,m33は0

		v1 = i_vertex[0].y;
		v2 = i_vertex[1].y;
		v4 = i_vertex[3].y;

		o_cparam[4] = invmat.m00 * v1 + invmat.m01 * v2 + invmat.m02
				* i_vertex[2].y + invmat.m03 * v4;
		o_cparam[5] = invmat.m10 * v1 + invmat.m11 * v2;// m12,m13は0;
		o_cparam[6] = invmat.m20 * v1 + invmat.m23 * v4;// m21,m22は0;
		o_cparam[7] = v1;// m30は1.0で、m31,m32,m33は0
		return;
	}

	/**
	 * 疑似アフィン変換の変換パラメタ
	 */
	private double[] _convparam = new double[8];

	/**
	 * この関数は、ラスタのi_vertexsで定義される四角形からパターンを取得して、インスタンスに格納します。
	 */
	public boolean pickFromRaster(INyARRgbRaster image,
			NyARIntPoint2d[] i_vertexs) throws NyARException {
		final double[] conv_param = this._convparam;
		int rx2, ry2;
		rx2 = this._size.w;
		ry2 = this._size.h;
		int[] rgb_tmp = new int[3];

		INyARRgbPixelReader reader = image.getRgbPixelReader();
		// 変形先領域の頂点を取得

		// 変換行列から現在の座標系への変換パラメタを作成
		calcPara(i_vertexs, conv_param);// 変換パラメータを求める
		for (int y = 0; y < ry2; y++) {
			for (int x = 0; x < rx2; x++) {
				final int ttx = (int) ((conv_param[0] * x * y + conv_param[1]
						* x + conv_param[2] * y + conv_param[3]) + 0.5);
				final int tty = (int) ((conv_param[4] * x * y + conv_param[5]
						* x + conv_param[6] * y + conv_param[7]) + 0.5);
				reader.getPixel((int) ttx, (int) tty, rgb_tmp);
				this._patdata[x + y * rx2] = (rgb_tmp[0] << 16)
						| (rgb_tmp[1] << 8) | rgb_tmp[2];
			}
		}
		return true;
	}
}