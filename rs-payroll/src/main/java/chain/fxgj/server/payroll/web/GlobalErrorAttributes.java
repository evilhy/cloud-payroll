package chain.fxgj.server.payroll.web;

import chain.css.exception.BusiVerifyException;
import chain.css.exception.ErrorMsg;
import chain.css.exception.ParamsIllegalException;
import chain.css.exception.ServiceHandleException;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.dto.base.ErrorDTO;
import chain.fxgj.server.payroll.exception.ForbiddenServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author chain
 * create by chain on 2019-01-30 17:47
 **/
@Component
@Slf4j
@Order(-2)
public class GlobalErrorAttributes extends DefaultErrorAttributes {
    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request,
                                                  boolean includeStackTrace) {
        return assembleError(request);
    }

    private Map<String, Object> assembleError(ServerRequest request) {
        Map<String, Object> errorAttributes = new LinkedHashMap<>();
        Throwable error = getError(request);
        ErrorDTO errorDTO = null;
        if (error instanceof IllegalArgumentException ||
                error instanceof HttpMessageNotReadableException) {
            log.error("IllegalArgumentException || HttpMessageNotReadableException :{}", error.getMessage(), error);
            errorDTO = new ErrorDTO(500, ErrorConstant.SYS_ERR.getErrorMsg(),
                    request.path(), request.methodName());
        } else if (error instanceof ParamsIllegalException) {
            log.warn("ParamsIllegalException :{}", error.getMessage());
            errorDTO = new ErrorDTO(400, ((ParamsIllegalException) error).getErrorMsg(),
                    request.path().toString(), request.methodName());
        } else if (error instanceof ServiceHandleException) {
            log.warn("ServiceHandleException :{}", error.getMessage());
            errorDTO = new ErrorDTO(400, ((ServiceHandleException) error).getErrorMsg(),
                    request.path().toString(), request.methodName());
        } else if (error instanceof BusiVerifyException) {
            log.warn("BusiVerifyException :{}", error.getMessage());
            errorDTO = new ErrorDTO(400, ((BusiVerifyException) error).getErrorMsg(),
                    request.path().toString(), request.methodName());
//        }else if (error instanceof FeignException) {
//            log.warn(error.getMessage());
//            errorDTO = JacksonUtil.jsonToBean(((FeignException) error).contentUTF8(),
//                    ErrorDTO.class);
        } else if (error instanceof ForbiddenServiceException) {
            log.warn("ForbiddenServiceException :{}", error.getMessage());
            ErrorMsg errorMsg = ((ForbiddenServiceException) error).getErrorMsg();
            errorDTO = new ErrorDTO(Integer.parseInt(errorMsg.getErrCode()), errorMsg,
                    request.path().toString(), request.methodName());
        } else if (error instanceof ConstraintViolationException) {
            log.warn("ConstraintViolationException :{}", error.getMessage());
            errorDTO = new ErrorDTO(400, "1400",
                    ((ConstraintViolationException) error).getConstraintViolations().iterator()
                            .next().getMessage(),
                    request.path(), request.methodName());
        } else if (error instanceof WebExchangeBindException) {
            log.warn("WebExchangeBindException :{}", error.getMessage());
            WebExchangeBindException e = (WebExchangeBindException) error;
            String msg = null;
            for (FieldError item : e.getBindingResult().getFieldErrors()) {
                if (msg == null) {
                    msg = ErrorConstant.MISS_PARAM.getErrorMsg().getErrMsg() + item.getField() +
                            " " +
                            item.getDefaultMessage();
                }
                log.warn("{}, {}, {}", item.getField(), item.getDefaultMessage(),
                        item.getRejectedValue());
            }
            errorDTO = new ErrorDTO(400, ErrorConstant.MISS_PARAM.getErrorMsg().getErrCode(), msg,
                    request.path(), request.methodName());
        } else if (error instanceof ResponseStatusException) {
            log.warn("ResponseStatusException :{}", error.getMessage());
            ResponseStatusException e = (ResponseStatusException) error;
            errorDTO = new ErrorDTO(e.getStatus().value(), "9999", e.getReason(), request.path(),
                    request.methodName());
        } else if (error instanceof WebApplicationException) {
            log.warn("WebApplicationException :{}", error.getMessage());
            Response response = ((WebApplicationException) error).getResponse();
            int status = response.getStatus();
            ErrorMsg errorMsg = (ErrorMsg) response.getEntity();
            errorDTO = new ErrorDTO(status, errorMsg.getErrCode(), errorMsg.getErrMsg(), request.path(),
                    request.methodName());

        } else {
            log.error(error.getMessage(), error);
            errorDTO = new ErrorDTO(500, ErrorConstant.SYS_ERR.getErrorMsg(),
                    request.path(), request.methodName());
        }
        errorAttributes.put("status", errorDTO.getStatus());
        errorAttributes.put("error_code", errorDTO.getErrCode());
        errorAttributes.put("error_msg", errorDTO.getErrMsg());
        errorAttributes.put("path", errorDTO.getPath());
        errorAttributes.put("method", errorDTO.getMethod());
//        errorAttributes.put("timestamp", errorDTO.getTimestamp());
        return errorAttributes;
    }
}
