/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package showsuspect.model;

/**
 *
 * @author veckardt
 */
public class FieldDef {

    private String type;
    private String backedBy;
    private String displayName;

    public FieldDef(String type, String backedBy, String displayName) {
        this.type = type;
        this.backedBy = backedBy;
        this.displayName = displayName;
    }

    public String getType() {
        return type;
    }

    public String getBackedBy() {
        return backedBy;
    }

    public String getDisplayName() {
        return displayName;
    }
}
