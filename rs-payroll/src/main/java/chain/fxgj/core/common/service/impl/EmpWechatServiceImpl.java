package chain.fxgj.core.common.service.impl;

import chain.css.exception.ParamsIllegalException;
import chain.fxgj.core.common.constant.DictEnums.CardUpdStatusEnum;
import chain.fxgj.core.common.constant.DictEnums.DelStatusEnum;
import chain.fxgj.core.common.constant.ErrorConstant;
import chain.fxgj.core.common.service.EmpWechatService;
import chain.fxgj.core.common.service.EmployeeEncrytorService;
import chain.fxgj.core.common.service.InsideService;
import chain.fxgj.core.common.service.PayRollAsyncService;
import chain.fxgj.core.common.util.TransUtil;
import chain.fxgj.core.jpa.dao.*;
import chain.fxgj.core.jpa.model.*;
import chain.fxgj.server.payroll.dto.EmployeeDTO;
import chain.fxgj.server.payroll.dto.ent.EntInfoDTO;
import chain.fxgj.server.payroll.dto.request.UpdBankCardDTO;
import chain.fxgj.server.payroll.dto.request.WechatLoginDTO;
import chain.fxgj.server.payroll.service.EmployeeService;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.utils.commons.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.Client;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EmpWechatServiceImpl implements EmpWechatService {
    @Autowired
    EmployeeWechatInfoDao employeeWechatInfoDao;
    @Autowired
    EmployeeEncrytorService employeeEncrytorService;
    @Autowired
    EmployeeInfoDao employeeInfoDao;
    @Autowired
    EntErpriseInfoDao entErpriseInfoDao;
    @Autowired
    CardbinInfoDao cardbinInfoDao;
    @Autowired
    EmployeeCardInfoDao employeeCardInfoDao;
    @Autowired
    InsideService insideService;
    @Autowired
    Client wechatClient;
    @Autowired
    PayRollAsyncService payRollAsyncService;
    @Autowired
    EmployeeCardLogDao employeeCardLogDao;
    @Autowired
    EmployeeService employeeService;

    @Override
    public UserPrincipal getWechatInfo(String jsessionId) {
        return null;
    }

    @Override
    public UserPrincipal setWechatInfo(String jsessionId, String openId, String nickname, String headimgurl, String idNumber) throws Exception {
        UserPrincipal userPrincipal = UserPrincipal.builder()
                .sessionId(jsessionId)
                .openId(openId)
                .sessionTimeOut(LocalDateTime.now().plusHours(8))
                .nickname(nickname)
                .headimgurl(headimgurl)
                .idNumber(idNumber)
                .build();

        //判断openId是否绑定
        EmployeeWechatInfo employeeWechatInfo = employeeWechatInfoDao.findFirstByOpenIdAndAndDelStatusEnum(openId, DelStatusEnum.normal);
        if (employeeWechatInfo != null) {
            log.info("员工信息不为空+++++++++++++++++++++");
            String idNumberEncrypt = employeeWechatInfo.getIdNumber();
            userPrincipal.setIdNumberEncrytor(idNumberEncrypt);
            idNumber = employeeEncrytorService.decryptIdNumber(idNumberEncrypt);
            userPrincipal.setIdNumber(idNumber);

            String phone = employeeEncrytorService.decryptPhone(employeeWechatInfo.getPhone());
            log.info("手机号phone:{}", phone);
            userPrincipal.setPhone(phone);
            userPrincipal.setWechatId(employeeWechatInfo.getId());
            userPrincipal.setQueryPwd(employeeWechatInfo.getQueryPwd());
            if (employeeWechatInfo.getAppPartner() != null) {
                userPrincipal.setAppPartner(employeeWechatInfo.getAppPartner());
            }

            //修改绑定信息
            WechatLoginDTO wechatLoginDTO = new WechatLoginDTO();
            wechatLoginDTO.setOpenId(openId);
            wechatLoginDTO.setJsessionId(jsessionId);
            wechatLoginDTO.setNickname(nickname);
            wechatLoginDTO.setHeadimgurl(headimgurl);
            insideService.login(openId, jsessionId, nickname, headimgurl);
        }

        //用户机构
        List<EmployeeInfo> employeeInfos = employeeService.getEmployeeInfos(idNumber, null).get();
        if (employeeInfos != null && employeeInfos.size() > 0) {
            userPrincipal.setName(employeeInfos.get(0).getEmployeeName());
            userPrincipal.setEntId(employeeInfos.get(0).getEntId());
        }


//        List<EntInfoDTO> entInfoDTOS = payRollAsyncService.getGroups(idNumber).get();
//        userPrincipal.setEntInfoDTOS(entInfoDTOS);
//        if (entInfoDTOS != null && entInfoDTOS.size() > 0
//                && entInfoDTOS.get(0).getGroupInfoList() != null && entInfoDTOS.get(0).getGroupInfoList().size() > 0) {
//            userPrincipal.setName(entInfoDTOS.get(0).getGroupInfoList().get(0).getEmployeeInfoList().get(0).getEmployeeName());
//            userPrincipal.setEntId(entInfoDTOS.get(0).getEntId());
//            userPrincipal.setEntName(entInfoDTOS.get(0).getEntName());
//        }
        return userPrincipal;
    }

    @Override
    public List<EmployeeDTO> getEmpList(String idNumber) {
        if (StringUtils.isEmpty(idNumber)) {
            throw new ParamsIllegalException(ErrorConstant.WECHAT_OUT.getErrorMsg());
        }

        List<EntInfoDTO> entInfoDTOS = new ArrayList<>();
        try {
            log.info("idNumber:[{}]", idNumber);
            entInfoDTOS = payRollAsyncService.getGroups(idNumber).get();
            log.info("go on,entInfoDTOS.size()[{}]", entInfoDTOS.size());
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error("e.printStackTrace()->[{}]", e.getMessage());
        } catch (ExecutionException e) {
            e.printStackTrace();
            log.error("e.printStackTrace()-->[{}]", e.getMessage());
        }

        List<EmployeeDTO> list = new ArrayList<>();
        log.info("entInfoDTOS[{}]", JacksonUtil.objectToJson(entInfoDTOS));
        for (EntInfoDTO entInfoDTO : entInfoDTOS) {
            for (EntInfoDTO.GroupInfo groupInfo : entInfoDTO.getGroupInfoList()) {
                LinkedList<EntInfoDTO.GroupInfo.EmployeeInfo> empList = groupInfo.getEmployeeInfoList();
                for (int i = 0; i < empList.size(); i++) {
                    EntInfoDTO.GroupInfo.EmployeeInfo emp = empList.get(i);
                    //if (emp.getDelStatus() == DelStatusEnum.normal.getCode()) {
                    EmployeeDTO employeeDTO = new EmployeeDTO(emp);
                    employeeDTO.setGroupId(groupInfo.getGroupId());
                    employeeDTO.setGroupName(groupInfo.getGroupName());
                    employeeDTO.setGroupShortName(groupInfo.getGroupShortName());
                    employeeDTO.setEntId(entInfoDTO.getEntId());
                    employeeDTO.setEntName(entInfoDTO.getEntName());
                    employeeDTO.setIdNumberStar(TransUtil.idNumberStar(idNumber));
                    list.add(employeeDTO);
                    // }
                }
            }
        }
        log.info("list.size()[{}]", list.size());
        return list;
    }

    @Override
    public CardbinInfo checkCard(String idNumber, UpdBankCardDTO updBankCardDTO) {
        String cardNo = updBankCardDTO.getCardNo();
        List<String> ids = new ArrayList<>();
        List<String> groupIds = updBankCardDTO.getBankCardGroups()
                .stream()
                .map(tuple -> {
                    ids.add(tuple.getId());
                    return tuple.getGroupId();
                }).collect(Collectors.toList());
        List<EmployeeCardInfo> allById = employeeCardInfoDao.findAllById(ids);
        for (EmployeeCardInfo employeeCardInfo : allById) {
            if (cardNo.equals(employeeCardInfo.getCardNo())) {
                throw new ParamsIllegalException(ErrorConstant.WECHAR_012.getErrorMsg());
            }
        }


        //验证是否正在修改审核中
        QEmployeeCardLog qEmployeeCardLog = QEmployeeCardLog.employeeCardLog;
        long count = employeeCardLogDao.selectFrom(qEmployeeCardLog)
                .where(qEmployeeCardLog.cardNo.eq(cardNo)
                        .and(qEmployeeCardLog.groupId.in(groupIds))
                        .and(qEmployeeCardLog.updStatus.eq(CardUpdStatusEnum.UNKOWN)))
                .fetchCount();
        if (count > 0) {
            throw new ParamsIllegalException(ErrorConstant.WECHAR_011.getErrorMsg());
        }

        //验证是否已被使用
        List<EmployeeCardInfo> list = employeeCardInfoDao.findAllByCardNo(cardNo);
        for (EmployeeCardInfo employeeCardInfo : list) {
            if (employeeCardInfo.getDelStatusEnum().equals(DelStatusEnum.normal)) {
                if (!employeeCardInfo.getEmployeeInfo().getIdNumber().equals(idNumber) ||
                        groupIds.contains(employeeCardInfo.getEmployeeInfo().getGroupId())) {
                    throw new ParamsIllegalException(ErrorConstant.WECHAR_010.getErrorMsg());
                }
            }
        }
        //查询卡bin信息
        List<CardbinInfo> cardbinInfos = cardbinInfoDao.findFirstByCardNo(cardNo);
        if (cardbinInfos == null || cardbinInfos.size() <= 0) {
            throw new ParamsIllegalException(ErrorConstant.CARD_ERR.getErrorMsg());
        }

        return cardbinInfos.get(0);
    }

    @Override
    public String getWechatId(String openId) {
        QEmployeeWechatInfo qEmployeeWechatInfo = QEmployeeWechatInfo.employeeWechatInfo;
        return employeeWechatInfoDao.select(qEmployeeWechatInfo.id)
                .from(qEmployeeWechatInfo)
                .where(qEmployeeWechatInfo.openId.eq(openId))
                .fetchFirst();
    }

}
