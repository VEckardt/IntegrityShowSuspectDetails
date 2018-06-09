/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package showsuspect.api;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import showsuspect.ShowSuspectController;

/**
 *
 * @author veckardt
 */
public class IntegrityMessages {

    // String propertiesName;
    Properties messages = new Properties();

    public IntegrityMessages(Object classe) {
        // this.propertiesName = propertiesName;
        String resourceFile = "/" + classe.toString().replace("class ", "").replace(".", "/resources/") + "RB.properties";
        System.out.println(resourceFile);
        // class showsuspect.ShowSuspect
        try {
            messages.load(this.getClass().getResourceAsStream(resourceFile));
        } catch (IOException ex) {
            Logger.getLogger(ShowSuspectController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getMessage(String key) {
        return messages.getProperty(key);
    }

    public String getMessage(String key, String value0) {
        return messages.getProperty(key).replaceAll("{0}", value0);
    }

    public String getMessage(String key, String value0, String value1) {
        return messages.getProperty(key).replaceAll("{0}", value0).replaceAll("{1}", value1);
    }
}
