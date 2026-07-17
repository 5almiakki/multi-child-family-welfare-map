package kr.dagagomap.exception;

public class PublicDataApiException extends BaseCustomException {

    public PublicDataApiException() {
    }

    public PublicDataApiException(String message) {
        super(message);
    }

    public PublicDataApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public PublicDataApiException(Throwable cause) {
        super(cause);
    }

    public PublicDataApiException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public String getCode() {
        return "PUBLIC_DATA_API_ERROR";
    }

}
