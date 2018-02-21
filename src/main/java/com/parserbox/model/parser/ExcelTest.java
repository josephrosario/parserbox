package com.parserbox.model.parser;

public class ExcelTest {


    /*
    public static void main(String[] args) {
        try {
            File f = new File("/home/Documents/sample_excel.xlsx");
            InputStream is = new FileInputStream(f);
            Workbook workbook = StreamingReader.builder()
                    .rowCacheSize(100)    // number of rows to keep in memory (defaults to 10)
                    .bufferSize(4096)     // buffer size to use when reading InputStream to file (defaults to 1024)
                    .open(is);            // InputStream or File for XLSX file (required)

            String v = "";

            for (int i = 0, len = workbook.getNumberOfSheets(); i < len; i++) {
                System.out.println("Sheet " + (i+1) + " : " + workbook.getSheetName(i));
            }


            for (Sheet sheet : workbook) {
                System.out.println(sheet.getSheetName());
                for (Row r : sheet) {
                    StringBuffer buffer = new StringBuffer(1024);
                    for (Cell c : r) {
                        CellType cellType =  c.getCellTypeEnum();
                        v = "";
                        if (cellType == CellType.STRING) {
                            v = c.getStringCellValue();
                        }
                        else
                        if (cellType == CellType.NUMERIC) {
                            if (DateUtil.isCellDateFormatted(c)) {
                                Date dt = c.getDateCellValue();
                                if (dt != null) {
                                    v = DateHelper.getSimpleDateStr(dt);
                                } else {
                                    v = "";
                                }
                            }
                        }
                        else if (cellType == CellType.BOOLEAN) {
                            v = "" + c.getBooleanCellValue();
                        }
                        else if (cellType == CellType.BLANK) {
                            v = "";
                        }

                        buffer.append(v).append(",  ");
                    }
                    System.out.println(buffer.toString());
                }
            }

        } catch (Exception e) {
            System.exit(0);
        } finally {
        }
    }
    */
}
