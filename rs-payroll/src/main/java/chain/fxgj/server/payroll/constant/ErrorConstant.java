package chain.fxgj.server.payroll.constant;

import chain.css.exception.ErrorMsg;

/**
 * @author chain
 * create by chain on 2018/9/6 下午2:46
 **/
public enum ErrorConstant {


    EXCEL_ERR1("9999", "上传文件有误,上传表结构与模板结构不符,表头缺少%s字段信息"),
    SYS_ERROR("9999", "%s"),
    SYSERR("9999", "系统异常,请稍后再试"),
    EXCEL_ERR("9999", "文件内容解析失败"),
    EXCEL_001("9999", "存在重复的列名！"),
    EXCEL_002("9999", "多列数据格式不合规！"),
    EXCEL_003("9999", "表格内容不能存在合并的单元格！"),
    EXCEL_004("9999", "无法识别表头！"),
    EXCEL_005("9999", "表头单元格不能为空"),
    EXCEL_006("9999", "金额项必须为数字,%s"),
    EXCEL_007("9999", "文件密码不正确"),
    EXCEL_008("9999", "该文件格式不支持"),
    EXCEL_009("9999", "文件打开失败"),
    EXCEL_010("9999", "表头解析失败！"),
    EXCEL_011("9999", "表头匹配失败！"),
    EXCEL_012("9999", "当前机构员工信息正在上传，请稍后再试！"),
    EXCEL_013("9999", "上传文件有误，%s不存在！"),

    AUTHERR("h001", "权限查询失败"),
    AUTH_ERR("h005", "暂无此权限操作"),

    EMP_IDMUNBER("idNumber", "%s"),
    EMP_PHONE("phone", "%s"),
    EMP_CARD("cardNo", "%s"),
    EMP_NAME("name", "%s"),
    EMP_POSITION_ERR("position", "员工在职状态异常"),

    IDERR("idNumber", "身份证号码有误"),
    IDEMPTY("idNumber", "身份证号码为空"),
    PHONE_ERR("phone", "手机号码有误"),
    PHONE_EMPTY("phone", "手机号码为空"),
    PHONE_ERR1("phone", "手机号已存在"),
    CARD_ERR("cardNo", "无法识别银行卡号信息"),
    CARD_ERR01("cardNo", "银行卡号不能重复"),
    ACC_UNBIND("h005", "该账户状态非绑定中，无法解绑！"),
    ACC_DElETE("h006", "当天无法删除该账户！"),
    ACC_DEL_ERR("h007", "绑定中的账户无法直接删除，需先解绑！"),
    ACC_VIRTUAL_DIFF("h008", "%s账户金额异常，请核对账务流水"),
    ACC_VIRTUAL_BIND("h009", "该用工单位尚未绑定账户，请前往用工单位绑定代发账户"),
    ACC_VIRTUAL_REPEAT_UNIT("h010", "用工单位全称或简称或账户号已存在"),
    ACC_VIRTUAL_EXIST("h014", "该账户已绑定"),
    ACC_VIRTUAL_PUBLIC("h013", "用工单位账户绑定必须为对公账户"),
    ACC_VIRTUAL_ENOUGH("h011", "用工单位账户还有余额，暂不能做此操作"),
    ACC_VIRTUAL_ING("h014", "用工单位账户还有正在处理中的流水，暂不能做此操作"),
    ACC_VIRTUAL_ENTGROUPUNIT("h012", "当前代发帐户已绑定用工单位，不可删除"),

    ACC_VIRTUAL_LESS("h010", "账户余额不足请及时充值以免影响资金发放"),
    Error0001("0001", "%s信息不存在"),
    Error0002("0002", "参数不完整"),
    Error0003("0003", "代发方案当前状态不能代发支付！"),
    Error0004("0002", "验证码已过期！"),
    Error0005("1005", "该账户当前存在审核中或代发中方案，暂不能解绑！"),
    //    WAGE_101("W101", "实发总额必须大于0！"),
    WAGE_ERR2("W102", "解析数据错误，行号%s"),
    WECHAT_OUT("o001", "请重新点击[我的工资条]查看"),
    SUPER_ERROR("0227", "非超管，无法获取机构审核信息"),

    EMPLOYEE_001("idNumber", "身份证不可修改"),
    IDNUMBER_EXSIT("h008", "身份证信息在该机构中已存在"),
    LOGIN_OUT("h009", "登录信息失效，请重新登录！"),
    NAME_ERR("name", "姓名不能为空"),
    CARD_EMPTY("cardNo", "银行卡不能为空"),
    CARD_ERR1("cardNo", "只支持借记卡"),
    CARD_ERR2("cardNo", "银行卡号已经存在"),
    ENTGROUP_001("g001", "企业不存在"),
    ENTGROUP_002("g002", "机构全称或机构简称已经存在"),
    ENTGROUP_003("g003", "当前机构代发中或已进行过代发无法删除"),
    ENTGROUP_004("g004", "根据身份证,未查询所属企业信息"),
    CUST_NAME_NULL("custName", "客户名称不能为空"),


    RECEPIT_001("r001", "当前工资不支持回执！"),
    WECHAR_001("r001", "请联系贵公司人事或者财务部门，确认放薪管家员工信息中是否已维护了您的信息"),
    WECHAR_002("r002", "身份证已绑定"),
    WECHAR_003("r003", "未完成工资条认证"),
    WECHAR_004("r004", "微信授权失败"),
    WECHAR_005("r005", "原始密码错误"),
    WECHAR_006("r006", "银行卡后六位输入错误"),
    WECHAR_007("r007", "密码输入错误"),
    WECHAR_008("r008", "验证码错误"),
    WECHAR_009("r009", "手机号已存在"),
    WECHAR_010("r010", "银行卡已存在"),
    WECHAR_011("r011", "银行卡在该机构下正在修改审核中"),
    WECHAR_012("r012", "银行卡相同，无需修改"),
    WECHAR_013("r013", "银行卡必须为纯数字"),

    WAGE_001("w001", "当前发放记录已推送，不能删除！"),
    ACCOUNT_EXIST("h009", "账号已存在！"),
    LESS_HANG_BALANCE("h010", "输入金额不得超过该账户挂帐金额"),
    CARD_EXSIT("h007", "银行卡在表中已存在"),
    ACCOUNT_ERR("h008", "账号不符合签约要求"),
    IS_TODAY("h011", "当天内无法解绑该账户！"),
    PARAM_ERR("h012", "参数错误"),
    BATCH_ERR("h013", "批量代发编号填写校验不一致！"),
    CERT_ERR("h014", "证件填写校验不一致！"),
    ACCNAME_ERR("h015", "账户名称填写校验不一致！"),
    TEL_ERR("h016", "预留电话填写校验不一致！"),

    TIGER0001("t001", "动账通知失败"),
    HXBANK0001("X001", "请求华夏银行接口失败！"),

    WAGE_NOEXIST("W099", "方案不存在"),
    WAGE_NOCHECK("W098", "方案状态已被审核，请刷新页面查看最新状态"),
    WAIT_CHECK("W100", "此方案待上级审核人审核"),
    WAGE_102("W102", "方案已提交，不能重复操作！"),

    PUSH_ERR("h017", "系统推送已关闭"),
    NOTICE_ERR("h018", "公告阅读渠道不正确"),
    ENT_EXIST("h019", "企业名称或简称已存在"),
    EMAIL_EXIST("h020", "手机号或邮箱已存在"),
    BANK_ERR1("b001", "当前银行收市(22:00-00:00),无法进行代发"),
    WAGE_103("W103", "%s:%s,为非数值项"),
    WAGE_104("W104", "%s:%s,金额长度超过16位！"),
    WAGE_105("W105", "文件已上传，不能再次上传！"),
    WAGE_106("W106", "文件中员工已上传工资，不能再次上传！"),
    WAGE_107("W107", "选择的代发账户号与工资表中的发薪账户号不一致！"),
    WAGE_108("W108", "验证失败,请重新上传工资表!"),
    WAGE_109("W109", "自定义方案类型个数暂不能超过10个"),
    WAGE_110("W110", "请勿重复点击确认方案"),
    WAGE_111("W111", "重复资金类型，请勿重复添加"),
    WAGE_112("W112", "该机构还未设置审核模式，请通知超管前往虎符，设置机构审核模式"),
    WAGE_113("W113", "有待审核的方案，不可以转让超管"),
    WAGE_114("W114", "该机构有待审核的方案，不能提交动账信息"),
    WAGE_115("W115", "该机构未设置审核人，请通知超管前往放薪虎符App上设置审核人"),
    WAGE_116("W116", "该机构审核人不足2人，请通知超管前往放薪虎符App上设置审核人"),
    WAGE_117("W117", "当前方案状态异常！"),
    WAGE_118("W118", "请等待上一级审核人审核，才能审核该方案！"),
    WAGE_119("W119", "当前方案未通过政府平台校验，请前往校验"),
    WAGE_120("W120", "当前已代发超限，无法进行代发业务"),
    SHOW_110("S110", "待上级审核完成再审核"),
    SHOW_100("S100", "次方案已被拒绝"),
    SHOW_101("S101", "该成员存在待审核的方案无法移除，为了您的资金安全您可以将其禁用，方案72小时后系统会自动拒绝"),
    SHOW_102("S102", "单人审核时，移除成员至少需要保留一个成员"),
    SHOW_103("S103", "多人审核时，移除成员至少需要保留二个成员"),
    SHOW_104("S104", "方案已经推送,请稍候查询结果"),

    FINANCE_001("f001", "您已预约了该产品，不能再次预约！"),
    FINANCE_002("f002", "活动已结束，不能预约！"),
    FINANCE_003("f003", "请勾选团购协议！"),
    FINANCE_004("f004", "参数不完整!"),

    ELIFE_001("e001", "身份证已参与,参与手机号:%s"),
    ELIFE_002("e002", "手机号已参与,参与身份证:%s"),
    TIMEERR("h021", "预约时间不能大于认购时间"),
    CARDEMPTY("cardNo", "银行卡号不能为空"),
    IDNUMBEREMPTY("h023", "证件号不能为空"),
    IDTYPEEMPTY("h024", "证件类型不能为空"),
    NAMEEMPTY("h025", "姓名不能为空"),
    BRANCHNOEMPTY("h026", "分行机构号不能为空"),
    OFFICEREMPTY("h027", "吸存码不能为空"),
    MANAGERINFOERR("h028", "客户经理账号异常"),
    BRANCHINFOERR("h029", "该支行机构号不隶属所在分行"),
    PHONEEXISTERR("h030", "该手机号已存在"),
    INTENTAMTERR("h031", "金额输入不能小于零或者超过一千万请联系客户经理！"),
    INTENTCAPERR("h032", "预约人数已达到上限，无法预约！"),
    NONPUBLICERR("h033", "暂不支持非对公户账户签约！"),
    NONCHKSTATUSERR("h034", "非核验状态操作，请刷新页面！"),
    NONNUMBERERR("h035", "账号输入应为纯数字"),
    NONONLYACCERR("h035", "账号输入应为纯数字"),
    EMPTYERR("h036", "%s为空"),
    EMPREPEAT("h037", "身份证或手机号或银行卡号已存在"),

    WZWAGE_001("0001", "代发企业账户不存在"),
    WZWAGE_002("0002", "代发人数与明细个数不相同"),
    WZWAGE_003("0003", "%s,发放金额为非数值！"),
    WZWAGE_004("0004", "代发总金额与明细累加金额不一致"),
    WZWAGE_005("0005", "代发总金额为非数值"),
    WZWAGE_006("0006", "代发企业未在放薪管家中配置机构信息"),
    WZWAGE_007("0007", "交易批次号已存在"),
    WZWAGE_008("0008", "交易批次号不存在"),
    WZWAGE_009("0009", "代发失败"),
    WZWAGE_010("0010", "未绑定代发账户"),
    WZWAGE_011("0011", "token验证失败"),
    WZWAGE_012("0012", "请求参数不完整"),
    WZWAGE_013("0013", "秘钥校验失败"),
    WZWAGE_014("0014", "银行卡号账号解密失败"),
    WZWAGE_015("0015", "身份证号解密失败"),

    ACTIVITY_001("a001", "授权登录失败，请重新进入!"),
    ACTIVITY_002("a002", "用户未登录，请重新登录!"),
    ACTIVITY_003("a003", "用户不存在!"),
    ACTIVITY_004("a004", "用户为非正常状态!"),
    ACTIVITY_005("a005", "您暂无活动配置权限，请联系超管!"),
    ACTIVITY_006("a006", "活动信息不存在!"),
    ACTIVITY_007("a007", "不能参与此活动!"),
    ACTIVITY_008("a008", "活动未开始!"),
    ACTIVITY_009("a009", "活动已结束!"),
    ACTIVITY_010("a010", "活动正分配奖金!"),
    ACTIVITY_011("a011", "资金清算错误!"),
    ACTIVITY_012("a012", "已参与此项活动!"),
    ACTIVITY_013("a013", "该时间段已有进行的活动！"),
    ACTIVITY_014("a014", "手机号验证失败，请重新获取验证码！"),
    ACTIVITY_015("a015", "不在活动设置机构范围内，不能参与此活动"),
    ACTIVITY_016("a016", "活动内测中，暂未开放！"),
    ACTIVITY_017("a017", "当前银行收市(22:00-00:00),不能配置活动"),
    ACTIVITY_018("a018", "资金清算中!"),
    ACTIVITY_019("a019", "当前活动状态下不允许修改!"),

    MISS_PARAM("1400", "参数不完整！"),
    SYS_ERR("CS99", "系统出现错误，请联系客服！"),

    PASSWORD("0017", "您的密码校验请求过于频繁，请于%s后再试!"),
    PASSWORDLIMITERR("0017", "错误次数过多，剩余%s次机会，请仔细确认！"),
    PASSWORDCHECKERR("0018", "错误次数过多，剩余%s次机会，次数超限后将锁定24小时！"),
    PASSWORDCDAILYLIMITERR("0020", "您今日短信请求已达上限，请于%s后再试!"),
    PASSWORDMASE("0021", "您设置的密码安全等级过低，请重新设置！"),
    PASSWORDCONTAINS("0022", "请勿设置与手机号、证件号相同的密码！"),
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
