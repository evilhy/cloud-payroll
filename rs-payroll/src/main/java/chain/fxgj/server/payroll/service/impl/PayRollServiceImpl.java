package chain.fxgj.server.payroll.service.impl;

import chain.css.exception.ParamsIllegalException;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.dto.response.NewestWageLogDTO;
import chain.fxgj.server.payroll.service.EmployeeEncrytorService;
import chain.fxgj.server.payroll.service.PayRollService;
import chain.fxgj.server.payroll.util.DateTimeUtils;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.payroll.client.feign.*;
import chain.utils.commons.JacksonUtil;
import chain.utils.commons.StringUtils;
import chain.utils.fxgj.constant.DictEnums.DelStatusEnum;
import chain.utils.fxgj.constant.DictEnums.FundLiquidationEnum;
import chain.utils.fxgj.constant.DictEnums.IsStatusEnum;
import chain.utils.fxgj.constant.DictEnums.PayStatusEnum;
import core.dto.request.employee.EmployeeQueryReq;
import core.dto.request.wageDetail.WageDetailQueryReq;
import core.dto.response.employee.EmployeeDTO;
import core.dto.response.ent.EntErpriseDTO;
import core.dto.response.group.GroupDTO;
import core.dto.response.wageDetail.WageDetailDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Description:
 * @Author: du
 * @Date: 2020/8/12 21:19
 */
@Service
@Slf4j
public class PayRollServiceImpl implements PayRollService {

    @Autowired
    EmployeeEncrytorService employeeEncrytorService;
    @Autowired
    WageDetailFeignController wageDetailFeignController;
    @Autowired
    WageSheetFeignController wageSheetFeignController;
    @Autowired
    EntErpriseFeignController entErpriseFeignController;
    @Autowired
    EmployeeFeignController employeeFeignController;
    @Autowired
    GroupFeignController groupFeignController;

    public List<NewestWageLogDTO> groupList(String entId, String groupId, String idNumber, UserPrincipal userPrincipal) {
        List<FundLiquidationEnum> dataAuths = userPrincipal.getDataAuths();
        log.info("groupList.dataAuths:[{}]", JacksonUtil.objectToJson(dataAuths));
        List<NewestWageLogDTO> list = new ArrayList<>();

        //根据身份证查询员工所属机构
        EmployeeQueryReq employeeQueryReq = EmployeeQueryReq.builder()
                .entId(entId)
                .idNumber(idNumber)
                .build();
        List<EmployeeDTO> employeeDTOList = employeeFeignController.query(employeeQueryReq);

        //获取机构
        if (null == employeeDTOList || employeeDTOList.size() <= 0) {
            log.info("=====> employeeQueryReq:{} result:{}", JacksonUtil.objectToJson(employeeQueryReq), JacksonUtil.objectToJson(employeeDTOList));
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("员工信息不存在"));
        }
        Set<String> groupIdSet = new HashSet<>();
        for (EmployeeDTO dto : employeeDTOList) {
            if (StringUtils.isNotBlank(dto.getGroupId())) {
                groupIdSet.add(dto.getGroupId());
            }
        }
        List<String> groupIds = new ArrayList<>(groupIdSet);
        if (null == groupIds || groupIds.size() <= 0) {
            log.info("=====> groupIdSet:{} groupIds:{}", JacksonUtil.objectToJson(groupIdSet), JacksonUtil.objectToJson(groupIds));
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("员工所属机构信息不存在"));
        }
        for (String id : groupIds) {

            //查询机构信息
            GroupDTO groupDTO = groupFeignController.findById(id);
            if (null == groupDTO) {
                log.info("=====> 机构信息不存在 groupId:{}", id);
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("机构信息不存在"));
            }
            NewestWageLogDTO logDTO = NewestWageLogDTO.builder()
                    .groupShortName(groupDTO.getShortGroupName())
                    .groupName(groupDTO.getGroupName())
                    .groupId(groupDTO.getGroupId())
                    .build();

            //企业信息
            EntErpriseDTO erpriseDTO = entErpriseFeignController.findById(groupDTO.getEntId());

            //数据权限
            log.info("getEmpList()dataAuths:[{}]", JacksonUtil.objectToJson(dataAuths));
            if (dataAuths != null && dataAuths.size() > 0) {
                for (int j = 0; j < dataAuths.size(); j++) {
                    FundLiquidationEnum fundLiquidationEnum = dataAuths.get(j);
                    if (fundLiquidationEnum != erpriseDTO.getLiquidation()) {
                        //数据权限不一致，不添加返回
                       continue;
                    }
                }
            }

            if (null == erpriseDTO) {
                log.info("=====> 机构所属企业信息不存在 groupId:{}，entId:{}", id, groupDTO.getEntId());
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("机构所属企业信息不存在"));
            }
            String entId1 = erpriseDTO.getEntId();
            logDTO.setEntId(entId1);
            logDTO.setEntName(erpriseDTO.getEntName());

            //根据entId、groupId、idNumber查询最新一笔代发明细 todo

            //查询代发方案
            WageDetailQueryReq wageDetailQueryReq = WageDetailQueryReq.builder()
                    .idNumber(idNumber)
                    .groupId(id)
                    .entId(entId1)
                    .isCountStatus(IsStatusEnum.YES)
                    .payStatus(Arrays.asList(PayStatusEnum.SUCCESS, PayStatusEnum.ING, PayStatusEnum.FAIL, PayStatusEnum.UNKNOWN))
                    .build();
            List<WageDetailDTO> detailDTOList = wageDetailFeignController.query(wageDetailQueryReq);
            if (null != detailDTOList && detailDTOList.size() > 0) {
                WageDetailDTO wageDetailDTO = detailDTOList.get(0);

                //查询方案明细
                logDTO.setIsRead(null == wageDetailDTO.getIsRead() ? null : wageDetailDTO.getIsRead().getCode().toString());
                logDTO.setCreateDate(null == wageDetailDTO.getCrtDateTime() ? null : DateTimeUtils.convert(wageDetailDTO.getCrtDateTime()));
                String employeeSid = null;
                if (StringUtils.isNotBlank(wageDetailDTO.getEmployeeSid())) {
                    employeeSid = employeeEncrytorService.decryptEmployeeId(wageDetailDTO.getEmployeeSid());
                }

                //员工在职状态
                EmployeeQueryReq employeeQueryReq1 = EmployeeQueryReq.builder()
                        .entId(entId1)
                        .idNumber(idNumber)
                        .delStatusEnum(DelStatusEnum.normal)
                        .groupId(id)
                        .employeeId(employeeSid)
                        .build();
                List<EmployeeDTO> employeeList = employeeFeignController.query(employeeQueryReq1);
                if (null == employeeList || employeeList.size() <= 0) {
                    log.info("=====> 未找到明细所属员工信息 employeeQueryReq1:{}", JacksonUtil.objectToJson(employeeQueryReq1));
                    throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("未找到明细所属员工信息"));
                }
                logDTO.setInServiceStatus(employeeList.get(0).getEmployeeId());
            }
            list.add(logDTO);
        }
        return list;
    }
}
