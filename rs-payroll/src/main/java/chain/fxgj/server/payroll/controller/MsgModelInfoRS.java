package chain.fxgj.server.payroll.controller;

import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.dto.msg.MsgModelInfoDTO;
import chain.fxgj.server.payroll.service.MsgModelInfoService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

@CrossOrigin
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
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        MsgModelInfoDTO msgModelInfoDTO = new MsgModelInfoDTO();
        msgModelInfoDTO.setSystemId(0);
        msgModelInfoDTO.setCheckType(1);
        msgModelInfoDTO.setBusiType(9);

        String finalId = id;
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            return msgModelInfoService.findByMsgModelInfo(msgModelInfoDTO);
        }).subscribeOn(Schedulers.elastic());
    }


}
