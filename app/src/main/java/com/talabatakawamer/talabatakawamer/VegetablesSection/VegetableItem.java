package com.talabatakawamer.talabatakawamer.VegetablesSection;

public class VegetableItem {

    public String name;
    public String des;
    // id product inside database
    public int id;
    public String imageUrl;
    public double priceSuper;
    public double priceFirst;
    //	0 for gram,1 for kg,2 for number
    // 3 spacial for kg give the user ability to change quantity by 0.5 not by one in @card_vegetables_item.xml.#layoutQuantity
    public int quantityType;
    // 0 for super,1 for (صنف اول), and 2 for both
    public int type_available;

    public VegetableItem(int id,String name,String des,String imageUrl,double priceFirst,double priceSuper,int quantityType,int type_available){

        this.id = id;
        this.name = name;
        this.des = des;
        this.imageUrl = imageUrl;
        this.priceFirst = priceFirst;
        this.priceSuper = priceSuper;
        this.quantityType = quantityType;
        this.type_available = type_available;
    }


}
