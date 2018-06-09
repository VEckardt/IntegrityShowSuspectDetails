/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package showsuspect;

import com.mks.api.response.APIException;
import com.mks.api.response.WorkItem;
import com.ptc.services.common.api.ApplicationProperties;
import showsuspect.api.IntegrityMessages;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import jfx.messagebox.MessageBox;
import static showsuspect.ShowSuspect.secondaryStage;
import showsuspect.api.IntegrityCommands;
import showsuspect.model.HistoryEntry;

/**
 *
 * @author veckardt
 */
public class ShowSuspectController extends ShowSuspectController2 implements Initializable {

    private final static IntegrityMessages MC = new IntegrityMessages(ShowSuspect.class);
    public static Boolean changesFound = false;
    public static String upstreamSuspect = "upstream suspect";
    public static final String TRACE_STATUS_FIELD = "Trace Status";

    @FXML
    private Button bFirstButton, bSecondButton, bThirdButton;
    @FXML
    private HBox hBox;
    String selection1;
    String itemType;
    @FXML
    private TableView<HistoryEntry> table = new TableView<>();
    private ObservableList<HistoryEntry> tabContent
            = FXCollections.observableArrayList();
    @FXML
    private TableColumn<HistoryEntry, String> colId, colUser, colDate, colFieldName, colValue;
    private final IntegrityCommands ia = new IntegrityCommands();
    private final Map<String, String> env = System.getenv();

    @FXML
    private void handleButtonAction(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    public void handleDiffVsCurrent(ActionEvent event) throws IOException {
        openBrowser("Current");
    }

    @FXML
    public void handleDiffVsPrevious(ActionEvent event) throws IOException {
        openBrowser("Previous");
    }

    @FXML
    public void handleCurrVsPrevious(ActionEvent event) throws IOException {
        openBrowser("Current vs. Previous");
    }

    @FXML
    public void handleCurrVsPreviousMouse(Event event) throws IOException {
        // if (event instanceOf MouseEvent)
        openBrowser("Current vs. Previous");
    }

    private void openBrowser(String option) {
        MyBrowser myBrowser = new MyBrowser(table.getSelectionModel().getSelectedItem(), option);
        Scene secondaryScene = new Scene(myBrowser, 800, 400);
        secondaryStage.setScene(secondaryScene);
        secondaryStage.setTitle(Copyright.title.replace("- v" + Copyright.programVersion, "for " + itemType + " " + selection1));
        secondaryStage.show();
        myBrowser = null;
    }

    class MyBrowser extends Region {

        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();

        public MyBrowser(HistoryEntry histEntry, String type) {

            ShowDiffController sdc = new ShowDiffController();
            String data = sdc.loadDiff(histEntry, type);
            webEngine.loadContent(data);
            getChildren().add(webView);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            selection1 = env.get("MKSSI_ISSUE0");
            if (selection1 == null) {
                selection1 = "20986";
            }

            if (!ia.initatedByIntegrity) {
                MessageBox.show(ShowSuspect.stage,
                        MC.getMessage("NOT_CONNECTED"),
                        "Show Suspect - Start Option",
                        MessageBox.ICON_ERROR | MessageBox.OK);
                System.exit(0);
            }

            ia.setLogArea(logArea);

            ApplicationProperties props = new ApplicationProperties(ShowSuspect.class);
            if (props.getProperty("ShowFirstButton", "no").contentEquals("no")) {
                disableButton(bFirstButton);
            }
            if (props.getProperty("ShowSecondButton", "no").contentEquals("no")) {
                disableButton(bSecondButton);
                bThirdButton.setText("Show Difference");
            }
            if (props.getProperty("ShowLogArea", "no").contentEquals("no")) {
                table.setPrefHeight(250 + 50);
                logArea.setPrefHeight(0);
            }

            table.setEditable(true);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            tabContent.clear();

            // this is the pointer to the function in ViewEntry object Name => getName
            colId.setCellValueFactory(new PropertyValueFactory<HistoryEntry, String>("TypeAndId"));
            colUser.setCellValueFactory(new PropertyValueFactory<HistoryEntry, String>("User"));
            colDate.setCellValueFactory(new PropertyValueFactory<HistoryEntry, String>("DateFormatted"));
            colFieldName.setCellValueFactory(new PropertyValueFactory<HistoryEntry, String>("FieldName"));
            colValue.setCellValueFactory(new PropertyValueFactory<HistoryEntry, String>("DisplayValue"));

            //
            // Determine the trace fields defined as backwards for this type
            //            
            WorkItem baseItem = ia.getWorkItem(selection1, "Type," + ShowSuspectController.TRACE_STATUS_FIELD);

            String traceStatus = baseItem.getField(ShowSuspectController.TRACE_STATUS_FIELD).getValueAsString();

            // VE Added
            itemType = baseItem.getField("Type").getValueAsString();

            WorkItem baseItemType = ia.getTypeDetails(baseItem.getField("Type").getValueAsString(), "visibleFields");
            String traceNames = "";
            // get all fields from type "relationship"
            ia.readFields("relationship", baseItemType.getField("visibleFields").getValueAsString());
            for (WorkItem entry : ia.allFields.values()) {
                if (entry.getField("trace").getBoolean() && !entry.getField("isForward").getBoolean()) {
                    traceNames += (traceNames.isEmpty() ? "" : ",") + entry.getField("name").getString();
                }
            }
            ia.log("Determined UpStream traceNames: " + traceNames, 1);
            //
            // get HistoryEntrys tha contain the changed fields for the related items of internest
            List<HistoryEntry> seList = ia.initSuspectEntryList(selection1, "", traceNames); // "Satisfies,Validates,Models,Decomposed From");

            ia.debug("seList.size: " + seList.size());
            if (!seList.isEmpty()) {
                // we have changes in the list that shall be visualized
                setMessageText("Loading data ...");
                label.setText("Item field changes which have enabled the suspect flag for " + itemType + " " + selection1 + ":");
                tabContent.addAll(seList);
                table.setItems(tabContent);
                table.getSelectionModel().selectFirst();
                log(tabContent.size() + " suspect change(s) found.");
                displayMessage("Data loaded.");
            } else {
                // we have no changes in the list 
                if (changesFound) {
                    // All changes seem to be "undone", so there is NO visible Delta
                    MessageBox.show(ShowSuspect.stage,
                            MC.getMessage("NO_MORE_SUSPECT").replace("{0}", selection1),
                            "Show Suspect - Start Option",
                            MessageBox.ICON_ERROR | MessageBox.OK);
                } else if (traceStatus.contains(upstreamSuspect)) {
                    // manually mrked suspect ???
                    MessageBox.show(ShowSuspect.stage,
                            MC.getMessage("NO_SUSPECT_ITEMS").replace("{0}", selection1).replace("{1}", upstreamSuspect),
                            "Show Suspect - Start Option",
                            MessageBox.ICON_ERROR | MessageBox.OK);
                } else {
                    // there is nothing to do this item 
                    MessageBox.show(ShowSuspect.stage,
                            MC.getMessage("NOT_SUSPECT").replace("{0}", selection1).replace("{1}", upstreamSuspect),
                            "Show Suspect - Start Option",
                            MessageBox.ICON_INFORMATION | MessageBox.OK);
                }
                System.exit(1);
            }
        } catch (APIException ex) {
            Logger.getLogger(ShowSuspectController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private void disableButton(Button button) {
        button.setVisible(false);
        button.setDisable(true);
        button.setMaxWidth(0);
        button.setPrefWidth(0);
        button.setMinWidth(0);
        button.setStyle("-fx-padding: 0 0 0 0;");
    }
}
