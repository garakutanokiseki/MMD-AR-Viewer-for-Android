/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
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
package jp.nyatla.nyartoolkit.core.transmat.rotmatrix;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARPerspectiveProjectionMatrix;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResult;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix33;

/**
 * このクラスは、回転行列を計算する行列クラスです。 通常は、ユーザが使用することはありません。
 */
public class NyARRotMatrix extends NyARDoubleMatrix33 {
	/**
	 * コンストラクタです。 参照する射影変換オブジェクトを指定して、インスタンスを生成します。
	 * 
	 * @param i_matrix
	 *            参照する射影変換オブジェクト
	 * @throws NyARException
	 */
	public NyARRotMatrix(NyARPerspectiveProjectionMatrix i_matrix)
			throws NyARException {
		this.__initRot_vec1 = new NyARRotVector(i_matrix);
		this.__initRot_vec2 = new NyARRotVector(i_matrix);
		return;
	}

	final private NyARRotVector __initRot_vec1;
	final private NyARRotVector __initRot_vec2;

	/**
	 * この関数は、{@link NyARTransMatResult}の内容を、回転行列にセットします。
	 * 
	 * @param i_prev_result
	 *            セットする姿勢変換行列。
	 */
	public final void initRotByPrevResult(NyARTransMatResult i_prev_result) {

		this.m00 = i_prev_result.m00;
		this.m01 = i_prev_result.m01;
		this.m02 = i_prev_result.m02;

		this.m10 = i_prev_result.m10;
		this.m11 = i_prev_result.m11;
		this.m12 = i_prev_result.m12;

		this.m20 = i_prev_result.m20;
		this.m21 = i_prev_result.m21;
		this.m22 = i_prev_result.m22;
		return;
	}

	/**
	 * この関数は、２次元座標系の矩形を表す直線式と、３次元座標系の矩形のオフセット座標値から、回転行列を計算します。
	 * <p>
	 * メモ - Cで実装するときは、配列のポインタ版関数と2重化すること
	 * </p>
	 * 
	 * @param i_linear
	 *            直線式。4要素である必要があります。
	 * @param i_sqvertex
	 *            矩形の３次元オフセット座標。4要素である必要があります。
	 * @throws NyARException
	 */
	public void initRotBySquare(final NyARLinear[] i_linear,
			final NyARDoublePoint2d[] i_sqvertex) throws NyARException {
		final NyARRotVector vec1 = this.__initRot_vec1;
		final NyARRotVector vec2 = this.__initRot_vec2;

		// 向かい合った辺から、２本のベクトルを計算

		// 軸１
		vec1.exteriorProductFromLinear(i_linear[0], i_linear[2]);
		vec1.checkVectorByVertex(i_sqvertex[0], i_sqvertex[1]);

		// 軸２
		vec2.exteriorProductFromLinear(i_linear[1], i_linear[3]);
		vec2.checkVectorByVertex(i_sqvertex[3], i_sqvertex[0]);

		// 回転の最適化？
		NyARRotVector.checkRotation(vec1, vec2);

		this.m00 = vec1.v1;
		this.m10 = vec1.v2;
		this.m20 = vec1.v3;
		this.m01 = vec2.v1;
		this.m11 = vec2.v2;
		this.m21 = vec2.v3;

		// 最後の軸を計算
		final double w02 = vec1.v2 * vec2.v3 - vec1.v3 * vec2.v2;
		final double w12 = vec1.v3 * vec2.v1 - vec1.v1 * vec2.v3;
		final double w22 = vec1.v1 * vec2.v2 - vec1.v2 * vec2.v1;
		final double w = Math.sqrt(w02 * w02 + w12 * w12 + w22 * w22);
		this.m02 = w02 / w;
		this.m12 = w12 / w;
		this.m22 = w22 / w;
		return;
	}

	/**
	 * この関数は、ZXY系の角度値を、回転行列にセットします。
	 * 
	 * @param i_angle
	 *            ZXY系のラジアン値
	 */
	public final void initRotByAngle(NyARDoublePoint3d i_angle) {
		this.setZXYAngle(i_angle);
	}

	/**
	 * この関数は、入力した３次元頂点を回転して返します。
	 * 
	 * @param i_in_point
	 *            回転する三次元座標
	 * @param i_out_point
	 *            回転した三次元座標
	 */
	public final void getPoint3d(final NyARDoublePoint3d i_in_point,
			final NyARDoublePoint3d i_out_point) {
		final double x = i_in_point.x;
		final double y = i_in_point.y;
		final double z = i_in_point.z;
		i_out_point.x = this.m00 * x + this.m01 * y + this.m02 * z;
		i_out_point.y = this.m10 * x + this.m11 * y + this.m12 * z;
		i_out_point.z = this.m20 * x + this.m21 * y + this.m22 * z;
		return;
	}

	/**
	 * この関数は、入力した複数の３次元頂点を回転して返します。
	 * 
	 * @param i_in_point
	 *            回転する三次元座標の配列
	 * @param i_out_point
	 *            回転した三次元座標の配列
	 * @param i_number_of_vertex
	 *            回転する座標の個数
	 */
	public final void getPoint3dBatch(final NyARDoublePoint3d[] i_in_point,
			NyARDoublePoint3d[] i_out_point, int i_number_of_vertex) {
		for (int i = i_number_of_vertex - 1; i >= 0; i--) {
			final NyARDoublePoint3d out_ptr = i_out_point[i];
			final NyARDoublePoint3d in_ptr = i_in_point[i];
			final double x = in_ptr.x;
			final double y = in_ptr.y;
			final double z = in_ptr.z;
			out_ptr.x = this.m00 * x + this.m01 * y + this.m02 * z;
			out_ptr.y = this.m10 * x + this.m11 * y + this.m12 * z;
			out_ptr.z = this.m20 * x + this.m21 * y + this.m22 * z;
		}
		return;
	}
}
