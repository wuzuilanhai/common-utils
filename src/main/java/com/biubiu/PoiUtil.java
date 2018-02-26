public class PoiUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PoiUtil.class);

    /**
     * 读取excel文件，创建表格实例
     *
     * @param sheet    excel文件
     * @param inStream 输入流
     */
    private static Sheet loadExcel(Sheet sheet, FileInputStream inStream) {
        try {
            Workbook workBook = WorkbookFactory.create(inStream);
            sheet = workBook.getSheetAt(0);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
                e.printStackTrace();
            }
            return sheet;
        }
    }

    /**
     * 获取单元格的值
     *
     * @param cell 单元格
     * @return 单元格的值
     */
    private static String getCellValue(Cell cell) {
        String cellValue = "";
        DataFormatter formatter = new DataFormatter();
        if (cell != null) {
            //判断单元格数据的类型，不同类型调用不同的方法
            switch (cell.getCellType()) {
                //数值类型
                case Cell.CELL_TYPE_NUMERIC:
                    //进一步判断 ，单元格格式是日期格式
                    if (DateUtil.isCellDateFormatted(cell)) {
                        cellValue = formatter.formatCellValue(cell);
                    } else {
                        //数值
                        double value = cell.getNumericCellValue();
                        int intValue = (int) value;
                        cellValue = value - intValue == 0 ? String.valueOf(intValue) : String.valueOf(value);
                        if (("" + cellValue).indexOf("E") != -1 || ("" + cellValue).indexOf("e") != -1 || ("" + cellValue).indexOf("+") != -1) {
                            BigDecimal bd = new BigDecimal("" + cellValue);
                            cellValue = bd.toString();
                        } else {
                            cellValue = "" + cellValue;
                        }
                    }
                    break;
                case Cell.CELL_TYPE_STRING:
                    cellValue = cell.getStringCellValue();
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    cellValue = String.valueOf(cell.getBooleanCellValue());
                    break;
                //判断单元格是公式格式，需要做一种特殊处理来得到相应的值
                case Cell.CELL_TYPE_FORMULA:
                    try {
                        cellValue = String.valueOf(cell.getNumericCellValue());
                    } catch (IllegalStateException e) {
                        cellValue = String.valueOf(cell.getRichStringCellValue());
                    }
                    break;
                case Cell.CELL_TYPE_BLANK:
                    cellValue = "";
                    break;
                case Cell.CELL_TYPE_ERROR:
                    cellValue = "";
                    break;
                default:
                    cellValue = cell.toString().trim();
                    break;
            }
        }
        return cellValue.trim();
    }

    /**
     * 初始化表格中的每一行，并得到每一个单元格的值
     *
     * @param sheet excel文件
     * @return 解析结果
     */
    private static Map<String, Object> init(Sheet sheet) {
        Map<String, Object> resultMap = Maps.newHashMap();
        if (!correctScheme(sheet)) {
            resultMap.put(ExcelConstants.INCORRECT_SCHEME.getKey(), ExcelConstants.INCORRECT_SCHEME.getValue());
            return resultMap;
        }
        List spiders = Lists.newArrayList();
        Set<String> codes = Sets.newHashSet();
        Map<String, Integer> codeAndIndex = Maps.newHashMap();
        int rowNum = sheet.getLastRowNum() + 1;
        for (int i = 1; i < rowNum; i++) {
            Row row = sheet.getRow(i);
            String code = getCellValue(row.getCell(1));
            if (!notChinnese(code)) {
                resultMap.put(ExcelConstants.CHINNESE_CODE.getKey(), String.format(ExcelConstants.CHINNESE_CODE.getValue().toString(), i + 1));
                break;
            }
            if (codes.contains(code)) {
                int index = codeAndIndex.get(code);
                resultMap.put(ExcelConstants.DUPLICATED_CODE.getKey(), String.format(ExcelConstants.DUPLICATED_CODE.getValue().toString(), index + 1, i + 1));
                break;
            }
            codes.add(code);
            codeAndIndex.put(code, i);
            String frequency = getCellValue(row.getCell(3));
            String hour = getCellValue(row.getCell(4));
            String cron = CronConstants.getCron(frequency);
            if (CronConstants.DAY.getTime().equals(frequency)) {
                cron = String.format(cron, hour);
            }
            ImportSpider importSpider = ImportSpider.builder()
                    .name(getCellValue(row.getCell(0)))
                    .code(code)
                    .startUrl(getCellValue(row.getCell(2)))
                    .frequency(cron)
                    .hour(hour)
                    .nextXPath(getCellValue(row.getCell(5)))
                    .pageType(getCellValue(row.getCell(6)))
                    .endXPath(getCellValue(row.getCell(7)))
                    .titleXPath(getCellValue(row.getCell(8)))
                    .summaryXPath(getCellValue(row.getCell(9)))
                    .authorXPath(getCellValue(row.getCell(10)))
                    .contentXPath(getCellValue(row.getCell(11)))
                    .build();
            spiders.add(importSpider);
        }
        resultMap.put(ExcelConstants.CODE_SET.getKey(), codes);
        resultMap.put(ExcelConstants.SUCCESS_LOAD.getKey(), spiders);
        return resultMap;
    }

    /**
     * 判断excel文件格式
     *
     * @param sheet excel文件
     * @return 正确与否
     */
    private static boolean correctScheme(Sheet sheet) {
        Row row = sheet.getRow(0);
        int num = row.getLastCellNum();
        if (num != SpiderExcelConstants.getKeyListSize()) {
            return false;
        }
        boolean correct = true;
        for (int i = 0; i < num; i++) {
            Cell cell = row.getCell(i);
            String title = getCellValue(cell);
            if (Strings.isNullOrEmpty(title)) {
                correct = false;
                break;
            }
            if (!title.equals(SpiderExcelConstants.getKeyList().get(i))) {
                correct = false;
                break;
            }
        }
        return correct;
    }

    /**
     * 解析excel数据
     *
     * @param stream 输入流
     * @return 解析结果
     */
    public static Map<String, Object> parseExcel(FileInputStream stream) {
        return init(loadExcel(null, stream));
    }

    private static boolean notChinnese(String str) {
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]*");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

}
