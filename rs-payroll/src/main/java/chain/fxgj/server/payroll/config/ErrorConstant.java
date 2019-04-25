package chain.fxgj.server.payroll.config;

import chain.css.exception.ErrorMsg;

public enum  ErrorConstant {

    MERCHANT_01("01","非法访问!"),
    MERCHANT_02("02","验证签名失败!"),
    MERCHANT_03("03","解密失败!"),
    MERCHANT_04("04","加密失败!"),
    MERCHANT_05("05","SHA1安全加密失败!"),
    MERCHANT_06("06","请求超时!"),
    MERCHANT_07("07","信息未认证!"),

    ;




    private String errorCode;
    private String errorMsg;

    ErrorConstant(String errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public ErrorMsg format(Object... param) {
        if (param != null) {
            return new ErrorMsg(errorCode, String.format(errorMsg, param));
        }
        return getErrorMsg();
    }

    public ErrorMsg getErrorMsg() {
        return new ErrorMsg(errorCode, errorMsg);
    }
}
