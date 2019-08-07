package chain.fxgj.core.common.service.impl;

import chain.fxgj.core.common.constant.DictEnums.DelStatusEnum;
import chain.fxgj.core.common.constant.DictEnums.EnterpriseStatusEnum;
import chain.fxgj.core.common.service.PayRollAsyncService;
import chain.fxgj.core.common.service.WechatBindService;
import chain.fxgj.core.jpa.dao.EmployeeInfoDao;
import chain.fxgj.core.jpa.dao.WechatFollowInfoDao;
import chain.fxgj.core.jpa.model.*;
import chain.fxgj.server.payroll.dto.EventDTO;
import chain.fxgj.server.payroll.dto.ent.EntInfoDTO;
import chain.fxgj.server.payroll.dto.response.EntInfoRes;
import chain.fxgj.server.payroll.web.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

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
    EmployeeInfoDao employeeInfoDao;
    @Autowired
    WechatBindService wechatBindService;
    @Autowired
    WechatFollowInfoDao wechatFollowInfoDao;

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
        String openId = eventDTO.getOpenId();
        QWechatFollowInfo qWechatFollowInfo = QWechatFollowInfo.wechatFollowInfo;
        WechatFollowInfo wechatFollowInfo = wechatFollowInfoDao.selectFrom(qWechatFollowInfo).where(qWechatFollowInfo.openId.eq(openId)).fetchFirst();
        //WechatFollowInfo wechatFollowInfo = wechatFollowInfoDao.findFirstByOpenId(eventDTO.getOpenId());
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
    public Future<List<EntInfoDTO>> getGroups(String idNumber,UserPrincipal userPrincipal) {
        return new AsyncResult<>(wechatBindService.getEntInfos(idNumber, userPrincipal));
    }
}
