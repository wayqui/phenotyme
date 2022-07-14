package es.upm.nlp.corenlpapi;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.tokensregex.MatchedExpression;
import edu.stanford.nlp.ling.tokensregex.types.Expressions;
import edu.stanford.nlp.ling.tokensregex.types.Value;
import edu.stanford.nlp.util.CoreMap;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Timex3Util {

    private static final Logger LOGGER = Logger.getLogger(Timex3Util.class.getName());

    static String iniSP = "-03-20";
    static String iniSU = "-06-21";
    static String iniFA = "-09-22";
    static String iniWI = "-12-21";

    public static String obtainTimex3Expression(MatchedExpression matched, int numval, String lastfullDATE) {
        CoreMap cm = matched.getAnnotation();

        Value v = matched.getValue();

        ArrayList<Expressions.PrimitiveValue> a = (ArrayList<edu.stanford.nlp.ling.tokensregex.types.Expressions.PrimitiveValue>) v.get();
        String typ = (String) a.get(0).get();
        String val = (String) a.get(1).get();
        String freq = (String) a.get(2).get();

        String rul = (String) a.get(4).get();

        LOGGER.info(typ + " | " + val + " | " + freq + " | " + rul);

        String anchorDate = lastfullDATE;
        String lastDATE = anchorDate;
        Pattern pAnchor = Pattern.compile("anchor\\((\\w+),(.),([^\\)]+)\\)");

        // TO DO: el get? poner los values!
        numval++;
        int ini = cm.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
        String text = cm.get(CoreAnnotations.TextAnnotation.class);
//        out.println(matched.getText() + " - " + matched.getCharOffsets());

        // To adapt to TE3 format - news mode
        if ((typ.equalsIgnoreCase("DATE") || typ.equalsIgnoreCase("TIME")) && val.startsWith("XXXX-XX") && anchorDate != null) {
            DateTime dt = new DateTime(lastfullDATE);
//                        DateTime dt = new DateTime(anchorDate);
            int month = dt.getMonthOfYear();
            int year = dt.getYear();
            val = year + "-" + String.format("%02d", month) + val.substring(7, val.length());
        } else if ((typ.equalsIgnoreCase("DATE") || typ.equalsIgnoreCase("TIME")) && val.startsWith("XXXX") && anchorDate != null) {
            DateTime dt = new DateTime(lastfullDATE);
//                        DateTime dt = new DateTime(anchorDate);
            int year = dt.getYear();
            val = year + val.substring(4, val.length());
        }

        // To adapt to TE3 format
        val = val.replaceAll("-X+", "");

        // TODO: also, use the dependency parsing to find modifiers
        // TODO: the ref can be other day...
        if (val.startsWith("Danchor(+,") && anchorDate != null) {
            String refDate = val.substring(10, val.length() - 1);
            val = getNextDate(anchorDate, refDate);
        } else if (val.startsWith("Danchor(-,") && anchorDate != null) {
            String refDate = val.substring(10, val.length() - 1);
            val = getLastDate(anchorDate, refDate);
        } else if (val.startsWith("Sanchor(+,") && anchorDate != null) {
            String refDate = val.substring(10, val.length() - 1);
            val = getNextSeason(anchorDate, refDate);
        } else if (val.startsWith("Sanchor(-,") && anchorDate != null) {
            String refDate = val.substring(10, val.length() - 1);
            val = getLastSeason(anchorDate, refDate);
        } else if (val.startsWith("Ranchor(+,") && anchorDate != null) {
            String gran = val.substring(10, val.length() - 1);
            DateTime dat = new DateTime(anchorDate);
            if (gran.equalsIgnoreCase("M")) {
                int day = dat.getDayOfMonth();
                int maxM = dat.dayOfMonth().getMaximumValue();
                val = (maxM - day) + "D";
            } else if (gran.equalsIgnoreCase("Y")) {
                int day = dat.getDayOfMonth();
                int maxM = dat.dayOfMonth().getMaximumValue();
                if (dat.getMonthOfYear() != 12) {
                    val = (12 - dat.getMonthOfYear()) + "M" + (maxM - day) + "D";
                } else {
                    val = (maxM - day) + "D";
                }
            }
        } else if (val.startsWith("Ranchor(-,") && anchorDate != null) {
            String gran = val.substring(10, val.length() - 1);
            DateTime dat = new DateTime(anchorDate);
            if (gran.equalsIgnoreCase("M")) {
                int day = dat.getDayOfMonth();
                val = day + "D";
            } else if (gran.equalsIgnoreCase("Y")) {
                int day = dat.getDayOfMonth();
                if (dat.getMonthOfYear() != 1) {
                    if (day == 1) {
                        val = (dat.getMonthOfYear() - 1) + "M";
                    } else {
                        val = (dat.getMonthOfYear() - 1) + "M" + (day - 1) + "D";
                    }
                } else {
                    val = (day - 1) + "D";
                }
            }
        } else if (val.startsWith("DWanchor(+,") && anchorDate != null) {
            String refDate = val.substring(11, val.length() - 1);
            val = getNextMonthS(new DateTime(anchorDate), refDate);
        } else if (val.startsWith("DWanchor(-,") && anchorDate != null) {
            String refDate = val.substring(11, val.length() - 1);
            val = getLastMonthS(new DateTime(anchorDate), refDate);
        } else if (val.startsWith("anchor") && anchorDate != null) {
            DateTime dt = new DateTime(anchorDate);

            Matcher m = pAnchor.matcher(val);
            m.find();
            String ref = m.group(1);
            String plus = m.group(2);
            String duration = m.group(3);

            LinkedHashMap<String, String> durations = new LinkedHashMap<String, String>();
            // If it is an anchor for a date (eg, "this month")
            if (plus.equalsIgnoreCase("x")) {
                durations.put(duration, "0");
            } else {
                durations = parseDuration(duration);
            }

            Set<String> durString = durations.keySet();

            for (Map.Entry<String,String> entry : durations.entrySet()) {
//                        for (String gran : durString) {
                String gran = entry.getKey();
                int plusI = Integer.valueOf(entry.getValue());

                // Needs to be more general, check if today, proceed otherwise if not
                if (gran.equalsIgnoreCase("D")) {

                    if (plus.equalsIgnoreCase("+")) {
                        dt = dt.plusDays(plusI);
                    } else if (plus.equalsIgnoreCase("-")) {
                        dt = dt.minusDays(plusI);
                    } else {
                        dt = new DateTime(lastfullDATE);
                        val = dt.toString("YYYY-MM-dd") + val.substring(val.lastIndexOf(")") + 1);

                    }
                } else if (gran.equalsIgnoreCase("M")) {
                    if (plus.equalsIgnoreCase("+")) {
                        dt = dt.plusMonths(plusI);
                    } else if (plus.equalsIgnoreCase("-")) {
                        dt = dt.minusMonths(plusI);
                    } else {
                        dt = new DateTime(lastfullDATE);
                        val = dt.toString("YYYY-MM");
                    }
                } else if (gran.equalsIgnoreCase("Y")) {
                    if (plus.equalsIgnoreCase("+")) {
                        dt = dt.plusYears(plusI);
                    } else if (plus.equalsIgnoreCase("-")) {
                        dt = dt.minusYears(plusI);
                    } else {
                        dt = new DateTime(lastfullDATE);
                        val = dt.toString("YYYY");
                    }
                } else if (gran.equalsIgnoreCase("CENT")) {
                    if (plus.equalsIgnoreCase("+")) {
                        dt = dt.plusYears(plusI * 100);
                    } else if (plus.equalsIgnoreCase("-")) {
                        dt = dt.minusYears(plusI * 100);
                    } else {
                        val = (dt.plusYears(100)).toString("YYYY");
                        if (val.length() == 4) {
                            val = val.substring(0, 2);
                        } else if (val.length() == 3) {
                            val = "0" + val.substring(0, 1);
                        } else {
                            val = "00";
                        }
                    }
                } else if (gran.equalsIgnoreCase("W")) {
                    if (plus.equalsIgnoreCase("+")) {
                        dt = dt.plusWeeks(plusI);
                    } else if (plus.equalsIgnoreCase("-")) {
                        dt = dt.minusWeeks(plusI);
                    } else {
                        val = dt.toString("YYYY") + "-W" + String.format("%02d", dt.getWeekOfWeekyear());
                    }
                } else if (gran.equalsIgnoreCase("H")) {
                    if (plus.equalsIgnoreCase("+")) {
                        dt = dt.plusHours(plusI);
                    } else {
                        dt = dt.minusHours(plusI);
                    }
                } else if (gran.equalsIgnoreCase("MIN")) {
                    if (plus.equalsIgnoreCase("+")) {
                        dt = dt.plusMinutes(plusI);
                    } else {
                        dt = dt.minusMinutes(plusI);
                    }
                } else if (gran.equalsIgnoreCase("S")) {
                    if (plus.equalsIgnoreCase("+")) {
                        dt = dt.plusSeconds(plusI);
                    } else {
                        dt = dt.minusSeconds(plusI);
                    }

                } else if (gran.equalsIgnoreCase("DAYW")) {
                    if (plus.equalsIgnoreCase("+")) {
                        dt = getNextDayWeek(dt, plusI);
                    } else if (plus.equalsIgnoreCase("-")) {
                        dt = getLastDayWeek(dt, plusI);
                    } else if (plus.equalsIgnoreCase("z")) {
                        int current = dt.getDayOfWeek();
                        if (plusI <= current) {
                            dt = dt.minusDays(current - plusI);
                        } else {
                            dt = dt.plusDays(plusI - current);
                        }
                    }
                } else if (gran.startsWith("Q")) {
                    if (plus.equalsIgnoreCase("x") && plus.matches("Q\\d+")) {
                        val = dt.toString("YYYY") + "-" + gran;
                    } else {
                        if (plus.equalsIgnoreCase("+")) {
                            dt = dt.plusMonths(3 * plusI);
                        } else if (plus.equalsIgnoreCase("-")) {
                            dt = dt.minusMonths(3 * plusI);
                        }
                        if (dt.getMonthOfYear() < 4) {
                            val = dt.toString("YYYY") + "-Q1";
                        } else if (dt.getMonthOfYear() < 7) {
                            val = dt.toString("YYYY") + "-Q2";
                        } else if (dt.getMonthOfYear() < 10) {
                            val = dt.toString("YYYY") + "-Q3";
                        } else {
                            val = dt.toString("YYYY") + "-Q4";
                        }
                    }
                } else if (gran.startsWith("HALF")) {
                    if (plus.equalsIgnoreCase("x") && plus.matches("HALF\\d+")) {
                        val = dt.toString("YYYY") + "-" + gran.replaceFirst("ALF", "");
                    } else {
                        if (plus.equalsIgnoreCase("+")) {
                            dt = dt.plusMonths(6 * plusI);
                        } else if (plus.equalsIgnoreCase("-")) {
                            dt = dt.minusMonths(6 * plusI);
                        }
                        if (dt.getMonthOfYear() < 7) {
                            val = dt.toString("YYYY") + "-H1";
                        } else {
                            val = dt.toString("YYYY") + "-H2";
                        }
                    }
                } else if (gran.startsWith("T")) {
                    if (plus.equalsIgnoreCase("x") && plus.matches("T\\d+")) {
                        val = dt.toString("YYYY") + "-" + gran;
                    } else {
                        if (plus.equalsIgnoreCase("+")) {
                            dt = dt.plusMonths(4 * plusI);
                        } else if (plus.equalsIgnoreCase("-")) {
                            dt = dt.minusMonths(4 * plusI);
                        }
                        if (dt.getMonthOfYear() < 5) {
                            val = dt.toString("YYYY") + "-T1";
                        } else if (dt.getMonthOfYear() < 9) {
                            val = dt.toString("YYYY") + "-T2";
                        } else {
                            val = dt.toString("YYYY") + "-T3";
                        }
                    }
                } else if (gran.equalsIgnoreCase("MONTHS")) {
                    if (plus.equalsIgnoreCase("+")) {
                        dt = getNextMonth(dt, plusI);
                    } else {
                        dt = getLastMonth(dt, plusI);
                    }
                }
            }

            if (val.matches("anchor\\([A-Z]+,.,.*(\\d+)W\\)")) {
                val = dt.getYear() + "-W" + String.format("%02d", dt.getWeekOfWeekyear());
            } else if (val.matches("anchor\\([A-Z]+,.,.*(\\d+)Y\\)")) {
                val = dt.toString("YYYY");
            } else if (val.matches("anchor\\([A-Z]+,.,.*(\\d+)M\\)")) {
                val = dt.toString("YYYY-MM");
            } else if (val.matches("\\d{0,4}-[H|T|Q]\\d")) {
            } else if (!plus.equalsIgnoreCase("x")) {
                val = dt.toString("YYYY-MM-dd") + val.substring(val.lastIndexOf(")") + 1);
            } else {

            }
        }

        if ((typ.equalsIgnoreCase("DURATION") || typ.equalsIgnoreCase("SET"))) {
            LinkedHashMap<String, String> auxVal = parseDuration(val);
            String auxfin = "P";
            int flagT = 0;
            int mins = 0;
            Set<String> durString = auxVal.keySet();
            for (String gran : durString) {
                if ((gran.equalsIgnoreCase("AF") || gran.equalsIgnoreCase("MO") || gran.equalsIgnoreCase("MI") || gran.equalsIgnoreCase("EV") || gran.equalsIgnoreCase("NI")) && flagT == 0) {
                    flagT = 1;
                    auxfin = auxfin + "T" + auxVal.get(gran).replaceFirst("\\.0", "") + gran;
                } else if (gran.equalsIgnoreCase("H") && flagT == 0) {
                    flagT = 1;
                    auxfin = auxfin + "T" + auxVal.get(gran).replaceFirst("\\.0", "") + gran;
                } else if (gran.equalsIgnoreCase("MIN") && flagT == 0) {
                    flagT = 1;
                    auxfin = auxfin + "T" + auxVal.get(gran).replaceFirst("\\.0", "") + "M";
                } else if (gran.equalsIgnoreCase("HALF")) {
                    flagT = 1;
                    auxfin = auxfin + auxVal.get(gran).replaceFirst("\\.0", "") + "H";
                } else if (gran.equalsIgnoreCase("S") && flagT == 0) {
                    flagT = 1;
                    auxfin = auxfin + "T" + auxVal.get(gran).replaceFirst("\\.0", "") + gran;
                } else {
                    auxfin = auxfin + auxVal.get(gran).replaceFirst("\\.0", "") + gran;
                }
            }
            val = auxfin;
            val = val.replaceFirst("MIN", "M");
            val = val.replaceFirst("HALF", "H");

        }
        if (typ.equalsIgnoreCase("TIME") && val.startsWith("T")) {
            val = lastfullDATE + val;
        }
        if (typ.equalsIgnoreCase("TIME") && val.matches("....-..-..(Tanchor\\(.*,.*,.*\\))*.*")) { //for date + time anchorbug
            val = val.replaceAll("(anchor\\(.*,.*,.*\\))", "");
            val = val.replaceAll("T+", "T");
        }

        if (typ.equalsIgnoreCase("DATE") && val.matches("\\d\\d\\d\\d-\\d\\d-\\d\\d")) {
            lastfullDATE = val;
        }

        if (typ.equalsIgnoreCase("TIME") && val.startsWith("\\d\\d\\d\\d-\\d\\d-\\d\\d")) {
            lastfullDATE = val.substring(0, 10);
        }

        if (typ.equalsIgnoreCase("DATE")) {
            lastDATE = val;
        }

        String addini = "<TIMEX3 tid=\"t" + numval + "\" type=\"" + typ + "\" value=\"" + val + "\">";
        if (!freq.isEmpty()) {
            addini = "<TIMEX3 tid=\"t" + numval + "\" type=\"" + typ + "\" value=\"" + val + "\" freq=\"" + freq + "\">";

        }

        String addfin = "</TIMEX3>";

        String toAdd = addini + text + addfin;
        if (text.endsWith(" ,")) {
            toAdd = addini + text.substring(0, text.length() - 2) + addfin + " ,";
        } else if (text.endsWith(",")) {
            toAdd = addini + text.substring(0, text.length() - 1) + addfin + ",";
        } else if (text.endsWith(" .")) {
            toAdd = addini + text.substring(0, text.length() - 2) + addfin + " .";
        } else if (text.endsWith(".")) {
            toAdd = addini + text.substring(0, text.length() - 1) + addfin + ".";
        } else if (text.endsWith(" ;")) {
            toAdd = addini + text.substring(0, text.length() - 2) + addfin + " ;";
        } else if (text.endsWith(";")) {
            toAdd = addini + text.substring(0, text.length() - 1) + addfin + ";";
        }

        //inp2 = inp2.substring(0, ini + offsetdelay) + toAdd + inp2.substring(ini + text.length() + offsetdelay);

        //offsetdelay = offsetdelay + toAdd.length() - text.length();


        LOGGER.info(toAdd);
        return toAdd;
    }


    /**
     * Returns the next month monthS given a date dt and the rank of a month
     * monthS
     *
     * @param dt DateTime of reference
     * @param monthS int with the rank of a month
     *
     * @return DateTime with the new date
     */
    public static DateTime getNextMonth(DateTime dt, int monthS) {
        int current = dt.getMonthOfYear();
        if (monthS <= current) {
            monthS += 12;
        }
        DateTime next = dt.plusMonths(monthS - current);
        return next;
    }

    /**
     * Returns the last month monthS given a date dt and the rank of a month
     * monthS
     *
     * @param dt DateTime of reference
     * @param monthS int with the rank of a month
     *
     * @return DateTime with the new date
     */
    public static DateTime getLastMonth(DateTime dt, int monthS) {
        int current = dt.getMonthOfYear();
        if (monthS < current) {
            monthS = current - monthS;
        } else {
            monthS = 12 - monthS + current;
        }
        DateTime next = dt.minusMonths(monthS);
        return next;
    }

    /**
     * Returns the next day of the week dayW given a date dt and the rank of a
     * day of the week dayW
     *
     * @param dt DateTime of reference
     * @param dayW int with the rank of a day of the week
     *
     * @return DateTime with the new date
     */
    public static DateTime getNextDayWeek(DateTime dt, int dayW) {
        int current = dt.getDayOfWeek();
        if (dayW <= current) {
            dayW += 7;
        }
        DateTime next = dt.plusDays(dayW - current);
        return next;
    }

    /**
     * Returns the last day of the week dayW given a date dt and the rank of a
     * day of the week dayW
     *
     * @param dt DateTime of reference
     * @param dayW int with the rank of a day of the week
     *
     * @return DateTime with the new date
     */
    public static DateTime getLastDayWeek(DateTime dt, int dayW) {
        int current = dt.getDayOfWeek();
        if (dayW < current) {
            dayW = current - dayW;
        } else {
            dayW = 7 - dayW + current;
        }
        DateTime next = dt.minusDays(dayW);
        return next;
    }

    /**
     * Returns the next month given a date dt and the name of a month
     *
     * @param dt DateTime of reference
     * @param monthSS int with the name of a month
     *
     * @return String with the new date
     */
    public static String getNextMonthS(DateTime dt, String monthSS) {
        int current = dt.getMonthOfYear();
        String a = monthSS.replaceAll("MONTHS", "");
        int monthS = Integer.valueOf(a);
        String next;
        if (monthS <= current) {
            next = (dt.getYear() + 1) + "-" + String.format("%02d", monthS);
        } else {
            next = dt.getYear() + "-" + String.format("%02d", monthS);
        }
        return next;
    }

    /**
     * Returns the last month given a date dt and the name of a month
     *
     * @param dt DateTime of reference
     * @param monthSS int with the name of a month
     *
     * @return String with the new date
     */
    public static String getLastMonthS(DateTime dt, String monthSS) {
        int current = dt.getMonthOfYear();
        String a = monthSS.replaceAll("MONTHS", "");
        int monthS = Integer.valueOf(a);
        String next;
        if (monthS >= current) {
            next = (dt.getYear() + 1) + "-" + String.format("%02d", monthS);
        } else {
            next = dt.getYear() + "-" + String.format("%02d", monthS);
        }
        return next;
    }

    /**
     * Returns the next date given a referece date dt and a relative date refD
     *
     * @param dt DateTime of reference
     * @param refD String with the relative date
     *
     * @return String with the new date
     */
    public static String getNextDate(String dt, String refD) {
        DateTime dtDT = new DateTime(dt);
        if (refD.matches("\\d\\d\\d\\d-\\d\\d(-\\d\\d)?")) {
            return refD;
        } else if (refD.matches("XXXX-\\d\\d-\\d\\d")) {
            refD = refD.replaceAll("XXXX", dt.substring(0, 4));
            DateTime refDDT = new DateTime(refD);
            if (refDDT.isAfter(dtDT)) {
                return refD;
            } else {
                return refDDT.plusYears(1).toString("YYYY-MM-dd");
            }
        } else if (refD.matches("XXXX-XX-\\d\\d")) {
            refD = refD.replaceAll("XXXX", dt.substring(0, 4));
            refD = refD.replaceAll("XX", dt.substring(5, 7));
            DateTime refDDT = new DateTime(refD);
            if (refDDT.isAfter(dtDT)) {
                return refD;
            } else {
                return refDDT.plusMonths(1).toString("YYYY-MM-dd");
            }
        }
        return refD;
    }

    /**
     * Returns the last date given a referece date dt and a relative date refD
     *
     * @param dt DateTime of reference
     * @param refD String with the relative date
     *
     * @return String with the new date
     */
    public static String getLastDate(String dt, String refD) {
        DateTime dtDT = new DateTime(dt);
        if (refD.matches("\\d\\d\\d\\d-\\d\\d(-\\d\\d)?")) {
            return refD;
        } else if (refD.matches("XXXX-\\d\\d-\\d\\d")) {
            refD = refD.replaceAll("XXXX", dt.substring(0, 4));
            DateTime refDDT = new DateTime(refD);
            if (refDDT.isBefore(dtDT)) {
                return refD;
            } else {
                return refDDT.minusYears(1).toString("YYYY-MM-dd");
            }
        } else if (refD.matches("XXXX-XX-\\d\\d")) {
            refD = refD.replaceAll("XXXX", dt.substring(0, 4));
            refD = refD.replaceAll("XX", dt.substring(5, 7));
            DateTime refDDT = new DateTime(refD);
            if (refDDT.isBefore(dtDT)) {
                return refD;
            } else {
                return refDDT.minusMonths(1).toString("YYYY-MM-dd");
            }
        }

        return refD;
    }

    /**
     * Returns the next season given a referece date dt and a season
     *
     * @param dt DateTime of reference
     * @param refD String with the season
     *
     * @return String with the new date
     */
    public static String getNextSeason(String dt, String refD) {
        if (refD.matches("\\d\\d\\d\\d-[A-Z][A-Z]")) {
            return refD;
        }
        String year = dt.substring(0, 4);
        String season = refD.substring(4, 7);
        String seasondate = year;
        DateTime dtDT = new DateTime(dt);
        if (season.equalsIgnoreCase("-SU")) {
            seasondate = seasondate + iniSU;
        } else if (season.equalsIgnoreCase("-SP")) {
            seasondate = seasondate + iniSP;
        } else if (season.equalsIgnoreCase("-WI")) {
            seasondate = seasondate + iniWI;
        } else if (season.equalsIgnoreCase("-FA")) {
            seasondate = seasondate + iniFA;
        }
        DateTime refDDT = new DateTime(seasondate);

        if (refDDT.isAfter(dtDT)) {
            return year + season;
        } else {
            return refDDT.plusYears(1).toString("YYYY") + season;
        }
    }

    /**
     * Returns the last season given a referece date dt and a season
     *
     * @param dt DateTime of reference
     * @param refD String with the season
     *
     * @return String with the new date
     */
    public static String getLastSeason(String dt, String refD) {
        if (refD.matches("\\d\\d\\d\\d-[A-Z][A-Z]")) {
            return refD;
        }
        String year = dt.substring(0, 4);
        String season = refD.substring(4, 7);
        String seasondate = year;
        DateTime dtDT = new DateTime(dt);
        if (season.equalsIgnoreCase("-SU")) {
            seasondate = seasondate + iniSU;
        } else if (season.equalsIgnoreCase("-SP")) {
            seasondate = seasondate + iniSP;
        } else if (season.equalsIgnoreCase("-WI")) {
            seasondate = seasondate + iniWI;
        } else if (season.equalsIgnoreCase("-FA")) {
            seasondate = seasondate + iniFA;
        }
        DateTime refDDT = new DateTime(seasondate);

        if (refDDT.isBefore(dtDT)) {
            return year + season;
        } else {
            return refDDT.minusYears(1).toString("YYYY") + season;
        }
    }

    /**
     * Function that parses a String with several durations
     *
     * @param input String with concatenated durations
     *
     * @return Map with durations (granularity,amount)
     */
    public static LinkedHashMap<String, String> parseDuration(String input) {
        LinkedHashMap<String, String> durations = new LinkedHashMap<String, String>();
        Pattern pAnchor = Pattern.compile("(\\d*\\.?\\d+|X)([a-zA-Z]+)");

        Matcher m = pAnchor.matcher(input);
        while (m.find()) {
            String numb = m.group(1);
            String unit = m.group(2);
            durations.put(unit, numb);

        }
//        Pattern pAnchor = Pattern.compile("anchor\\((\\w+),([+-]?\\d+),(\\w+)\\)");

        return durations;
    }

}
