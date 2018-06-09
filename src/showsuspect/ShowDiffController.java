/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package showsuspect;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebView;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import org.outerj.daisy.diff.HtmlCleaner;
import org.outerj.daisy.diff.XslFilter;
import org.outerj.daisy.diff.html.HTMLDiffer;
import org.outerj.daisy.diff.html.HtmlSaxDiffOutput;
import org.outerj.daisy.diff.html.TextNodeComparator;
import org.outerj.daisy.diff.html.dom.DomTreeBuilder;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import showsuspect.model.HistoryEntry;

/**
 * FXML Controller class
 *
 * @author veckardt
 */
public class ShowDiffController implements Initializable {

    @FXML
    static WebView webView;

    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public String loadDiff(HistoryEntry histEntry, String type) {

        boolean htmlOut = true;
        // InputStream oldStream = null;
        // InputStream newStream = null;
        // String outputFile = "c:/temp/daisydiff.htm";
        String[] css = new String[]{};
        String data = "";

        String oldString;
        String newString;
        switch (type) {
            case "Current":
                oldString = histEntry.getRichValue();
                newString = histEntry.getCurrValue();
                break;
            case "Previous":
                oldString = histEntry.getPrevValue();
                newString = histEntry.getRichValue();
                break;
            default:
                oldString = histEntry.getPrevValue();
                newString = histEntry.getCurrValue();
                break;
        }

        // System.out.println("oldString: " + oldString);
        // System.out.println("newString: " + newString);

        try {
            SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory
                    .newInstance();

            TransformerHandler result = tf.newTransformerHandler();

            //
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            // StreamResult sr = new StreamResult(out);
            // orig: result.setResult(new StreamResult(new File(outputFile)));
            result.setResult(new StreamResult(out));


            // oldStream = new FileInputStream("c:/temp/a.html");

            // String oldString = "<!-- MKS HTML -->Must have manual user interface for basic functional inputs";
            // String newString = "<!-- MKS HTML -->Must have a mini-USB port to support timing synchronization. Must be USB 2.0 compliant. The watch must come pre-loaded with all appropriate drivers so that the synchronization process requires no setup from the user";

            InputStream oldStream = new ByteArrayInputStream(oldString.getBytes("UTF-8"));

            // newStream = new FileInputStream("c:/temp/b.html");
            InputStream newStream = new ByteArrayInputStream(newString.getBytes("UTF-8"));

            XslFilter filter = new XslFilter();

            // if (htmlDiff) {

            ContentHandler postProcess = htmlOut ? filter.xsl(result,
                    "org/outerj/daisy/diff/htmlheader.xsl") : result;

            Locale locale = Locale.getDefault();
            String prefix = "diff";

            HtmlCleaner cleaner = new HtmlCleaner();

            InputSource oldSource = new InputSource(oldStream);
            InputSource newSource = new InputSource(newStream);

            DomTreeBuilder oldHandler = new DomTreeBuilder();
            cleaner.cleanAndParse(oldSource, oldHandler);
            System.out.print(".");
            TextNodeComparator leftComparator = new TextNodeComparator(
                    oldHandler, locale);

            DomTreeBuilder newHandler = new DomTreeBuilder();
            cleaner.cleanAndParse(newSource, newHandler);
            System.out.print(".");
            TextNodeComparator rightComparator = new TextNodeComparator(
                    newHandler, locale);

            postProcess.startDocument();
            postProcess.startElement("", "diffreport", "diffreport",
                    new AttributesImpl());
            doCSS(css, postProcess);
            postProcess.startElement("", "diff", "diff",
                    new AttributesImpl());
            HtmlSaxDiffOutput output = new HtmlSaxDiffOutput(postProcess,
                    prefix);

            HTMLDiffer differ = new HTMLDiffer(output);

            // System.out.println("differ: "+output...toString());

            differ.diff(leftComparator, rightComparator);
            System.out.print(".");
            postProcess.endElement("", "diff", "diff");
            postProcess.endElement("", "diffreport", "diffreport");
            postProcess.endDocument();

            data = out.toString();

            String infoString = "Suspect Date " + histEntry.getDateFormatted();

            data = data.replace("Click on the changed parts for a detailed description. Use the left and right arrow keys to walk through the modifications.", infoString);
            data = data.replace("<a style=\"font-size: 100%;\" class=\"diffpage-html-a\" href=\"http://code.google.com/p/daisydiff/\">Daisy Diff</a> c", "C");
            data = data.replace("Compare report.", "" + histEntry.getMainType() + " " + histEntry.getId() + " with " + type.toLowerCase() + " " + histEntry.getFieldName());

            data = data.replace("href=\"css/diff.css\"", "href=\"" + getClass().getResource("css/diff.css") + "\"");
            data = data.replace("</div>", "</div>" + histEntry.getFieldName() + ":<br><hr>");
            data = data.replace("</body>", "</body><hr>");

            // System.out.println("out: " + data);

            // try (PrintWriter printWriter = new PrintWriter(outputFile)) {
            //     printWriter.println(data);
            // }
        } catch (Throwable e) {
            e.printStackTrace();
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
            if (e instanceof SAXException) {
                ((SAXException) e).getException().printStackTrace();
            }
            // help();
        }
        return data;
    }

    private static void doCSS(String[] css, ContentHandler handler) throws SAXException {
        handler.startElement("", "css", "css",
                new AttributesImpl());
        for (String cssLink : css) {
            AttributesImpl attr = new AttributesImpl();
            attr.addAttribute("", "href", "href", "CDATA", cssLink);
            attr.addAttribute("", "type", "type", "CDATA", "text/css");
            attr.addAttribute("", "rel", "rel", "CDATA", "stylesheet");
            handler.startElement("", "link", "link",
                    attr);
            handler.endElement("", "link", "link");
        }

        handler.endElement("", "css", "css");
    }
}
