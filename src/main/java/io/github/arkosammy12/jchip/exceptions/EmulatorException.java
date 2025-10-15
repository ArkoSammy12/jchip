package io.github.arkosammy12.jchip.exceptions;

public class EmulatorException extends RuntimeException {

    public EmulatorException(String message) {
        super(message);
    }

    public EmulatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmulatorException(Throwable cause) {
        super(cause);
    }

}
