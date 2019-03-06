package chain.fxgj.server.payroll.constant;

import chain.css.exception.ErrorMsg;

/**
 * @author chain
 * create by chain on 2018/9/6 下午2:46
 **/
public enum ErrorConstant {

    MISS_PARAM("CE01", "缺少参数!"),
    
    EXIST("CE01", "%s已存在"),

    NOT_EXIST("CE01", "%s不存在"),

    NOT_EMPLY("CE02", "%s不能为空"),

    EXIST_PERMISSION("CE03", "%s包含权限,不能删除!"),

    SYS_ERR("CS99", "系统繁忙，请稍后再试！"),

    SYS_ERROR("9999", "%s"),

    LOGIN_OUT("h009", "登录信息失效，请重新登录！"),

    AUTH_WARN("h004", "手机号不在白名单内,请联系管理员!"),

    AUTH_ERR("h005", "您无权限操作,请联系管理员!"),

    CODE_NOTFOUND("h006", "没有可用的验证码"),

    ACCESS_CODE_INVALID("h006", "访问码失效!"),

    CODE_INVALID("h007", "验证码已失效"),

    CODE_TIME_OUT("h008", "验证码已过期"),

    REGISTER_TIME_OUT("h009", "请重新扫码登录!"),

    MOBILE_ERROR("h010", "手机号格式不正确!"),

    CODE_ERR("h004", "验证码错误"),

    USER_DISABLE("h011", "用户被禁用"),

    UN_INVITED("h012", "用户未邀请"),

    PASSWORD_ERROR("h013", "密码错误"),
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
