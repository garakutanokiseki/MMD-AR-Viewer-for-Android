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
import jp.nyatla.nyartoolkit.core.utils.*;

/**
 * このクラスは、遠近法を使用して画像からパターンを取得します。 取得領域は、領域を定義する４頂点と、除外する枠線の幅（割合）から定義します。
 */
public class NyARColorPatt_Perspective implements INyARColorPatt {
	/** パターン格納用のバッファ */
	protected int[] _patdata;
	/** 射影変換のLT位置 */
	protected NyARIntPoint2d _pickup_lt = new NyARIntPoint2d();
	/** ソースパターンのサイズ */
	protected NyARIntSize _pickup_wh = new NyARIntSize();
	/** サンプリング解像度 */
	protected int _resolution;
	/** このラスタのサイズ */
	protected NyARIntSize _size;
	/** 射影変換パラメータ計算オブジェクト */
	protected NyARPerspectiveParamGenerator _perspective_gen;
	private NyARRgbPixelReader_INT1D_X8R8G8B8_32 _pixelreader;
	private static final int LOCAL_LT = 1;
	private static final int BUFFER_FORMAT = NyARBufferType.INT1D_X8R8G8B8_32;

	private void initializeInstance(int i_width, int i_height,
			int i_point_per_pix) {
		assert i_width > 2 && i_height > 2;
		this._size = new NyARIntSize(i_width, i_height);
		this._patdata = new int[i_height * i_width];
		this._pixelreader = new NyARRgbPixelReader_INT1D_X8R8G8B8_32(
				this._patdata, this._size);
		return;
	}

	/**
	 * コンストラクタです。 このラスタのサイズと、サンプリング定数を指定して、インスタンスを生成します。
	 * 
	 * @param i_width
	 *            このラスタの幅
	 * @param i_height
	 *            このラスタの高さ
	 * @param i_point_per_pix
	 *            1ピクセルあたりの縦横サンプリング数。2なら2x2=4ポイントをサンプリングする。
	 */
	public NyARColorPatt_Perspective(int i_width, int i_height,
			int i_point_per_pix) {
		initializeInstance(i_width, i_height, i_point_per_pix);
		setEdgeSize(0, 0, i_point_per_pix);
		return;
	}

	/**
	 * コンストラクタです。 このラスタのサイズと、サンプリング定数、エッジ幅を指定して、インスタンスを生成します。
	 * 
	 * @param i_width
	 *            このラスタの幅
	 * @param i_height
	 *            このラスタの高さ
	 * @param i_point_per_pix
	 *            1ピクセルあたりの縦横サンプリング数。2なら2x2=4ポイントをサンプリングする。
	 * @param i_edge_percentage
	 *            エッジの割合。ARToolKitと同じなら25を指定してください。
	 */
	public NyARColorPatt_Perspective(int i_width, int i_height,
			int i_point_per_pix, int i_edge_percentage) {
		// 入力制限
		initializeInstance(i_width, i_height, i_point_per_pix);
		setEdgeSizeByPercent(i_edge_percentage, i_edge_percentage,
				i_point_per_pix);
		return;
	}

	/**
	 * 矩形領域のエッジ（枠）サイズをドット数で指定します。 エッジの計算方法は以下の通りです。
	 * <ol>
	 * <li>マーカ全体を(i_x_edge*2+width)x(i_y_edge*2+height)の解像度でパラメタを計算します。
	 * <li>ピクセルの取得開始位置を(i_x_edge/2,i_y_edge/2)へ移動します。
	 * <li>開始位置から、width x height個のピクセルを取得します。
	 * </ol>
	 * ARToolKit標準マーカの場合は、width/2,height/2を指定してください。
	 * 
	 * @param i_x_edge
	 *            X方向のエッジのサイズ。
	 * @param i_y_edge
	 *            Y方向のエッジのサイズ。
	 * @param i_resolution
	 *            1ピクセルあたりの縦横サンプリング数。2なら2x2=4ポイントをサンプリングする。
	 */
	public void setEdgeSize(int i_x_edge, int i_y_edge, int i_resolution) {
		assert (i_x_edge >= 0);
		assert (i_y_edge >= 0);
		// Perspectiveパラメタ計算器を作成
		this._perspective_gen = new NyARPerspectiveParamGenerator_O1(LOCAL_LT,
				LOCAL_LT);
		// ピックアップ開始位置を計算
		this._pickup_lt.setValue(i_x_edge * i_resolution + LOCAL_LT, i_y_edge
				* i_resolution + LOCAL_LT);
		this._pickup_wh.setValue((i_x_edge * 2 + this._size.w) * i_resolution,
				(i_y_edge * 2 + this._size.h) * i_resolution);
		this._resolution = i_resolution;
		return;
	}

	/**
	 * 矩形領域のエッジ（枠）サイズを、割合で指定します。
	 * 
	 * @param i_x_percent
	 *            左右のエッジの割合です。0から50の間の数で指定します。
	 * @param i_y_percent
	 *            上下のエッジの割合です。0から50の間の数で指定します。
	 * @param i_resolution
	 *            1ピクセルあたりの縦横サンプリング数。2なら2x2=4ポイントをサンプリングする。
	 */
	public void setEdgeSizeByPercent(int i_x_percent, int i_y_percent,
			int i_resolution) {
		assert (i_x_percent >= 0);
		assert (i_y_percent >= 0);
		setEdgeSize(this._size.w * i_x_percent / 50, this._size.h * i_y_percent
				/ 50, i_resolution);
		return;
	}

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

	private final int[] __pickFromRaster_rgb_tmp = new int[3];
	/** 　ワーク変数 */
	protected final double[] __pickFromRaster_cpara = new double[8];

	/**
	 * この関数は、ラスタのi_vertexsで定義される四角形からパターンを取得して、インスタンスに格納します。
	 */
	public boolean pickFromRaster(INyARRgbRaster image,
			NyARIntPoint2d[] i_vertexs) throws NyARException {
		// 遠近法のパラメータを計算
		final double[] cpara = this.__pickFromRaster_cpara;
		if (!this._perspective_gen.getParam(this._pickup_wh, i_vertexs, cpara)) {
			return false;
		}

		final int resolution = this._resolution;
		final int img_x = image.getWidth();
		final int img_y = image.getHeight();
		final int res_pix = resolution * resolution;

		final int[] rgb_tmp = this.__pickFromRaster_rgb_tmp;

		// ピクセルリーダーを取得
		INyARRgbPixelReader reader = image.getRgbPixelReader();
		int p = 0;
		for (int iy = 0; iy < this._size.h * resolution; iy += resolution) {
			// 解像度分の点を取る。
			for (int ix = 0; ix < this._size.w * resolution; ix += resolution) {
				int r, g, b;
				r = g = b = 0;
				for (int i2y = iy; i2y < iy + resolution; i2y++) {
					int cy = this._pickup_lt.y + i2y;
					for (int i2x = ix; i2x < ix + resolution; i2x++) {
						// 1ピクセルを作成
						int cx = this._pickup_lt.x + i2x;
						final double d = cpara[6] * cx + cpara[7] * cy + 1.0;
						int x = (int) ((cpara[0] * cx + cpara[1] * cy + cpara[2]) / d);
						int y = (int) ((cpara[3] * cx + cpara[4] * cy + cpara[5]) / d);
						if (x < 0) {
							x = 0;
						}
						if (x >= img_x) {
							x = img_x - 1;
						}
						if (y < 0) {
							y = 0;
						}
						if (y >= img_y) {
							y = img_y - 1;
						}

						reader.getPixel(x, y, rgb_tmp);
						r += rgb_tmp[0];
						g += rgb_tmp[1];
						b += rgb_tmp[2];
					}
				}
				r /= res_pix;
				g /= res_pix;
				b /= res_pix;
				this._patdata[p] = ((r & 0xff) << 16) | ((g & 0xff) << 8)
						| ((b & 0xff));
				p++;
			}
		}
		// ピクセル問い合わせ
		// ピクセルセット
		return true;
	}

}