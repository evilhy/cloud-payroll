package chain.fxgj.server.payroll.dto.testfile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 员工企业列表
 */
@XmlRootElement(name = "EmpEntResDTO")
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
public class EmpEntResDTOP {

    /**
     * 企业id
     */
    private String entId;

    /**
     * 企业全称
     */
    private String entName;

    /**
     * 企业简称
     */
    private String shortEntName;

}
