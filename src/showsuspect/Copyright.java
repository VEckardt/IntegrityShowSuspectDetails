/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package showsuspect;

/**
 *
 * @author veckardt
 */
public class Copyright {
    
    // Change History
    //
    // v0.6
    // (F) Retrieving the UpStream Trace Fields automatically
    // (B) Log File Name changed
    //
    // v0.5
    // first official stable release 
    //

    public static final String COPYRIGHT = "(c)";
    public static final String copyright = "Copyright " + COPYRIGHT + " 2013, 2014, 2015 PTC Inc.";
    public static final String copyrightHtml = "Copyright &copy; 2013, 2014, 2015 PTC Inc.";
    public static final String programName = "Integrity - Show Suspect Details";
    public static final String programVersion = "1.0.6.1";
    public static final String author = "Author: Volker Eckardt, Matthias Rump";
    public static final String email = "email: veckardt@ptc.com";
    public static final String title = programName + " - v" + programVersion;

    public static String getCopyright() {
        String copy = ("* " + programName + " - Version " + programVersion);
        copy = copy + ("\n* An utility to show the changes which caused the suspect flag");
        copy = copy + ("\n* Tested with Integrity 10.4 and 10.6");
        copy = copy + ("\n*");
        copy = copy + ("\n* " + copyright);
        copy = copy + ("\n* " + author + ", " + email + "\n");
                
        return copy;
    }

    public static void write() {
        System.out.println(getCopyright());
    }

    public static void usage() {
        System.out.println("*");
        System.out.println("* Usage: ");
        System.out.println("*   <path-to-javaw>\\javaw -jar <path-to-jar>\\IntegrityUndo.jar");
        System.out.println("* Example:");
        System.out.println("*   ..\\jar\\bin\\javaw -jar C:\\IntegrityClient10\\IntegrityShowSuspect.jar");
        System.out.println("* Additional Notes:");
        System.out.println("*   - a configuration file 'IntegrityShowSuspect.properties' can be used to specify default values");
        System.out.println("*   - a log file is created in directory '%temp%', the filename is 'IntegrityShowSuspect_YYYY-MM-DD.log'");
        System.out.println("*");
    }
}
