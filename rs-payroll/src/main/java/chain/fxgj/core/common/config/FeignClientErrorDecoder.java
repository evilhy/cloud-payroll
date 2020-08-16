package chain.fxgj.core.common.config;

import chain.css.exception.ErrorMsg;
import chain.css.exception.ParamsIllegalException;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

import java.io.IOException;

import static feign.FeignException.errorStatus;

//@Configuration
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
        if (response.status() >= 400 && response.status() <= 500) {
            JSONObject object=JSONObject.fromObject(body);
            String message="";
            if (object.containsKey("message")) {
                message = object.getString("message");
            } else if (object.containsKey("error_msg")) {
                message = object.getString("error_msg");
            }
            //r010-银行卡已存在
            if (message.contains("-")){
                if (message.split("-").length>0){
                    message=message.split("-")[1];
                }
            }
            log.info("message:{}",message);
            throw new ParamsIllegalException(new ErrorMsg("r008", message));
        }
        return errorStatus(methodKey, response);
    }

    public static void main(String[] args) {
        String str="r010-银行卡已存在";
        System.out.println(str.split("-")[1]);
    }
}
