/**
 * Public class for accessing data for individual categories in the ItemCategory table
 */
package edu.unh.cs.cs619.bulletzone.datalayer.itemType;

public class ItemProperty {
    public enum PropertyType {Null, Additive, Average, Multiplicative, Restorative};
    public enum ID {Size, Weight, Price, PropertyVal1, PropertyVal2, PropertyVal3};

    private final int itemPropertyID;
    private final String name;
    private final PropertyType propertyType;

    ItemProperty(int id, String propertyName, PropertyType pType) {
        itemPropertyID = id;
        name = propertyName;
        propertyType = pType;
    }

    @Override
    public String toString() { return name; }

    public class Accumulator {
        private double value;
        private int count;

        Accumulator(double val) {
            value = val;
            count = 0;
        }

        /**
         * Combines the passed operand with what's already been accumulated
         * @param operand  Value to be accumulated next
         */
        public void accumulate(double operand) {
            switch(propertyType) {
                case Additive: case Restorative:
                    value += operand;
                    break;
                case Average:
                    if (Double.isNaN(operand))
                        return; //do nothing
                    count++;
                    if (count == 1)
                        value = operand; //Get rid of NaN
                    else
                        value += operand;
                    break;
                case Multiplicative:
                    value *= operand;
                    break;
            }
        }

        /**
         * If accumulating an average, return the average, otherwise return the value
         * @return the accumulator value or the average (if this property accumulates to an average)
         */
        public double getResult() {
            if (propertyType == PropertyType.Average)
                return value / count;
            return value;
        }
    }

    /**
     * Initializes an accumulator for this property type
     * @return the starting value for accumulating values of this property type
     */
    public Accumulator getIdentity() {
        double initValue = 0.0;
        if (propertyType == PropertyType.Multiplicative)
            initValue = 1.0;
        else if (propertyType == PropertyType.Average)
            initValue = Double.NaN;
        return new Accumulator(initValue);
    }

    /**
     * Determines if this property corresponds to the given ID
     * @param id  PropertyID to compare with
     * @return true if there's a match, and false otherwise
     */
    public boolean is(ID id) { return itemPropertyID == id.ordinal();}

    public int getID() {
        return itemPropertyID;
    }

    public String getName(){
        return name;
    }

    public PropertyType getType() { return propertyType; }
}
