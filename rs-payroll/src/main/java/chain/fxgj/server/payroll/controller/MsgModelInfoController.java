package chain.fxgj.server.payroll.controller;

import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.dto.msg.MsgModelInfoDTO;
import chain.fxgj.feign.client.MsgModuleInfoFeignService;
import chain.fxgj.feign.dto.response.WageMsgModelInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
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
public class MsgModelInfoController {


    @Autowired
    private MsgModuleInfoFeignService msgModuleInfoFeignService;

    /**
     * 查询账户信息 todo zhuchangjian 确认下此接口是否有用
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
            WageMsgModelInfoDTO paramDto=new WageMsgModelInfoDTO();
            BeanUtils.copyProperties(msgModelInfoDTO,paramDto);
            MsgModelInfoDTO resDto=null;
            WageMsgModelInfoDTO msgModelInfo=msgModuleInfoFeignService.findByMsgModelInfo(paramDto);
            if (msgModelInfo!=null){
                resDto=new MsgModelInfoDTO();
                BeanUtils.copyProperties(msgModelInfo,resDto);
            }
            return  resDto;
        }).subscribeOn(Schedulers.elastic());
    }


}
