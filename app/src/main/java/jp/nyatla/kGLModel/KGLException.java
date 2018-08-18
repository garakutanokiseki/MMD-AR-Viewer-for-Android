/* 
 * PROJECT: NyARMqoView
 * --------------------------------------------------------------------------------
 * Copyright (c)2008 A虎＠nyatla.jp
 * airmail@ebony.plala.or.jp
 * http://nyatla.jp/
 * 
 * KGL用Exceptionクラス
 */
package jp.nyatla.kGLModel;

public class KGLException extends Exception {
	private static final long serialVersionUID = 1L;

	public KGLException() {
		super();
	}

	public KGLException(Exception e) {
		super(e);
	}
}
