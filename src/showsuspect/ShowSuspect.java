/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package showsuspect;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author veckardt
 */
public class ShowSuspect extends Application {

    public static Stage stage = null;
    public static Stage secondaryStage;
    Image applicationIcon = new Image(getClass().getResourceAsStream("resources/RTGBaenTL.png"));

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("ShowSuspect.fxml"));

        Scene scene = new Scene(root);
        ShowSuspect.stage = primaryStage;
        primaryStage.setTitle(Copyright.title);

        
        primaryStage.getIcons().add(applicationIcon);

        // configure the secondary stage.
        secondaryStage = new Stage(StageStyle.DECORATED);
        secondaryStage.setTitle(Copyright.title);
        secondaryStage.getIcons().add(applicationIcon);

        // MODAL
        secondaryStage.initOwner(primaryStage);
        secondaryStage.initModality(Modality.WINDOW_MODAL);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
