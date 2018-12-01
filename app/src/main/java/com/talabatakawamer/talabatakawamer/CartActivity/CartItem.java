package com.talabatakawamer.talabatakawamer.CartActivity;

import com.talabatakawamer.talabatakawamer.VegetablesSection.VegetableItem;

public class CartItem {


    public final VegetableItem vegetableItem;
    // save price in JD
    public double price;
    public double pricePerUnit;
    public double quantity;
    public String type;

    public CartItem(VegetableItem vegetableItem, double price, double pricePerUnit, double quantity, String type) {

        this.vegetableItem = vegetableItem;
        this.price = price;
        this.pricePerUnit = pricePerUnit;
        this.quantity = quantity;
        this.type = type;
    }

}
