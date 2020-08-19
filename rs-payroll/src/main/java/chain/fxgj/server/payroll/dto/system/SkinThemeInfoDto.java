package chain.fxgj.server.payroll.dto.system;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SkinThemeInfoDto {

    /**
     * 皮肤名称
     */
    private String skinName;
    /**
     * 皮肤url;
     */
    private String skinUrl;
}
