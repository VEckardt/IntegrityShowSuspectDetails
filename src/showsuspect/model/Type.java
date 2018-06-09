/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package showsuspect.model;

import com.mks.api.response.WorkItem;

/**
 *
 * @author veckardt
 */
public class Type {

    public static final String validFields = "id,name,description,documentClass,significantEdit,associatedType";
    private int id;
    private String name = "";
    private String description = "";
    private String documentClass = "";
    private String significantEdit = "";
    private String associatedType = "";

    public Type() {
    }

    public Type(int id) {
        this.id = id;
    }

    public Type(WorkItem wi) {

        this.id = wi.getField("id").getInteger();
        this.name = wi.getField("name").getValueAsString();
        this.description = wi.getField("description").getValueAsString();
        this.documentClass = wi.getField("documentClass").getValueAsString();
        this.significantEdit = wi.getField("significantEdit").getValueAsString();
        this.associatedType = wi.getField("associatedType").getValueAsString();

    }

    public Type(int id, String name, String description, String documentClass, String significantEdit) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.documentClass = documentClass;
        this.significantEdit = significantEdit;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getSignificantEdit() {
        return this.significantEdit;
    }

    public String getDocumentClass() {
        return this.documentClass;
    }
    //

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String text) {
        this.name = text;
    }

    public void setDescription(String text) {
        this.description = text;
    }
}