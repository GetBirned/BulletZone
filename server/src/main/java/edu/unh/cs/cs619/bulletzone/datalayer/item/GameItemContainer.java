/**
 * Public class for accessing data for individual items in the ItemContainer table
 */

package edu.unh.cs.cs619.bulletzone.datalayer.item;

import java.util.Collection;
import java.util.HashSet;

import edu.unh.cs.cs619.bulletzone.datalayer.itemType.ItemProperty;

public class GameItemContainer extends GameItem {
    protected String name;
    protected HashSet<GameItem> containedItems = new HashSet<>();

    public GameItemContainer(GameItemRecord rec, String containerName) {
        super(rec);
        name = containerName;
    }

    @Override
    public String toString() { return getTypeName() + " " + name + " (ID: " + getId() + ")"; }

    public String getName() { return name; }

    public Collection<GameItem> getItems() { return containedItems;}

    /**
     * Calculates and returns the appropriate value for the passed property
     * @param property  ItemProperty to get the calculated value for
     * @return the sum/product/etc. of child properties with our own
     */
    @Override
    public double getProperty(ItemProperty property) {
        //accumulate the child properties
        ItemProperty.Accumulator acc = property.getIdentity();
        acc.accumulate(super.getProperty(property)); // include our own property with children
        //System.out.println(property + " is " + result);
        for (GameItem item : containedItems) {
            acc.accumulate(item.getProperty(property));
            //System.out.println(property + " is now " + result);
        }
        double result = acc.getResult();
        //System.out.println(property + " is finally " + result);

        return result;
    }

    /**
     * More-efficient calculation of weight
     * @return the weight of this container and all its children
     */
    @Override
    public double getWeight() {
        double weight = super.getWeight();
        for (GameItem item: containedItems) {
            weight += item.getWeight();
        }
        return weight;
    }

    @Override
    /**
     * More-efficient calculation of price
     * @return the price of this container and all its children
     */
    public double getPrice() {
        double price = super.getPrice();
        for (GameItem item: containedItems) {
            price += item.getPrice();
        }
        return price;
    }

    @Override
    /**
     * More-efficient calculation of size
     * @return the size of this container and all its children
     */
    public double getSize() {
        double size = super.getSize();
        for (GameItem item: containedItems) {
            size += item.getSize();
        }
        return size;
    }
    /**
     * Package-local version that adds an item to the collection without updating the database
     * @param child Item to be added to the collection
     */
    void addItem(GameItem child)
    {
        containedItems.add(child);
        child.setParent(this);
    }

    /**
     * Package-local version that removes an item from the collection without updating the database
     * @param child Item to be added to the collection
     */
    void removeItem(GameItem child)
    {
        child.setParent(null);
        containedItems.remove(child);
    }

    /**
     * Package-local version that removes all items from the collection without updating the database
     */
    void removeAll()
    {
        for (GameItem item: containedItems) {
            item.setParent(null);
        }
        containedItems.clear();
    }
}
