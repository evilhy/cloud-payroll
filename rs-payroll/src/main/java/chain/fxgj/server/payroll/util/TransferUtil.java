package chain.fxgj.server.payroll.util;

import chain.fxgj.feign.dto.web.WageUserPrincipal;
import chain.fxgj.server.payroll.dto.ent.EntInfoDTO;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.utils.commons.JacksonUtil;
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

    public static WageUserPrincipal userPrincipalToWageUserPrincipal (UserPrincipal userPrincipal){
        WageUserPrincipal wageUserPrincipal = new WageUserPrincipal();
        if (null != userPrincipal) {
            wageUserPrincipal.setSessionId(userPrincipal.getSessionId());
            wageUserPrincipal.setName(userPrincipal.getName());
            wageUserPrincipal.setRoles(userPrincipal.getRoles());
            wageUserPrincipal.setSessionTimeOut(userPrincipal.getSessionTimeOut());
            wageUserPrincipal.setAccouts(userPrincipal.getAccouts());
            wageUserPrincipal.setAppPartner(userPrincipal.getAppPartner());
            wageUserPrincipal.setBranchNo(userPrincipal.getBranchNo());
            wageUserPrincipal.setDataAuths(userPrincipal.getDataAuths());
            wageUserPrincipal.setEntId(userPrincipal.getEntId());
            List<EntInfoDTO> entInfoDTOS = userPrincipal.getEntInfoDTOS();
            List<chain.fxgj.feign.dto.ent.EntInfoDTO> entInfoDTOSList = wageUserPrincipal.getEntInfoDTOS();
            if (null != entInfoDTOS && entInfoDTOS.size() > 0) {
                for (EntInfoDTO entInfoDTO : entInfoDTOS) {
                    chain.fxgj.feign.dto.ent.EntInfoDTO feiginEntInfoDto = new chain.fxgj.feign.dto.ent.EntInfoDTO();
                    BeanUtils.copyProperties(entInfoDTO,feiginEntInfoDto);
                    entInfoDTOSList.add(feiginEntInfoDto);
                }
            }
            wageUserPrincipal.setEntInfoDTOS(entInfoDTOSList);
            wageUserPrincipal.setEntName(userPrincipal.getEntName());
            wageUserPrincipal.setGroupIds(userPrincipal.getGroupIds());
            wageUserPrincipal.setHeadimgurl(userPrincipal.getHeadimgurl());
            wageUserPrincipal.setIdNumber(userPrincipal.getIdNumber());
            wageUserPrincipal.setIdNumberEncrytor(userPrincipal.getIdNumberEncrytor());
            wageUserPrincipal.setLastOptDateTime(userPrincipal.getLastOptDateTime());
            wageUserPrincipal.setLoginDateTime(userPrincipal.getLoginDateTime());
            wageUserPrincipal.setManagerId(userPrincipal.getManagerId());
            wageUserPrincipal.setNickname(userPrincipal.getNickname());
            wageUserPrincipal.setOfficer(userPrincipal.getOfficer());
            wageUserPrincipal.setOpenId(userPrincipal.getOpenId());
            wageUserPrincipal.setPhone(userPrincipal.getPhone());
            wageUserPrincipal.setQueryPwd(userPrincipal.getQueryPwd());
            wageUserPrincipal.setSubBranchNo(userPrincipal.getSubBranchNo());
            wageUserPrincipal.setTimeOutMinute(userPrincipal.getTimeOutMinute());
            wageUserPrincipal.setUid(userPrincipal.getUid());
            wageUserPrincipal.setUserName(userPrincipal.getUserName());
            wageUserPrincipal.setWechatId(userPrincipal.getWechatId());
        }
        log.info("wageUserPrincipal:[{}]", JacksonUtil.objectToJson(wageUserPrincipal));
        return wageUserPrincipal;
    }

    public static UserPrincipal WageUserPrincipalToUserPrincipal (WageUserPrincipal wageUserPrincipal){
        UserPrincipal userPrincipal = new UserPrincipal();
        if (null != wageUserPrincipal) {
            userPrincipal.setSessionId(wageUserPrincipal.getSessionId());
            userPrincipal.setName(wageUserPrincipal.getName());
            userPrincipal.setRoles(wageUserPrincipal.getRoles());
            userPrincipal.setSessionTimeOut(wageUserPrincipal.getSessionTimeOut());
            userPrincipal.setAccouts(wageUserPrincipal.getAccouts());
            userPrincipal.setAppPartner(wageUserPrincipal.getAppPartner());
            userPrincipal.setBranchNo(wageUserPrincipal.getBranchNo());
            userPrincipal.setDataAuths(wageUserPrincipal.getDataAuths());
            userPrincipal.setEntId(wageUserPrincipal.getEntId());
            List<chain.fxgj.feign.dto.ent.EntInfoDTO> entInfoDTOS = wageUserPrincipal.getEntInfoDTOS();
            List<EntInfoDTO> entInfoDTOSList = new ArrayList<>();
            if (null != entInfoDTOS && entInfoDTOS.size()>0) {
                for (chain.fxgj.feign.dto.ent.EntInfoDTO entInfoDTO : entInfoDTOS) {
                    EntInfoDTO entInfoDTO1 = new EntInfoDTO();
                    BeanUtils.copyProperties(entInfoDTO,entInfoDTO1);
                    entInfoDTOSList.add(entInfoDTO1);
                }
            }

            userPrincipal.setEntInfoDTOS(entInfoDTOSList);
            userPrincipal.setEntName(wageUserPrincipal.getEntName());
            userPrincipal.setGroupIds(wageUserPrincipal.getGroupIds());
            userPrincipal.setHeadimgurl(wageUserPrincipal.getHeadimgurl());
            userPrincipal.setIdNumber(wageUserPrincipal.getIdNumber());
            userPrincipal.setIdNumberEncrytor(wageUserPrincipal.getIdNumberEncrytor());
            userPrincipal.setLastOptDateTime(wageUserPrincipal.getLastOptDateTime());
            userPrincipal.setLoginDateTime(wageUserPrincipal.getLoginDateTime());
            userPrincipal.setManagerId(wageUserPrincipal.getManagerId());
            userPrincipal.setNickname(wageUserPrincipal.getNickname());
            userPrincipal.setOfficer(wageUserPrincipal.getOfficer());
            userPrincipal.setOpenId(wageUserPrincipal.getOpenId());
            userPrincipal.setPhone(wageUserPrincipal.getPhone());
            userPrincipal.setQueryPwd(wageUserPrincipal.getQueryPwd());
            userPrincipal.setSubBranchNo(wageUserPrincipal.getSubBranchNo());
            userPrincipal.setTimeOutMinute(wageUserPrincipal.getTimeOutMinute());
            userPrincipal.setUid(wageUserPrincipal.getUid());
            userPrincipal.setUserName(wageUserPrincipal.getUserName());
            userPrincipal.setWechatId(wageUserPrincipal.getWechatId());
        }
        log.info("userPrincipal:[{}]", JacksonUtil.objectToJson(wageUserPrincipal));
        return userPrincipal;
    }
}