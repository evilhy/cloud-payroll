package chain.fxgj.server.payroll.dto.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * http header
 *
 * @author chain
 * create by chain on 2018/12/12 6:25 PM
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
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

    /**
     * 页码
     */
    @Builder.Default
    Integer pageNum = 1;
    /**
     * 每页显示行数 (默认30)
     */
    @Builder.Default
    Integer limit = 30;
    /**
     * 排序字段
     */
    String sortField;
    /**
     * 排序方式
     */
    @Builder.Default
    String direction = "DESC";
    /**
     * 渠道
     */
    Integer liquidation;
    /**
     * 版本
     */
    Integer version;
    /**
     * 子版本
     */
    Integer subVersion;

    @Override
    public String toString() {
        return "HeaderDTO{" +
                "jsessionId='" + jsessionId + '\'' +
                ", remoteAddr='" + remoteAddr + '\'' +
                ", requestUrl='" + requestUrl + '\'' +
                ", requestMethod='" + requestMethod + '\'' +
                ", pageNum='" + pageNum + '\'' +
                ", limit='" + limit + '\'' +
                ", sortField='" + sortField + '\'' +
                ", direction='" + direction + '\'' +
                ", liquidation='" + liquidation + '\'' +
                ", version='" + version + '\'' +
                ", subVersion='" + subVersion + '\'' +
                '}';
    }
}
