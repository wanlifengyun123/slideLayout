package com.example.chart;

import java.io.Serializable;
import java.util.List;

public class MultiGroupHistogramGroupData implements Serializable {
    private String groupName;
    private List<MultiGroupHistogramChildData> childDataList;

    public String getGroupName() {
        return groupName;
    }

    public List<MultiGroupHistogramChildData> getChildDataList() {
        return childDataList;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setChildDataList(List<MultiGroupHistogramChildData> childDataList) {
        this.childDataList = childDataList;
    }
}
