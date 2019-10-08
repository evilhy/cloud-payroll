package chain.fxgj.server.payroll.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@XmlRootElement(name = "PageResponse")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDTO<T> {
    /**
     * 分页列表
     */
    List<T> content;
    /**
     * 总页数
     */
    int totalPages;
    /**
     * 元素总数
     */
    long totalElements;
    /**
     * 当前页
     */
    int page;
    /**
     * 每页显示条
     */
    int size;
    /**
     * 当前时间
     */
    Long now;

    public PageResponseDTO(List<T> content, int totalPages, long totalElements, int page, int size) {
        this.content = content;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.page = page;
        this.size = size;
        this.now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
