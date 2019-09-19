package chain.fxgj.core.common.config;

import chain.css.exception.ErrorMsg;
import chain.css.exception.ParamsIllegalException;
import chain.fxgj.core.common.constant.ErrorConstant;
import chain.utils.commons.JacksonUtil;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.springframework.context.annotation.Configuration;
import reactor.core.Exceptions;
import java.io.IOException;
import static feign.FeignException.errorStatus;

@Configuration
@Slf4j
public class FeignClientErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        log.info("feign client response:{}", response);
        String body = null;
        try {
            body = Util.toString(response.body().asReader());
        } catch (IOException e) {
            log.error("feign.IOException:{}", e);
        }
        log.info("body:{}",body);
        if (response.status() >= 400 && response.status() <= 500) {
            JSONObject object=JSONObject.fromObject(body);
            String message=object.getString("message");
            log.info("message:{}",body);
            throw new ParamsIllegalException(new ErrorMsg("r008", message));
        }
        return errorStatus(methodKey, response);
    }
}
