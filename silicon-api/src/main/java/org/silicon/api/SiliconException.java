package org.silicon.api;

/**
 * Base runtime exception for Silicon API errors.
 * <p>
 * Used for backend-specific failures and API misuse where a runtime exception
 * is appropriate, avoiding checked-exception boilerplate.
 */
public class SiliconException extends RuntimeException {

    public SiliconException() {
    }

    public SiliconException(String message) {
        super(message);
    }

    public SiliconException(String message, Throwable cause) {
        super(message, cause);
    }

    public SiliconException(Throwable cause) {
        super(cause);
    }

    public SiliconException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
