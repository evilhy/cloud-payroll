package chain.fxgj.server.payroll.constant;

import chain.css.exception.ErrorMsg;

/**
 * @author chain
 * create by chain on 2018/9/6 下午2:46
 **/
public enum ErrorConstant {
    MISS_PARAM("1400", "参数不完整！"),
    SYS_ERR("CS99", "系统出现错误，请联系客服！"),

    EXCEL_ERR1("9999", "上传文件有误,上传表结构与模板结构不符,表头缺少%s字段信息"),
    SYS_ERROR("9999", "%s"),
    SYSERR("9999", "系统异常,请稍后再试"),

    AUTH_ERR("h005", "暂无此权限操作"),

    WECHAT_OUT("o001", "请重新点击[我的工资条]查看"),

    WZWAGE_011("0011", "token验证失败"),

    WECHAR_006("r006", "银行卡后六位输入错误"),
    WECHAR_007("r007", "密码输入错误"),

    WECHAR_013("r013", "银行卡必须为纯数字"),


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
