package com.hfocean.clinic.web.util;

import com.hfocean.clinic.order.service.ExportService;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by Haibiao.Zhang on 2018/8/14.
 */
public class ExcelDownLoadParams {

    /**
     * 查询参数
     */
    private Map<String, Object> params;

    /**
     * 查询service
     */
    private ExportService exportService;

    /**
     * 全部记录个数
     */
    private int allRowNumbers;

    /**
     * 返回实体
     */
    private Class clazz;

    /**
     * excel头部数组
     */
    private String[] assetHeadTemp;

    /**
     * excel字段数据
     */
    private String[] assetNameTemp;

    /**
     * 响应
     */
    private HttpServletResponse response;

    public Map<String, Object> getParams() {
        return params;
    }

    public ExcelDownLoadParams setParams(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    public ExportService getExportService() {
        return exportService;
    }

    public ExcelDownLoadParams setExportService(ExportService exportService) {
        this.exportService = exportService;
        return this;
    }

    public int getAllRowNumbers() {
        return allRowNumbers;
    }

    public ExcelDownLoadParams setAllRowNumbers(int allRowNumbers) {
        this.allRowNumbers = allRowNumbers;
        return this;
    }

    public Class getClazz() {
        return clazz;
    }

    public ExcelDownLoadParams setClazz(Class clazz) {
        this.clazz = clazz;
        return this;
    }

    public String[] getAssetHeadTemp() {
        return assetHeadTemp;
    }

    public ExcelDownLoadParams setAssetHeadTemp(String[] assetHeadTemp) {
        this.assetHeadTemp = assetHeadTemp;
        return this;
    }

    public String[] getAssetNameTemp() {
        return assetNameTemp;
    }

    public ExcelDownLoadParams setAssetNameTemp(String[] assetNameTemp) {
        this.assetNameTemp = assetNameTemp;
        return this;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public ExcelDownLoadParams setResponse(HttpServletResponse response) {
        this.response = response;
        return this;
    }

}
