/**
 * 
 */
package nta.storage;

import java.io.IOException;

/**
 * @author hyunsik
 *
 */
public class AlreadyOpenStoreException extends IOException {
	private static final long serialVersionUID = 9013376772212652755L;

	/**
	 * 
	 */
	public AlreadyOpenStoreException() {
	}

	/**
	 * @param message
	 */
	public AlreadyOpenStoreException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public AlreadyOpenStoreException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public AlreadyOpenStoreException(String message, Throwable cause) {
		super(message, cause);
	}

}
