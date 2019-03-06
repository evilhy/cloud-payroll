package chain.fxgj.server.payroll.controller;

import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.dto.msg.MsgModelInfoDTO;
import chain.fxgj.core.common.service.MsgModelInfoService;
import chain.fxgj.server.payroll.web.WebContext;
import chain.utils.commons.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

@RestController
@Validated
@RequestMapping(value = "/msgmode")
@Slf4j
public class MsgModelInfoRS {

    @Autowired
    MsgModelInfoService msgModelInfoService;

    /**
     * 查询账户信息
     *
     * @param id
     * @return
     */
    @GetMapping
    @TrackLog
    public Mono<MsgModelInfoDTO> get(@RequestParam(required = false) String id) {

        MsgModelInfoDTO msgModelInfoDTO = new MsgModelInfoDTO();
        msgModelInfoDTO.setSystemId(0);
        msgModelInfoDTO.setCheckType(1);
        msgModelInfoDTO.setBusiType(9);

        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        String finalId = id;
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            ;
            return msgModelInfoService.findByMsgModelInfo(msgModelInfoDTO);
        }).subscribeOn(Schedulers.elastic());
    }


}
