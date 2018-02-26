/**
 * Created by zhanghaibiao on 2017/5/17.
 */
public class ExcelUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelUtil.class);

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 导出Excel文件
     */
    public static void exportExcel(String fileName, Map<String, String> exportMap, List<Object> listContent, HttpServletResponse response, boolean flag) {
        try {
            //定义输出流，以便打开保存对话框
            OutputStream os = response.getOutputStream();// 取得输出流
            response.reset();// 清空输出流
            response.setHeader("Content-disposition", "attachment; filename=" + new String(fileName.getBytes("GB2312"), "ISO8859-1"));
            // 设定输出文件头
            response.setContentType("application/msexcel");// 定义输出类型

            WritableWorkbook workbook = Workbook.createWorkbook(os);
            WritableSheet sheet = workbook.createSheet("Sheet1", 0);
            jxl.SheetSettings sheetset = sheet.getSettings();
            sheetset.setProtected(false);
            WritableFont NormalFont = new WritableFont(WritableFont.ARIAL, 10);
            WritableFont BoldFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
            // 用于标题居中
            WritableCellFormat wcf_center = new WritableCellFormat(BoldFont);
            wcf_center.setBorder(Border.ALL, BorderLineStyle.THIN); // 线条
            wcf_center.setVerticalAlignment(VerticalAlignment.CENTRE); // 文字垂直对齐
            wcf_center.setAlignment(Alignment.CENTRE); // 文字水平对齐
            wcf_center.setWrap(false); // 文字是否换行

            // 用于正文居左
            WritableCellFormat wcf_left = new WritableCellFormat(NormalFont);
            wcf_left.setBorder(Border.NONE, BorderLineStyle.THIN); // 线条
            wcf_left.setVerticalAlignment(VerticalAlignment.CENTRE); // 文字垂直对齐
            wcf_left.setAlignment(Alignment.LEFT); // 文字水平对齐
            wcf_left.setWrap(false); // 文字是否换行

            Object[] columArr = exportMap.keySet().toArray();
            Object[] filedNameArr = exportMap.values().toArray();
            if (flag) {
                sheet.addCell(new Label(0, 0, "序号", wcf_center));
            }
            for (int i = 0; i < columArr.length; i++) {
                if (flag) {
                    sheet.addCell(new Label(i + 1, 0, columArr[i].toString(), wcf_center));
                } else {
                    sheet.addCell(new Label(i, 0, columArr[i].toString(), wcf_center));
                }
            }
            int i = 1;
            if (listContent != null) {

                for (Object obj : listContent) {
                    int j = 0;
                    String fieldNameTemp = null;
                    Object value = null;
                    Method method = null;
                    Class methodTypeClass = null;
                    sheet.addCell(new Label(j, i, String.valueOf(i), wcf_left));
                    j++;
                    for (Object fieldName : filedNameArr) {
                        fieldNameTemp = "get" + fieldName.toString().substring(0, 1).toUpperCase()
                                + fieldName.toString().substring(1);
                        method = obj.getClass().getMethod(fieldNameTemp, null);
                        methodTypeClass = method.getReturnType();
                        //时间格式转换
                        if (Date.class.getSimpleName().equals(methodTypeClass.getSimpleName())) {
                            Date date = (Date) method.invoke(obj, null);
                            value = date == null ? null : dateFormat.format(date);
                        } else {
                            value = method.invoke(obj, null);
                        }
                        if (value == null) {
                            value = "";
                        }
                        sheet.addCell(new Label(j, i, value.toString(), wcf_left));
                        j++;
                    }
                    i++;
                }
            }
            workbook.write();
            workbook.close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }

}
