package cn.gson.oasys.support;

import java.util.ArrayList;
import java.util.List;

public class Page<T> {

    private List<T> list = new ArrayList<>();

    private int pageNo = 0;
    private int pageSize = DEFAULT_PAGE_SIZE;
    private long totalCount = 0;
    private long todoCount = 0;

    public Page() {}

    public Page(int pageNo, int pageSize, long totalCount, List<T> list) {
        this.pageSize = pageSize;
        this.totalCount = totalCount;
        this.pageNo = pageNo;
        if (list != null) {
            this.list = list;
        }
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        int totalPage = getTotalPage();
        if (pageNo > totalPage) {
            this.pageNo = totalPage;
        } else if (pageNo < 1) {
            this.pageNo = 1;
        } else {
            this.pageNo = pageNo;
        }
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getTodoCount() {
        return todoCount;
    }

    public void setTodoCount(long todoCount) {
        this.todoCount = todoCount;
    }

    public int getPageStartIndex() {
        int index = (this.pageNo - 1) * pageSize;
        return index < 0 ? 0 : index;
    }

    public int getTotalPage() {
        return (int) Math.ceil((double) totalCount / pageSize);
    }

    public static final int DEFAULT_PAGE_SIZE = 10;
}