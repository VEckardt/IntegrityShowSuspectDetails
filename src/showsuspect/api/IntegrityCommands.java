/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package showsuspect.api;

import com.mks.api.Command;
import com.mks.api.Option;
import com.mks.api.response.APIException;
import com.mks.api.response.Field;
import com.mks.api.response.Item;
import com.mks.api.response.Response;
import com.mks.api.response.WorkItem;
import com.mks.api.response.WorkItemIterator;
import com.ptc.services.common.api.ExceptionHandler;
import com.ptc.services.common.api.IntegrityAPI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TextArea;
import showsuspect.ShowSuspectController;
import showsuspect.model.FieldDef;
import showsuspect.model.HistoryEntry;

/**
 *
 * @author veckardt
 */
public class IntegrityCommands extends IntegrityAPI {

    static Map<String, String> env = System.getenv();
    private final HashMap<String, FieldDef> fieldsMap = new HashMap<>();
    // private final HashMap<String, Type> typeMap = new HashMap<>();
    // private final HashMap<String, String> fvaFieldsMap = new HashMap<>();
    // private final HashMap<String, String> referencedMap = new HashMap<>();
    private boolean debugFlag = false;
    private TextArea logArea;
    private static final SimpleDateFormat dt = new SimpleDateFormat("MMM d, yyyy h:mm:ss a", Locale.US);
    private static String referencesID = "";
    //-------------------------------------------------------
    public static final String REFERENCES = "References";
    public static final String SIGNIFICANT__EDIT__DATE_FIELD = "Significant Edit Date";
    public static final String MODIFIED__BY_FIELD = "Modified By";
    public static final String SIGNIFICANT_EDIT_FIELD = "significantEdit";
    public static final String MKS_ISSUE_HISTORY = "MKSIssueHistory";
    public static final String MKS_ISSUE_MODIFIED_DATE = "MKSIssueModifiedDate";
    public static final String MKS_ISSUE_MODIFIED_BY = "MKSIssueModifiedBy";
    public static final String BACKED_BY = "backedBy";
    public static final String BRANCH = "Branch";
    public static final String BASE = "Base";
    public static final String SHARED = "Shared";

    /**
     * default constructor
     */
    public IntegrityCommands() {
        super(env, "IntegrityShowSuspect");
    }

    /**
     * set the textArea for Logging
     *
     * @param logArea
     */
    public void setLogArea(TextArea logArea) {
        this.logArea = logArea;
    }

    /**
     * Creates a List of HistoryEntry object, absed on the field changes that
     * caused the suspect flagging
     *
     * @param selection the item ID of interest
     * @param docClass currently unused ???
     * @param validFieldsTrace The relationships of interest
     * @return A Lsi of the Entries that could have triggered the suspect
     * flagging
     */
    public ArrayList<HistoryEntry> initSuspectEntryList(String selection, String docClass, String validFieldsTrace) {
        // a result container
        ArrayList<HistoryEntry> historyList = new ArrayList();

        // the current Item to analyze
        WorkItem wiSelection = getWorkItemWithHistory(selection, "");

        try {

            Field historyField = wiSelection.getField(MKS_ISSUE_HISTORY);

            // check if the field is currently suspect
            // if not, do noting
            for (String traceFld : validFieldsTrace.split(",")) {

                log("Checking relationship '" + traceFld + "' ...");
                try {
                    wiSelection.getField(traceFld);
                } catch (NoSuchElementException ex) {
                    continue;
                }

                //
                // get content of relationship field, may come with: 123,234?,445
                //
                String traceIDs = wiSelection.getField(traceFld).getValueAsString();
                //
                // so at least one suspect? would be indicate by the '?'
                //
                if (traceIDs.contains("?")) {

                    log("Relationship '" + traceFld + "' is currently suspect!");

                    // iterate related traceIDs for the given field
                    for (String traceID : traceIDs.split(",")) {

                        // and check by traceID if it is suspect
                        if (traceID.contains("?")) {

                            // purify the traceID's String to get the 'tracedItemID'
                            String tracedItemID = traceID.replace("?", "");
                            log("Found traced item " + tracedItemID + " ...");

                            // walk through all historical entries for the current item
                            for (Object historyItem : historyField.getList()) {

                                Item histEntry = (Item) historyItem;
                                //log("checking  histEntry # " + histEntry.getId() + " ...");                               
                                String modBy = histEntry.getField(MKS_ISSUE_MODIFIED_BY).getValueAsString();
                                Date modDate = histEntry.getField(MKS_ISSUE_MODIFIED_DATE).getDateTime();

                                // and check if the historical entry contains the field under review
                                // if not, just move to the next entry in the history
                                Field traceStatusField;
                                try {
                                    // both must be fulfilled, otherwise step over and go to the next entry in the history
                                    histEntry.getField(traceFld); //a Change in the Trace Relationship Field(probably flagged as suspect) 
                                    traceStatusField = histEntry.getField(ShowSuspectController.TRACE_STATUS_FIELD); //any Change in the 'Trace Status' Field 
                                    // log("Found historical entry with '" + traceFld + "'");
                                } catch (NoSuchElementException ex) {
                                    // log("Skippend this historical entry.");
                                    continue; //step over and go to the next entry in the history

                                }
                                // get the "historical Item" one second before this history event                                 
                                WorkItem wiBeforeSuspect = getWorkItem(selection, ShowSuspectController.TRACE_STATUS_FIELD, removeSecond(modDate));

                                if (wiBeforeSuspect != null) {
                                    // get the prior 'Trace Status' value, to check if the Item has already been SUSPECT                                    
                                    String traceStatusBeforeSuspect = getStringValue(wiBeforeSuspect, ShowSuspectController.TRACE_STATUS_FIELD);

                                    Boolean found = false;
                                    if (traceStatusField.getValueAsString().contains(ShowSuspectController.upstreamSuspect)
                                            && !traceStatusBeforeSuspect.contains(ShowSuspectController.upstreamSuspect)) {
                                        // item has become suspect on the current history entry
                                        log("Item has become suspect on the " + modDate + " based on a change in item " + tracedItemID, 1);

                                        // Get Type of the related Item
                                        String relTypeName = this.getItemType(tracedItemID);

                                        // Get Significant Edit Fields of the related Item
                                        WorkItem baseItem = this.getTypeDetails(relTypeName, SIGNIFICANT_EDIT_FIELD);
                                        String signEditFields = baseItem.getField(SIGNIFICANT_EDIT_FIELD).getValueAsString();
                                        log(signEditFields, 1);
                                        // init "local field Map <name,workItem>"
                                        log("Fields in stack: " + this.allFields.size());
                                        this.readFields(null, signEditFields);
                                        log("Fields in stack: " + this.allFields.size());

                                        // get the current value
                                        log("Step 1: Get current item values from " + tracedItemID + " ...", 1);
                                        WorkItem wiCurrent = getWorkItem(tracedItemID, signEditFields + "," + MODIFIED__BY_FIELD + "," + SIGNIFICANT__EDIT__DATE_FIELD + "," + REFERENCES, null);

                                        // get the value one second before the change happend
                                        log("Step 2: Get item values from " + tracedItemID + "before significant change  ...", 1);
                                        WorkItem wiBefore = getWorkItem(tracedItemID, signEditFields, removeSecond(modDate));

                                        // get the value one second AFTER the change happend, to 'isolate' the actual change
                                        log("Step 3: Get item values from " + tracedItemID + "after significant change ...", 1);
                                        WorkItem wiAtChange = getWorkItem(tracedItemID, MODIFIED__BY_FIELD + "," + SIGNIFICANT__EDIT__DATE_FIELD, addSecond(modDate));

                                        if (wiBefore != null) {
                                            // iterate the sigificante edit fields and compare them individually
                                            for (String signEditField : signEditFields.split(",")) {
                                                log("Checking field '" + signEditField + "' ...");

                                                if (wiBefore.getField(signEditField) == null && wiCurrent.getField(signEditField) == null) {
                                                    //log("Both values are null, continuing ...");                                                    
                                                    continue;
                                                }

                                                // get the current fields historical values
                                                String valueBefore = getStringValue(wiBefore, signEditField);
                                                String valueCurrent = getStringValue(wiCurrent, signEditField);

                                                log(signEditField + ": before=" + valueBefore + " #|# current=" + valueCurrent);

                                                if (!valueBefore.contentEquals(valueCurrent)) {
                                                    log("Field content for '" + signEditField + "' has changed!");

                                                    HistoryEntry hEntry = new HistoryEntry(
                                                            tracedItemID,
                                                            relTypeName,
                                                            null, // referencedMap.get(tracedItemID),
                                                            getStringValue(wiAtChange, MODIFIED__BY_FIELD),
                                                            wiCurrent.getField(SIGNIFICANT__EDIT__DATE_FIELD).getDateTime(),
                                                            // modDate,
                                                            signEditField,
                                                            getFieldDisplayName(signEditField),
                                                            valueCurrent,
                                                            valueCurrent,
                                                            valueBefore);
                                                    searchLatestChangeRec(tracedItemID, wiCurrent.getField(REFERENCES).getValueAsString(), hEntry);
                                                    historyList.add(hEntry);
                                                }
                                            }

                                            found = true;
                                        }
                                    }
                                    if (found) {
                                        ShowSuspectController.changesFound = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (APIException ex) {
            Logger.getLogger(ShowSuspectController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            ExceptionHandler eh = new ExceptionHandler(ex);

            log(eh.getCommand());
            log(eh.getMessage());
        }

        return historyList;
    }

    /**
     * Looks for the person and the timestamp when the change has happend
     * exactly if found, it will update the HistoryEntry, otherwise it will do
     * nothing
     *
     * @param itemID the item to start with
     * @param sharedItemID the shared item id from the itemID
     * @param histEntryToFind the HistoryEntry
     */
    public void searchLatestChangeRec(String itemID, String sharedItemID, HistoryEntry histEntryToFind) {

        WorkItem wiField = this.allFields.get(histEntryToFind.getFieldName());
        String fieldType = wiField.getField("type").getValueAsString();

        log("searchLatestChangeRec: field to check is " + histEntryToFind.getFieldName());

        Boolean found = false;
        // if the field is a shared field with fva, start in this item at first
        if (fieldType.contentEquals("fva")) {
            log("searchLatestChangeRec: the field is a FVA field");
            String sharedFieldName = wiField.getField(BACKED_BY).getValueAsString().replace("References.", "");
            found = updateModInfo(SHARED, sharedItemID.replaceAll("\\D+", ""), histEntryToFind, sharedFieldName);
        }

        // if the field is a standard field (not a fva)
        if (!fieldType.contentEquals("fva") || !found) {
            log("searchLatestChangeRec: the field is a standard field (not a fva)");
            found = updateModInfo(BASE, itemID, histEntryToFind, histEntryToFind.getFieldName());
        }

        // try to find also a change in the bracnhed from item
        if (!found) {
            log("searchLatestChangeRec: References is set to: " + referencesID + ", checking before Branch.");
            String sharedFieldName = wiField.getField(BACKED_BY).getValueAsString().replace("References.", "");
            updateModInfo(BRANCH, referencesID, histEntryToFind, sharedFieldName);
        }

    }

    /**
     * updateModInfo - sets the Modified Info (if available)
     *
     * @param level
     * @param itemID
     * @param histEntryToFind
     * @param fieldName
     * @return true if succeed, else false
     */
    public Boolean updateModInfo(String level, String itemID, HistoryEntry histEntryToFind, String fieldName) {

        histEntryToFind.getDate();

        // the current Item to analyze                
        WorkItem wi = getWorkItemWithHistory(itemID, level);

        Field historyField = wi.getField(MKS_ISSUE_HISTORY);

        log(level + ": Field to look for in history is '" + fieldName + "' ...");
        log(level + ": For all " + historyField.getList().size() + " Item History Entries ...");

        for (Object historyItem : historyField.getList()) {
            Item histEntry = (Item) historyItem;

            // log("Base: Checking entry " + modBy + ", " + modDate + " ...");
            if (histEntry.contains(REFERENCES)) {

                referencesID = histEntry.getField(REFERENCES).getValueAsString().replaceAll("\\D+", "");
                log("Item references to: " + referencesID);
            }

            try {
                // 
                if (histEntry.contains(fieldName)) {
                    // histEntry.getField(fieldName);
                    log("Found historical entry with '" + fieldName + "' = " + histEntry.getField(fieldName).getValueAsString());
                }

                // so far all is valid
                if (histEntryToFind.getCurrValue() != null) {
                    if (histEntry.getField(fieldName).getValueAsString().contentEquals(histEntryToFind.getCurrValue())) {

                        String modBy = histEntry.getField(MKS_ISSUE_MODIFIED_BY).getItem().getField("fullname").getValueAsString();
                        log("modBy = " + modBy);

                        Date modDate = histEntry.getField(MKS_ISSUE_MODIFIED_DATE).getDateTime();
                        log("modDate = " + modDate);

                        histEntryToFind.setUser(modBy);
                        histEntryToFind.setDate(modDate);

                        log(level + ": " + histEntryToFind.getFieldName() + " found.");

                        return true;
                    }
                }

            } catch (NoSuchElementException ex) {
                log("Skipped this historical entry.");
                continue;
            }
        }
        return false;
    }

    private String getFieldDisplayName(String field) {
        // System.out.println("getFieldDisplayName => "+field);
        try {
            return fieldsMap.get(field).getDisplayName();
        } catch (NullPointerException npe) {
            return field;
        }
    }

    public void log(String text) {
        log(text, null);
    }

    public void log(String text1, String text2) {
        System.out.println(text1 + (text2 == null ? "" : ": " + text2));
        if (logArea != null) {
            logArea.appendText("\n" + text1 + (text2 == null ? "" : ": " + text2));
        }
    }

    public void debugOn() {
        debugFlag = true;
    }

    public void debug(String text) {
        if (debugFlag) {
            log("DEBUG", text);
        }
    }

    public static Date removeSecond(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        // cal.set(Calendar.HOUR_OF_DAY, 0);
        // cal.set(Calendar.MINUTE, 0);
        // cal.set(Calendar.SECOND, 0);
        cal.add(Calendar.SECOND, -1);
        // cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date addSecond(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        // cal.set(Calendar.HOUR_OF_DAY, 0);
        // cal.set(Calendar.MINUTE, 0);
        // cal.set(Calendar.SECOND, 0);
        cal.add(Calendar.SECOND, 1);
        // cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     *
     * @param itemID
     * @param level
     * @return
     */
    public WorkItem getWorkItemWithHistory(String itemID, String level) {
        try {
            Command cmd = new Command(Command.IM, "viewissue");
            // cmd.addOption(new Option("showRichContent"));
            cmd.addOption(new Option("showHistory"));

            if (level != null) {
                if (level.contentEquals(BASE)) {
                    cmd.addOption(new Option("showHistoryWithIndirectEdits"));
                }
            }

            cmd.addSelection(itemID);

            Response response = this.executeCmd(cmd);
            // to handle versioned items correctly
            WorkItemIterator wit = response.getWorkItems();
            return (wit.hasNext() ? wit.next() : null);

        } catch (APIException ex) {
            Logger.getLogger(ShowSuspectController.class
                    .getName()).log(Level.SEVERE, ex.getMessage(), ex);
            ExceptionHandler eh = new ExceptionHandler(ex);

            log(eh.getCommand());
            log(eh.getMessage());

            return null;
        }
    }

    /**
     *
     * @param itemID
     * @param modDate
     * @return
     */
    public WorkItem getWorkItemWithHistory(String itemID, Date modDate) {
        try {
            Command cmd = new Command(Command.IM, "viewissue");
            // cmd.addOption(new Option("showRichContent"));
            cmd.addOption(new Option("showHistory"));
            cmd.addSelection(itemID);
            if (modDate != null) {
                cmd.addOption(new Option("AsOf", dt.format(modDate)));
            }

            Response response = this.executeCmd(cmd);
            // to handle versioned items correctly
            WorkItemIterator wit = response.getWorkItems();
            return (wit.hasNext() ? wit.next() : null);

        } catch (APIException ex) {
            Logger.getLogger(ShowSuspectController.class
                    .getName()).log(Level.SEVERE, ex.getMessage(), ex);
            ExceptionHandler eh = new ExceptionHandler(ex);

            log(eh.getCommand());
            log(eh.getMessage());

            return null;
        }
    }

    public WorkItem getWorkItem(String itemID, String fields, Date modDate) {
        // get the value one second AFTER the change happend
        try {
            Command cmd = new Command(Command.IM, "issues");
            cmd.addSelection(itemID);
            if (modDate != null) {
                cmd.addOption(new Option("AsOf", dt.format(modDate)));
            }

            // Try to handle richt fields with data properly
            // log ("fields: "+fields);
            // cmd.addOption(new Option("fields", fields.replace("Text,", "Text::rich,")));
            // 
            // cmd.addOption(new Option("hostname", "mks.de.miele.net"));
            cmd.addOption(new Option("fields", fields));
            Response response = this.executeCmd(cmd);
            // to handle versioned items correctly
            WorkItemIterator wit = response.getWorkItems();
            return (wit.hasNext() ? wit.next() : null);
            // return response.getWorkItem(itemID);

        } catch (APIException ex) {
            Logger.getLogger(ShowSuspectController.class
                    .getName()).log(Level.SEVERE, ex.getMessage(), ex);
            ExceptionHandler eh = new ExceptionHandler(ex);

            log(eh.getCommand());
            log(eh.getMessage());

            return null;
        }
    }

    /**
     * @param wi
     * @param field
     * @return
     *
     */
    public String getStringValue(WorkItem wi, String field) {

        String value = null;
        if (wi.contains(field)) {
            value = wi.getField(field).getValueAsString();
        }
        return value == null ? "" : value;
    }
}
