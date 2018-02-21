package com.parserbox.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappingInfo {

    /**
     * list of id and name pairs used in grid list
     */
    List<Map<Object, Object>> records = new ArrayList();

    /**
     * tells controller all is fine
     */
    boolean success = true;
    /**
     * totalCount used by grids such as extjs
     */
    int totalCount = 0;

    public MappingInfo() {}

    public void put(Object objid, Object name) {
        Map<Object, Object> m = new HashMap<Object,Object>();
        m.put("objid", objid);
        m.put("name", name);
        records.add(m);
        totalCount = records.size();
    }


    public List<Map<Object, Object>> getRecords() {
        return records;
    }

    public void setRecords(List<Map<Object, Object>> records) {
        this.records = records;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
