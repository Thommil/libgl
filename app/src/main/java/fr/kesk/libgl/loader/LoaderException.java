package fr.kesk.libgl.loader;

/**
 * Generic exception for loaders
 * 
 * @author Thomas MILLET
 *
 */
@SuppressWarnings("serial")
public class LoaderException extends Exception {

	/**
	 * @param detailMessage
	 */
	public LoaderException(String detailMessage) {
		super(detailMessage);
	}

	/**
	 * @param throwable
	 */
	public LoaderException(Throwable throwable) {
		super(throwable);
	}

	/**
	 * @param detailMessage
	 * @param throwable
	 */
	public LoaderException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
