/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package showsuspect.model;

import com.ptc.services.common.tools.DateUtil;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author veckardt
 */
public class HistoryEntry {

    public static String dtDayTimeFormat = DateUtil.DEFAULT_DATETIMEONLY_FORMAT;
    public static Locale locale = Locale.getDefault();
    public static SimpleDateFormat dtTime = new SimpleDateFormat(dtDayTimeFormat, locale);
    String id;
    String referencesId;
    String user;
    Date date;
    String fieldName;
    String itemType;
    String fieldDisplayName;
    // String value;
    String richValue;
    String currValue;
    String prevValue;

    public String getUser() {
        return user;
    }

    public Date getDate() {
        return date;
    }

    public String getDateFormatted() {
        return dtTime.format(date);
    }

    public String getId() {
        return id;
    }

    public String getCurrValue() {
        return currValue;
    }

    public String getPrevValue() {
        return prevValue;
    }

    public String getTypeAndId() {
        return itemType.replace("Shared ", "").substring(0, 1) + "-" + id;
    }

    public String getDisplayDate() {
        return DateUtil.getDateDisplayString(date);
    }

    public String getFieldName() {
        return fieldName.replace("Shared ", "");
    }

    public String getFieldDisplayName() {
        return fieldDisplayName;
    }

    public String getAttachment() {
        String attachmentRef = "attachmentname=";
        if (richValue.contains(attachmentRef)) {
            Integer num = richValue.split(attachmentRef, -1).length - 1;
            return "" + num.toString() + "*";
        }
        return "";
    }

    public String getDisplayValue() {

        String result = removeTags(richValue.replace("&#160;", " "));
        return result.trim();
    }

    public String getRichValue() {
        return richValue;
    }
    private static final Pattern REMOVE_TAGS = Pattern.compile("<.+?>");

    public static String removeTags(String string) {
        if (string == null || string.length() == 0) {
            return string;
        }

        Matcher m = REMOVE_TAGS.matcher(string);
        return m.replaceAll("").trim();
    }

    @Override
    public String toString() {
        return "[" + id + ", " + itemType + ", " + referencesId + ", " + user + ", " + date + ", " + fieldName + ", " + richValue + "]";
    }

    public String getReferencesId() {
        return referencesId;
    }

    public String getItemType() {
        return itemType;
    }

    public String getMainType() {
        return itemType.replace("Shared ", "");
    }

    public HistoryEntry(String id,
            String itemType,
            String referencesId,
            String user,
            Date date,
            String fieldName,
            String fieldDisplayName,
            String richValue,
            String currValue,
            String prevValue) {
        this.id = id;
        this.itemType = itemType;
        this.referencesId = referencesId;
        this.user = user;
        this.date = date;
        this.fieldName = fieldName;
        this.fieldDisplayName = fieldDisplayName;
        // this.value = value;
        this.richValue = richValue;
        this.currValue = currValue;
        this.prevValue = prevValue;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
