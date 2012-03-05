package com.company.comanda.brian.model;

public class FoodMenuItem 
{
    
    private String name;
    private String imageString;
    private String keyId;
    private String description;
    private long categoryId;
    private float price;
    
    public FoodMenuItem()
    {
     this.name = "";
     this.imageString = "";
    }
    public String getName() 
    {
        return this.name;
    }
    public void setName(String t) 
    {
        this.name = t;
    }  
    public String toString()
    {
     return "MenuItem (Name:" + this.name + ")";
    }
    public String getImageString() {
        return imageString;
    }
    public void setImageString(String imageString) {
        this.imageString = imageString;
    }
    public String getKeyId() {
        return keyId;
    }
    public void setKeyId(String keyID) {
        this.keyId = keyID;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public long getCategoryId() {
        return categoryId;
    }
    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }
    public float getPrice() {
        return price;
    }
    public void setPrice(float price) {
        this.price = price;
    }
    
    
}
