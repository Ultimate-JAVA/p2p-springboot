package com.gz.p2p.vo;

import java.io.Serializable;
import java.util.List;

/**
 * @Auther: 翟文海
 * @Date: 2022/5/11/011 22:42
 * @Description:
 */
public class PaginationVo<T> implements Serializable {

    private static final long serialVersionUID = -8864732882483829975L;
    private List<T> dates;
    private Integer totalSize;

    public PaginationVo() {
    }

    public PaginationVo(List<T> dates, Integer totalSize) {
        this.dates = dates;
        this.totalSize = totalSize;
    }

    public List<T> getDates() {
        return dates;
    }

    public void setDates(List<T> dates) {
        this.dates = dates;
    }

    public Integer getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Integer totalSize) {
        this.totalSize = totalSize;
    }
}
