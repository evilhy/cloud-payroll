package chain.fxgj.server.payroll.dto.nj;

import chain.fxgj.server.payroll.constant.DictEnums.IsStatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import core.dto.response.ent.EntIdGroupIdDTO;
import lombok.*;

import java.util.List;

/**
 * post 请求包装
 */
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
@Data
public class NjReqDTO {

    /**
     * 登录凭证
     */
    private String accessToken;

}
