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
package jp.nyatla.nyartoolkit.core.utils;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.matrix.*;

/**
 * このクラスは、遠近法を用いてPerspectiveパラメータを計算します。 遠近法を可読性を重視して実装している為、若干低速です。 高速化をした
 * {@link NyARPerspectiveParamGenerator_O1}関数を使用してください。
 */
public class NyARPerspectiveParamGenerator_Reference extends
		NyARPerspectiveParamGenerator {
	/**
	 * コンストラクタです。
	 * 
	 * @param i_local_x
	 *            パラメータ計算の基準点を指定します。
	 * @param i_local_y
	 *            パラメータ計算の基準点を指定します。
	 */
	public NyARPerspectiveParamGenerator_Reference(int i_local_x, int i_local_y) {
		super(i_local_x, i_local_y);
		return;
	}

	/**
	 * この関数は、遠近法のパラメータを計算して、返却します。
	 */
	public final boolean getParam(int i_dest_w, int i_dest_h, double x1,
			double y1, double x2, double y2, double x3, double y3, double x4,
			double y4, double[] o_param) throws NyARException {
		double ltx = this._local_x;
		double lty = this._local_y;
		double rbx = ltx + i_dest_w;
		double rby = lty + i_dest_h;

		NyARDoubleMatrix44 mat_x = new NyARDoubleMatrix44();
		mat_x.m00 = ltx;
		mat_x.m01 = lty;
		mat_x.m02 = -ltx * x1;
		mat_x.m03 = -lty * x1;
		mat_x.m10 = rbx;
		mat_x.m11 = lty;
		mat_x.m12 = -rbx * x2;
		mat_x.m13 = -lty * x2;
		mat_x.m20 = rbx;
		mat_x.m21 = rby;
		mat_x.m22 = -rbx * x3;
		mat_x.m23 = -rby * x3;
		mat_x.m30 = ltx;
		mat_x.m31 = rby;
		mat_x.m32 = -ltx * x4;
		mat_x.m33 = -rby * x4;
		mat_x.inverse(mat_x);
		NyARDoubleMatrix44 mat_y = new NyARDoubleMatrix44();
		mat_y.m00 = ltx;
		mat_y.m01 = lty;
		mat_y.m02 = -ltx * y1;
		mat_y.m03 = -lty * y1;
		mat_y.m10 = rbx;
		mat_y.m11 = lty;
		mat_y.m12 = -rbx * y2;
		mat_y.m13 = -lty * y2;
		mat_y.m20 = rbx;
		mat_y.m21 = rby;
		mat_y.m22 = -rbx * y3;
		mat_y.m23 = -rby * y3;
		mat_y.m30 = ltx;
		mat_y.m31 = rby;
		mat_y.m32 = -ltx * y4;
		mat_y.m33 = -rby * y4;
		mat_y.inverse(mat_y);
		double a = mat_x.m20 * x1 + mat_x.m21 * x2 + mat_x.m22 * x3 + mat_x.m23
				* x4;
		double b = mat_x.m20 + mat_x.m21 + mat_x.m22 + mat_x.m23;
		double d = mat_x.m30 * x1 + mat_x.m31 * x2 + mat_x.m32 * x3 + mat_x.m33
				* x4;
		double f = mat_x.m30 + mat_x.m31 + mat_x.m32 + mat_x.m33;

		double g = mat_y.m20 * y1 + mat_y.m21 * y2 + mat_y.m22 * y3 + mat_y.m23
				* y4;
		double h = mat_y.m20 + mat_y.m21 + mat_y.m22 + mat_y.m23;
		double i = mat_y.m30 * y1 + mat_y.m31 * y2 + mat_y.m32 * y3 + mat_y.m33
				* y4;
		double j = mat_y.m30 + mat_y.m31 + mat_y.m32 + mat_y.m33;

		NyARDoubleMatrix22 tm = new NyARDoubleMatrix22();
		tm.m00 = b;
		tm.m01 = -h;
		tm.m10 = f;
		tm.m11 = -j;
		tm.inverse(tm);

		double A, B, C, D, E, F, G, H;

		C = tm.m00 * (a - g) + tm.m01 * (d - i); // C
		F = tm.m10 * (a - g) + tm.m11 * (d - i); // F
		G = a - C * b;
		H = d - C * f;
		A = (mat_x.m00 * x1 + mat_x.m01 * x2 + mat_x.m02 * x3 + mat_x.m03 * x4)
				- C * (mat_x.m00 + mat_x.m01 + mat_x.m02 + mat_x.m03);
		B = (mat_x.m10 * x1 + mat_x.m11 * x2 + mat_x.m12 * x3 + mat_x.m13 * x4)
				- C * (mat_x.m10 + mat_x.m11 + mat_x.m12 + mat_x.m13);
		D = (mat_y.m00 * y1 + mat_y.m01 * y2 + mat_y.m02 * y3 + mat_y.m03 * y4)
				- F * (mat_y.m00 + mat_y.m01 + mat_y.m02 + mat_y.m03);
		E = (mat_y.m10 * y1 + mat_y.m11 * y2 + mat_y.m12 * y3 + mat_y.m13 * y4)
				- F * (mat_y.m10 + mat_y.m11 + mat_y.m12 + mat_y.m13);

		o_param[0] = A;
		o_param[1] = B;
		o_param[2] = C;
		o_param[3] = D;
		o_param[4] = E;
		o_param[5] = F;
		o_param[6] = G;
		o_param[7] = H;

		return true;

	}
}
