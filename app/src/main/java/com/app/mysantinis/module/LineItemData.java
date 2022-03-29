package com.app.mysantinis.module;

public class LineItemData {
    public int uberEats;
    public int grubHub;
    public int doorDash;
    public int postmates;
    public int total;
    public LineItemData() {
        this.uberEats = 0;
        this.grubHub = 0;
        this.doorDash = 0;
        this.total = 0;
    }
    public LineItemData(int uberEats, int grubHub, int doorDash, int postmates, int discounts) {
        this.uberEats = uberEats;
        this.grubHub = grubHub;
        this.doorDash = doorDash;
        this.postmates = postmates;
        this.total = total;
    }
}
