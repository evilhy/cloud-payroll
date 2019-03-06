package chain.fxgj.core.common.constant.DictEnums;


/**
 * @author lius
 * create by lius on 2018/9/5 下午13:39
 **/
public enum ModelStatusEnum implements SysDictEnum {
    //模板状态(0停用、1启用)
    DISABLE("停用"),
    ENSABLE("启用"),;

    private String desc;

    ModelStatusEnum(String desc) {
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
