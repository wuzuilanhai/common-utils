package com.biubiu;

import com.alibaba.fastjson.JSONArray;

/**
 * Created by Haibiao.Zhang on 2018/10/19.
 */
public class RptExcelDomain {

    private String sheetName;

    private String sheetTitle;

    private int sheetTitleHeight;

    private JSONArray sheetData;

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public String getSheetTitle() {
        return sheetTitle;
    }

    public void setSheetTitle(String sheetTitle) {
        this.sheetTitle = sheetTitle;
    }

    public int getSheetTitleHeight() {
        return sheetTitleHeight;
    }

    public void setSheetTitleHeight(int sheetTitleHeight) {
        this.sheetTitleHeight = sheetTitleHeight;
    }

    public JSONArray getSheetData() {
        return sheetData;
    }

    public void setSheetData(JSONArray sheetData) {
        this.sheetData = sheetData;
    }

}
