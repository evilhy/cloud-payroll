package chain.fxgj.core.common.service.impl;

import chain.fxgj.core.common.constant.DictEnums.CustStatusEnum;
import chain.fxgj.core.common.constant.DictEnums.DelStatusEnum;
import chain.fxgj.core.common.service.ManagerService;
import chain.fxgj.core.jpa.dao.*;
import chain.fxgj.core.jpa.model.*;
import chain.fxgj.server.payroll.dto.request.DistributeDTO;
import chain.fxgj.server.payroll.dto.response.ManagerInfoDTO;
import chain.utils.commons.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ManagerServiceImpl implements ManagerService {
    private static final Logger logger = LoggerFactory.getLogger(ManagerServiceImpl.class);
    @Autowired
    private ManagerInfoDao managerInfoDao;
    @Autowired
    private EmployeeInfoDao employeeInfoDao;
    @Autowired
    private EntErpriseInfoDao entErpriseInfoDao;
    @Autowired
    private EntGroupInfoDao entGroupInfoDao;
    @Autowired
    private EntNoneManagerDao entNoneManagerDao;

    @Override
    public ManagerInfoDTO managerInfoByIdNumber(String idNumber) {
        ManagerInfoDTO managerInfoDTO = new ManagerInfoDTO();
        EmployeeInfo employeeInfo = new EmployeeInfo();
        employeeInfo.setIdNumber(idNumber);
        employeeInfo.setDelStatusEnum(DelStatusEnum.normal);
        //TODO 一个员工可在多个企业
        List<EmployeeInfo> infoList = employeeInfoDao.findAll(Example.of(employeeInfo));
        if (infoList == null || infoList.size() == 0) {
            throw new RuntimeException("没有此身份证的员工信息");
        }
        String entId = infoList.get(0).getEntId();
        EntErpriseInfo entErpriseInfo = entErpriseInfoDao.findById(entId).orElse(null);
        if (entErpriseInfo == null) {
            throw new RuntimeException("未获取到该员工所在的企业信息");
        }
        String officer = entErpriseInfo.getOfficer();
        String branch = entErpriseInfo.getBranch();
        if (StringUtils.isBlank(officer) || StringUtils.isBlank(branch)) {
            logger.info("==>企业没有关联客户经理");
            return managerInfoDTO;
        }
        ManagerInfo managerInfo = new ManagerInfo();
        managerInfo.setOfficer(officer);
        managerInfo.setSubBranchNo(branch);
        managerInfo.setCustStatus(CustStatusEnum.NORMAL);
        List<ManagerInfo> managerInfoList = managerInfoDao.findAll(Example.of(managerInfo));
        if (managerInfoList == null || managerInfoList.size() == 0) {
            logger.info("==>企业没有关联客户经理");
            return managerInfoDTO;
        }
        ManagerInfo info = managerInfoList.get(0);
        managerInfoDTO.setOfficer(info.getOfficer());
        managerInfoDTO.setAvatarUrl(info.getAvatarUrl());
        managerInfoDTO.setBranchOrgName(info.getBranchName());
        managerInfoDTO.setBranchOrgNo(info.getBranchNo());
        managerInfoDTO.setId(info.getId());
        managerInfoDTO.setIsConfirmed(info.getIsConfirmed());
        managerInfoDTO.setManagerName(info.getManagerName());
        managerInfoDTO.setMobile(info.getPhone());
        managerInfoDTO.setScore(info.getScore());
        managerInfoDTO.setStatus(info.getCustStatus().getDesc());
        managerInfoDTO.setSubBranchOrgName(info.getSubBranchName());
        managerInfoDTO.setWechatId(info.getWechatId());
        managerInfoDTO.setSubBranchOrgNo(info.getSubBranchNo());
        managerInfoDTO.setWechatQrImgae(info.getWechatQrImgae());
        managerInfoDTO.setWechatQrUrl(info.getWechatQrUrl());
        return managerInfoDTO;
    }

    @Override
    public void noticEnterprise(DistributeDTO distributeDTO) {
        String groupId = distributeDTO.getGroupId();
        EntGroupInfo entGroupInfo = entGroupInfoDao.findById(groupId).get();
        if (entGroupInfo == null) {
            throw new RuntimeException("机构信息不存在");
        }
        String entId = entGroupInfo.getEntId();
        EntErpriseInfo entErpriseInfo = entErpriseInfoDao.findById(entId).get();
        if (entErpriseInfo == null) {
            throw new RuntimeException("企业信息不存在");
        }
        EntNoneManager entNoneManager = new EntNoneManager();
        entNoneManager.setEntId(entId);
        entNoneManager.setEntName(entErpriseInfo.getEntName());
        entNoneManager.setRemark("企业未分配客户经理");
        entNoneManagerDao.save(entNoneManager);
    }
}