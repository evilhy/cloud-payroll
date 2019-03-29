package chain.fxgj.server.payroll.dto.advertising;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 广告位轮播图
 */
@XmlRootElement(name = "AdvertisingRotationDTO")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class AdvertisingRotationDTO {


    /**
     * 图片url
     */
    private String url;

    /**
     * 图片超链接
     */
    private String link;

    /**
     * 轮播序号(0 - ...
     */
    private Integer sort;

    /**
     * 发布状态(0已发布、1未发布、2已下架)
     */
    private Integer releaseStatus;

    /**
     * 发布状态描述(0已发布、1未发布、2已下架)
     */
    private String releaseStatusDesc;
}
