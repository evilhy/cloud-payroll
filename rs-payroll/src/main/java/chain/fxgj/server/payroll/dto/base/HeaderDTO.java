package chain.fxgj.server.payroll.dto.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * http header
 *
 * @author chain
 * create by chain on 2018/12/12 6:25 PM
 **/
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class HeaderDTO {
    /**
     * 登录用户jsessionId
     */
    String jsessionId;
    /**
     * ip地址
     */
    String remoteAddr;
    /**
     * 访问url
     */
    String requestUrl;
    /**
     * 访问 方法
     */
    String requestMethod;

    @Override
    public String toString() {
        return "HeaderDTO{" +
                "jsessionId='" + jsessionId + '\'' +
                ", remoteAddr='" + remoteAddr + '\'' +
                ", requestUrl='" + requestUrl + '\'' +
                ", requestMethod='" + requestMethod + '\'' +
                '}';
    }
}
