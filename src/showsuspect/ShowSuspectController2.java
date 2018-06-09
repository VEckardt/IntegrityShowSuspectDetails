/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package showsuspect;

import javafx.animation.FadeTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import showsuspect.model.HistoryEntry;

/**
 *
 * @author veckardt
 */
public class ShowSuspectController2 {

    private FadeTransition messageTransition = null;
    @FXML
    public Label messageBar;
    @FXML
    public TextArea logArea;
    @FXML
    public Label label;
    Boolean debug = false;

    public void setMessageText(String text) {
        messageBar.setText(text);
    }

    // logs the text
    public void log(String text) {
        if (logArea.getText().isEmpty()) {
            logArea.setText(text);
        } else {
            logArea.appendText("\n" + text);
        }
        System.out.println(text);
    }

    public void debug(String text) {
        if (debug) {
            System.out.println(text);
        }
    }

    public void setLogArea(TextArea logArea) {
        this.logArea = logArea;
    }

    /**
     * Displays a short message in the message bar
     *
     * @param message
     */
    public void displayMessage(String message) {
        if (messageBar != null) {
            if (messageTransition != null) {
                messageTransition.stop();
            } else {
                messageTransition = new FadeTransition(Duration.millis(2000), messageBar);
                messageTransition.setFromValue(1.0);
                messageTransition.setToValue(0.0);
                messageTransition.setDelay(Duration.millis(2000));
                messageTransition.setOnFinished(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        messageBar.setVisible(false);
                    }
                });
            }
            messageBar.setText(message);
            messageBar.setVisible(true);
            messageBar.setOpacity(1.0);
            messageTransition.playFromStart();
        }
    }

    public void initFilter(final TextField tFilter, final ObservableList<HistoryEntry> tableContent, final TableView<HistoryEntry> tableView) {
        // tFilter = TextFields.createSearchField();
        // txtField.setPromptText("Filter");
        tFilter.textProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable o) {
                if (tFilter.textProperty().get().isEmpty()) {
                    tableView.setItems(tableContent);
                    return;
                }
                ObservableList<HistoryEntry> tableItems = FXCollections.observableArrayList();
                ObservableList<TableColumn<HistoryEntry, ?>> cols = tableView.getColumns();
                for (int i = 0; i < tableContent.size(); i++) {

                    for (int j = 0; j < cols.size(); j++) {
                        TableColumn col = cols.get(j);
                        if (col.getCellData(tableContent.get(i)) != null) {
                            String cellValue = col.getCellData(tableContent.get(i)).toString();
                            cellValue = cellValue.toLowerCase();
                            if (cellValue.contains(tFilter.textProperty().get().toLowerCase())) {
                                tableItems.add(tableContent.get(i));
                                break;
                            }
                        }
                    }
                }
                tableView.setItems(tableItems);
            }
        });
    }
}
