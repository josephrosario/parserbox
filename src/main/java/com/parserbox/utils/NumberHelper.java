package com.parserbox.utils;


import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import com.parserbox.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class NumberHelper  {
    public static final NumberFormat percentFormatter = NumberFormat.getPercentInstance();

    private static Log log = LogFactory.getLog(NumberHelper.class);

    /**
     * NumberHelper constructor comment.
     */
    public NumberHelper() {
        super();
    }


    public static int getInvValue(Integer v) {
        return (v == null) ? 0 : v.intValue();
    }

    public static double getDoubleValue(BigDecimal v) {
        return (v == null) ? 0.0 : v.doubleValue();
    }

    public static double getDoubleValue(Double v) {
        return (v == null) ? 0.0 : v.doubleValue();
    }

    public static String getDoubleString(Object obj) {
        double d = getDoubleValue(obj);
        return Double.toString(d);
    }

    public static double getDoubleValue(Object obj) {
        if (obj == null)
            return 0;
        if (obj instanceof String) {
            String str = (String)obj;
            return (StringUtils.isBlank(str)) ? 0 : Double.parseDouble(str);
        }
        if (obj instanceof Double)
            return ((Double) obj).doubleValue();
        if (obj instanceof BigDecimal)
            return ((BigDecimal) obj).doubleValue();
        return 0;
    }




    public static BigDecimal getCleanNumber(Object o, Locale locale) throws Exception {
        if (o == null) return null;
        if (o instanceof BigDecimal) return (BigDecimal) o;
        if (o instanceof Number) return new BigDecimal(((Number)o).doubleValue());
        if (o instanceof String) return getCleanNumber((String) o, locale);
        throw new IllegalArgumentException("Accept only BigDecimal, Number, or String");
    }

    public static BigDecimal getCleanNumber(String v,  Locale locale) throws Exception {
        if (StringUtils.isBlank(v) || "null".equals(v))
            return null;

        v = clean(v, locale);

        //handling formatted currency negative values
        if (v.startsWith("(") && v.endsWith(")")) {
            v = "-" + v.substring(1, v.length() - 1);
        }
        if (v.equals("-")) {
            return null;
        }
        if (v.endsWith("-")) {
            v = "-" + StringUtils.removeEnd(v, "-");
        }
        return new BigDecimal(v);
    }
    public static String clean(String v, Locale locale) throws Exception {
        if (v == null)
            return null;
        v = v.trim();

        if (v.length() == 0)
            return "";

        if (v.length() > 1) {
            v = StringUtils.remove(v, "$");
            if (locale != null) {
                try {
                    Currency curr = Currency.getInstance(locale);
                    if (curr != null) {
                        String s = curr.getSymbol();
                        v = StringUtils.removeStart(v, s);
                    }
                }
                catch(Exception e) {
                    log.info(e);
                }
            }
            v = StringUtils.removeEnd(v, "%");
            v = StringUtils.removeStart(v, "\"");
            v = StringUtils.removeEnd(v, "\"");
            v = StringUtils.removeStart(v, "'");
            v = StringUtils.removeEnd(v, "'");
            v = StringUtils.remove(v, ",");
        }

        return v;
    }
    public static int getInt(double dbl, boolean roundUp) {
        BigDecimal n = new BigDecimal(dbl);
        return roundUp ? n.setScale(0, BigDecimal.ROUND_UP).intValue() :
                n.setScale(0, BigDecimal.ROUND_DOWN).intValue();
    }

    public static String getReal(Object obj) {
        return getReal(obj, true);
    }

    public static String getReal(Object obj, boolean blanks_sw) {
        if (obj == null) {
            return (blanks_sw) ? "" : "0";
        }
        if (obj instanceof BigDecimal) {
            BigDecimal n = (BigDecimal) obj;
            return n.setScale(0, BigDecimal.ROUND_HALF_UP).toString();
        }
        return "";
    }

    public static String getString(double n, int scale) {
        return getString (new BigDecimal(n), scale);
    }
    public static String getString(BigDecimal n, int scale) {
        if (n == null) return "0.00";
        return n.setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    public static String getPercentageString(Object obj) {
        return getPercentageString(obj, true);
    }

    public static String getPercentageString(Object obj, boolean blanks_sw) {
        if (obj == null) {
            return (blanks_sw) ? "" : "0";
        }
        if (obj instanceof Number) {
            return Double.toString(((Number) obj).doubleValue());
        }
        return "";
    }

    public static String getPercentageString(Object obj, String dflt) {
        if (obj == null || !(obj instanceof Number)) return dflt;
        return NumberFormat.getPercentInstance().format(((Number)obj).doubleValue());
    }

    public static String getNonNull(Object obj) {
        if (obj == null)
            return "";
        return obj.toString();
    }

    public static String getNonNull(Object obj, String defaultStr) {

        if (obj == null)
            return defaultStr;
        return obj.toString();
    }

    public static BigDecimal getNegative(BigDecimal value) {
        if (value == null || value.doubleValue() < 0) return value;
        return value.negate();
    }

    public static BigDecimal getPositive(BigDecimal value) {
        if (value == null) return null;
        return value.abs();
    }

    /**
     * This method should be used for dollars.cents amounts
     */
    public static boolean isZero(Number n) {
        return n == null || Math.abs(n.doubleValue()) < Constants.EPS;
    }

    /**
     * This is a strict version which should be used for rates and values audit
     */
    public static boolean isZeroStrict(Number n) {
        return n == null || Math.abs(n.doubleValue()) < 0.00000001;
    }

    public static boolean isSameNumber(String s1, String s2) {
        if (!NumberUtils.isCreatable(s1) || !NumberUtils.isCreatable(s2))
            return false;

        // leading zeroes require special attention, lets just compare the strings before the decimal point
        // in other words, we don't want "01" to be equal "001"
        if ((s1.startsWith("0") || s2.startsWith("0")) &&
                !s1.startsWith("0E-") && !s1.startsWith("0e-") && !s2.startsWith("0E+") && !s2.startsWith("0e+")) {

            boolean check = StringUtils.substringBefore(s1, ".").equals(StringUtils.substringBefore(s2, "."));
            if (!check) return false;
        }

        Number n1 = new BigDecimal(s1);
        Number n2 = new BigDecimal(s2);
        return isSameNumber(n1, n2);
    }

    /**
     * Checks if 2 numbers represent the same value
     * The numbers can be of a different type, and still return true.
     * The comparision ignores changes beyond 8 decimal places
     *
     * IMPORTANT NOTE: a null and a zero are different values by design
     * if 2 null values are provided, the method returns true
     * @return
     */
    public static boolean isSameNumber(Number n1, Number n2) {

        // first check if both are nulls
        if (n1 == null && n2 == null) return true;

        // then check if one is null
        if (n1 == null || n2 == null) return false;

        // then use equals
        if (n1.equals(n2)) return true;

        // then use conversion to BigDecimal
        // there is a very small chance that some value is too big for double, so we will look if one of the values is already a BigDecimal
        BigDecimal base = null;
        BigDecimal other = null;
        if (n1 instanceof BigDecimal) {
            base = (BigDecimal)n1;
            other = (n2 instanceof BigDecimal) ? (BigDecimal)n2 : BigDecimal.valueOf(n2.doubleValue());
        } else if (n2 instanceof BigDecimal) {
            base = (BigDecimal)n2;
            other = (n1 instanceof BigDecimal) ? (BigDecimal)n1 : BigDecimal.valueOf(n1.doubleValue());
        } else {
            return isZeroStrict(n1.doubleValue() - n2.doubleValue());
        }

        return isZeroStrict(base.subtract(other));
    }



    public static String getPercentage(Number num) {
        double value = 0;
        if (num != null) {
            value = num.doubleValue();
        }
        DecimalFormat f = new DecimalFormat("##0.##");
        return f.format(value);
    }

    public static String getLongPercentage(Number num) {
        double value = 0;
        if (num != null) {
            value = num.doubleValue();
        }
        DecimalFormat f = new DecimalFormat("##0.########");
        return f.format(value);
    }

    public static double getPercentageValue(Number a, Number b) {
        if (a == null || b == null)
            return 0;
        return getPercentageValue(a.doubleValue(), b.doubleValue());
    }

    public static double getPercentageValue(int a, int b) {
        return getPercentageValue(new Integer(a).doubleValue(), new Integer(b).doubleValue());
    }

    public static double getPercentageValue(double a, double b) {
        return getPercentageValue(a, b, 2);
    }

    public static double getPercentageValue(double number, double total, int decimalPlace) {
        if (number == 0 || total == 0)
            return 0;
        BigDecimal c = new BigDecimal((number / total));
        BigDecimal m = c.multiply(new BigDecimal(100));
        double r = m.doubleValue();
        BigDecimal bd = new BigDecimal(r);
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        r = bd.doubleValue();
        return r;
    }

    public static double roundTo2Decimals(double d) {
        if (d == 0) return 0;
        return Math.round(d*100.0)/100.0;
    }

    public static int makePositive(int v) {
        return (v < 0) ? v * -1 : v;
    }

    public static double makePositive(double v) {
        return (v < 0) ? v * -1 : v;
    }

    public static long makePositive(long v) {
        return (v < 0) ? v * -1 : v;
    }

    public static BigDecimal makePositive(BigDecimal v) {
        return (v != null && v.doubleValue() < 0) ? v.negate() : v;
    }

    public static int makeNegative(int v) {
        return (v > 0) ? -v : v;
    }

    public static double makeNegative(double v) {
        return (v > 0) ? -v : v;
    }

    public static long makeNegative(long v) {
        return (v > 0) ? -v : v;
    }

    public static BigDecimal makeNegative(BigDecimal v) {
        return (v != null && v.doubleValue() > 0) ? v.negate() : v;
    }

    public static double getDouble(Object o) {
        if (o == null)
            return 0;
        if (o instanceof Number)
            return ((Number) o).doubleValue();
        if (o instanceof String) {
            String s = (String) o;
            if (StringUtils.isBlank(s))
                return 0;
            return new Double((String) o).doubleValue();
        }
        return 0;
    }

    public static int getInt(Object o) {
        if (o == null)
            return 0;
        if (o instanceof Number)
            return ((Number) o).intValue();
        if (o instanceof String) {
            String s = (String) o;
            if (StringUtils.isBlank(s))
                return 0;
            if(StringUtils.isNumeric(s))
                return new Integer((String) o).intValue();
        }
        return 0;
    }

    public static long getLong(Object o) {
        if (o == null)
            return 0;
        if (o instanceof Number)
            return ((Number) o).longValue();
        if (o instanceof String) {
            String s = (String) o;
            if (StringUtils.isBlank(s))
                return 0;
            return new Long((String) o).longValue();
        }
        return 0;
    }

    public static short getShort(Object o) {
        if (o == null)
            return 0;
        if (o instanceof Number)
            return ((Number) o).shortValue();
        if (o instanceof String) {
            String s = (String) o;
            if (StringUtils.isBlank(s))
                return 0;
            long l = 0;
            try {
                l = Long.parseLong(s.trim());
            } catch (NumberFormatException ex) { }

            if (l < Short.MIN_VALUE || l > Short.MAX_VALUE) {
                return 0;
            }

            return (short) l;
        }
        return 0;
    }

    //TODO: check usages, probably we can remove this method and just use add2
    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        if (a == null || b == null)
            return null;

        BigDecimal c = new BigDecimal(0);
        if (a != null)
            c.add(a);
        if (b != null)
            c.add(b);
        return c;
    }

    /**
     * same as add, but doesn't return null if one of the arguments is null
     * @return
     */
    public static BigDecimal add2(BigDecimal a, BigDecimal b) {
        if (a == null)
            return b;
        if (b == null)
            return a;
        return a.add(b);
    }

}
