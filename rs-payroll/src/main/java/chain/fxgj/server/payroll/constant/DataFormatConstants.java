package chain.fxgj.server.payroll.constant;

import java.time.format.DateTimeFormatter;

/**
 * @author chain
 * create by chain on 2018/9/12 下午6:30
 **/
public class DataFormatConstants {
    /**
     * 默认日期时间格式
     */
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyyMMddHHmmss";
    /**
     * 默认日期格式
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyyMMdd";
    /**
     * 默认时间格式
     */
    public static final String DEFAULT_TIME_FORMAT = "HHmmss";


    public final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
    public final static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);

}
