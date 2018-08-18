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
package jp.nyatla.nyartoolkit.core.match;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARCode;

/**
 * このクラスは、カラーで２パターンの一致度を計算します。
 * 評価アルゴリズムは、ARToolKitの、AR_TEMPLATE_MATCHING_COLORかつAR_MATCHING_WITH_PCAと同様です。
 * 比較対象のデータには、{@link NyARMatchPattDeviationColorData}クラスの物を使います。 　
 * <p>
 * bug - このクラスは動作確認が不十分です。動作しないかもしれませんので、注意してください。
 * </p>
 */
public class NyARMatchPatt_Color_WITH_PCA extends
		NyARMatchPatt_Color_WITHOUT_PCA {
	private final int EVEC_MAX = 10;// #define EVEC_MAX 10

	private int evec_dim;// static int evec_dim;
	private double[][] evec;// static double
							// evec[EVEC_MAX][AR_PATT_SIZE_Y*AR_PATT_SIZE_X*3];
	private double[][] epat = new double[4][EVEC_MAX];// static double
														// epat[AR_PATT_NUM_MAX][4][EVEC_MAX];

	/**
	 * コンストラクタ。 基準パターンの解像度を指定して、インスタンスを生成します。
	 * このコンストラクタで生成したインスタンスの基準パターンは、NULLになっています。 後で基準パターンを{@link #setARCode}
	 * 関数で設定してください。
	 * 
	 * @param i_width
	 *            基準パターンのサイズ
	 * @param i_height
	 *            基準パターンのサイズ
	 */
	public NyARMatchPatt_Color_WITH_PCA(int i_width, int i_height) {
		super(i_width, i_height);
		return;
	}

	/**
	 * コンストラクタ。 基準パターンを元に、評価インスタンスを生成します。
	 * 
	 * @param i_code_ref
	 *            セットする基準パターン
	 */
	public NyARMatchPatt_Color_WITH_PCA(NyARCode i_code_ref) {
		super(i_code_ref);
		return;
	}

	/**
	 * この関数は、現在の基準パターンと検査パターンを比較して、類似度を計算します。
	 * 
	 * @param i_patt
	 *            検査パターンを格納したオブジェクトです。このサイズは、基準パターンと一致している必要があります。
	 * @param o_result
	 *            結果を受け取るオブジェクトです。
	 * @return 検査に成功するとtrueを返します。
	 * @throws NyARException
	 */
	public boolean evaluate(NyARMatchPattDeviationColorData i_patt,
			NyARMatchPattResult o_result) throws NyARException {
		final int[] linput = i_patt.refData();
		int sum;
		double max = 0.0;
		int res = NyARMatchPattResult.DIRECTION_UNKNOWN;
		/*
		 * NyARException.trap( "NyARMatchPatt_Color_WITH_PCA\n"+
		 * "この箇所の移植は不完全です！"+
		 * "ARToolKitの移植条件を完全に再現できていないため、evec,epatの計算が無視されています。"+
		 * "gen_evec(void)も含めて移植の必要があるはずですが、まだ未解析です。");
		 */double[] invec = new double[EVEC_MAX];
		for (int i = 0; i < this.evec_dim; i++) {
			invec[i] = 0.0;
			for (int j = 0; j < this._rgbpixels; i++) {
				invec[i] += this.evec[i][j] * linput[j];
			}
			invec[i] /= i_patt.getPow();
		}
		double min = 10000.0;
		for (int j = 0; j < 4; j++) {
			double sum2 = 0;
			for (int i = 0; i < this.evec_dim; i++) {
				sum2 += (invec[i] - this.epat[j][i])
						* (invec[i] - this.epat[j][i]);
			}
			if (sum2 < min) {
				min = sum2;
				res = j;
				// res2 = k;//kは常にインスタンスを刺すから、省略可能
			}
		}
		sum = 0;
		final int[] code_data = this._code_patt.getColorData(res).refData();
		for (int i = 0; i < this._rgbpixels; i++) {// for(int
			sum += linput[i] * code_data[i];// sum
											// +=input[i][i2][i3]*pat[res2][res][i][i2][i3];
		}
		max = sum / this._code_patt.getColorData(res).getPow()
				/ i_patt.getPow();
		o_result.direction = res;
		o_result.confidence = max;
		return true;
	}
}
