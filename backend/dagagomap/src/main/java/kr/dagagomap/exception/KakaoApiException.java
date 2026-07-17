package kr.dagagomap.exception;

public class KakaoApiException extends BaseCustomException {

    public KakaoApiException() {
        super();
    }

    public KakaoApiException(String message) {
        super(message);
    }

    public KakaoApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public KakaoApiException(Throwable cause) {
        super(cause);
    }

    public KakaoApiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public String getCode() {
        return "KAKAO_API_ERROR";
    }

}
