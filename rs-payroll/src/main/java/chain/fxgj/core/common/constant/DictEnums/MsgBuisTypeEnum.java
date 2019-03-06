package chain.fxgj.core.common.constant.DictEnums;



/**
 * @author lius
 * create by lius on 2018/9/5 下午13:39
 **/
public enum MsgBuisTypeEnum implements SysDictEnum {
    //业务类型(0 忘记密码 1修改邮箱)
    EMAIL_01("忘记登录密码"),
    EMAIL_02("修改邮箱申请"),
    EMAIL_03("修改邮箱确认"),
    EMAIL_04("邀请成员"),
    EMAIL_05("移除成员"),
    EMAIL_06("入驻申请"),
    EMAIL_07("账户验证"),


    SMS_01("身份验证短信验证码"),
    SMS_02("添加成员"),
    SMS_03("放薪管家"),
    SMS_04("工资条短信推送"),

    WEIXIN_01("工资条下发通知"),
    XMFH("厦门分行短信预警"),

    SMS_05("温州分行短信确认验证码"),;

    private String desc;

    MsgBuisTypeEnum(String desc) {
        this.desc = desc;
    }

    @Override
    public String getDesc() {
        return this.desc;
    }

    @Override
    public Integer getCode() {
        return this.ordinal();
    }


    //sms_code  sms_timeout position user_name(employee_name)  ent_name  group_name  plan_name total_amt url

    //check_code email_timeout app_tel
    //postion_user_name  ent_name  date_time

    //身份验证短信验证码	【放薪管家】您的验证码为{sms_code}，请在{sms_timeout}分钟内输入。感谢您对放薪管家的支持，祝您生活愉快！
    //添加成员	【放薪管家】您好，亲爱的用户：{position}-{user_name} 邀请您成为{ent_name}的管理成员，欢迎使用放薪管家。
    //放薪管家	【放薪管家】{sms_code}
    //工资条短信推送	{group_name}公司，{employee_name}您好！您的{plan_name}工资条已经生成，金额为{total_amt}，查看明细请点击此处：{url}【华夏银行放薪管家】

    //忘记登录密码	亲爱的用户您好：<br> 您正在找回登录密码，验证码为：{check_code}。<br> 该验证码{email_timeout}分钟内有效，请即时输入。如非本人操作，请联系客服：{app_tel}。
    //修改邮箱申请	亲爱的用户您好：<br> 您更换邮箱身份验证的验证码为：{check_code}。<br> 该验证码{email_timeout}分钟内有效，请即时输入。如非本人操作，请联系客服：{app_tel}。
    //修改邮箱确认	亲爱的用户您好：<br> 您绑定新邮箱的验证码为：{check_code}。<br> 该验证码{email_timeout}分钟内有效，请即时输入。如非本人操作，请联系客服：{app_tel}。
    //邀请成员	<div> 亲爱的用户您好： <br><b>{postion_user_name}</b>邀请您成为<b>{ent_name}</b>的放薪管家管理成员 <br><div align='center' ><a href='{href}{code}' style='text-decoration:none;'><input type=button style='top: 10px; transform-origin: 68px 10px 0px; width:140px; height:40px;' value="点击加入"></a></div> <br>如果按钮无法点击，请点击下面的链接加入： <br><a href='{href}{code}'><font size='3'><b>{href}{code}</b></font></a> <br><div align='center'><font color='#333333'> 本次邀请有效时间为48小时内</font></div> <br><div align='center'><font color='#999999'> 如果您觉得这封邮件跟您没有任何关系，请忽略此邮件，并且不要泄露给任何人</font></div> </div>
    //移除成员	<div> 亲爱的用户您好： <br><b>{postion_user_name}</b>于<b>{date_time}</b>把你移除了<b>{ent_name}</b>的放薪管家管理成员。 <div/><br><div align='center'><font color='#999999'> 如果您觉得这封邮件跟您没有任何关系，请忽略此邮件，并且不要泄露给任何人</font></div> </div>
    //入驻申请	<div> 亲爱的管理员您好： <br>有新的企业申请入驻,请尽快处理。 <div/><br><div align='center'><font color='#999999'> 如果您觉得这封邮件跟您没有任何关系，请忽略此邮件，并且不要泄露给任何人</font></div> </div>
    //账户验证	<div> 亲爱的管理员您好： <br>有新的账户绑定需要验证,请尽快处理。 <div/><br><div align='center'><font color='#999999'> 如果您觉得这封邮件跟您没有任何关系，请忽略此邮件，并且不要泄露给任何人</font></div> </div>

}
