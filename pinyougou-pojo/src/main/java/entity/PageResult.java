package entity;

import java.io.Serializable;
import java.util.List;

public class PageResult implements Serializable {

    private Long totalCount;
    private List total;


    public PageResult() {
    }

    public PageResult(Long totalCount, List total) {
        this.totalCount = totalCount;
        this.total = total;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public List getTotal() {
        return total;
    }

    public void setTotal(List total) {
        this.total = total;
    }
}
