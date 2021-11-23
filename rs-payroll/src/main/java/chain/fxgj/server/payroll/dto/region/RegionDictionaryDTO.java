package chain.fxgj.server.payroll.dto.region;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * @Description:地区数据字典
 * @Author: du
 * @Date: 2020/12/3 17:29
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegionDictionaryDTO {

    /**
     * 地区行政编号
     */
    private String regionCode;

    /**
     * 地区名称
     */
    private String regionName;

    /**
     * 上级地区编号
     */
    private String regionParentCode;

    /**
     * 上级地区名称
     */
    private String regionParentName;

    /**
     * 地区级别 code
     */
    private Integer regionLevel;

    /**
     * 地区级别 code
     */
    private String regionLevelVal;

    /**
     * 省市区码(设计初衷：导航定位(六位行政区码))
     */
    private String provinceCityCode;

    /**
     * 经度
     */
    private String longitude;

    /**
     * 纬度
     */
    private String latitude;
}
