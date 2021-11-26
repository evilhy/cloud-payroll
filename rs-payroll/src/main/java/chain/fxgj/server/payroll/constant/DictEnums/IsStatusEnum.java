package chain.fxgj.server.payroll.constant.DictEnums;

import core.enums.SysDictEnum;

/**
 * @author chain
 * create by chain on 2018/9/3 下午7:53
 **/
public enum IsStatusEnum implements SysDictEnum {
    NO("否"),
    YES("是"),
    ;

    private String desc;

    IsStatusEnum(String desc) {
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
