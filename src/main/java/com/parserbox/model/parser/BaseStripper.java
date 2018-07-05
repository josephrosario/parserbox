package com.parserbox.model.parser;

import com.parserbox.model.ColumnValue;
import com.parserbox.model.GrabberFilterValue;
import com.parserbox.model.ParsingFilter;
import com.parserbox.model.RowValue;
import com.parserbox.utils.DateHelper;
import com.parserbox.utils.FormulaProcessor;
import com.parserbox.utils.NumberHelper;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.DateValidator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class handles parsing of a PDF document.
 * Data is processed and extracted into a text grid using various parsing and
 * filtering methods that use array parameters to configure the output appropriately.
 *
 * @see org.apache.pdfbox.text.PDFTextStripper
 */
public class BaseStripper  {
    Log log = LogFactory.getLog(this.getClass());
    boolean findAndReplaceSwitch = false;
    boolean killSwitch = false;

    double scalingMultiplier = 1.0;
    String current_line = "";
    List<List<TextPositionWrapper>> current_positions = new ArrayList<>();
    List<TextPositionWrapper> currentPositionsSeparated = new ArrayList<>();
    List<List<TextPositionWrapper>> total_row_positions = new ArrayList<>();
    List<ParsingFilter> parsingFilters = new ArrayList<>();
    List<ParsingFilter> parsingFiltersWithMarkers = new ArrayList<>();
    List<ParsingFilter> parsingFiltersWithGrabbers = new ArrayList<>();
    List<ParsingFilter> parsingFiltersWithMarkersAndGrabbers = new ArrayList<>();
    List<ParsingFilter> parsingFiltersWithFilterTypes = new ArrayList<>();
    List<ParsingFilter> parsingFiltersWithSkipForBlankValue = new ArrayList<>();
    List<ParsingFilter> parsingFiltersWithFormulas = new ArrayList<>();

    List<String> dataTypes = new ArrayList<>();

    Map<String, ParsingFilter> parsingFiltersMap = new HashMap<>();
    Map<String, ParsingFilter> parsingFiltersNameMap = new HashMap<>();
    Map<String, ParsingFilter> parsingFiltersWithFormulaNameMap = new HashMap<>();
    Map<String, ParsingFilter> parsingFiltersWithFormattingInputMap = new HashMap<>();
    Map<String, ParsingFilter> parsingFiltersWithFormattingOutputMap = new HashMap<>();

    List<GrabberFilterValue[]> totalGrabberRowValues = new ArrayList<>();

    int totalMarkerTypes = 0;
    List<Integer> markerTypePositions = new ArrayList<>();

    Map<String, List<String>> markerValues = new HashMap<>();
    Locale locale = null;
    TimeZone timezone = null;

    List<String> filtersSetToGrabNext = new ArrayList<>();
    List<ParsingFilter> grabbersFound = new ArrayList<>();

    List<RowValue> rows = new ArrayList();

    boolean pageStart = false;
    boolean pageEnd = false;

    Map<Integer, List<RowValue>> rowsForPage = new HashMap<>();
    Map<Integer, List<List<TextPositionWrapper>>> positionsForPage = new HashMap<>();

    long currentRowIndex = 0;

    ITextStripper parent;

    List lines = new ArrayList();
    Map<String, List<GrabberFilterValue>> currentGrabberMap = new HashMap<>();

    /**
     * Constructor
     * @param parent
     * @throws IOException
     */
    public BaseStripper(ITextStripper parent) throws IOException {
        super();
        this.parent = parent;

    }

    /**
     * killSwitch getter method
     */
    public boolean isKillSwitch() {
        return killSwitch;
    }

    /**
     * killSwitch setter method
     */
    public void setKillSwitch(boolean killSwitch) {
        this.killSwitch = killSwitch;
    }

    /**
     * Initializes critical variables.
     */
    public void initVariables() {
        this.current_line = "";
        this.current_positions.clear();
        this.currentPositionsSeparated.clear();
        this.total_row_positions.clear();
        this.rows.clear();
        this.rowsForPage.clear();
        this.positionsForPage.clear();
        this.lines.clear();
    }

    /**
     * Method called by the parent to handle the writing of a string.
     * @param text The text to write to the stream.
     * @param textPositions The TextPositions belonging to the text.
     * @throws IOException If there is an error when writing the text.
     */
    protected void writeString(String text, List<TextPositionWrapper> textPositions) throws IOException {
        current_positions.add(textPositions);
        for (TextPositionWrapper p : textPositions) {
            currentPositionsSeparated.add(p);
        }
        current_line += text;
    }
    protected void writeString(String text, TextPositionWrapper textPosition) throws IOException {
        List<TextPositionWrapper> list = new ArrayList<>();
        list.add(textPosition);
        writeString(text, list);
    }

    /**
     * Method override to handle Line Separator processing.
     * @throws IOException If there is an error when writing the text.
     */
    protected void writeLineSeparator() throws IOException {
        this.writeALine();
    }

    /**
     * Method override to handle Start of Page processing.
     * @throws IOException if something went wrong
     */
    protected void writePageStart() throws IOException {
        pageStart = true;
        int cp = parent.getCurrentPageNumber();
        rowsForPage.put(cp, new ArrayList<>());
        positionsForPage.put(cp, new ArrayList<>());

    }

    /**
     * Method override to handle End of Page processing.
     * @throws IOException If there is an error when writing the text.
     */
    protected void writePageEnd() throws IOException {
        pageEnd = true;
        this.writeALine();
    }

    /**
     * Handles the primary parsing work based on parameters set in the initialization.
     * @throws IOException If there is an error when writing the text.
     */
    protected void writeALine() throws IOException {

        RowValue rowValue = new RowValue();
        List<TextPositionWrapper> row_positions = new ArrayList<>();

        for (List<TextPositionWrapper> list : this.current_positions) {
            for (TextPositionWrapper t : list) {
                row_positions.add(t);

                double d = t.getXDirAdj() * this.scalingMultiplier;
                rowValue.appendToLine(t.getUnicode());
                for (ParsingFilter p : this.parsingFiltersWithMarkersAndGrabbers) {
                    if (p.getColumnMarkerWidth__c() > 0) {
                        int left = NumberHelper.getInt(p.getColumnMarkerLeft__c());
                        int width = NumberHelper.getInt(p.getColumnMarkerWidth__c());
                        if (d >= left && d <= (left + width)) {
                            ColumnValue v = rowValue.getColumnValue(p.getId());
                            v.appendText(t);
                            v.setColumnLeft(p.getColumnMarkerLeft__c());
                            v.setColumnWidth(p.getColumnMarkerWidth__c());
                        }
                    }
                }
            }
        }
        this.current_line = "";
        this.current_positions.clear();

        rowValue.setRowNumber(rows.size());

        if (! isFilterTokenFound(rowValue)) {
            rows.add(rowValue);
            rowValue.setRowPositions(row_positions);
            List<RowValue> rowsList = rowsForPage.get(parent.getCurrentPageNumber());
            rowsList.add(rowValue);
        }
        total_row_positions.add(row_positions);
        List<List<TextPositionWrapper>> posByPage = positionsForPage.get( parent.getCurrentPageNumber());
        posByPage.add(row_positions);
    }

    /**
     * Retrieves a held grabber.
     * @param p the {@link ParsingFilter}
     * @return
     */
    public GrabberFilterValue getCurrentGrabberFilterValue(ParsingFilter p, RowValue currentRow) {
        if (this.currentGrabberMap == null) {
            this.currentGrabberMap = new HashMap<>();
        }

        List<GrabberFilterValue> grabberList = this.currentGrabberMap.get(p.getId());
        if (grabberList == null)  {
            grabberList = new ArrayList<>();
            this.currentGrabberMap.put(p.getId(), grabberList);
        };

        // find the grabber closest to the row location
        GrabberFilterValue grabberValue = null;

        for (int i = grabberList.size(); --i >= 0;) {
            GrabberFilterValue g = grabberList.get(i);
            int currentRowNumber = currentRow.getRowNumber();
            int grabberRowNumber = g.getRow().getRowNumber();
            if (currentRowNumber >= grabberRowNumber) {
                grabberValue = g;
                break;
            }
        }
        return grabberValue;
    }

    /**
     * Sets the grabber value to be used later when duplicated on subsequent lines
     * @param p
     * @param value
     * @param currentRow
     * @return {@link GrabberFilterValue}
     */
    public GrabberFilterValue setCurrentGrabberFilterValue(ParsingFilter p, String value, RowValue currentRow) {
        if (this.currentGrabberMap == null) {
            this.currentGrabberMap = new HashMap<>();
        }
        List<GrabberFilterValue> grabberList = this.currentGrabberMap.get(p.getId());

        if (grabberList == null) {
            grabberList = new ArrayList<>();
            this.currentGrabberMap.put(p.getId(), grabberList);
        }

        GrabberFilterValue grabberValue = null;

        for (GrabberFilterValue g : grabberList) {
            RowValue r = g.getRow();
            if (r != null && r == currentRow) {
                grabberValue = g;
            }
        }

        if (grabberValue == null) {
            grabberValue = new GrabberFilterValue();
            grabberList.add(grabberValue);
        }

        grabberValue.setRow(currentRow);
        grabberValue.setValue(value);
        grabberValue.setName(p.getName());
        return grabberValue;
    }

    /**
     * Iterates over all rows retrieved to apply filters and markers.
     * @throws IOException
     */
    protected void processColumns() throws IOException {

        processNonFormulaColumns();
        for (RowValue rowValue : rows) {

            if (isFilterTokenFound(rowValue)) {
                rowValue.setSkipRow(true);
                continue;
            }

            // Sanitize data based on filter data types.
            rowValue = sanitizeData(rowValue);

        }


        processFormulaColumns();

        for (RowValue rowValue : rows) {
            if (rowValue.isSkipRow()) continue;

            // Do any find - replacing of values based on parsing filters
            if (this.findAndReplaceSwitch) {
                rowValue = findAndReplace(rowValue);
            }

            // Sanitize data based on filter data types.
            rowValue = sanitizeData(rowValue);

            // Skip the ordered text array based on an filter settings
            if (isLineToSkip(rowValue)) {
                rowValue.setSkipRow(true);
                continue;
            }
        }
    }

    /**
     * Processes a column and can also recurse in order to processor embedded formulas.
     * @param rowValue
     * @param p
     * @return String
     */
    protected String processRowColumn(RowValue rowValue, ParsingFilter p) {

        ColumnValue columnValue = rowValue.getColumnValue(p.getId());
        if (columnValue.isFormulaHandled()) {
            return columnValue.getText();
        }
        columnValue.setFormulaHandled(true);  // do this here to make sure we don't accidently recurse indefinitely
        String formulaPattern = StringUtils.trimToEmpty(p.getFormulaPattern__c());

        if (StringUtils.containsIgnoreCase(formulaPattern,".this")) {
            formulaPattern = StringUtils.replacePattern(formulaPattern, "(?i).this", p.getName());
        }
        if (StringUtils.containsIgnoreCase(formulaPattern,".me")) {
            formulaPattern = StringUtils.replacePattern(formulaPattern, "(?i).me", p.getName());
        }
        for (ParsingFilter fP : this.parsingFiltersWithMarkersAndGrabbers) {
            String name = fP.getName();
            String datatype = fP.getDataType__c();
            if (StringUtils.containsIgnoreCase(formulaPattern, name)) {
                ColumnValue cv = rowValue.getColumnValue(fP.getId());
                String str = "";
                if (cv.isFormulaHandled() || fP.isNotFormulaMarker() ) {
                    str = cv.getText();
                } else {
                    str = processRowColumn(rowValue, fP);
                }

                if ("number".equals(datatype) || "currency".equals(datatype)) {
                    formulaPattern = StringUtils.replace(formulaPattern, name,  str );
                } else {
//                    formulaPattern = StringUtils.replace(formulaPattern, name, "'" + str + "'");
                    formulaPattern = StringUtils.replace(formulaPattern, name,  str );
                }
            }
        }

        if (StringUtils.containsIgnoreCase(formulaPattern, p.getName())) {
            formulaPattern = StringUtils.replacePattern(formulaPattern, "(?i)" + p.getName(), columnValue.getText());
        }
        columnValue.setText(formulaPattern);
        return formulaPattern;
    }

    protected void processFormulaColumns() throws IOException {

        DecimalFormat df = new DecimalFormat("#0.##");

        HashMap<String, ParsingFilter> nameMap = new HashMap<>();
        HashMap<String, ParsingFilter> formulaNameMap = new HashMap<>();
        HashMap<String, ParsingFilter> nonFormulaNameMap = new HashMap<>();
        for (ParsingFilter p : this.parsingFiltersWithMarkersAndGrabbers) {
            if (StringUtils.isNotBlank(p.getFormulaPattern__c())) {
                formulaNameMap.put(p.getName(), p);
            } else {
                nonFormulaNameMap.put(p.getName(), p);
            }
            nameMap.put(p.getName(), p);
        }
        // Iterate over each formula within each row
        for (RowValue rowValue : rows) {
            // Pre-format the formulas for further processing by iterating each formula marker and
            // replacing referenced markers with raw values where possible.
            for (ParsingFilter p : this.parsingFiltersWithFormulas) {

                ColumnValue columnValue = rowValue.getColumnValue(p.getId());
                String txt = processRowColumn(rowValue, p);
                columnValue.setText(txt);
            }
            // Iterate over the formulas and handle based on the data type
            for (ParsingFilter p : this.parsingFiltersWithFormulas) {
                ColumnValue columnValue = rowValue.getColumnValue(p.getId());
                String formulaPattern = columnValue.getText();
                if (StringUtils.isBlank(formulaPattern)) continue;

                String dataType = p.getDataType__c();
                try {
                    FormulaProcessor formulaProcessor = parent.getFormulaProcessor();
                    if (formulaProcessor != null) {
                        formulaPattern = formulaProcessor.getValue(formulaPattern);
                    }
                }
                catch (Exception e) {
                }
                columnValue.setText(formulaPattern);
            }
        }
    }

    /**
     * Clean up method for formulas
     * @param str
     * @return String
     */
    protected String removeFormulaOperators(String str) {
        str = StringUtils.remove(str, "-");
        str = StringUtils.remove(str, "+");
        str = StringUtils.remove(str, "*");
        str = StringUtils.remove(str, "/");
        str = StringUtils.remove(str, "=");
        return str;
    }

    /**
     * Main processing method to extract values based on grabber criteria
     * @throws IOException
     */
    protected void processNonFormulaColumns() throws IOException {

        for (ParsingFilter p : this.parsingFiltersWithMarkersAndGrabbers) {

            int rowCnt = 0;
            boolean belowGrabberFound = false;
            int belowGrabberJump = 0;

            for (RowValue rowValue : rows) {
                rowCnt++;

                String line = rowValue.getLine().toString();
                ColumnValue columnValue = rowValue.getColumnValue(p.getId());

                if (p.isGrabber()) {
                    String textToSearch = p.isMarkerColumn() ? columnValue.getText() : line;
                    columnValue.setText("");

                    String valueString = "";
                    GrabberFilterValue heldGrabberObject = this.getCurrentGrabberFilterValue(p, rowValue);

                    String grabberType = p.getGrabberType__c().toLowerCase();
                    String grabber1 = p.getGrabber1__c();
                    String grabber2 = p.getGrabber2__c();

                    if ( ! p.isSearchType1MaskorRegex()) {
                        textToSearch = StringUtils.removeAll(textToSearch, " ");
                        if (grabber1 != null) {
                            grabber1 = StringUtils.removeAll(grabber1, " ");
                        }
                        if (grabber2 != null) {
                            grabber2 = StringUtils.removeAll(grabber2, " ");
                        }
                    }

                    if (grabberType.startsWith("any non-space at first position")) {
                        TextPositionWrapper t = columnValue.getTextPosition(0);
                        if (t != null) {
                            double d = t.getXDirAdj() * this.scalingMultiplier;
                            double leftMarkerPosition = columnValue.getColumnLeft();

                            if (d >= leftMarkerPosition && d < leftMarkerPosition + 10) {
                                if (StringUtils.isBlank(grabber1)) {
                                    valueString = textToSearch;
                                } else {
                                    if (p.isSearchType1MaskorRegex()) {
                                        Pattern pattern = p.getSearchType1Pattern();
                                        if (pattern != null) {
                                            Matcher m = pattern.matcher(textToSearch);
                                            if (m.find()) {
                                                String g = m.group();
                                                valueString = textToSearch;
                                            }
                                        }
                                    }
                                    else
                                    if (StringUtils.contains(textToSearch, grabber1)) {
                                        valueString = textToSearch;
                                    }
                                }
                            }
                        }
                    }
                    else
                    if (grabberType.startsWith("all") || grabberType.startsWith("any non-space")) {
                        if (StringUtils.isBlank(grabber1)) {
                            valueString = textToSearch;
                        } else {
                            if (p.isSearchType1MaskorRegex()) {
                                Pattern pattern = p.getSearchType1Pattern();
                                if (pattern != null) {
                                    Matcher m = pattern.matcher(textToSearch);
                                    if (m.find()) {
                                        String g = m.group();
                                        valueString = textToSearch;
                                    }
                                }
                            }
                            else
                            if (StringUtils.contains(textToSearch, grabber1)) {
                                valueString = textToSearch;
                            }
                        }
                    }
                    else
                    if (grabberType.startsWith("left")) {
                        if (p.isSearchType1MaskorRegex()) {
                            Pattern pattern = p.getSearchType1Pattern();
                            if (pattern != null) {
                                Matcher m = pattern.matcher(textToSearch);
                                if (m.find()) {
                                    String g = m.group();
                                    valueString = StringUtils.substringBefore(textToSearch, g);
                                }
                            }
                        }
                        else
                        if (StringUtils.contains(textToSearch, grabber1)) {
                            valueString = StringUtils.substringBefore(textToSearch, grabber1);
                        }
                        if (StringUtils.isNotBlank(valueString)) {
                            if (StringUtils.isNotBlank(p.getJumpTo__c())) {
                                int jump = NumberHelper.getInt(p.getJumpTo__c());
                                if (jump > 0) {
                                    if (jump < valueString.length()) {
                                        jump = valueString.length() - (jump-1);
                                    } else {
                                        jump = 1;
                                    }
                                    valueString = valueString.substring(0,jump);
                                }
                            }
                        }
                    }
                    else
                    if (grabberType.startsWith("right")) {
                        if (p.isSearchType1MaskorRegex()) {
                            Pattern pattern = p.getSearchType1Pattern();
                            if (pattern != null) {
                                Matcher m = pattern.matcher(textToSearch);
                                if (m.find()) {
                                    String g = m.group();
                                    valueString = StringUtils.substringAfter(textToSearch, g);
                                }
                            }
                        }
                        else
                        if (StringUtils.contains(textToSearch, grabber1)) {
                            valueString = StringUtils.substringAfter(textToSearch, grabber1);
                        }

                        if (StringUtils.isNotBlank(valueString)) {
                            if (StringUtils.isNotBlank(p.getJumpTo__c())) {
                                int jump = NumberHelper.getInt(p.getJumpTo__c());
                                if (jump > 0) {
                                    if (jump > valueString.length()) {
                                        jump = valueString.length() - 1;
                                    }
                                    else {
                                        jump -=1;
                                    }
                                    valueString = valueString.substring(jump);
                                }
                            }
                        }
                    }
                    else
                    if (grabberType.startsWith("between")) {

                        String str1 = null;
                        String str2 = null;
                        if (p.isSearchType1MaskorRegex()) {
                            Pattern pattern = p.getSearchType1Pattern();
                            if (pattern == null) continue;
                            Matcher m = pattern.matcher(textToSearch);
                            if (m.find()) {
                                str1 = m.group();
                            }
                        }
                        else {
                            str1 = grabber1;
                        }
                        if (StringUtils.isBlank(str1)) continue;

                        if (p.isSearchType2MaskorRegex()) {
                            Pattern pattern = p.getSearchType2Pattern();
                            if (pattern == null) continue;
                            Matcher m = pattern.matcher(textToSearch);
                            if (m.find()) {
                                str2 = m.group();
                            }
                        }
                        else {
                            str2 = grabber2;
                        }
                        str1 = StringUtils.trimToEmpty(str1);
                        str2 = StringUtils.trimToEmpty(str2);
                        valueString = StringUtils.substringBetween(textToSearch, str1, str2);
                    }

                    else
                    if (grabberType.startsWith("containing")) {
                        String textHold = textToSearch;
                        if (grabberType.endsWith("in row")) {
                            textToSearch = line;
                        }
                        if (p.isSearchType1MaskorRegex()) {
                            Pattern pattern = p.getSearchType1Pattern();
                            if (pattern != null) {
                                Matcher m = pattern.matcher(textToSearch);
                                if (m.find()) {
                                    valueString = textHold;
                                }
                            }

                        } else {
                            if (isFoundWithSimpleExpression(textToSearch, grabber1)) {
                                valueString = textHold;
                            }
                        }
                    }
                    else
                    if (StringUtils.containsIgnoreCase(grabberType, "regular")) {
                        Pattern pattern = p.getGrabber1Pattern();
                        if (pattern != null) {
                            Matcher m = pattern.matcher(textToSearch);
                            if (m.find()) {
                                valueString = textToSearch;
                            }
                        }
                    }
                    else
                    if (grabberType.startsWith("above")) {
                        boolean found = false;
                        if (p.isSearchType1MaskorRegex()) {
                            Pattern pattern = p.getSearchType1Pattern();
                            if (pattern != null) {
                                Matcher m = pattern.matcher(textToSearch);
                                found = m.find();
                            }
                        }
                        else {
                            found = StringUtils.contains(textToSearch, grabber1);
                        }
                        if (found) {
                            int jump = 0;
                            if (StringUtils.isNotBlank(p.getJumpTo__c())) {
                                jump = NumberHelper.getInt(p.getJumpTo__c());
                                if (jump > 0) {
                                    int r = rowCnt-jump-1;
                                    if (r < 0) r = 0;
                                    RowValue rv = rows.get(r);
                                    line = rv.getLine().toString();
                                    valueString = p.isMarkerColumn() ?
                                            rv.getColumnValue(p.getId()).getTextOriginal() : rv.getLine().toString();
                                }
                            }
                            if (jump <= 0) {
                                for (int i = rowCnt-1; --i >= 0;) {
                                    RowValue rv = rows.get(i);
                                    line = rv.getLine().toString();
                                    String text = p.isMarkerColumn() ?
                                            rv.getColumnValue(p.getId()).getTextOriginal() : rv.getLine().toString();

                                    if (StringUtils.contains(text, grabber1)) {
                                        break;
                                    }
                                    if (StringUtils.isNotBlank(text)) {
                                        valueString = text;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    else
                    if (grabberType.startsWith("below")) {
                        boolean found = false;
                        if (p.isSearchType1MaskorRegex()) {
                            Pattern pattern = p.getSearchType1Pattern();
                            if (pattern != null) {
                                Matcher m = pattern.matcher(textToSearch);
                                found = m.find();
                            }
                        }
                        else {
                            found = StringUtils.contains(textToSearch, grabber1);
                        }
                        if (found) {
                            belowGrabberFound = true;
                            if (StringUtils.isNotBlank(p.getJumpTo__c())) {
                                int jump = NumberHelper.getInt(p.getJumpTo__c());
                                if (jump > 0) {
                                    belowGrabberJump = rowCnt + jump;
                                    if (belowGrabberJump > rows.size()) belowGrabberJump = rows.size();
                                }
                            }
                        }
                        else
                        if (belowGrabberFound) {
                            if (belowGrabberJump > 0) {
                                if (belowGrabberJump == rowCnt) {
                                    valueString = textToSearch;
                                    belowGrabberFound = false;
                                    belowGrabberJump = 0;
                                }
                            } else {
                                if(StringUtils.isNotBlank(textToSearch)) {
                                    valueString = textToSearch;
                                    belowGrabberFound = false;
                                }
                            }
                        }
                    }
                    if (StringUtils.isNotBlank(valueString)) {
                        if (StringUtils.isNotBlank(p.getGrabberValueContext__c())) {
                            if (StringUtils.equalsIgnoreCase(p.getGrabberValueContext__c(), "row")) {
                                valueString = line;
                            }
                        }
                        this.setGrabberFound(p);
                        heldGrabberObject = this.setCurrentGrabberFilterValue(p, valueString, rowValue);
                        if (p.isSkipFirstOccurrence__c()) {                 // Check if we skip the first occurrence
                            rowValue.setSkipRow(true);
                        }
                    }
                    if (heldGrabberObject != null) {
                        String grabberValue = heldGrabberObject.getValue();
                        columnValue.setText(grabberValue);

                        if (p.isFillBlanks__c() == false) {                 // Check if we fill in subsequent rows
                            heldGrabberObject.setValue("");
                        }
                    }
                }
            }
        }
    }

    /**
     * Used to handle simple expressions mainly for markers
     * @param textToSearch
     * @param expression
     * @return
     */
    boolean isFoundWithSimpleExpression(String textToSearch, String expression) {
        return isFilterTokenFound(textToSearch, expression);
    }

    /**
     * Sets an item in the grabberIdsFound map.
     * This map is used to track what grabbers have been found for a given range
     * and is important when users jump over page in large documents.
     * @param p {@link ParsingFilter}
     */
    private void setGrabberFound(ParsingFilter p) {
        if (p == null) return;
        if (this.grabbersFound.contains(p)) return;
        this.grabbersFound.add(p);
    }

    /**
     * Getter method for the grabberIdsFoundMap
     * @return grabberIdsFoundMap
     */
    public List<ParsingFilter> getGrabbersFound() {
        return grabbersFound;
    }

    /**
     * Checks row values for data type accuracy.
     * @param row
     * @return
     */
    public RowValue sanitizeData(RowValue row) {

        Map<String, ColumnValue> columnValues = row.getColumnValues();
        for (String Id : columnValues.keySet()) {

            ColumnValue columnVal = columnValues.get(Id);
            String text = columnVal.getText();

            if (StringUtils.isBlank(text)) continue;

            ParsingFilter p = this.parsingFiltersMap.get(Id);
            String dataType = p.getDataType__c();

            // Assume text by default so no check needed.
            if (StringUtils.isBlank(dataType)) continue;

            // Treat both currency and number the same for now
            if (StringUtils.equals("currency", dataType)  ||
                    StringUtils.equals("number", dataType)) {
                BigDecimal value = null;

                // do a little extra work for special number formats
                if (StringUtils.endsWithIgnoreCase(text, "CR") ||
                        StringUtils.endsWithIgnoreCase(text,"CM")) {
                    text = "-" + text.replaceAll("[^0-9.]", "");
                }


                try {value = NumberHelper.getCleanNumber(text, getLocale());} catch(Exception e) {}
                if (value == null) {
                    text = "";
                } else {
                    text = value.toString();
                }
            }
            else
            if (StringUtils.equals("date", dataType)) {
                try {
                    text = StringUtils.removeAll(text, " ");
                    if (StringUtils.isNotBlank(p.getFormattingPatternInput__c())) {
                        String pattern = StringUtils.trimToEmpty(p.getFormattingPatternInput__c());
                        FastDateFormat f = FastDateFormat.getInstance(pattern);
                        Date d = f.parse(text);
                        if (d == null) {
                            text = "";
                        }
                    } else {
                        if (DateValidator.getInstance().isValid(text) == false) {
                            text = "";
                        }
                    }
                }
                catch(Exception e) {
                    text = "";
                }
            }
            columnVal.setText(text);
        }
        return row;
    }

    /**
     * Find and replace values as instructed.
     * @param row
     * @return
     */
    public RowValue findAndReplace(RowValue row) {
        Map<String, ColumnValue> columnValues = row.getColumnValues();
        for (String Id : columnValues.keySet()) {
            ParsingFilter p = this.parsingFiltersMap.get(Id);
            if (StringUtils.isNotBlank(p.getFindReplaceOld__c())) {
                ColumnValue columnVal = columnValues.get(Id);
                String text = columnVal.getText();
                if (StringUtils.isBlank(text)) continue;
                String newValue = StringUtils.trimToEmpty(p.getFindReplaceNew__c());
                if (p.getFindReplacePattern() != null) {
                    text = StringUtils.replacePattern(text, p.getFindReplaceOld__c(), newValue);
                } else {
                    text = StringUtils.replace(text, p.getFindReplaceOld__c(), newValue);
                }
                columnVal.setText(text);
            }
            else
            if (StringUtils.isNotBlank(p.getFindReplaceNew__c())) {
                ColumnValue columnVal = columnValues.get(Id);
                String text = columnVal.getText();
                if (StringUtils.isBlank(text)) continue;
                columnVal.setText(p.getFindReplaceNew__c());
            }
        }
        return row;
    }

    /**
     * Searches the row columns to determine if the line is to be skipped based on the FilterBlanksType.
     * @param row
     * @return
     */
    public boolean isLineToSkip(RowValue row) {
        boolean skip = false;

        Map<String, ColumnValue> columnValues = row.getColumnValues();
        // First check if a specific marker with a blank value can for the skipping of the row.
        for (ParsingFilter p : this.parsingFiltersWithSkipForBlankValue) {
            ColumnValue columnVal = columnValues.get(p.getId());
            String v = columnVal.getText();
            if (StringUtils.isBlank(v)) return true;
        }

        for (ParsingFilter p : this.parsingFiltersWithFilterTypes) {
            String filterBlanksType = p.getFilterBlanksType__c();
            if (StringUtils.isBlank(filterBlanksType)) continue;

            for (String Id : columnValues.keySet()) {

                ColumnValue columnVal = columnValues.get(Id);
                String v = columnVal.getText();

                boolean isBlank = StringUtils.isBlank(v);

                if (filterBlanksType.equals("all_markers")) {
                    if (isBlank == false) { skip = false; break;} else { skip = true; }
                }
                else
                if (filterBlanksType.equals("one_marker")) {
                    if (isBlank == true) { skip = true; break;}
                }
                else
                if (filterBlanksType.equals("all_tagged")) {
                    ParsingFilter pt = this.parsingFiltersMap.get(Id);
                    if (pt.isMarkerTag__c()) {
                        if (isBlank == false) { skip = false; break;} else { skip = true; }
                    }
                }
                else
                if (filterBlanksType.equals("one_tagged")) {
                    ParsingFilter pt = this.parsingFiltersMap.get(Id);
                    if (pt.isMarkerTag__c()) {
                        if (isBlank == true) { skip = true; break;}
                    }
                }
            }
        }
        return skip;
    }

    /**
     * Searches a line for the existence of tokens by first iterating over filters.
     * @return The indicator signifying if token/s found.
     */
    public boolean isFilterTokenFound(RowValue row) {

        StringBuffer buffer = new StringBuffer(row.getLine().length());
        for (ParsingFilter filter : this.parsingFiltersWithFilterTypes) {
            String searchLine = "";
            if (filter.getColumnMarkerWidth__c() > 0) {
                for (TextPositionWrapper t : row.getRowPositions()) {
                    int left = NumberHelper.getInt(filter.getColumnMarkerLeft__c());
                    int width = NumberHelper.getInt(filter.getColumnMarkerWidth__c());
                    double d = t.getXDirAdj() * this.scalingMultiplier;
                    if (d >= left && d <= (left + width)) {
                        String c = t.getUnicode();
                        buffer.append(c);
                    }
                }
                searchLine = buffer.toString();
                buffer.setLength(0);
            } else {
                searchLine = row.getLine().toString();
            }
            if (isFilterTokenFound(searchLine, filter.getFilterLine1__c())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Searches a line for the existence of tokens by first iterating over filters.
     * @return The indicator signifying if token/s found.
     */
    public boolean isFilterTokenFound(String line) {
        for (ParsingFilter filter : this.parsingFiltersWithFilterTypes) {
            if (isFilterTokenFound(line, filter.getFilterLine1__c())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Searches a line for the existence of tokens.
     * @param line The line to search.
     * @return The indicator signifying if token/s found.
     */
    public boolean isFilterTokenFound(String line, String str) {
        if (StringUtils.isBlank(line) || StringUtils.isBlank((str))){
            return false;
        }
        str = str.toUpperCase();

        // Check to see if line contains at least one of the tokens.
        if (StringUtils.contains(str, " OR ")) {
            String[] tokens = StringUtils.splitByWholeSeparator(str, " OR ");
            int totalTokens = tokens.length;
            for (int i = 0; i < totalTokens; i++) {
                String t = StringUtils.trim(tokens[i]);
                if (StringUtils.containsIgnoreCase(line, t)) {
                    return true;
                }
            }
        }
        // Check to see if line contains all combinations of tokens.
        if (StringUtils.contains(str, " AND ")) {
            String[] tokens = StringUtils.splitByWholeSeparator(str, " AND ");
            boolean found = false;
            int foundCount = 0;
            int totalTokens = tokens.length;
            for (int i = 0; i < totalTokens; i++) {
                String t = StringUtils.trim(tokens[i]);
                if (StringUtils.containsIgnoreCase(line, t)) {
                    foundCount++;
                }
            }
            if (foundCount == totalTokens) {
                return true;
            }
        }
        // Check to see if line contains entire search string.
        if (StringUtils.containsIgnoreCase(line, str)) {
            return true;
        }
        return false;
    }

    /**
     * The header row is returned.
     * @return The header row as a String[]
     */
    public String[] getHeaderRow() {
        int mLen = this.parsingFiltersWithMarkersAndGrabbers.size();
        if (mLen == 0) return null;

        String[] headerRow = new String[mLen];
        for (int i = 0; i < (mLen); i++) {
            headerRow[i] = new String();
        }
        int pCnt = 0;
        for (ParsingFilter p : this.parsingFiltersWithMarkersAndGrabbers) {
            headerRow[pCnt++] = p.getName();
        }
        return headerRow;
    }

    /**
     * The current page is formatted as HTML using the positions gathered from the document.
     * @param minified An idicator to force the HTML output to be minified.
     * @return The HTML string for the page.
     * @throws Exception
     */
    public String getUnparsedPageAsHTML(boolean minified, int page) throws Exception {
        String tab = minified ? "" : "\t";
        String newline = minified ? "" : "\n";

        StringBuffer html = new StringBuffer(1024);
        DecimalFormat df = new DecimalFormat("#0.##");

        html.append("<style>" + newline);
        html.append("._d {" + newline);
        html.append(tab + "position: absolute;" + newline);    //div position will be absolute
        html.append("}" + newline);
        html.append("._b {" + newline);
        html.append(tab + "border-width:1px; border-style:solid; border-color:#d7c9b1; width:100%; height:100%" + newline);    //text box for excel
        html.append("}" + newline);

        html.append("</style>" + newline);

        html.append("<div style='background-color:yellow; position:relative'>" + newline);

        List<List<TextPositionWrapper>> positionsList = positionsForPage.get(page);

        for (List<TextPositionWrapper> positions : positionsList) {
            for (TextPositionWrapper p : positions) {
                if (p.isTextBoxMode()) {
                    html
                            .append("<div class='_d' style='")
                            .append("top:").append(df.format(p.getYDirAdj())).append("px;")
                            .append("left:").append(df.format(p.getXDirAdj() * this.scalingMultiplier)).append("px;")
                            .append("width:").append(df.format(p.getWidthInPixels() * this.scalingMultiplier)).append("px;")
                            .append("'>")
                            .append("<input class='_b' readonly type='text' ")
                            .append("value='")
                            .append(p.getUnicode())
                            .append("' ")
                            .append("title='")
                            .append(p.getUnicode())
                            .append("' ")
                            .append(">")
                            .append("</div>");
                } else {
                    html
                            .append("<div class='_d' style='")
                            .append("top:").append(df.format(p.getYDirAdj() * this.scalingMultiplier)).append("px;")
                            .append("left:").append(df.format(p.getXDirAdj() * this.scalingMultiplier)).append("px;")
                            .append("font-size:").append(df.format(p.getFontSizeInPt() * this.scalingMultiplier)).append("px;")
                            .append("'>")
                            .append(p.getUnicode())
                            .append("</div>");
                }
            }
        }
        html.append("</div>" + newline);
        return html.toString();
    }

    /**
     * The current page is formatted as an HTML table.
     * @param minified An idicator to force the HTML output to be minified.
     * @return The HTML string for the parsed page.
     * @throws Exception
     */
    public String getParsedPageAsHTML(boolean minified, int page) throws Exception {

        String tab = minified ? "" : "\t";
        String newline = minified ? "" : "\n";
        StringBuffer html = new StringBuffer(1024);

        html.append("<style>" + newline);

        html.append("._line {" + newline);
        html.append(tab + "border-bottom: 1px solid #ddd;" + newline);
        html.append("}" + newline);

        html.append("._cell {" + newline);
        html.append(tab + "padding: 4px;" + newline);
        html.append(tab + "text-align: left;" + newline);
        html.append("}" + newline);

        html.append("._stripped:nth-child(even){background-color: #f2f2f2}" + newline);

        html.append("._header {" + newline);
        html.append(tab + "padding: 4px;" + newline);
        html.append(tab + "padding: 4px;" + newline);
        html.append(tab + "text-align: left;" + newline);
        html.append("}" + newline);

        html.append("._grabber {" + newline);
        html.append(tab + "color: brown;" + newline);
        html.append("}" + newline);

        html.append("._frame {" + newline);
        html.append(tab + "padding: 6px;" + newline);
        html.append("}" + newline);

        html.append("</style>" + newline);

        html.append("<div class='_frame'>" + newline);
        html.append("<table>" + newline);
        html.append("<tr>" + newline);

        for (ParsingFilter p : this.parsingFiltersWithMarkersAndGrabbers) {
            if (p.isMarkerTag__c()) {
                html.append("<th class='_header _line _grabber'>");
            } else {
                html.append("<th class='_header _line'>");
            }
            html.append(p.isMarkerTag__c() ? "*" : "");
            String name = StringUtils.trim(p.getName());
            if (name.length() >= 10) {
                name = StringUtils.substring(name, 0, 10) + "...";
            }
            html.append(name);
            html.append("</th>" + newline);
        }
        html.append("</tr>" + newline);

        List<RowValue> rowsList = rowsForPage.get(page);

        for (RowValue row : rowsList) {
            if (row.isSkipRow()) continue;
            StringBuffer rowText = new StringBuffer(128);
            boolean wasData = false;
            rowText.append("<tr class='_stripped'>" + newline);

            for (ParsingFilter p : this.parsingFiltersWithMarkersAndGrabbers) {
                ColumnValue columnValue = row.getColumnValue(p.getId());
                String val = StringUtils.trimToEmpty(columnValue.getText());

                rowText.append("<td class='_cell'>").append(val).append("</td>" + newline);
                if (StringUtils.isNotBlank(val)) {
                    wasData = true;
                }
            }

            rowText.append("</tr>" + newline);
            if (wasData) {
                html.append(rowText);
            }
        }
        html.append("</table>" + newline);
        html.append("</div>");
        return html.toString();
    }

    /**
     * The current page is formatted as a JSON Array
     * @return The JSON string for the parsed page.
     * @throws Exception
     */
    public String getParsedPageAsJSON(int page) throws Exception {
        return getParsedPageAsJSON(page);
    }
    /**
     * The current page is formatted as a JSON Array
     * @return The JSON string for the parsed page.
     * @throws Exception
     */
    public String getParsedPageAsJSON(int page, boolean includeHeader) throws Exception {

        JSONArray list = new JSONArray();
        List<RowValue> rowsList = rowsForPage.get(page);
        for (RowValue rowValue : rowsList) {
            if (rowValue.isSkipRow()) continue;
            Map<String, ColumnValue> columnValues = rowValue.getColumnValues();
            JSONObject object = new JSONObject();
            for (ParsingFilter p : this.parsingFiltersWithMarkersAndGrabbers) {
                String value = "";
                ColumnValue columnValue = columnValues.get(p.getId());
                if (columnValue != null) {
                    value = columnValue.getText();
                }
                object.put(p.getId(), value);
            }
            list.add(object);
        }
        if (! includeHeader ) {
            return list.toJSONString();
        }

        JSONObject ret = new JSONObject();
        JSONObject header = new JSONObject();
        for (ParsingFilter p : this.parsingFiltersWithMarkersAndGrabbers) {
            header.put(p.getId(), p.getName());
        }
        ret.put("data", list);
        ret.put("header", header);

        return ret.toJSONString();
    }
    /**
     * The current page is formatted as a JSON Array of Arrays
     * This will not include any name: value pairs
     * @return The JSON string for the parsed page.
     * @throws Exception
     */
    public String getParsedPageAsJSONArrays(int page, boolean includeHeader) throws Exception {

        JSONArray list = new JSONArray();
        List<RowValue> rowsList = rowsForPage.get(page);
        if ( rowsList != null) {
            for (RowValue rowValue : rowsList) {
                if (rowValue.isSkipRow()) continue;
                Map<String, ColumnValue> columnValues = rowValue.getColumnValues();
                JSONArray object = new JSONArray();
                for (ParsingFilter p : this.parsingFiltersWithMarkersAndGrabbers) {
                    String dataType = StringUtils.trimToEmpty(p.getDataType__c());
                    String value = "";
                    ColumnValue columnValue = columnValues.get(p.getId());
                    if (columnValue != null) {
                        value = columnValue.getText();
                    }
                    if (StringUtils.isNotBlank(p.getFormattingPatternOutput__c())) {
                        String outputPattern = StringUtils.trimToEmpty(p.getFormattingPatternOutput__c());

                        if (StringUtils.isNotBlank(value)) {
                            try {
                                if (dataType.equals("date")) {
                                    Date date = null;
                                    if (StringUtils.isNotBlank(p.getFormattingPatternInput__c())) {
                                        String inputPattern = StringUtils.trimToEmpty(p.getFormattingPatternInput__c());
                                        FastDateFormat f = FastDateFormat.getInstance(inputPattern);
                                        date = f.parse(value);
                                    }
                                    else {
                                        if (DateValidator.getInstance().isValid(value)) {
                                            date = DateHelper.getDate(value);
                                        }
                                    }
                                    if (date != null) {
                                        value = DateFormatUtils.format(date, outputPattern);
                                    }
                                }
                            } catch (Exception e) {
                                value = e.getMessage()  ;
                            }
                        }
                    }

                    object.add(value);
                }
                list.add(object);
            }
        }

        if (! includeHeader ) {
            return list.toJSONString();
        }

        JSONObject ret = new JSONObject();
        JSONArray header = new JSONArray();
        JSONArray headerWithFilterIds = new JSONArray();
        for (ParsingFilter p : this.parsingFiltersWithMarkersAndGrabbers) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Name", p.getName());
            jsonObject.put("Id", p.getId());
            headerWithFilterIds.add(jsonObject);

            header.add(p.getName());
        }
        ret.put("data", list);
        ret.put("header", header);
        ret.put("headerWithIds", headerWithFilterIds);

        return ret.toJSONString();
    }




    /**
     * The current page is formatted as a List
     * @return The List for the parsed page.
     * @throws Exception
     */
    public  List<Map<String, String>> getParsedPageAsList(int page) throws Exception {

        List<Map<String, String>> list = new ArrayList();

        List<RowValue> rowsList = rowsForPage.get(page);

        for (RowValue rowValue : rowsList) {
            if (rowValue.isSkipRow()) continue;
            Map<String, ColumnValue> columnValues = rowValue.getColumnValues();
            Map<String, String> object = new HashMap<>();
            for (ParsingFilter p : this.parsingFiltersWithMarkersAndGrabbers) {
                String value = "";
                ColumnValue columnValue = columnValues.get(p.getId());
                if (columnValue != null) {
                    value = columnValue.getText();
                }
                object.put(p.getId(), value);
            }
            list.add(object);
        }
        return list;
    }



    /**
     * This is the parsing filters getter method.
     * @return List of parsing filters.
     */
    public List<ParsingFilter> getParsingFilters() {
        return parsingFilters;
    }

    /**
     * Sets the parsing filters maps
     * @param parsingFilters
     */
    public void setParsingFilters(List<ParsingFilter> parsingFilters) throws Exception {
        this.parsingFilters = parsingFilters;
        if (parsingFilters == null) return;

        int markersIndex = 0;
        List<Integer> markerPositions = new ArrayList<>();
        List<Integer> grabberPositions = new ArrayList<>();

        for (ParsingFilter p : parsingFilters) {
            parsingFiltersMap.put(p.getId(), p);
            parsingFiltersNameMap.put(p.getName(), p);
            String dataType = StringUtils.trimToEmpty(p.getDataType__c());
            if (p.isMarkerType()) {
                boolean validMarkerorGrabber = false;

                if (p.isSkipForBlankValue__c()) {
                    this.parsingFiltersWithSkipForBlankValue.add(p);
                }
                if (StringUtils.isNotBlank(p.getFormulaPattern__c())) {
                    parsingFiltersWithFormulaNameMap.put(p.getName(), p);
                    parsingFiltersWithFormulas.add(p);
                }
                if (StringUtils.isNotBlank(p.getFormattingPatternOutput__c())) {
                    parsingFiltersWithFormattingOutputMap.put(p.getId(), p);
                }
                if (StringUtils.isNotBlank(p.getFormattingPatternInput__c())) {
                    parsingFiltersWithFormattingInputMap.put(p.getId(), p);
                }
                if (StringUtils.isNotBlank(p.getFormulaPattern__c()) ||
                        (p.getColumnMarkerWidth__c() > 0 && StringUtils.isBlank(p.getGrabberType__c())) ) {
                    validMarkerorGrabber = true;
                    markerPositions.add(new Integer(markersIndex));
                    this.parsingFiltersWithMarkers.add(p);
                    this.parsingFiltersWithMarkersAndGrabbers.add(p);
                    this.dataTypes.add(p.getDataType__c());
                }
                else
                if (StringUtils.isNotBlank(p.getGrabberType__c())) {
                    boolean validGrabber = false;
                    if (StringUtils.startsWithIgnoreCase(p.getGrabberType__c(), "all") ||
                            StringUtils.containsIgnoreCase(p.getGrabberType__c(), "any non-space")) {
                        validGrabber = true;
                    } else if (StringUtils.isNotBlank(p.getGrabber1__c())) {
                        if (StringUtils.startsWithIgnoreCase(p.getGrabberType__c(), "between")) {
                            if (StringUtils.isNotBlank(p.getGrabber2__c())) {
                                validGrabber = true;
                            }
                        } else {
                            validGrabber = true;
                        }
                    }
                    if (validGrabber) {
                        validMarkerorGrabber = true;
                        grabberPositions.add(new Integer(markersIndex));
                        this.parsingFiltersWithGrabbers.add(p);
                        this.parsingFiltersWithMarkersAndGrabbers.add(p);
                        this.dataTypes.add(p.getDataType__c());
                    }
                }
                if (validMarkerorGrabber) {
                    markersIndex++;
                }

                // Compile the regular expression pattern if necessary.
                if (StringUtils.containsIgnoreCase(p.getGrabberType__c(), "regular")) {
                    if (StringUtils.isNotBlank(p.getGrabber1__c())) {
                        try {
                            Pattern pattern = Pattern.compile(p.getGrabber1__c());
                            p.setGrabber1Pattern(pattern);
                        }
                        catch (Exception e) {
                            throw new RuntimeException("Invalid regular expression pattern:  " + e.getMessage());
                        }
                    }
                }
                if (StringUtils.isNotBlank(p.getFindReplaceOld__c())) {
                    findAndReplaceSwitch = true;
                    try {
                        Pattern pattern = Pattern.compile(p.getFindReplaceOld__c());
                        p.setFindReplacePattern(pattern);
                    }
                    catch (Exception e) {
                        throw new RuntimeException("Invalid regular expression pattern:  " + e.getMessage());
                    }
                }
                if (StringUtils.isNotBlank(p.getFindReplaceNew__c())) {
                    findAndReplaceSwitch = true;
                }

                if (p.isSearchType1Mask() && StringUtils.isNotBlank(p.getGrabber1__c())) {
                    p.setSearchType1Pattern(convertMaskToRegExp(p.getGrabber1__c()));
                }
                if (p.isSearchType2Mask() && StringUtils.isNotBlank(p.getGrabber2__c())) {
                    p.setSearchType2Pattern(convertMaskToRegExp(p.getGrabber2__c()));
                }

                if (p.isSearchType1Regex() && StringUtils.isNotBlank(p.getGrabber1__c())) {
                    p.setSearchType1Pattern(Pattern.compile(p.getGrabber1__c()));
                }
                if (p.isSearchType2Regex()&& StringUtils.isNotBlank(p.getGrabber2__c())) {
                    p.setSearchType2Pattern(Pattern.compile(p.getGrabber2__c()));
                }
            } else if (p.isFilterType()) {
                parsingFiltersWithFilterTypes.add(p);
            }
        }
        this.markerTypePositions.addAll(grabberPositions);
        this.markerTypePositions.addAll(markerPositions);
    }

    public Pattern convertMaskToRegExp(String maskPattern) throws Exception{

        StringBuffer buffer = new StringBuffer(maskPattern.length() + 50);

        for (char c : maskPattern.toCharArray()) {

            if (c == 195) { // Alpha only code
                buffer.append("[a-zA-Z]");
            }
            else
            if (c == 223) { // Blank code
                buffer.append(" ");
            }
            else
            if (c == 209) { // Numerics code
                buffer.append("\\d");
            }
            else
            if (c == 248) { // Any code
                buffer.append("[\\W\\w\\d]");
            }
            else
            if (Character.isAlphabetic(c) || Character.isDigit(c)) {
                buffer.append(c);
            }
            else {
                buffer.append("\\").append(c);
            }
        }
        if (buffer.length() > 0 ) {
            Pattern pattern = Pattern.compile(buffer.toString());
            return pattern;
        }
        return null;
    }


    /**
     * This is the getter method for the scaling multiplier.
     * @return scaling multiplier
     */
    public double getScalingMultiplier() {
        return scalingMultiplier;
    }

    /**
     * This is the setter method for the scaling multiplier.
     */
    public void setScalingMultiplier(double scalingMultiplier) {
        this.scalingMultiplier = scalingMultiplier;
    }

    /**
     * This is the getter method for the grabbers map.
     * @return GrabberFilterValue Map
     */
    public Map<String, List<GrabberFilterValue>> getCurrentGrabberMap() {
        return currentGrabberMap;
    }

    /**
     * This is the setter method for the grabbers map.
     */
    public void setCurrentGrabberMap(Map<String, List<GrabberFilterValue>> currentGrabberMap) {
        this.currentGrabberMap = currentGrabberMap;
    }

    /**
     * Getter for the locale.
     * @return
     */
    public Locale getLocale() {
        return this.locale == null ? Locale.getDefault() : this.locale;
    }

    /**
     * Setter for the locale.
     * @param locale
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * Getter for timezone
     * @return TimeZone
     */
    public TimeZone getTimzone() {
        return this.timezone == null ? TimeZone.getDefault() : this.timezone;
    }

    public void setTimezone(TimeZone timzone) {
        this.timezone = timzone;
    }

    /**
     * Getter for the proccessed rows list.
     * @return List
     */
    public List<RowValue> getRows() {
        return rows;
    }

    /**
     * Getter for parsing filters with markers and grabbers
     * @return List
     */
    public List<ParsingFilter> getParsingFiltersWithMarkersAndGrabbers() {
        return parsingFiltersWithMarkersAndGrabbers;
    }

    /**
     * Setter for parsing filters with markers and grabbers
     */
    public void setParsingFiltersWithMarkersAndGrabbers(List<ParsingFilter> parsingFiltersWithMarkersAndGrabbers) {
        this.parsingFiltersWithMarkersAndGrabbers = parsingFiltersWithMarkersAndGrabbers;
    }

    /**
     * Getter for the parsing filters map
     * @return Map
     */
    public Map<String, ParsingFilter> getParsingFiltersMap() {
        return parsingFiltersMap;
    }

    /**
     * Setter for the parsing filters map
     */
    public void setParsingFiltersMap(Map<String, ParsingFilter> parsingFiltersMap) {
        this.parsingFiltersMap = parsingFiltersMap;
    }

    /**
     * Getter for the parsing filters map
     * @return Map
     */
    public Map<String, ParsingFilter> getParsingFiltersWithFormattingInputMap() {
        return parsingFiltersWithFormattingInputMap;
    }

    /**
     * Setter for the parsing filters map
     */
    public void setParsingFiltersWithFormattingInputMap(Map<String, ParsingFilter> parsingFiltersWithFormattingInputMap) {
        this.parsingFiltersWithFormattingInputMap = parsingFiltersWithFormattingInputMap;
    }

    /**
     * Getter for the parsing filters map
     * @return Map
     */
    public Map<String, ParsingFilter> getParsingFiltersWithFormattingOutputMap() {
        return parsingFiltersWithFormattingOutputMap;
    }

    /**
     * Setter for the parsing filters map
     */
    public void setParsingFiltersWithFormattingOutputMap(Map<String, ParsingFilter> parsingFiltersWithFormattingOutputMap) {
        this.parsingFiltersWithFormattingOutputMap = parsingFiltersWithFormattingOutputMap;
    }
}
