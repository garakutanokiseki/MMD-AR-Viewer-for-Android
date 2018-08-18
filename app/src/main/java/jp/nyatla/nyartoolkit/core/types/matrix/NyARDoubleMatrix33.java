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
package jp.nyatla.nyartoolkit.core.types.matrix;

import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;

/**
 * このクラスは、3x3行列を格納します。
 */
public class NyARDoubleMatrix33 implements INyARDoubleMatrix {
	/** 行列の要素値です。 */
	public double m00;
	/** 行列の要素値です。 */
	public double m01;
	/** 行列の要素値です。 */
	public double m02;
	/** 行列の要素値です。 */
	public double m10;
	/** 行列の要素値です。 */
	public double m11;
	/** 行列の要素値です。 */
	public double m12;
	/** 行列の要素値です。 */
	public double m20;
	/** 行列の要素値です。 */
	public double m21;
	/** 行列の要素値です。 */
	public double m22;

	/**
	 * この関数は、オブジェクトの配列を生成して返します。
	 * 
	 * @param i_number
	 *            配列の長さ
	 * @return 新しいオブジェクト配列
	 */
	public static NyARDoubleMatrix33[] createArray(int i_number) {
		NyARDoubleMatrix33[] ret = new NyARDoubleMatrix33[i_number];
		for (int i = 0; i < i_number; i++) {
			ret[i] = new NyARDoubleMatrix33();
		}
		return ret;
	}

	/**
	 * この関数は、要素数9の配列を、行列にセットします。
	 */
	public void setValue(double[] i_value) {
		this.m00 = i_value[0];
		this.m01 = i_value[1];
		this.m02 = i_value[2];
		this.m10 = i_value[3];
		this.m11 = i_value[4];
		this.m12 = i_value[5];
		this.m20 = i_value[6];
		this.m21 = i_value[7];
		this.m22 = i_value[8];
		return;
	}

	/**
	 * この関数は、オブジェクトの内容をインスタンスにコピーします。
	 * 
	 * @param i_value
	 *            コピー元のオブジェクト
	 */
	public void setValue(NyARDoubleMatrix33 i_value) {
		this.m00 = i_value.m00;
		this.m01 = i_value.m01;
		this.m02 = i_value.m02;
		this.m10 = i_value.m10;
		this.m11 = i_value.m11;
		this.m12 = i_value.m12;
		this.m20 = i_value.m20;
		this.m21 = i_value.m21;
		this.m22 = i_value.m22;
		return;
	}

	/**
	 * この関数は、要素数9の配列に、行列の内容をコピーします。
	 */
	public void getValue(double[] o_value) {
		o_value[0] = this.m00;
		o_value[1] = this.m01;
		o_value[2] = this.m02;
		o_value[3] = this.m10;
		o_value[4] = this.m11;
		o_value[5] = this.m12;
		o_value[6] = this.m20;
		o_value[7] = this.m21;
		o_value[8] = this.m22;
		return;
	}

	/**
	 * この関数は、逆行列を計算して、インスタンスにセットします。
	 * 
	 * @param i_src
	 *            逆行列を計算するオブジェクト。thisを指定できます。
	 * @return 逆行列を得られると、trueを返します。
	 */
	public boolean inverse(NyARDoubleMatrix33 i_src) {
		final double a11, a12, a13, a21, a22, a23, a31, a32, a33;
		final double b11, b12, b13, b21, b22, b23, b31, b32, b33;
		a11 = i_src.m00;
		a12 = i_src.m01;
		a13 = i_src.m02;
		a21 = i_src.m10;
		a22 = i_src.m11;
		a23 = i_src.m12;
		a31 = i_src.m20;
		a32 = i_src.m21;
		a33 = i_src.m22;

		b11 = a22 * a33 - a23 * a32;
		b12 = a32 * a13 - a33 * a12;
		b13 = a12 * a23 - a13 * a22;

		b21 = a23 * a31 - a21 * a33;
		b22 = a33 * a11 - a31 * a13;
		b23 = a13 * a21 - a11 * a23;

		b31 = a21 * a32 - a22 * a31;
		b32 = a31 * a12 - a32 * a11;
		b33 = a11 * a22 - a12 * a21;

		double det_1 = a11 * b11 + a21 * b12 + a31 * b13;
		if (det_1 == 0) {
			return false;
		}
		det_1 = 1 / det_1;

		this.m00 = b11 * det_1;
		this.m01 = b12 * det_1;
		this.m02 = b13 * det_1;

		this.m10 = b21 * det_1;
		this.m11 = b22 * det_1;
		this.m12 = b23 * det_1;

		this.m20 = b31 * det_1;
		this.m21 = b32 * det_1;
		this.m22 = b33 * det_1;

		return true;
	}

	/**
	 * この関数は、行列を回転行列として、ZXY系の角度値を計算します。
	 * 
	 * @param o_out
	 *            　角度値を受け取るオブジェクトです。 角度値の範囲は、0-PIです。
	 */
	public final void getZXYAngle(NyARDoublePoint3d o_out) {
		double sina = this.m21;
		if (sina >= 1.0) {
			o_out.x = Math.PI / 2;
			o_out.y = 0;
			o_out.z = Math.atan2(-this.m10, this.m00);
		} else if (sina <= -1.0) {
			o_out.x = -Math.PI / 2;
			o_out.y = 0;
			o_out.z = Math.atan2(-this.m10, this.m00);
		} else {
			o_out.x = Math.asin(sina);
			o_out.z = Math.atan2(-this.m01, this.m11);
			o_out.y = Math.atan2(-this.m20, this.m22);
		}
	}

	/**
	 * この関数は、行列を回転行列として、ZXY系の角度値をセットします。
	 * 
	 * @param i_angle
	 *            　セットする角度値です。
	 */
	public final void setZXYAngle(NyARDoublePoint3d i_angle) {
		setZXYAngle(i_angle.x, i_angle.y, i_angle.z);
		return;
	}

	/**
	 * この関数は、行列を回転行列として、ZXY系の角度値をセットします。
	 * 
	 * @param i_x
	 *            　X軸の角度値
	 * @param i_y
	 *            　X軸の角度値
	 * @param i_z
	 *            　X軸の角度値
	 */
	public final void setZXYAngle(final double i_x, final double i_y,
			final double i_z) {
		final double sina = Math.sin(i_x);
		final double cosa = Math.cos(i_x);
		final double sinb = Math.sin(i_y);
		final double cosb = Math.cos(i_y);
		final double sinc = Math.sin(i_z);
		final double cosc = Math.cos(i_z);
		this.m00 = cosc * cosb - sinc * sina * sinb;
		this.m01 = -sinc * cosa;
		this.m02 = cosc * sinb + sinc * sina * cosb;
		this.m10 = sinc * cosb + cosc * sina * sinb;
		this.m11 = cosc * cosa;
		this.m12 = sinc * sinb - cosc * sina * cosb;
		this.m20 = -cosa * sinb;
		this.m21 = sina;
		this.m22 = cosb * cosa;
		return;
	}

	/**
	 * この関数は、3次元座標を座標変換します。
	 * 
	 * @param i_position
	 *            変換する三次元座標
	 * @param o_out
	 *            変換後の座標を受け取るオブジェクト
	 */
	public final void transformVertex(NyARDoublePoint3d i_position,
			NyARDoublePoint3d o_out) {
		transformVertex(i_position.x, i_position.y, i_position.z, o_out);
		return;
	}

	/**
	 * この関数は、3次元座標を座標変換します。
	 * 
	 * @param i_x
	 *            変換する三次元座標(X)
	 * @param i_y
	 *            変換する三次元座標(Y)
	 * @param i_z
	 *            変換する三次元座標(Z)
	 * @param o_out
	 *            変換後の座標を受け取るオブジェクト
	 */
	public final void transformVertex(double i_x, double i_y, double i_z,
			NyARDoublePoint3d o_out) {
		o_out.x = this.m00 * i_x + this.m01 * i_y + this.m02 * i_z;
		o_out.y = this.m10 * i_x + this.m11 * i_y + this.m12 * i_z;
		o_out.z = this.m20 * i_x + this.m21 * i_y + this.m22 * i_z;
		return;
	}
}
