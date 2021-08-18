package chain.fxgj.server.payroll.config;

import chain.css.exception.ErrorMsg;

public enum ErrorConstant {

    MERCHANT_01("M001", "非法访问!"),
    MERCHANT_02("M002", "验证签名失败!"),
    MERCHANT_03("M003", "解密失败!"),
    MERCHANT_04("M004", "加密失败!"),
    MERCHANT_05("M005", "SHA1安全加密失败!"),
    MERCHANT_06("M006", "请求超时!"),
    MERCHANT_07("M007", "信息未认证!"),

    Error0004("0002", "验证码已过期！"),
    WECHAR_008("r008", "验证码错误"),


    WECHAR_003("r003", "未完成工资条认证"),
    FINANCE_003("f003", "请勾选团购协议！"),
    FINANCE_004("f004", "参数不完整!"),
    MISS_PARAM("1400", "参数不完整！"),
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
