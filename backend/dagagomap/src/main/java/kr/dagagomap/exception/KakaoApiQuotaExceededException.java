package kr.dagagomap.exception;

public class KakaoApiQuotaExceededException extends BaseCustomException {

	public KakaoApiQuotaExceededException() {
		super();
	}

	public KakaoApiQuotaExceededException(String message) {
		super(message);
	}

	public KakaoApiQuotaExceededException(String message, Throwable cause) {
		super(message, cause);
	}

	public KakaoApiQuotaExceededException(Throwable cause) {
		super(cause);
	}

	public KakaoApiQuotaExceededException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	@Override
	public String getCode() {
		return "KAKAO_QUOTA_EXCEEDED_ERROR";
	}

}
