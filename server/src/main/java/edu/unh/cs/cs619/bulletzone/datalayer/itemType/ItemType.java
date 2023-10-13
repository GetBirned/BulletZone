/**
 * Public class for accessing data for individual types in the ItemType table
 */
package edu.unh.cs.cs619.bulletzone.datalayer.itemType;

public class ItemType implements Describable {
    protected final ItemTypeRecord info;
    protected final boolean container_flag;

    ItemType(ItemTypeRecord rec) {
        info = rec;
        container_flag = (rec.category.getName().equals("Frame"));
    }

    @Override
    public String toString() { return info.name; }

    public int getID() {
        return info.itemTypeID;
    }

    public String getName() {
        return info.name;
    }

    public ItemCategory getCategory() {
        return info.category;
    }

    public boolean isContainer() {
        return container_flag;
    }

    public double getProperty(ItemProperty property) {
        if (info.category.propertyMap.containsKey(property))
            return info.val[info.category.propertyMap.get(property)];
        //otherwise, produce a default value
        return property.getIdentity().getResult();
    }

    public double getSize() { return info.val[ItemProperty.ID.Size.ordinal()]; }

    public double getWeight() {
        return info.val[ItemProperty.ID.Weight.ordinal()];
    }

    public double getPrice() {
        return info.val[ItemProperty.ID.Price.ordinal()];
    }
}
