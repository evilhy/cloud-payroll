package chain.fxgj.server.payroll.service.impl;

import chain.fxgj.feign.dto.response.WageDetailInfoDTO;
import chain.fxgj.feign.dto.response.WageRes100703;
import chain.fxgj.feign.dto.web.WageUserPrincipal;
import chain.fxgj.server.payroll.service.EmpWechatService;
import chain.fxgj.server.payroll.service.WechatRedisService;
import chain.payroll.dto.response.PayrollRes100703DTO;
import chain.payroll.dto.response.PayrollWageDetailDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class WechatRedisServiceImpl implements WechatRedisService {
    @Override
    public PayrollRes100703DTO wageListByMongo(String idNumber, String groupId, String year, String type) {
        return null;
    }

    @Override
    public WageRes100703 wageListByMysql(String idNumber, String groupId, String year, String type) {
        return null;
    }

    @Override
    public List<PayrollWageDetailDTO> getWageDetailByMongo(String idNumber, String groupId, String wageSheetId) {
        List<PayrollWageDetailDTO> payrollWageDetailDTOS = new ArrayList<>();
        return payrollWageDetailDTOS;
    }

    @Override
    public List<WageDetailInfoDTO> getWageDetailByMysql(String idNumber, String groupId, String wageSheetId) {
        List<WageDetailInfoDTO> wageDetailInfoDTOS = new ArrayList<>();
        return wageDetailInfoDTOS;
    }
}
