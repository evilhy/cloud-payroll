package chain.fxgj.server.payroll.dto.testfile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * 员工企业查询
 */
@XmlRootElement(name = "IndexEmpEntDTO")
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
public class IndexEmpEntDTOP {

    /**
     * 员工企业列表(未排序)
     */
    private List<EmpEntResDTOP> entList;

    /**
     * 最近代发企业
     */
    private EmpEntResDTOP empEntDTO;


}
