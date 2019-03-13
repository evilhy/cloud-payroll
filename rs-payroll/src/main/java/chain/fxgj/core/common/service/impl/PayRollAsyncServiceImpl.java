package chain.fxgj.core.common.service.impl;

import chain.fxgj.core.common.config.properties.PayrollProperties;
import chain.fxgj.core.common.config.properties.WechatProperties;
import chain.fxgj.core.common.constant.DictEnums.DelStatusEnum;
import chain.fxgj.core.common.constant.DictEnums.EnterpriseStatusEnum;
import chain.fxgj.core.common.constant.DictEnums.SystemIdEnum;
import chain.fxgj.core.common.constant.FxgjDBConstant;
import chain.fxgj.core.common.service.PayRollAsyncService;
import chain.fxgj.core.jpa.dao.EmployeeInfoDao;
import chain.fxgj.core.jpa.dao.MsgModelInfoDao;
import chain.fxgj.core.jpa.dao.WechatFollowInfoDao;
import chain.fxgj.core.jpa.model.*;
import chain.fxgj.server.payroll.dto.EventDTO;
import chain.fxgj.server.payroll.dto.ent.EntInfoDTO;
import chain.fxgj.server.payroll.dto.request.ReadWageDTO;
import chain.fxgj.server.payroll.dto.request.WechatLoginDTO;
import chain.fxgj.server.payroll.dto.response.EntInfoRes;
import chain.fxgj.server.payroll.service.WechatBindService;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Future;


/**
 *
 */
@Service
@Slf4j
public class PayRollAsyncServiceImpl implements PayRollAsyncService {

    @Autowired
    @Qualifier("wechatClient")
    Client wechatClient;
    @Autowired
    @Qualifier("insideClient")
    Client insideClient;
    @Autowired
    private WechatProperties wechatProperties;
    @Autowired
    private PayrollProperties payrollProperties;
    @Autowired
    EmployeeInfoDao employeeInfoDao;
    @Autowired
    WechatBindService wechatBindService;
    @Autowired
    WechatFollowInfoDao wechatFollowInfoDao;
    @Autowired
    MsgModelInfoDao msgModelInfoDao;

    @Override
    @Async
    public Future<Response> updBindingInfo(WechatLoginDTO wechatLoginDTO) {
        log.info("请求参数:{}", wechatLoginDTO);
        log.info("请求路径:{}", payrollProperties.getInsideUrl() + "roll/login");
        Response response = wechatClient.target(payrollProperties.getInsideUrl() + "roll/login")
                .request()
                .header(FxgjDBConstant.LOGTOKEN, StringUtils.trimToEmpty(MDC.get(FxgjDBConstant.LOG_TOKEN)))
                .post(Entity.entity(wechatLoginDTO, MediaType.APPLICATION_JSON_TYPE));
        return new AsyncResult<>(response);
    }

    @Override
    @Async
    public Future<EntInfoRes> getNewestEntInfo(String idNumber) {
        QEmployeeInfo qEmployeeInfo = QEmployeeInfo.employeeInfo;
        QEntErpriseInfo qEntErpriseInfo = QEntErpriseInfo.entErpriseInfo;
        EntErpriseInfo entErpriseInfo = employeeInfoDao.select(qEntErpriseInfo).from(qEmployeeInfo)
                .leftJoin(qEntErpriseInfo).on(qEmployeeInfo.entId.eq(qEntErpriseInfo.id))
                .where(qEmployeeInfo.idNumber.eq(idNumber)
                        .and(qEmployeeInfo.delStatusEnum.eq(DelStatusEnum.normal))
                        .and(qEntErpriseInfo.entStatus.in(EnterpriseStatusEnum.NORMAL, EnterpriseStatusEnum.INIT)))
                .orderBy(qEntErpriseInfo.crtDateTime.desc()).fetchFirst();
        EntInfoRes entInfoRes = new EntInfoRes();
        entInfoRes.setEntId(entErpriseInfo.getId());
        entInfoRes.setEntName(entErpriseInfo.getEntName());
        return new AsyncResult<>(entInfoRes);
    }

    @Override
    public Future<Response> eventHandle(EventDTO eventDTO) {

        WechatFollowInfo wechatFollowInfo = wechatFollowInfoDao.findFirstByOpenId(eventDTO.getOpenId());
        if (wechatFollowInfo == null) {
            wechatFollowInfo = new WechatFollowInfo();
            wechatFollowInfo.setCrtDateTime(LocalDateTime.now());
        }
        //事件类型，subscribe(订阅)、unsubscribe(取消订阅)
        if (eventDTO.getEvent().equals("subscribe")) {
            wechatFollowInfo.setDelStatusEnum(DelStatusEnum.normal);
        } else if (eventDTO.getEvent().equals("unsubscribe")) {
            wechatFollowInfo.setDelStatusEnum(DelStatusEnum.delete);
        }
        wechatFollowInfo.setOpenId(eventDTO.getOpenId());
        wechatFollowInfo.setUpdDateTime(LocalDateTime.now());

        wechatFollowInfoDao.save(wechatFollowInfo);
        return new AsyncResult<>(null);
    }

    @Override
    @Async
    public Future<Response> readWage(ReadWageDTO readWageDTO) {
        WebTarget webTarget = insideClient.target(payrollProperties.getInsideUrl() + "roll/readWage");
        log.info("管家url:{}", webTarget.getUri());
        Response response = webTarget.request()
                .header(FxgjDBConstant.LOGTOKEN, StringUtils.trimToEmpty(MDC.get(FxgjDBConstant.LOG_TOKEN)))
                .post(Entity.entity(readWageDTO, MediaType.APPLICATION_JSON_TYPE));
        log.debug("{},{}", response.getStatus(), response.readEntity(String.class));
        return new AsyncResult<>(null);
    }

    @Override
    @Async
    public Future<List<EntInfoDTO>> getGroups(String idNumber) {
        return new AsyncResult<>(wechatBindService.getEntInfos(idNumber));
    }
}
