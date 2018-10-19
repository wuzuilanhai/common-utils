package com.biubiu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Haibiao.Zhang on 2018/10/19.
 */
public class InventoryTendencyExportUtil {

    private HSSFWorkbook wb = new HSSFWorkbook();

    private Object[] joHeads1 = null;

    private Object[] joHeads2 = null;

    public void export(HttpServletResponse response, String json) {
        JSONObject jo = JSON.parseObject(json);

        String title = "库存变动趋势分析表";
        RptExcelDomain rptDo = new RptExcelDomain();
        rptDo.setSheetName(title);
        rptDo.setSheetTitle(title);

        //绘制表头
        joHeads1 = jo.getJSONArray("head1").toArray();
        joHeads2 = jo.getJSONArray("head2").toArray();
        //绘制表格内容行
        JSONArray jaData;

        JSONArray joData = jo.getJSONArray("data");
        jaData = setRowData(joData);
        rptDo.setSheetData(jaData);

        writeExcelSheetSelf(rptDo);

        try {
            writeExcel(response);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void writeExcel(HttpServletResponse response) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            //将转换成的Workbook对象通过流形式下载
            wb.write(os);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] content = os.toByteArray();
        InputStream is = new ByteArrayInputStream(content);
        // 设置response参数，可以打开下载页面
        response.reset();
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + new String(("库存变动趋势分析表.xls").getBytes(), "iso-8859-1"));
        ServletOutputStream out = response.getOutputStream();
        try (BufferedInputStream bis = new BufferedInputStream(is); BufferedOutputStream bos = new BufferedOutputStream(out)) {
            byte[] buff = new byte[2048];
            int bytesRead;
            while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                bos.write(buff, 0, bytesRead);
            }
        }
    }


    /**
     * 构建数据excel结构
     *
     * @param joData x
     * @return JSONArray x
     */
    private JSONArray setRowData(JSONArray joData) {

        JSONArray result = new JSONArray();
        for (int i = 0; i < joData.size(); i++) {
            JSONObject jo = joData.getJSONObject(i);
            ArrayList<Object> arr = new ArrayList<>();

            arr.add(jo.getString("date"));

            Object[] data = jo.getJSONArray("jpData").toArray();
            Collections.addAll(arr, data);

            JSONObject rjo = new JSONObject();
            rjo.put("data", arr);
            result.add(rjo);
        }
        return result;
    }

    /**
     * 重载excel创建
     */
    private void writeExcelSheetSelf(RptExcelDomain rptDomain) {
        // 创建Excel的工作sheet,对应到一个excel文档的tab
        HSSFSheet sheet = wb.createSheet(rptDomain.getSheetName());
        // 设置excel每列宽度
        sheet.setColumnWidth(0, 5000);
        sheet.setColumnWidth(1, 3500);

        int colCount = joHeads2.length + 1;
        ArrayList<String> headers = new ArrayList<>();
        headers.add("时间");

        // 创建Excel的sheet的一行
        HSSFRow row = sheet.createRow(0);
        row.setHeight((short) rptDomain.getSheetTitleHeight());// 设定行的高度
        // 创建一个Excel的单元格
        HSSFCell cell_title = row.createCell(0);

        // 合并单元格(startRow，endRow，startColumn，endColumn)
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, colCount - 1));

        // 给Excel的单元格设置样式和赋值
        cell_title.setCellValue(rptDomain.getSheetTitle());

        /*---------------------------------------
         * 创建sheet的列名
         *--------------------------------------*/
        row = sheet.createRow(1);
        int baseL = headers.size();
        for (int i = 0; i < baseL; i++) {
            HSSFCell cell_header = row.createCell(i);
            // 给Excel的单元格设置样式和赋值
            sheet.addMergedRegion(new CellRangeAddress(1, 2, i, i));
            cell_header.setCellValue(headers.get(i));
        }
        HSSFCell cell_header;

        int offset1 = baseL;
        for (Object aJoHeads2 : joHeads1) {
            cell_header = row.createCell(offset1);
            int merge = 2;
            sheet.addMergedRegion(new CellRangeAddress(1, 1, offset1, offset1 + merge - 1));
            cell_header.setCellValue((String) aJoHeads2);

            offset1 = offset1 + merge;
        }


        row = sheet.createRow(2);
        for (int i = 0; i < joHeads2.length; i++) {
            cell_header = row.createCell(i + 1);
            cell_header.setCellValue((String) joHeads2[i]);
        }


        JSONArray data = rptDomain.getSheetData();

        for (int i = 0; i < data.size(); i++) {
            JSONObject rowData = data.getJSONObject(i);

            row = sheet.createRow(3 + i);

            Object[] _cellData = rowData.getJSONArray("data").toArray();
            for (int j = 0; j < _cellData.length; j++) {
                HSSFCell cell_Data = row.createCell(j);
                Object cellData = _cellData[j];
                if (cellData == null) continue;
                cell_Data.setCellValue(cellData.toString());

            }//完成行数据装载
        }
    }

}
