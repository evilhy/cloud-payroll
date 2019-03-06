package chain.fxgj.core.common.constant;

import java.time.format.DateTimeFormatter;

/**
 * The type Base constant.
 *
 * @author chain
 * create by xiongjj on 2018 /5/3 下午7:23
 */
public class FxgjDBConstant {
    /**
     * The constant ENTITY_PAGES.
     */
    public final static String ENTITY_PAGES = "chain.fxgj.core.jpa.model";
    /**
     * The constant DAO_PAGES.
     */
    public final static String DAO_PAGES = "chain.fxgj.core.jpa.dao";
    /**
     * The constant PREFIX.
     */
    public final static String PREFIX = "fxgj";
    /**
     * The constant DATA_SOURCE_PROPERTIES.
     */
    public final static String DATA_SOURCE_PROPERTIES = PREFIX + ".datasource";

    /**
     * The constant PERSISTENCE_UNIT.
     */
    public final static String PERSISTENCE_UNIT = PREFIX + "PersistenceUnit";

    /**
     * The constant DATA_SOURCE_NAME.
     */
    public final static String DATA_SOURCE_NAME = PREFIX + "DataSource";
    /**
     * The constant ENTITY_MANAGER_FACTORY_REF.
     */
    public final static String ENTITY_MANAGER_FACTORY_REF = PREFIX + "EntityManagerFactory";
    /**
     * The constant ENTITY_MANAGER
     */
    public final static String ENTITY_MANAGER_REF = PREFIX + "EntityManager";
    /**
     * The constant TRANSACTION_MANAGER_REF.
     */
    public final static String TRANSACTION_MANAGER_REF = PREFIX + "TransactionManager";
    /**
     * The constant JDBC_TEMPLATE_REF.
     */
    public final static String JDBC_TEMPLATE_REF = PREFIX + "JdbcTemplate";

    public final static String DATE_TIME_FORMAT = "yyyyMMddHHmmss";
    public final static String DEFAULT_DATE_FORMAT = "yyyyMMdd";

    public final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    public final static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);

    public final static String LOG_TOKEN = "log_token";  //日志
    public final static String LOGTOKEN = "logToken";    //报文头

}
