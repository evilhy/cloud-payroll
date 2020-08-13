package chain.fxgj.server.payroll.config.feign;

import chain.css.exception.ErrorMsg;
import chain.css.exception.ServiceHandleException;
import chain.utils.commons.JacksonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Map;

/**
 * @program: cloud-account
 * @description: feign 服务异常不进入熔断
 * @author: lius
 * @create: 2019/10/20 00:18
 */
@Configuration
public class NotBreakerConfiguration {
    @Bean
    public ErrorDecoder errorDecoder() {
        return new UserErrorDecoder();
    }

    /**
     * 自定义错误
     */
    public class UserErrorDecoder implements ErrorDecoder {
        private Logger logger = LoggerFactory.getLogger(getClass());
        @Autowired
        private ObjectMapper objectMapper;

        @Override
        public Exception decode(String methosdKey, Response response) {
            ServiceHandleException serviceHandleException = null;

            try {
                Map<String, String> content = objectMapper.readValue(response.body().asInputStream(),
                        new TypeReference<Map<String, String>>() {
                        });

                logger.info("===feign 服务异常==>content={}", JacksonUtil.objectToJson(content));

                Error error = JacksonUtil.jsonToBean(JacksonUtil.objectToJson(content), Error.class);
                ErrorMsg errorMsg = new ErrorMsg(error.getErrCode(), error.getErrMsg());
                serviceHandleException = new ServiceHandleException(errorMsg);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return serviceHandleException;
        }
    }
}
