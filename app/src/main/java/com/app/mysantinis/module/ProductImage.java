package com.app.mysantinis.module;


public class ProductImage {

    private Integer id;
    private String imageId;
    private String productName;
    private String productCode;
    private String dealerPrice;
    private String resellerPrice;
    private Integer stocks;
    private String productImage;
    private String description;
    int pos,productQuantity;
    float totalCash;
    String productPrice;
    String ProductID;

    public ProductImage() {
    }

    public ProductImage(String productName, String productPrice, int productQuantity, String productImage, String ProductID ) {
        this.productImage = productImage;
        this.productQuantity = productQuantity;
        this.productPrice = productPrice;
        this.productName = productName;
        this.ProductID = ProductID;
    }

    public void setProductID(String ProductId){this.ProductID = ProductId;}

    public String getProductID(){return ProductID;}

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public float getTotalCash() {
        return totalCash;
    }

    public void setTotalCash(float totalCash) {
        this.totalCash = totalCash;
    }

    public int getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(int productQuantity) {
        this.productQuantity = productQuantity;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getDealerPrice() {
        return dealerPrice;
    }

    public void setDealerPrice(String dealerPrice) {
        this.dealerPrice = dealerPrice;
    }

    public String getResellerPrice() {
        return resellerPrice;
    }

    public void setResellerPrice(String resellerPrice) {
        this.resellerPrice = resellerPrice;
    }

    public Integer getStocks() {
        return stocks;
    }

    public void setStocks(Integer stocks) {
        this.stocks = stocks;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}