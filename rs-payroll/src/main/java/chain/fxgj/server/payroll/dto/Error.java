package chain.fxgj.server.payroll.dto;

import chain.css.exception.ErrorMsg;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 错误对象
 *
 * @author lius
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Error {
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

    public Error(int status, String errorCode, String errorMsg, String path, String method) {
        this.status = status;
        this.errCode = errorCode;
        this.errMsg = errorMsg;
        this.path = path;
        this.method = method;
        this.timestamp = new Date();
    }

    public Error(int status, ErrorMsg errorMsg, String path, String method) {
        this(status, errorMsg.getErrCode(), errorMsg.getErrMsg(), path, method);
    }

}