package chain.fxgj.server.payroll.dto.base;

import chain.css.exception.ErrorMsg;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 异常信息
 *
 * @author chain
 * create by chain on 2018/6/8 下午8:34
 **/

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ErrorDTO {
    /**
     * http status
     */
    @Builder.Default
    int status = 500;

    /**
     * 错误码
     */
    @JsonProperty("error_code")
    String errCode;
    /**
     * 错误信息
     */
    @JsonProperty("error_msg")
    String errMsg;

    /**
     * 时间戳
     */
    Date timestamp;
    /**
     * 请求url
     */
    String path;

    /**
     * 请求方法
     */
    String method;

    public ErrorDTO(int status, String errorCode, String errorMsg, String path, String method) {
        this.status = status;
        this.errCode = errorCode;
        this.errMsg = errorMsg;
        this.path = path;
        this.method = method;
        this.timestamp = new Date();
    }

    public ErrorDTO(int status, ErrorMsg errorMsg, String path, String method) {
        this(status, errorMsg.getErrCode(), errorMsg.getErrMsg(), path, method);
    }

}