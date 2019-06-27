package com.developerchen.core.exception;

/**
 * Thrown with tip message.
 *
 * @author syc
 */
public
class TipException extends RuntimeException {

    private static final long serialVersionUID = 5041655746640791738L;

    private String message;

    /**
     * Constructs an <code>TipException</code> with no
     * detail message.
     */
    public TipException() {
        super();
    }

    /**
     * Constructs an <code>TipException</code> with the
     * specified detail message.
     *
     * @param message the detail message.
     */
    public TipException(String message) {
        super(message);
        this.message = message;
    }

    /**
     * Constructs a new exception with the specified detail message and
     * cause.
     *
     * <p>Note that the detail message associated with <code>cause</code> is
     * <i>not</i> automatically incorporated in this exception's detail
     * message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link Throwable#getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link Throwable#getCause()} method).  (A <tt>null</tt> value
     *                is permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     * @since 1.5
     */
    public TipException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    /**
     * Constructs a new exception with the specified cause and a detail
     * message of <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of <tt>cause</tt>).
     * This constructor is useful for exceptions that are little more than
     * wrappers for other throwables (for example, {@link
     * java.security.PrivilegedActionException}).
     *
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link Throwable#getCause()} method).  (A <tt>null</tt> value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     * @since 1.5
     */
    public TipException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
