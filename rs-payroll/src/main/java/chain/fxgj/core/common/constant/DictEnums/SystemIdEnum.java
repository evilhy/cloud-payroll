package chain.fxgj.core.common.constant.DictEnums;


/**
 * @author lius
 * create by lius on 2018/9/5 下午13:39
 **/
public enum SystemIdEnum implements SysDictEnum {
    //(0 放薪管家,1厦门分行预警模板)
    FXGJ("放薪管家"),
    XMFH("厦门分行"),;

    private String desc;

    SystemIdEnum(String desc) {
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
