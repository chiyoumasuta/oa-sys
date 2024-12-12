package cn.gson.oasys.support;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.poi.xssf.usermodel.*;

public class ExcelUtil {

    /*默认每列至少字节数(列宽)*/
    public static int DEFAULT_COLOUMN_WIDTH = 20;

    /**
     * 导出Excel
     *
     * @param title 大标题(若为空则没有首行大标题)
     * @param heads 表格列头名称
     * @param rows  表格数据
     * @param out
     * @throws Exception
     */
    public static void download(String title, String[] heads, List<String[]> rows, OutputStream out) {
        // 创建一个工作簿
        XSSFWorkbook wb = new XSSFWorkbook();
        // 创建一张表
        Sheet sheet = wb.createSheet();
        // 列头样式
        XSSFCellStyle headerStyle = wb.createCellStyle();
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        XSSFFont headerFont = wb.createFont();
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setFontName("宋体");
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        // 单元格样式
        XSSFCellStyle cellStyle = wb.createCellStyle();
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        XSSFFont cellFont = wb.createFont();
        cellFont.setFontName("宋体");
        cellFont.setFontHeightInPoints((short) 11);
        cellStyle.setFont(cellFont);
        cellStyle.setWrapText(true);

        //设置列宽
        int[] arrColWidth = new int[heads.length];
        for (int j = 0; j < heads.length; j++) {
            String headName = heads[j];
            int bytes = headName.getBytes().length;
            arrColWidth[j] = bytes < DEFAULT_COLOUMN_WIDTH ? DEFAULT_COLOUMN_WIDTH : bytes;
            sheet.setColumnWidth(j, arrColWidth[j] * 300);
        }

        int rowIndex;
        if (StringUtils.isNotBlank(title)) {
            XSSFCellStyle titleStyle = wb.createCellStyle(); //标题样式
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            Font ztFont = wb.createFont();
            ztFont.setFontHeightInPoints((short) 20);    // 字体大小
            ztFont.setFontName("宋体");             // 将“宋体”字体应用到当前单元格上
            ztFont.setBold(true);  //加粗
            titleStyle.setFont(ztFont);
            Row firstTitle = sheet.createRow(0);
            firstTitle.setHeightInPoints(40); //行高
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, heads.length - 1)); //合并大标题占据的列宽
            Cell cell = firstTitle.createCell(0);
            cell.setCellValue(title);
            cell.setCellStyle(titleStyle);

            // 遍历集合数据，产生数据行
            rowIndex = 1;
        } else {
            // 遍历集合数据，产生数据行
            rowIndex = 0;
        }

        sheet.autoSizeColumn(0);

        if (rows.isEmpty()) {//空数据,只下载表头
            Row headerRow = sheet.createRow(1); //表头 rowIndex =0
            headerRow.setHeightInPoints(20); //行高
            for (int i = 0; i < heads.length; i++) {
                headerRow.createCell(i).setCellValue(heads[i]);
                headerRow.getCell(i).setCellStyle(headerStyle);
            }
        }
        //HSSF生成的Excel 97(.xls)格式每个sheet页不能超过65536条数据
        for (String[] row : rows) {
            if (StringUtils.isNotBlank(title)) {
                if (rowIndex == 65535 || rowIndex == 1) {
                    if (rowIndex != 1) {
                        sheet = wb.createSheet();//如果数据超过了，则在第二页显示
                        for (int j = 0; j < heads.length; j++) {
                            String headName = heads[j];
                            int bytes = headName.getBytes().length;
                            arrColWidth[j] = bytes < DEFAULT_COLOUMN_WIDTH ? DEFAULT_COLOUMN_WIDTH : bytes;
                            sheet.setColumnWidth(j, arrColWidth[j] * 300);
                        }
                    }
                    Row headerRow = sheet.createRow(1); //表头 rowIndex =1
                    headerRow.setHeightInPoints(20);
                    for (int i = 0; i < heads.length; i++) {
                        headerRow.createCell(i).setCellValue(heads[i]);
                        headerRow.getCell(i).setCellStyle(headerStyle);
                    }
                    rowIndex = 2;//数据内容从 rowIndex=1开始
                }
            } else {
                if (rowIndex == 65535 || rowIndex == 0) {
                    if (rowIndex != 0) {
                        sheet = wb.createSheet();//如果数据超过了，则在第二页显示
                        for (int j = 0; j < heads.length; j++) {
                            String headName = heads[j];
                            int bytes = headName.getBytes().length;
                            arrColWidth[j] = bytes < DEFAULT_COLOUMN_WIDTH ? DEFAULT_COLOUMN_WIDTH : bytes;
                            sheet.setColumnWidth(j, arrColWidth[j] * 300);
                        }
                    }
                    Row headerRow = sheet.createRow(0); //表头 rowIndex =0
                    headerRow.setHeightInPoints(20);
                    for (int i = 0; i < heads.length; i++) {
                        headerRow.createCell(i).setCellValue(heads[i]);
                        headerRow.getCell(i).setCellStyle(headerStyle);
                    }
                    rowIndex = 1;//数据内容从 rowIndex=1开始
                }
            }
            Row dataRow = sheet.createRow(rowIndex);
            dataRow.setHeightInPoints(20);
            for (int i = 0; i < heads.length; i++) {//填充此行的每列数据
                Cell dataCell = dataRow.createCell(i);
                dataCell.setCellValue(row[i]);
                dataCell.setCellStyle(cellStyle);
            }
            rowIndex++;
        }

        try {
            // 文件流
            wb.write(out);
            wb.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            throw new RuntimeException("导出失败");
        }
    }

    /**
     * 导出Excel
     *
     * @param title 大标题(若为空则没有首行大标题)
     * @param heads 表格列头名称
     * @param rows  表格数据
     * @param out
     * @throws Exception
     */
    public static XSSFWorkbook download(String title, String[] heads, List<String[]> rows) {
        // 创建一个工作簿
        XSSFWorkbook wb = new XSSFWorkbook();
        // 创建一张表
        Sheet sheet = wb.createSheet();
        // 列头样式
        XSSFCellStyle headerStyle = wb.createCellStyle();
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        XSSFFont headerFont = wb.createFont();
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setFontName("宋体");
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        // 单元格样式
        XSSFCellStyle cellStyle = wb.createCellStyle();
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        XSSFFont cellFont = wb.createFont();
        cellFont.setFontName("宋体");
        cellFont.setFontHeightInPoints((short) 11);
        cellStyle.setFont(cellFont);

        //设置列宽
        int[] arrColWidth = new int[heads.length];
        for (int j = 0; j < heads.length; j++) {
            String headName = heads[j];
            int bytes = headName.getBytes().length;
            arrColWidth[j] = bytes < DEFAULT_COLOUMN_WIDTH ? DEFAULT_COLOUMN_WIDTH : bytes;
            sheet.setColumnWidth(j, arrColWidth[j] * 300);
        }

        int rowIndex;
        if (StringUtils.isNotBlank(title)) {
            XSSFCellStyle titleStyle = wb.createCellStyle(); //标题样式
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            Font ztFont = wb.createFont();
            ztFont.setFontHeightInPoints((short) 20);    // 字体大小
            ztFont.setFontName("宋体");             // 将“宋体”字体应用到当前单元格上
            ztFont.setBold(true);  //加粗
            titleStyle.setFont(ztFont);
            Row firstTitle = sheet.createRow(0);
            firstTitle.setHeightInPoints(40); //行高
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, heads.length - 1)); //合并大标题占据的列宽
            Cell cell = firstTitle.createCell(0);
            cell.setCellValue(title);
            cell.setCellStyle(titleStyle);

            // 遍历集合数据，产生数据行
            rowIndex = 1;
        } else {
            // 遍历集合数据，产生数据行
            rowIndex = 0;
        }

        if (rows.isEmpty()) {//空数据,只下载表头
            Row headerRow = sheet.createRow(1); //表头 rowIndex =0
            headerRow.setHeightInPoints(20); //行高
            for (int i = 0; i < heads.length; i++) {
                headerRow.createCell(i).setCellValue(heads[i]);
                headerRow.getCell(i).setCellStyle(headerStyle);
            }
        }
        //HSSF生成的Excel 97(.xls)格式每个sheet页不能超过65536条数据
        for (String[] row : rows) {
            if (StringUtils.isNotBlank(title)) {
                if (rowIndex == 65535 || rowIndex == 1) {
                    if (rowIndex != 1) {
                        sheet = wb.createSheet();//如果数据超过了，则在第二页显示
                        for (int j = 0; j < heads.length; j++) {
                            String headName = heads[j];
                            int bytes = headName.getBytes().length;
                            arrColWidth[j] = bytes < DEFAULT_COLOUMN_WIDTH ? DEFAULT_COLOUMN_WIDTH : bytes;
                            sheet.setColumnWidth(j, arrColWidth[j] * 300);
                        }
                    }
                    Row headerRow = sheet.createRow(1); //表头 rowIndex =1
                    headerRow.setHeightInPoints(20);
                    for (int i = 0; i < heads.length; i++) {
                        headerRow.createCell(i).setCellValue(heads[i]);
                        headerRow.getCell(i).setCellStyle(headerStyle);
                    }
                    rowIndex = 2;//数据内容从 rowIndex=1开始
                }
            } else {
                if (rowIndex == 65535 || rowIndex == 0) {
                    if (rowIndex != 0) {
                        sheet = wb.createSheet();//如果数据超过了，则在第二页显示
                        for (int j = 0; j < heads.length; j++) {
                            String headName = heads[j];
                            int bytes = headName.getBytes().length;
                            arrColWidth[j] = bytes < DEFAULT_COLOUMN_WIDTH ? DEFAULT_COLOUMN_WIDTH : bytes;
                            sheet.setColumnWidth(j, arrColWidth[j] * 300);
                        }
                    }
                    Row headerRow = sheet.createRow(0); //表头 rowIndex =0
                    headerRow.setHeightInPoints(20);
                    for (int i = 0; i < heads.length; i++) {
                        headerRow.createCell(i).setCellValue(heads[i]);
                        headerRow.getCell(i).setCellStyle(headerStyle);
                    }
                    rowIndex = 1;//数据内容从 rowIndex=1开始
                }
            }
            Row dataRow = sheet.createRow(rowIndex);
            dataRow.setHeightInPoints(20);
            for (int i = 0; i < heads.length; i++) {//填充此行的每列数据
                Cell dataCell = dataRow.createCell(i);
                dataCell.setCellValue(row[i]);
                dataCell.setCellStyle(cellStyle);
            }
            rowIndex++;
        }
        return wb;
    }

    /**
     * 转换Excel中的颜色为颜色代码
     */
    public static String toHex(Color color){
        byte[] aa = null;

        if (color instanceof XSSFColor) {
            XSSFColor xc = (XSSFColor) color;
            aa = xc.getRGBWithTint();
        }
        if(aa == null){
            return null;
        }
        String hexcode = "#";
        for(int n : aa){
            if(n<0){
                n += 256;
            }
            String c = "0123456789ABCDEF";
            String b;
            int a = n % 16;
            b = c.substring(a, a+1);
            a = (n - a) / 16;
            hexcode += c.substring(a, a+1) + b;
        }
        return hexcode;
    }


    /**
     * 根据字段名称和对象数组返回组装好的rows
     * @param fields 字段名称
     * @param object 对象数组
     * @return java.util.List<java.lang.String [ ]>
     */
    public static List<String[]> getRows(String[] fields, List<?> object) {
        List<String[]> list = new ArrayList<>();
        for (Object t : object) {
            String[] fieldsValue = new String[fields.length];
            for (int i = 0; i < fields.length; i++) {
                fieldsValue[i] = getFieldValueByName(fields[i],t);
            }
            list.add(fieldsValue);
        }
        return list;
    }

    public static String getFieldValueByName(String fieldName, Object t) {
        try {
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = "get" + firstLetter + fieldName.substring(1);
            Method method = t.getClass().getMethod(getter, new Class[] {});
            Object value = method.invoke(t, new Object[] {});
            if(null != value) {
                if(value instanceof Date) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    return dateFormat.format(value);
                }
                if(value instanceof Boolean) {
                    return ((Boolean) value).booleanValue() ? "是" : "否";
                }
            }
            return value == null ? "" : value.toString();
        } catch (Exception e) {
        }
        return "";
    }
}
