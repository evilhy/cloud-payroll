package chain.fxgj.server.payroll.dto;

import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageDTO<T> {
    /**
     * 当前页码
     */
    private int pageNum;

    /**
     * 每页条数
     */
    private int pageSize;

    /**
     * 总条数
     */
    private long totalElements;

    /**
     * 总页数
     */
    private int totalPages;

    /**
     * 是否最后一页
     */
    private boolean last;

    /**
     * 是否第一页
     */
    private boolean first;

    /**
     * 分页列表
     */
    private List<T> content;

    /**
     * 当前自己排位
     */
    private Long rankNo = 0L;

    /**
     * 总和
     */
    private String sum;

    /**
     * 总条数-类型
     */
    private String tjNum;

    /**
     * 总和-类型
     */
    private String tjSum;

    /**
     * Instantiates a new Page dto.
     *
     * @param page the page
     */
    public PageDTO(@NotNull Page<T> page) {
        this.content = page.getContent();
        this.pageNum = page.getNumber() + 1;
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.last = page.isLast();
        this.first = page.isFirst();
    }


    public PageDTO(@NotNull Page<T> page, String sum) {
        this.content = page.getContent();
        this.pageNum = page.getNumber() + 1;
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.last = page.isLast();
        this.first = page.isFirst();
        this.sum = sum;
    }

    public PageDTO(@NotNull Page<T> page, String sum, String tjNum, String tjSum) {
        this.content = page.getContent();
        this.pageNum = page.getNumber() + 1;
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.last = page.isLast();
        this.first = page.isFirst();
        this.sum = sum;
        this.tjNum = tjNum;
        this.tjSum = tjSum;
    }

    public PageDTO(int pageNum, int pageSize, long totalElements, int totalPages, boolean last, boolean first, List<T> content) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.last = last;
        this.first = first;
        this.content = content;
    }

    public PageDTO(List<T> content, Pageable pageable, long totalElements) {
        this.content = content;
        this.totalElements = totalElements;
        this.pageNum = pageable.getPageNumber();
        this.pageSize = pageable.getPageSize();
        this.totalPages = (int) Math.ceil(totalElements / pageSize);

    }
}
