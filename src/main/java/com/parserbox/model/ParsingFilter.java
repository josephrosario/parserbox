package com.parserbox.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public class ParsingFilter {

    @JsonProperty("Id")
    private String Id;
    @JsonProperty("Name")
    private String Name;
    @JsonProperty("FilterType__c")
    private String FilterType__c;
    @JsonProperty("ColumnMarkerHeight__c")
    private double ColumnMarkerHeight__c;
    @JsonProperty("ColumnMarkerLeft__c")
    private double ColumnMarkerLeft__c;
    @JsonProperty("ColumnMarkerTop__c")
    private double ColumnMarkerTop__c;
    @JsonProperty("ColumnMarkerWidth__c")
    private double ColumnMarkerWidth__c;
    @JsonProperty("FilterLine1__c")
    private String FilterLine1__c;
    @JsonProperty("FilterBlanksType__c")
    private String FilterBlanksType__c;

    @JsonProperty("FindReplaceOld__c")
    private String FindReplaceOld__c;
    private Pattern FindReplacePattern;


    @JsonProperty("FindReplaceNew__c")
    private String FindReplaceNew__c;

    @JsonProperty("GrabberType__c")
    private String GrabberType__c;

    @JsonProperty("Grabber1__c")
    private String Grabber1__c;

    private Pattern SearchType1Pattern;
    private Pattern SearchType2Pattern;
    private Pattern Grabber1Pattern;

    @JsonProperty("Grabber2__c")
    private String Grabber2__c;

    @JsonProperty("SearchType1__c")
    private String SearchType1__c;

    @JsonProperty("SearchType2__c")
    private String SearchType2__c;

    @JsonProperty("FillBlanks__c")
    private boolean FillBlanks__c;

    @JsonProperty("SkipFirstOccurrence__c")
    private boolean SkipFirstOccurrence__c;

    @JsonProperty("MarkerTag__c")
    private boolean MarkerTag__c;

    @JsonProperty("SkipForBlankValue__c")
    private boolean SkipForBlankValue__c;

    @JsonProperty("Order__c")
    private String Order__c;

    @JsonProperty("DataType__c")
    private String DataType__c;

    @JsonProperty("ExpressionPattern__c")
    private String ExpressionPattern__c;

    @JsonProperty("FormulaPattern__c")
    private String FormulaPattern__c;

    @JsonProperty("FormattingPatternInput__c")
    private String FormattingPatternInput__c;

    @JsonProperty("FormattingPatternOutput__c")
    private String FormattingPatternOutput__c;

    @JsonProperty("IncludeInExport__c")
    private boolean IncludeInExport__c;

    @JsonProperty("ShowGrabberSearchString__c")
    private boolean ShowGrabberSearchString__c;

    @JsonProperty("JumpTo__c")
    private String JumpTo__c;

    @JsonProperty("GrabberValueContext__c")
    private String GrabberValueContext__c;


    private RowValue lastRowValueFound;


    public static final String MARKER_TYPE = "marker";
    public static final String FILTER_TYPE = "filter";

    public static final String SEARCH_TYPE_MASK = "M";
    public static final String SEARCH_TYPE_REGEX = "R";
    public static final String SEARCH_TYPE_STRING = "S";

    public boolean isFilterType() {
        return StringUtils.equalsIgnoreCase(getFilterType__c(), FILTER_TYPE);
    }
    public boolean isMarkerType() {
        return StringUtils.equalsIgnoreCase(getFilterType__c(), MARKER_TYPE);
    }
    public boolean isGrabber() {
        if (! isMarkerType()) return false;
        return StringUtils.isNotBlank(this.getGrabberType__c());
    }
    public boolean isMarkerColumn() {
        return this.getColumnMarkerWidth__c() > 0;
    }

    public boolean isSearchType1Regex() {return StringUtils.equalsIgnoreCase(getSearchType1__c(), SEARCH_TYPE_REGEX);}
    public boolean isSearchType2Regex() {return StringUtils.equalsIgnoreCase(getSearchType2__c(), SEARCH_TYPE_REGEX);}

    public boolean isSearchType1Mask() {return StringUtils.equalsIgnoreCase(getSearchType1__c(), SEARCH_TYPE_MASK);}
    public boolean isSearchType2Mask() {return StringUtils.equalsIgnoreCase(getSearchType2__c(), SEARCH_TYPE_MASK);}

    public boolean isSearchType1MaskorRegex() {return isSearchType1Mask() || isSearchType1Regex();}
    public boolean isSearchType2MaskorRegex() {return isSearchType2Mask() || isSearchType2Regex();}

    public boolean isSearchType1String() {
        return StringUtils.isBlank(getSearchType1__c()) ||
                StringUtils.equalsIgnoreCase(getSearchType1__c(), SEARCH_TYPE_MASK);
    }
    public boolean isSearchType2String() {
        return StringUtils.isBlank(getSearchType2__c()) ||
                StringUtils.equalsIgnoreCase(getSearchType2__c(), SEARCH_TYPE_MASK);
    }

    public boolean isNotFormattingInput() {
        return ! isFormattingInput();
    }
    public boolean isFormattingInput() {
        return StringUtils.isNotBlank(getFormattingPatternInput__c());
    }

    public boolean isNotFormattingOutput() {
        return ! isFormattingOutput();
    }
    public boolean isFormattingOutput() {
        return StringUtils.isNotBlank(getFormattingPatternOutput__c());
    }


    public boolean isNotFormulaMarker() {
        return ! isFormulaMarker();
    }
    public boolean isFormulaMarker() {
        return StringUtils.isNotBlank(getFormulaPattern__c());
    }


    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getFilterType__c() {
        return FilterType__c;
    }

    public void setFilterType__c(String filterType__c) {
        FilterType__c = filterType__c;
    }


    public double getColumnMarkerHeight__c() {
        return ColumnMarkerHeight__c;
    }

    public void setColumnMarkerHeight__c(double columnMarkerHeight__c) {
        ColumnMarkerHeight__c = columnMarkerHeight__c;
    }

    public double getColumnMarkerLeft__c() {
        return ColumnMarkerLeft__c;
    }

    public void setColumnMarkerLeft__c(double columnMarkerLeft__c) {
        ColumnMarkerLeft__c = columnMarkerLeft__c;
    }

    public double getColumnMarkerTop__c() {
        return ColumnMarkerTop__c;
    }

    public void setColumnMarkerTop__c(double columnMarkerTop__c) {
        ColumnMarkerTop__c = columnMarkerTop__c;
    }

    public double getColumnMarkerWidth__c() {
        return ColumnMarkerWidth__c;
    }

    public void setColumnMarkerWidth__c(double columnMarkerWidth__c) {
        ColumnMarkerWidth__c = columnMarkerWidth__c;
    }

    public String getFilterLine1__c() {
        return FilterLine1__c;
    }

    public void setFilterLine1__c(String filterLine1__c) {
        FilterLine1__c = filterLine1__c;
    }

    public String getFilterBlanksType__c() {
        return FilterBlanksType__c;
    }

    public void setFilterBlanksType__c(String filterBlanksType__c) {
        FilterBlanksType__c = filterBlanksType__c;
    }

    public String getFindReplaceOld__c() {
        return FindReplaceOld__c;
    }

    public void setFindReplaceOld__c(String findReplaceOld__c) {
        FindReplaceOld__c = findReplaceOld__c;
    }

    public Pattern getFindReplacePattern() {
        return FindReplacePattern;
    }

    public void setFindReplacePattern(Pattern findReplacePattern) {
        FindReplacePattern = findReplacePattern;
    }

    public Pattern getSearchType1Pattern() {
        return SearchType1Pattern;
    }

    public void setSearchType1Pattern(Pattern searchType1Pattern) {
        SearchType1Pattern = searchType1Pattern;
    }

    public Pattern getSearchType2Pattern() {
        return SearchType2Pattern;
    }

    public void setSearchType2Pattern(Pattern searchType2Pattern) {
        SearchType2Pattern = searchType2Pattern;
    }

    public String getFindReplaceNew__c() {
        return FindReplaceNew__c;
    }

    public void setFindReplaceNew__c(String findReplaceNew__c) {
        FindReplaceNew__c = findReplaceNew__c;
    }

    public String getGrabberType__c() {
        return GrabberType__c;
    }

    public void setGrabberType__c(String grabberType__c) {
        GrabberType__c = grabberType__c;
    }

    public String getGrabber1__c() {
        return Grabber1__c;
    }

    public void setGrabber1__c(String grabber1__c) {
        Grabber1__c = grabber1__c;
    }

    public Pattern getGrabber1Pattern() {
        return Grabber1Pattern;
    }

    public void setGrabber1Pattern(Pattern grabber1Pattern) {
        Grabber1Pattern = grabber1Pattern;
    }

    public String getGrabber2__c() {
        return Grabber2__c;
    }

    public void setGrabber2__c(String grabber2__c) {
        Grabber2__c = grabber2__c;
    }


    public String getSearchType1__c() {
        return SearchType1__c;
    }

    public void setSearchType1__c(String searchType1__c) {
        SearchType1__c = searchType1__c;
    }

    public String getSearchType2__c() {
        return SearchType2__c;
    }

    public void setSearchType2__c(String searchType2__c) {
        SearchType2__c = searchType2__c;
    }

    public boolean isFillBlanks__c() {
        return FillBlanks__c;
    }

    public void setFillBlanks__c(boolean fillBlanks__c) {
        FillBlanks__c = fillBlanks__c;
    }


    public boolean isSkipFirstOccurrence__c() {
        return SkipFirstOccurrence__c;
    }

    public void setSkipFirstOccurrence__c(boolean skipFirstOccurrence__c) {
        SkipFirstOccurrence__c = skipFirstOccurrence__c;
    }

    public String getDataType__c() {
        return DataType__c;
    }

    public void setDataType__c(String dataType__c) {
        DataType__c = dataType__c;
    }

    public String getExpressionPattern__c() {
        return ExpressionPattern__c;
    }

    public void setExpressionPattern__c(String expressionPattern__c) {
        ExpressionPattern__c = expressionPattern__c;
    }

    public String getFormulaPattern__c() {
        return FormulaPattern__c;
    }

    public void setFormulaPattern__c(String formulaPattern__c) {
        FormulaPattern__c = formulaPattern__c;
    }

    public String getFormattingPatternInput__c() {
        return FormattingPatternInput__c;
    }

    public void setFormattingPatternInput__c(String formattingPatternInput__c) {
        FormattingPatternInput__c = formattingPatternInput__c;
    }

    public String getFormattingPatternOutput__c() {
        return FormattingPatternOutput__c;
    }

    public void setFormattingPatternOutput__c(String formattingPatternOutput__c) {
        FormattingPatternOutput__c = formattingPatternOutput__c;
    }

    public String getJumpTo__c() {
        return JumpTo__c;
    }

    public void setJumpTo__c(String jumpTo__c) {
        JumpTo__c = jumpTo__c;
    }


    public String getGrabberValueContext__c() {
        return GrabberValueContext__c;
    }

    public void setGrabberValueContext__c(String grabberValueContext__c) {
        GrabberValueContext__c = grabberValueContext__c;
    }

    public boolean isMarkerTag__c() {
        return MarkerTag__c;
    }

    public void setMarkerTag__c(boolean markerTag__c) {
        MarkerTag__c = markerTag__c;
    }

    public String getOrder__c() {
        return Order__c;
    }

    public void setOrder__c(String order__c) {
        Order__c = order__c;
    }

    public boolean isShowGrabberSearchString__c() {
        return ShowGrabberSearchString__c;
    }

    public void setShowGrabberSearchString__c(boolean showGrabberSearchString__c) {
        ShowGrabberSearchString__c = showGrabberSearchString__c;
    }

    public boolean isIncludeInExport__c() {
        return IncludeInExport__c;
    }

    public void setIncludeInExport__c(boolean includeInExport__c) {
        IncludeInExport__c = includeInExport__c;
    }

    public boolean isSkipForBlankValue__c() {
        return SkipForBlankValue__c;
    }
    public void setSkipForBlankValue__c(boolean skipForBlankValue__c) {
        SkipForBlankValue__c = skipForBlankValue__c;
    }
}
