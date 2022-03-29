package com.app.mysantinis.module;

import android.widget.TableLayout;

public class IncomingOrderData {
    private String text1;
    private String text2;
    private String[] text4;
    private TableLayout tblLayout;
    private String totaltax;
    private String cname;
    private String cphone;
    private String cemail;
    private String wordpressID;
    private String cloverID;

    public IncomingOrderData(String text1, String text2, String[] text4, TableLayout tblLayout, String totaltax, String cname, String cphone, String cemail, String wordpressID, String cloverID) {
        this.text1 = text1;
        this.text2 = text2;
        this.text4 = text4;
        this.tblLayout = tblLayout;
        this.totaltax = totaltax;
        this.cname = cname;
        this.cphone = cphone;
        this.cemail  =cemail;
        this.wordpressID = wordpressID;
        this.cloverID = cloverID;
    }

    public String getText1() {
        return text1;
    }

    public String getText2() {
        return text2;
    }

    public String[] getText4() {
        return text4;
    }

    public TableLayout getTblLayout(){
        return tblLayout;
    }

    public String getTotaltax() {
        return totaltax;
    }

    public String getCname() {
        return cname;
    }

    public String getCphone() {
        return cphone;
    }

    public String getCemail() {
        return cemail;
    }

    public String getWordpressID() {
        return wordpressID;
    }

    public String getCloverID() {
        return cloverID;
    }

}
