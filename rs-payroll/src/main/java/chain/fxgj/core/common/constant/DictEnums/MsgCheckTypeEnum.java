package chain.fxgj.core.common.constant.DictEnums;


/**
 * @author lius
 * create by lius on 2018/9/5 下午13:39
 **/
public enum MsgCheckTypeEnum implements SysDictEnum {
    //(0  邮箱  1 短信 2 微信 )
    EMAIL("邮箱"),
    SMS("短信"),
    WEIXIN("微信"),;

    private String desc;

    MsgCheckTypeEnum(String desc) {
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
}
