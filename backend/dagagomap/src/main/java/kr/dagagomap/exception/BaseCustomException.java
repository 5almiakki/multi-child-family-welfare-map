package kr.dagagomap.exception;

public abstract class BaseCustomException extends RuntimeException {

    public BaseCustomException() {
        super();
    }

    public BaseCustomException(String message) {
        super(message);
    }

    public BaseCustomException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseCustomException(Throwable cause) {
        super(cause);
    }

    public BaseCustomException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    abstract public String getCode();

}
