package chain.fxgj.server.payroll.service.impl;

import chain.fxgj.feign.client.CustManagerFeignService;
import chain.fxgj.server.payroll.service.FundService;
import chain.payroll.client.feign.EntErpriseFeignController;
import chain.utils.commons.JacksonUtil;
import chain.utils.commons.StringUtils;
import chain.wage.manager.core.dto.custmanager.WageManagerInfoDTO;
import chain.wisales.client.feign.PromiseAppointmentFeignService;
import chain.wisales.core.dto.promise.FundAppointmentInfoDTO;
import core.dto.response.ent.EntIdListDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
public class FundServiceImpl implements FundService {

    @Autowired
    PromiseAppointmentFeignService promiseAppointmentFeignService;
    @Autowired
    EntErpriseFeignController entErpriseFeignController;
    @Autowired
    CustManagerFeignService custManagerFeignService;

    @Override
    public FundAppointmentInfoDTO qryFundAppointmentInfo(String jsessionId, String openId) {

        FundAppointmentInfoDTO fundAppointmentInfoDTO = FundAppointmentInfoDTO.builder()
                .jsessionId(jsessionId)
                .openId(openId)
                .appointmentType("0")
                .build();
        //【一】根据openId 查询 基金预约数据
        FundAppointmentInfoDTO fundAppointmentInfoDTORes = promiseAppointmentFeignService.queryAppointmentByOpenId(openId);
        //1.查询唯销预约信息
        //a.有预约信息，放入缓存
        if (null != fundAppointmentInfoDTORes && StringUtils.isNotEmpty(fundAppointmentInfoDTORes.getOpenId())) {
            BeanUtils.copyProperties(fundAppointmentInfoDTORes, fundAppointmentInfoDTO);
            fundAppointmentInfoDTO.setAppointmentType("1");
            return fundAppointmentInfoDTO;
        }
        //b.无预约信息，根据openId查询经理信息
        // 先根据 openId 查询所有企业idList
        EntIdListDTO entIdListDTO = entErpriseFeignController.qryEntIdListByOpenId(openId);
        Set<String> entIdList = entIdListDTO.getEntIdList();
        if (!entIdList.isEmpty()) {
            for (String entId : entIdList) {
                //根据entId查询经理信息
                WageManagerInfoDTO wageManagerInfoDTO = custManagerFeignService.managerInfoByEntId(entId);
                if (null != wageManagerInfoDTO) {
                    fundAppointmentInfoDTO.setManagerId(wageManagerInfoDTO.getId());
                    fundAppointmentInfoDTO.setManagerName(wageManagerInfoDTO.getManagerName());
                    fundAppointmentInfoDTO.setBranchNo(wageManagerInfoDTO.getBranchOrgNo());
                    fundAppointmentInfoDTO.setBranchName(wageManagerInfoDTO.getBranchOrgName());
                    fundAppointmentInfoDTO.setSubBranchName(wageManagerInfoDTO.getSubBranchOrgName());
                    fundAppointmentInfoDTO.setSubBranchNo(wageManagerInfoDTO.getSubBranchOrgNo());
                    fundAppointmentInfoDTO.setManagerPhone(wageManagerInfoDTO.getMobile());
                    break;
                }
            }
        }
        return fundAppointmentInfoDTO;
    }

    @Override
    public FundAppointmentInfoDTO fundAppointmentInfoSave(String jsessionId, FundAppointmentInfoDTO fundSaveDTO) {

        Boolean aBoolean = promiseAppointmentFeignService.addAppointment(fundSaveDTO);
        if (aBoolean) {
            fundSaveDTO.setAppointmentType("1");
        } else {
            log.error("唯销入库失败");
        }
        log.info("fundAppointmentInfoSave.fundSaveDTO:[{}]", JacksonUtil.objectToJson(fundSaveDTO));
        return fundSaveDTO;
    }

    @Override
    public FundAppointmentInfoDTO qryFunInfo(String jsessionId) {
        return null;
    }
}
