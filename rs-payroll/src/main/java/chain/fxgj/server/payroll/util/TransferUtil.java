package chain.fxgj.server.payroll.util;

import chain.fxgj.server.payroll.dto.ent.EntInfoDTO;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.utils.commons.JacksonUtil;
import core.dto.wechat.CacheUserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 转换工具类
 *
 */
@Slf4j
public class TransferUtil {

    public static CacheUserPrincipal userPrincipalToWageUserPrincipal (UserPrincipal userPrincipal){
        CacheUserPrincipal cacheUserPrincipal = new CacheUserPrincipal();
        if (null != userPrincipal) {
            cacheUserPrincipal.setSessionId(userPrincipal.getSessionId());
            cacheUserPrincipal.setName(userPrincipal.getName());
            cacheUserPrincipal.setRoles(userPrincipal.getRoles());
            cacheUserPrincipal.setSessionTimeOut(userPrincipal.getSessionTimeOut());
            cacheUserPrincipal.setAccouts(userPrincipal.getAccouts());
            cacheUserPrincipal.setAppPartner(userPrincipal.getAppPartner());
            cacheUserPrincipal.setBranchNo(userPrincipal.getBranchNo());
            cacheUserPrincipal.setDataAuths(userPrincipal.getDataAuths());
            cacheUserPrincipal.setEntId(userPrincipal.getEntId());
            List<EntInfoDTO> entInfoDTOS = userPrincipal.getEntInfoDTOS();
            List<core.dto.wechat.EntInfoDTO> entInfoDTOSList = new ArrayList<>();
            if (null != entInfoDTOS && entInfoDTOS.size() > 0) {
                for (EntInfoDTO entInfoDTO : entInfoDTOS) {
                    core.dto.wechat.EntInfoDTO feiginEntInfoDto = new core.dto.wechat.EntInfoDTO();
                    BeanUtils.copyProperties(entInfoDTO,feiginEntInfoDto);
                    entInfoDTOSList.add(feiginEntInfoDto);
                }
            }
            cacheUserPrincipal.setEntInfoDTOS(entInfoDTOSList);
            cacheUserPrincipal.setEntName(userPrincipal.getEntName());
            cacheUserPrincipal.setGroupIds(userPrincipal.getGroupIds());
            cacheUserPrincipal.setHeadimgurl(userPrincipal.getHeadimgurl());
            cacheUserPrincipal.setIdNumber(userPrincipal.getIdNumber());
            cacheUserPrincipal.setIdNumberEncrytor(userPrincipal.getIdNumberEncrytor());
            cacheUserPrincipal.setLastOptDateTime(userPrincipal.getLastOptDateTime());
            cacheUserPrincipal.setLoginDateTime(userPrincipal.getLoginDateTime());
            cacheUserPrincipal.setManagerId(userPrincipal.getManagerId());
            cacheUserPrincipal.setNickname(userPrincipal.getNickname());
            cacheUserPrincipal.setOfficer(userPrincipal.getOfficer());
            cacheUserPrincipal.setOpenId(userPrincipal.getOpenId());
            cacheUserPrincipal.setPhone(userPrincipal.getPhone());
            cacheUserPrincipal.setQueryPwd(userPrincipal.getQueryPwd());
            cacheUserPrincipal.setSubBranchNo(userPrincipal.getSubBranchNo());
            cacheUserPrincipal.setTimeOutMinute(userPrincipal.getTimeOutMinute());
            cacheUserPrincipal.setUid(userPrincipal.getUid());
            cacheUserPrincipal.setUserName(userPrincipal.getUserName());
            cacheUserPrincipal.setWechatId(userPrincipal.getWechatId());
        }
        log.info("cacheUserPrincipal:[{}]", JacksonUtil.objectToJson(cacheUserPrincipal));
        return cacheUserPrincipal;
    }

    public static UserPrincipal WageUserPrincipalToUserPrincipal (CacheUserPrincipal cacheUserPrincipal){
        UserPrincipal userPrincipal = new UserPrincipal();
        if (null != cacheUserPrincipal) {
            userPrincipal.setSessionId(cacheUserPrincipal.getSessionId());
            userPrincipal.setName(cacheUserPrincipal.getName());
            userPrincipal.setRoles(cacheUserPrincipal.getRoles());
            userPrincipal.setSessionTimeOut(cacheUserPrincipal.getSessionTimeOut());
            userPrincipal.setAccouts(cacheUserPrincipal.getAccouts());
            userPrincipal.setAppPartner(cacheUserPrincipal.getAppPartner());
            userPrincipal.setBranchNo(cacheUserPrincipal.getBranchNo());
            userPrincipal.setDataAuths(cacheUserPrincipal.getDataAuths());
            userPrincipal.setEntId(cacheUserPrincipal.getEntId());
            List<core.dto.wechat.EntInfoDTO> entInfoDTOS = cacheUserPrincipal.getEntInfoDTOS();
            List<EntInfoDTO> entInfoDTOSList = new ArrayList<>();
            if (null != entInfoDTOS && entInfoDTOS.size()>0) {
                for (core.dto.wechat.EntInfoDTO entInfoDTO : entInfoDTOS) {
                    EntInfoDTO entInfoDTO1 = new EntInfoDTO();
                    BeanUtils.copyProperties(entInfoDTO,entInfoDTO1);
                    entInfoDTOSList.add(entInfoDTO1);
                }
            }

            userPrincipal.setEntInfoDTOS(entInfoDTOSList);
            userPrincipal.setEntName(cacheUserPrincipal.getEntName());
            userPrincipal.setGroupIds(cacheUserPrincipal.getGroupIds());
            userPrincipal.setHeadimgurl(cacheUserPrincipal.getHeadimgurl());
            userPrincipal.setIdNumber(cacheUserPrincipal.getIdNumber());
            userPrincipal.setIdNumberEncrytor(cacheUserPrincipal.getIdNumberEncrytor());
            userPrincipal.setLastOptDateTime(cacheUserPrincipal.getLastOptDateTime());
            userPrincipal.setLoginDateTime(cacheUserPrincipal.getLoginDateTime());
            userPrincipal.setManagerId(cacheUserPrincipal.getManagerId());
            userPrincipal.setNickname(cacheUserPrincipal.getNickname());
            userPrincipal.setOfficer(cacheUserPrincipal.getOfficer());
            userPrincipal.setOpenId(cacheUserPrincipal.getOpenId());
            userPrincipal.setPhone(cacheUserPrincipal.getPhone());
            userPrincipal.setQueryPwd(cacheUserPrincipal.getQueryPwd());
            userPrincipal.setSubBranchNo(cacheUserPrincipal.getSubBranchNo());
            userPrincipal.setTimeOutMinute(cacheUserPrincipal.getTimeOutMinute());
            userPrincipal.setUid(cacheUserPrincipal.getUid());
            userPrincipal.setUserName(cacheUserPrincipal.getUserName());
            userPrincipal.setWechatId(cacheUserPrincipal.getWechatId());
        }
        log.info("userPrincipal:[{}]", JacksonUtil.objectToJson(cacheUserPrincipal));
        return userPrincipal;
    }
}