package chain.fxgj.server.payroll.constant;

/**
 * @author chain
 * create by chain on 2018/10/31 4:18 PM
 **/
public class PayrollConstants {
    public final static String JSESSIONID = "jsession-id";

    public final static String PREFIX = "fxgj";
    public final static String LOG_TOKEN = "log-token";  //日志
    public final static String JSESSION_ID = "jsession-id";    //登录会话ID
    public final static String LOGIN_USER_ID = "login-user-id";    //登录用户ID
    public final static String LOGIN_CN_NAME = "login-cn-name";    //登录用户姓名
    public final static String LOGIN_ORG_ID = "login-org-id";    //登录用户所属机构
    public final static String PAGE_NUM = "page-num";    //分页页码起始为1
    public final static String LIMIT = "limit";    //每页显示条数，默认为20
    public final static String SORT_FIELD = "sort-field";    //排序字段名
    public final static String DIRECTION = "direction";    //排序方向：DESC-倒序，ASC-正序。默认为ASC
    public final static String PLAT_ID = "plat-id";    //平台id(EnumPlatform)
    public final static String APPPARTNER = "apppartner";
    public final static String APPPARTNER_DESC = "apppartnerdesc";
    public final static String SUB_TOKEN = "sub_token";  //子线程日志
    /**
     * 凭证超时时间   默认值：600
     */
    public final static Integer MERCHANT_EXPIRESIN = 3600;

    /**
     * 构造网页授权链接，获取code
     */
    public static final String OAUTH_AUTHORIZE_URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=SCOPE&state=STATE#wechat_redirect";

    /**
     * 构造网页授权链接，获取code
     */
    public static final String SNSAPI_USERINFO = "snsapi_userinfo";

    /**
     * 活动 凭证超时时间   默认值：600
     */
    public final static Integer ACTIVITY_EXPIRESIN = 30;

    /**
     * 活动
     */
    public static final String ACTIVITYACCEDE_LOCK_ACTIVITYID_IDNUMBER = "activityaccede_lock_activityid_idnumber";


}
