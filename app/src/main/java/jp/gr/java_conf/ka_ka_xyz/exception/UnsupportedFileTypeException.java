package jp.gr.java_conf.ka_ka_xyz.exception;

public class UnsupportedFileTypeException extends MMDARException{
	
	
	/**
	 * Generated by eclipse.
	 */
	private static final long serialVersionUID = 4309119858700458687L;

	public UnsupportedFileTypeException(){
		super();
	}
	
	public UnsupportedFileTypeException(String msg){
		super(msg);
	}
	
	public UnsupportedFileTypeException(Throwable t){
		super(t);
	}

	public UnsupportedFileTypeException(String msg, Throwable t){
		super(msg, t);
	}
}
