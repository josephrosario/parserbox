package com.parserbox.utils;

import com.parserbox.model.DataCacheItem;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.*;
import org.springframework.core.io.ClassPathResource;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.*;
import java.util.*;

public class FormulaProcessor {

    private static List<DataCacheItem> supportFunctions = new ArrayList<>();



    ScriptEngineManager engineManager;
    ScriptEngine engine;
    private Log log = LogFactory.getLog(this.getClass());
    Invocable invocable;


    public FormulaProcessor() throws Exception {
        engineManager = new ScriptEngineManager();
        engine = engineManager.getEngineByName("nashorn");
        ClassPathResource resource = new ClassPathResource("static/js/formula-parser.js");
        String fString = readFromInputStream(resource.getInputStream());
        engine.eval(fString);
        engine.eval("var parser = new formulaParser.Parser();");
        engine.eval("function execPFormula(formulaString) { return parser.parse(formulaString); } ");

        invocable = (Invocable) engine;

    }

    private String readFromInputStream(InputStream inputStream)
            throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line + "\n");
            }
        }
        return resultStringBuilder.toString();
    }


    public String getValue(String formulaString) throws Exception {
        String str = "";
        try {
            if (StringUtils.isBlank(formulaString)) return str;
            if (StringUtils.startsWith(formulaString, "=")) {
                formulaString = StringUtils.removeStart(formulaString, "=");
            }
            ScriptObjectMirror o = (ScriptObjectMirror)invocable.invokeFunction("execPFormula", formulaString);
            Object e = o.get("error");
            if (e != null) {
                str = e.toString();
            }
            else {
                Object r = o.get("result");
                if (r != null) {
                    str = r.toString();
                    if (StringUtils.endsWith(str,".0")) {
                        str = StringUtils.substringBeforeLast(str, ".0");
                    }
                }
            }
        }
        catch(Exception e) {
            log.info(e);
        }

        return str;
    }


    public void testIt2() throws Throwable {
        Date prior = new Date();
        engine.eval("var parser = new formulaParser.Parser();");
        Object o = engine.eval("parser.parse('SUM(1, 6, 7)');");
        Date recent = new Date();

        log.info("Elapsed time = " + (recent.getTime() - prior.getTime()));

        System.out.println(o);
    }

    public static List<DataCacheItem> getExcelFunctions() throws Exception {
        List<DataCacheItem> functions = new ArrayList<>();

        InputStream inputStream = null;
        Workbook workbook = null;

        try {
            ClassPathResource resource = new ClassPathResource("static/excelfunctions.xlsx");
            inputStream = resource.getInputStream();

            workbook = WorkbookFactory.create(inputStream);
            if (workbook == null) {
                new RuntimeException("Could not load excel data source.");
            }

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                Cell cell1 = row.getCell(0);
                Cell cell2 = row.getCell(1);
                Cell cell3 = row.getCell(2);

                if (cell2.getCellTypeEnum() == CellType.STRING) {
                    DataCacheItem f = new DataCacheItem();
                    f.setCategory(cell1.getStringCellValue());
                    f.setId(StringUtils.trimToEmpty(cell2.getStringCellValue()).toUpperCase());
                    f.setShortDescription(cell3.getStringCellValue());
                    functions.add(f);
                }
            }
        } catch(Exception e) {
            throw e;
        }

        finally {
            try {
                inputStream.close();
                workbook.close();
            }
            catch (Exception ce) {
                System.out.println(ce);
            }
        }

        return functions;
    }
    public static Map<String, DataCacheItem> getExcelFunctionsMap() throws Exception {
        Map<String, DataCacheItem> functionsMap = new HashMap<>();

        for (DataCacheItem f : getExcelFunctions()) {
            functionsMap.put(f.getId(), f);
        }
        return functionsMap;

    }

    public static synchronized void loadSupportedFunctions() throws Exception {

        InputStream inputStream = null;
        BufferedReader br = null;

        Map<String, DataCacheItem> excelFunctionMap = getExcelFunctionsMap();

        List<String> functions = new ArrayList();
        supportFunctions.clear();

        try {
            ClassPathResource resource = new ClassPathResource("static/js/formula-parser.js");
            inputStream = resource.getInputStream();
            br = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            boolean begin = false;
            while ((line = br.readLine()) != null) {

                if (StringUtils.containsIgnoreCase(line,"exports.ABS")) {
                    begin = true;
                }

                if (begin) {
                    if (StringUtils.containsIgnoreCase(line,"exports.")) {
                        if (StringUtils.containsIgnoreCase(line,"function")) {
                            String str = StringUtils.substringBetween(line, "exports.",  " ");
                            if (excelFunctionMap.containsKey(str)) {
                                supportFunctions.add(excelFunctionMap.get(str));
                            }
                        }
                    }
                }
             }

            Collections.sort(supportFunctions, (a, b)-> a.getId().compareToIgnoreCase(b.getId()));

        }
        catch (Exception e) {
            throw e;
        }
        finally {
            try {
                inputStream.close();
                br.close();
            }
            catch (Exception ce) {
                System.out.println(ce);
            }
        }

     }

    public static List<DataCacheItem> getSupportFunctions() {
        return supportFunctions;
    }

}
