package com.hfocean.clinic.web.util;

import com.hfocean.clinic.common.util.CustomBeanUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by Haibiao.Zhang on 2018/8/13.
 */
public class ExcelUtil {

    private static final String WINDOW_PATH = "E:/tmp/";

    private static final String LINUX_PATH = "/tmp/";

    private static final String PATTERN = "yyyyMMddHHmmss";

    private static final String EXCEL_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private static final String START_NUM = "startNum";

    private static final String END_NUM = "endNum";

    private static final String FILE_NAME = "订单报表";

    private static final String XLSX = ".xlsx";

    private static final String ZIP = ".zip";

    private static final int MAX_ROW_COUNT = 10000;

    private static final int ROW_MEMORY = 100;

    /**
     * 导出excel
     *
     * @param excelDownLoadParams 下载参数
     * @throws Exception 异常
     */
    public static void excelDownLoad(ExcelDownLoadParams excelDownLoadParams) throws Exception {
        Map<String, Object> params = excelDownLoadParams.getParams();
        int allRowNumbers = excelDownLoadParams.getAllRowNumbers();
        Class clazz = excelDownLoadParams.getClazz();
        String[] assetHeadTemp = excelDownLoadParams.getAssetHeadTemp();
        String[] assetNameTemp = excelDownLoadParams.getAssetNameTemp();
        HttpServletResponse response = excelDownLoadParams.getResponse();

        //设置相应头
        setHeader(response);

        //设置批次文件名
        List<String> fileNames = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN);
        String filePath;
        if (isWindows()) {
            filePath = WINDOW_PATH;
        } else {
            filePath = LINUX_PATH;
        }
        if (!new File(filePath).exists()) {
            new File(filePath).mkdirs();
        }
        String fileEnd = sdf.format(new Date());
        String fileName = FILE_NAME + fileEnd;
        File zip = new File(filePath + fileName + ZIP);//压缩文件路径

        if (allRowNumbers > MAX_ROW_COUNT) {
            //分批次生成excel
            int count = allRowNumbers / MAX_ROW_COUNT;
            int tempSize = (allRowNumbers % MAX_ROW_COUNT) == 0 ? count : count + 1;
            for (int i = 0; i < tempSize; i++) {
                if (i == (allRowNumbers / MAX_ROW_COUNT)) {
                    params.put(START_NUM, i * MAX_ROW_COUNT);
                    params.put(END_NUM, MAX_ROW_COUNT);
                } else {
                    params.put(START_NUM, i * MAX_ROW_COUNT);
                    params.put(END_NUM, MAX_ROW_COUNT);
                }
                List result = excelDownLoadParams.getExportService().queryResultByMap(params);
                List<Map> listMap = CustomBeanUtil.listBean2listMap(result, clazz);

                String tempExcelFile = filePath + fileName + "[" + (i + 1) + "]" + XLSX;
                File tempFile = new File(tempExcelFile);
                if (!tempFile.exists()) {
                    tempFile.createNewFile();
                }
                fileNames.add(tempExcelFile);
                FileOutputStream fos = new FileOutputStream(tempExcelFile);
                SXSSFWorkbook wb = new SXSSFWorkbook(ROW_MEMORY);
                doCreateWb(wb, listMap, fos, assetHeadTemp, assetNameTemp);
            }

            //导出zip压缩文件
            exportZip(response, fileNames, zip);
        } else {
            params.put(START_NUM, 0);
            params.put(END_NUM, MAX_ROW_COUNT);
            List result = excelDownLoadParams.getExportService().queryResultByMap(params);
            List<Map> listMap = CustomBeanUtil.listBean2listMap(result, clazz);

            String tempExcelFile = filePath + fileName + XLSX;
            File tempFile = new File(tempExcelFile);
            if (!tempFile.exists()) {
                tempFile.createNewFile();
            }
            fileNames.add(tempExcelFile);
            FileOutputStream fos = new FileOutputStream(tempExcelFile);
            SXSSFWorkbook wb = new SXSSFWorkbook(ROW_MEMORY);
            doCreateWb(wb, listMap, fos, assetHeadTemp, assetNameTemp);
            exportZip(response, fileNames, zip);
        }
    }

    private static boolean isWindows() {
        return System.getProperties().getProperty("os.name").toUpperCase().contains("WINDOWS");
    }

    private static void doCreateWb(SXSSFWorkbook wb, List<Map> listMap, FileOutputStream fos, String[] assetHeadTemp, String[] assetNameTemp) throws Exception {
        try {
            wb = exportDataToExcelXLSX(wb, listMap, assetHeadTemp, assetNameTemp);
            wb.write(fos);
            fos.flush();
        } catch (RuntimeException e) {
            throw new Exception(e);
        } finally {
            fos.flush();
            fos.close();
            listMap.clear();
        }
    }

    private static SXSSFWorkbook exportDataToExcelXLSX(SXSSFWorkbook wb, List<Map> listMap, String[] assetHeadTemp, String[] assetNameTemp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(EXCEL_PATTERN);
        CellStyle columnHeadStyle = wb.createCellStyle();
        columnHeadStyle.setWrapText(true);
        Font f = wb.createFont();// 字体
        f.setFontHeightInPoints((short) 9);// 字号
        columnHeadStyle.setFont(f);
        Sheet sheet = wb.createSheet("sheet");
        Row row = sheet.createRow(0);
        Cell cell;
        sheet.createFreezePane(0, 1, 0, 1);
        for (int i = 0; i < assetHeadTemp.length; i++) {
            cell = row.createCell(i);
            cell.setCellStyle(columnHeadStyle);
            cell.setCellValue(assetHeadTemp[i]);
            sheet.setColumnWidth(i, 3000);
        }
        if (listMap != null && listMap.size() > 0) {
            int rowIndex = 1;
            Object value;
            for (Map map : listMap) {
                row = sheet.createRow(rowIndex++);
                int index = 0;
                for (String anAssetNameTemp : assetNameTemp) {
                    cell = row.createCell(index++);
                    value = map.get(anAssetNameTemp);
                    if (value instanceof Date) {
                        cell.setCellValue(dateFormat.format(value));
                    } else {
                        cell.setCellValue(value != null ? value.toString() : "");
                    }
                }
            }
        }
        return wb;
    }

    private static void setHeader(HttpServletResponse response) throws UnsupportedEncodingException {
        String filename = FILE_NAME + ZIP;
        filename = new String(filename.getBytes("GBK"), "iso-8859-1");
        response.reset();
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + filename);
        response.addHeader("pargam", "no-cache");
        response.addHeader("Cache-Control", "no-cache");
    }

    private static void exportZip(HttpServletResponse response, List<String> fileNames, File zip) throws IOException {
        OutputStream outPut = response.getOutputStream();

        //1.压缩文件
        File[] srcFile = new File[fileNames.size()];
        for (int i = 0; i < fileNames.size(); i++) {
            srcFile[i] = new File(fileNames.get(i));
        }
        byte[] byt = new byte[1024];
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip), Charset.forName("UTF-8"));
        for (File aSrcFile : srcFile) {
            FileInputStream in = new FileInputStream(aSrcFile);
            out.putNextEntry(new ZipEntry(aSrcFile.getName()));
            int length;
            while ((length = in.read(byt)) > 0) {
                out.write(byt, 0, length);
            }
            out.closeEntry();
            in.close();
        }
        out.close();

        //2.删除服务器上的临时文件(excel)
        for (File temFile : srcFile) {
            if (temFile.exists() && temFile.isFile()) {
                temFile.delete();
            }
        }

        //3.返回客户端压缩文件
        FileInputStream inStream = new FileInputStream(zip);
        byte[] buf = new byte[4096];
        int readLength;
        while ((readLength = inStream.read(buf)) != -1) {
            outPut.write(buf, 0, readLength);
        }
        inStream.close();
        outPut.close();

        //4.删除压缩文件
        if (zip.exists() && zip.isFile()) {
            zip.delete();
        }
    }


}
