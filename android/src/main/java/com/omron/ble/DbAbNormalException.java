package com.omron.ble;

/**
 * Created by 123 on 2016/6/24.
 */
public class DbAbNormalException extends RuntimeException {

    private static final long serialVersionUID = 5172710183389028792L;

    /**
     * Constructs a new {@code NullPointerException} that includes the current
     * stack trace.
     */
    public DbAbNormalException() {
    }

    /**
     * Constructs a new {@code NullPointerException} with the current stack
     * trace and the specified detail message.
     *
     * @param detailMessage
     *            the detail message for this exception.
     */
    public DbAbNormalException(String detailMessage) {
        super(detailMessage);
    }
}
