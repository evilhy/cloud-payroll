package chain.fxgj.server.payroll.web;

import chain.fxgj.core.common.constant.DictEnums.AppPartnerEnum;
import chain.fxgj.server.payroll.dto.ent.EntInfoDTO;
import lombok.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author chain
 * create by chain on 2018/8/20 下午10:30
 **/
@Getter
@Setter
@EqualsAndHashCode
@ToString(exclude = {"entInfoDTOS"})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPrincipal implements Principal {
    /**
     * sessionId
     */
    private String sessionId;
    /**
     * 用户名
     */
    private String name;
    /**
     * 角色列表
     */
    private String[] roles;
    /**
     * 用户session超时时间
     */
    private LocalDateTime sessionTimeOut;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 企业id
     */
    private String entId;
    /**
     * 企业名称
     */
    private String entName;
    /**
     * 用户 微信openId
     */
    private String openId;
    /**
     * 用户 微信openId
     */
    private String uid;
    /**
     * 合作商平台标识
     */
    @Builder.Default
    private AppPartnerEnum appPartner = AppPartnerEnum.FXGJ;
    /**
     * 用户身份证号码（明文）
     */
    private String idNumber;
    /**
     * 用户身份证号码（密文）
     */
    private String idNumberEncrytor;
    /**
     * 用户 手机号
     */
    private String phone;
    /**
     * 用户昵称
     */
    private String nickname;
    /**
     * 用户头像url
     */
    private String headimgurl;
    /**
     * 用户绑定微信表 唯一标识
     */
    private String wechatId;
    /**
     * 用户查询密码
     */
    private String queryPwd;
    /**
     * 放薪经理标识
     */
    private String managerId;
    /**
     * 分行号
     */
    private String branchNo;
    /**
     * 用户 岗位
     */
    private String officer;
    /**
     * 分行号
     */
    private String subBranchNo;
    /**
     * 用户所在机构列表
     */
    private String[] groupIds;
    /**
     * 用户 账户列
     */
    private String[] accouts;
    /**
     * 用户登陆时间
     */
    @Builder.Default
    private LocalDateTime loginDateTime = LocalDateTime.now();
    /**
     * 用户超时时间
     */
    private Integer timeOutMinute;
    /**
     * 用户最后操作时间
     */
    private LocalDateTime lastOptDateTime;
    /**
     * 用户所在企业列表
     */
    private List<EntInfoDTO> entInfoDTOS;


    public UserPrincipal(String name, String sessionId, LocalDateTime sessionTimeOut, String[] roles) {
        this.name = name;
        this.sessionId = sessionId;
        this.sessionTimeOut = sessionTimeOut;
        this.roles = roles;
        this.loginDateTime = LocalDateTime.now();
    }
}
