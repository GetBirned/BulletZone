/**
 * Public class for accessing data for individual items in the Item table
 */
package edu.unh.cs.cs619.bulletzone.datalayer.item;

import edu.unh.cs.cs619.bulletzone.datalayer.itemType.Describable;
import edu.unh.cs.cs619.bulletzone.datalayer.itemType.ItemProperty;
import edu.unh.cs.cs619.bulletzone.datalayer.itemType.ItemType;
import edu.unh.cs.cs619.bulletzone.datalayer.permission.OwnableEntity;

public class GameItem extends OwnableEntity implements Describable {
    protected ItemType itemType;
    protected double usageMonitor;
    protected String originalName;
    protected double value;
    protected GameItemContainer parent;

    public ItemType getType() { return itemType; }

    public String getTypeName(){
        return itemType.getName();
    }

    public String getOriginalName() { return originalName; }

    public GameItemContainer getParent() { return parent; }

    public boolean isContainer() { return itemType.isContainer();}

    /** Returns the property of only the item, not all its contents
     * @param property  Which property to report
     * @return the value for the selected property of just this item (even if it is a container)
     */
    public double getLocalProperty(ItemProperty property) { return itemType.getProperty(property); }

    /** Returns the aggregate property of the item, including its contents (if any) */
    public double getProperty(ItemProperty property) {return getLocalProperty(property);}

    public double getSize() {return itemType.getSize();}
    public double getWeight() {return itemType.getWeight();}
    public double getPrice() {return Double.isNaN(value)? itemType.getPrice() : value;}

    //----------------------------------END OF PUBLIC METHODS--------------------------------------
    GameItem(GameItemRecord rec) {
        super(rec);
        itemType = rec.itemType;
        usageMonitor = rec.usageMonitor;
        originalName = rec.originalName;
        value = rec.value;
    }

    protected void setParent(GameItemContainer container) {
        parent = container;
    }

    /* These are not provided, just so nobody accidentally does them when not appropriate
    void setOriginalName(String name) { originalName = name; }
    void setValue(double val) { value = val; }
    void unsetValue() { value = Double.NaN; }
    */
}
