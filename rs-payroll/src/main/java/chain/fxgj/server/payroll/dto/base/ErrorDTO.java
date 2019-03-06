package chain.fxgj.server.payroll.dto.base;

import chain.css.exception.ErrorMsg;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 异常信息
 *
 * @author chain
 * create by chain on 2018/6/8 下午8:34
 **/

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDTO {
    /**
     * http status
     */
    int status = 500;
    /**
     * 错误码
     */
    String errorCode;
    /**
     * 错误信息
     */
    String errorMsg;

    /**
     * 时间戳
     */
    LocalDateTime timestamp;

    /**
     * 访问路径
     */
    String path;

    /**
     * Method
     */
    String method;

    public ErrorDTO(int status, String errorCode, String errorMsg, String path, String method) {
        this.status = status;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.path = path;
        this.method = method;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorDTO(int status, ErrorMsg errorMsg, String path, String method) {
        this(status, errorMsg.getErrCode(), errorMsg.getErrMsg(), path, method);
    }
}
