package com.app.mysantinis.module;


public class ProductModel {

        public String productName;
        public String price;
        public String quantity;
        public String imagePath;
        public String idProduct;
        public String details;
        //    public int price;


    public ProductModel(String productName, String price, String quantity, String imagePath, String idProduct) {
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.imagePath = imagePath;
        this.idProduct = idProduct;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getIdProduct() {
        return idProduct;
    }
}
