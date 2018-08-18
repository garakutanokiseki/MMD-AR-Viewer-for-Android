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
package jp.nyatla.nyartoolkit.core.squaredetect;

import jp.nyatla.nyartoolkit.core.types.*;

/**
 * このクラスは、輪郭線を四角形と仮定して、その頂点位置を計算します。 ARToolKitの四角形検出処理の一部です。
 */
public class NyARCoord2SquareVertexIndexes {
	private static final double VERTEX_FACTOR = 1.0;// 線検出のファクタ
	private final NyARVertexCounter __getSquareVertex_wv1 = new NyARVertexCounter();
	private final NyARVertexCounter __getSquareVertex_wv2 = new NyARVertexCounter();

	/**
	 * コンストラクタです。 インスタンスを生成します。
	 */
	public NyARCoord2SquareVertexIndexes() {
		return;
	}

	/**
	 * この関数は、座標集合から頂点候補になりそうな場所を４箇所探して、そのインデクス番号を返します。
	 * 
	 * @param i_coord
	 *            輪郭点集合を格納したオブジェクト。
	 * @param i_area
	 *            矩形判定のヒント値。矩形の大きさを、そのラベルを構成するピクセルの数で指定します。
	 *            (注)このパラメータは、マーカノデザイン、枠の大きさが影響等、ラベルの大きさに影響を受けます。
	 * @param o_vertex
	 *            4頂点のインデクスを受け取る配列です。4要素以上の配列を指定してください。
	 * @return 頂点が見つかるとtrueを返します。
	 */
	public boolean getVertexIndexes(NyARIntCoordinates i_coord, int i_area,
			int[] o_vertex) {
		final NyARVertexCounter wv1 = this.__getSquareVertex_wv1;
		final NyARVertexCounter wv2 = this.__getSquareVertex_wv2;
		int i_coord_num = i_coord.length;
		int vertex1_index = getFarPoint(i_coord.items, i_coord_num, 0);
		int prev_vertex_index = (vertex1_index + i_coord_num) % i_coord_num;
		int v1 = getFarPoint(i_coord.items, i_coord_num, vertex1_index);
		final double thresh = (i_area / 0.75) * 0.01 * VERTEX_FACTOR;

		o_vertex[0] = vertex1_index;

		if (!wv1.getVertex(i_coord.items, i_coord_num, vertex1_index, v1,
				thresh)) {
			return false;
		}
		if (!wv2.getVertex(i_coord.items, i_coord_num, v1, prev_vertex_index,
				thresh)) {
			return false;
		}

		int v2;
		if (wv1.number_of_vertex == 1 && wv2.number_of_vertex == 1) {
			o_vertex[1] = wv1.vertex[0];
			o_vertex[2] = v1;
			o_vertex[3] = wv2.vertex[0];
		} else if (wv1.number_of_vertex > 1 && wv2.number_of_vertex == 0) {
			// 頂点位置を、起点から対角点の間の1/2にあると予想して、検索する。
			if (v1 >= vertex1_index) {
				v2 = (v1 - vertex1_index) / 2 + vertex1_index;
			} else {
				v2 = ((v1 + i_coord_num - vertex1_index) / 2 + vertex1_index)
						% i_coord_num;
			}
			if (!wv1.getVertex(i_coord.items, i_coord_num, vertex1_index, v2,
					thresh)) {
				return false;
			}
			if (!wv2.getVertex(i_coord.items, i_coord_num, v2, v1, thresh)) {
				return false;
			}
			if (wv1.number_of_vertex == 1 && wv2.number_of_vertex == 1) {
				o_vertex[1] = wv1.vertex[0];
				o_vertex[2] = wv2.vertex[0];
				o_vertex[3] = v1;
			} else {
				return false;
			}
		} else if (wv1.number_of_vertex == 0 && wv2.number_of_vertex > 1) {
			// v2 = (v1+ end_of_coord)/2;
			if (v1 <= prev_vertex_index) {
				v2 = (v1 + prev_vertex_index) / 2;
			} else {
				v2 = ((v1 + i_coord_num + prev_vertex_index) / 2) % i_coord_num;

			}
			if (!wv1.getVertex(i_coord.items, i_coord_num, v1, v2, thresh)) {
				return false;
			}
			if (!wv2.getVertex(i_coord.items, i_coord_num, v2,
					prev_vertex_index, thresh)) {
				return false;
			}
			if (wv1.number_of_vertex == 1 && wv2.number_of_vertex == 1) {
				o_vertex[1] = v1;
				o_vertex[2] = wv1.vertex[0];
				o_vertex[3] = wv2.vertex[0];
			} else {

				return false;
			}
		} else {
			return false;
		}
		return true;
	}

	/**
	 * i_pointの輪郭座標から、最も遠方にある輪郭座標のインデクスを探します。
	 * 
	 * @param i_xcoord
	 * @param i_ycoord
	 * @param i_coord_num
	 * @return
	 */
	private static int getFarPoint(NyARIntPoint2d[] i_coord, int i_coord_num,
			int i_point) {
		//
		final int sx = i_coord[i_point].x;
		final int sy = i_coord[i_point].y;
		int d = 0;
		int w, x, y;
		int ret = 0;
		for (int i = i_point + 1; i < i_coord_num; i++) {
			x = i_coord[i].x - sx;
			y = i_coord[i].y - sy;
			w = x * x + y * y;
			if (w > d) {
				d = w;
				ret = i;
			}
		}
		for (int i = 0; i < i_point; i++) {
			x = i_coord[i].x - sx;
			y = i_coord[i].y - sy;
			w = x * x + y * y;
			if (w > d) {
				d = w;
				ret = i;
			}
		}
		return ret;
	}
}

/**
 * get_vertex関数を切り離すためのクラス
 * 
 */
final class NyARVertexCounter {
	public final int[] vertex = new int[10];// 6まで削れる

	public int number_of_vertex;

	private double thresh;

	private NyARIntPoint2d[] _coord;

	public boolean getVertex(NyARIntPoint2d[] i_coord, int i_coord_len, int st,
			int ed, double i_thresh) {
		this.number_of_vertex = 0;
		this.thresh = i_thresh;
		this._coord = i_coord;
		return get_vertex(st, ed, i_coord_len);
	}

	/**
	 * static int get_vertex( int x_coord[], int y_coord[], int st, int
	 * ed,double thresh, int vertex[], int *vnum) 関数の代替関数
	 * 
	 * @param x_coord
	 * @param y_coord
	 * @param st
	 * @param ed
	 * @param thresh
	 * @return
	 */
	private boolean get_vertex(int st, int ed, int i_coord_len) {
		// メモ:座標値は65536を超えなければint32で扱って大丈夫なので変更。
		// dmaxは4乗なのでやるとしてもint64じゃないとマズイ
		int v1 = 0;
		final NyARIntPoint2d[] coord = this._coord;
		final int a = coord[ed].y - coord[st].y;
		final int b = coord[st].x - coord[ed].x;
		final int c = coord[ed].x * coord[st].y - coord[ed].y * coord[st].x;
		double dmax = 0;
		if (st < ed) {
			// stとedが1区間
			for (int i = st + 1; i < ed; i++) {
				final double d = a * coord[i].x + b * coord[i].y + c;
				if (d * d > dmax) {
					dmax = d * d;
					v1 = i;
				}
			}
		} else {
			// stとedが2区間
			for (int i = st + 1; i < i_coord_len; i++) {
				final double d = a * coord[i].x + b * coord[i].y + c;
				if (d * d > dmax) {
					dmax = d * d;
					v1 = i;
				}
			}
			for (int i = 0; i < ed; i++) {
				final double d = a * coord[i].x + b * coord[i].y + c;
				if (d * d > dmax) {
					dmax = d * d;
					v1 = i;
				}
			}
		}

		if (dmax / (double) (a * a + b * b) > thresh) {
			if (!get_vertex(st, v1, i_coord_len)) {
				return false;
			}
			if (number_of_vertex > 5) {
				return false;
			}
			vertex[number_of_vertex] = v1;// vertex[(*vnum)] = v1;
			number_of_vertex++;// (*vnum)++;

			if (!get_vertex(v1, ed, i_coord_len)) {
				return false;
			}
		}
		return true;
	}
}