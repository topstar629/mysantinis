package com.app.mysantinis.module;

public class CloseOutData {
    public static String period;
    public static String dailySales;
    public static String discounts;
    public static String cashTotal;
    public static String refunds;
    public static String giftPayment;
    public static String uberEat;
    public static String grubHub;
    public static String doorDash;
    public static String postmates;
    public static String onlineOrdering;
    public static String tips;
    public static String cardPayment;
    public CloseOutData(String period, String dailySales, String cardPayment, String discounts, String cashTotal, String grubHub, String doorDash, String postmates, String onlineOrdering, String refunds, String giftPayment, String uberEat, String tips) {
        CloseOutData.period = period;
        CloseOutData.dailySales = dailySales;
        CloseOutData.cardPayment = cardPayment;
        CloseOutData.discounts = discounts;
        CloseOutData.cashTotal = cashTotal;
        CloseOutData.grubHub = grubHub;
        CloseOutData.doorDash = doorDash;
        CloseOutData.postmates = postmates;
        CloseOutData.onlineOrdering = onlineOrdering;
        CloseOutData.refunds = refunds;
        CloseOutData.giftPayment = giftPayment;
        CloseOutData.uberEat = uberEat;
        CloseOutData.tips = tips;
    }
}
