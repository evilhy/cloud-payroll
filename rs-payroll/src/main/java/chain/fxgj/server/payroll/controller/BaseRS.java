package chain.fxgj.server.payroll.controller;

import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.server.payroll.config.ErrorConstant;
import chain.fxgj.server.payroll.dto.SelectListDTO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.security.PermitAll;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description:数据字典
 * @Author: du
 * @Date: 2021/7/15 10:12
 */
@RestController
@RequestMapping("/base")
@Slf4j
@SuppressWarnings("unchecked")
public class BaseRS {

    /**
     * 字典值
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}/dictItem")
    @TrackLog
    public Mono<List<SelectListDTO>> dictItem(@PathVariable("id") String id) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            Class enumClass = null;
            try {
                enumClass =
                        Class.forName("chain.utils.fxgj.constant.DictEnums." + id);
            } catch (ClassNotFoundException e) {
                throw new ParamsIllegalException(ErrorConstant.MISS_PARAM.getErrorMsg());
            }
            try {
                Method code = enumClass.getMethod("getCode");
                Method getDesc = enumClass.getMethod("getDesc");
                Object[] objs = enumClass.getEnumConstants();
                List<SelectListDTO> listDTOs = new ArrayList<>();
                for (Object obj : objs) {
                    SelectListDTO selectListDTO = new SelectListDTO();
                    selectListDTO.setCode((Integer) code.invoke(obj));
                    selectListDTO.setDesc((String) getDesc.invoke(obj));
                    listDTOs.add(selectListDTO);
                }
                return listDTOs;
            } catch (Exception e) {
                throw new ParamsIllegalException(ErrorConstant.MISS_PARAM.getErrorMsg());
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
