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
package jp.nyatla.nyartoolkit.core.types.stack;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * このクラスは、{@link NyARIntPoint2d}型の可変長配列です。
 */
public class NyARIntPointStack extends NyARObjectStack<NyARIntPoint2d> {
	/**
	 * コンストラクタです。 配列の最大長さを指定して、インスタンスを生成します。
	 * 
	 * @param i_length
	 *            配列の最大長さ
	 * @throws NyARException
	 */
	public NyARIntPointStack(int i_length) throws NyARException {
		super.initInstance(i_length, NyARIntPoint2d.class);
		return;
	}

	/**
	 * この関数は、配列要素を作成します。
	 */
	protected NyARIntPoint2d createElement() {
		return new NyARIntPoint2d();
	}
}
