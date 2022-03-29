package com.app.mysantinis.starprnt;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;

import com.app.mysantinis.General;
import com.app.mysantinis.R;
import com.app.mysantinis.module.CloseOutData;
import com.starmicronics.starioextension.ICommandBuilder;
import com.starmicronics.starioextension.ICommandBuilder.CodePageType;
import com.starmicronics.starioextension.ICommandBuilder.InternationalType;
import com.starmicronics.starioextension.StarIoExt.CharacterCode;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class EnglishReceiptsImpl extends ILocalizeReceipts {

    public EnglishReceiptsImpl() {
        mLanguageCode = "En";

        mCharacterCode = CharacterCode.Standard;
    }

    static public void receiptFormat(ICommandBuilder builder, int lineLength, Charset encoding) {
        builder.appendAlignment(ICommandBuilder.AlignmentPosition.Center);
        builder.appendBitmap(BitmapFactory.decodeResource(General.instanceOfApplication.getResources(), R.drawable.slogo), false);
        builder.append(("\n\n\n").getBytes(encoding));

        try {

//            StringBuilder outputStr = new StringBuilder();
//            outputStr.append(StringUtils.center("ONLINE ORDER", lineLength)).append("\n");
            builder.appendFontStyle(ICommandBuilder.FontStyleType.B);
            builder.append(("ONLINE ORDER   " + General.orderId + "\n").getBytes(encoding));
            builder.appendFontStyle(ICommandBuilder.FontStyleType.A);
            builder.append(General.restaurantName.getBytes(encoding));
            builder.append(("\n\n\n").getBytes(encoding));
//            outputStr.append(StringUtils.center(General.restaurantName, lineLength)).append("\n");
//            outputStr.append("\n");
//            builder.appendFontStyle(ICommandBuilder.FontStyleType.B);
            builder.appendAlignment(ICommandBuilder.AlignmentPosition.Left);
            String strDate = General.detailData.getString("date_modified");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
            SimpleDateFormat outputDateFormat = new SimpleDateFormat("d MMMM, yyyy hh:mm a");
            Date d = dateFormat.parse(strDate);
            builder.append(StringUtils.rightPad(String.format("%s",outputDateFormat.format(d)), lineLength, ' ').getBytes(encoding));
            builder.append(("\n").getBytes(encoding));
            builder.append(("Name: ").getBytes(encoding));
//            builder.appendFontStyle(ICommandBuilder.FontStyleType.A);
            builder.append(General.detailData.getJSONObject("billing").getString("first_name").getBytes(encoding));
            builder.append(" ".getBytes(encoding));
            builder.append(General.detailData.getJSONObject("billing").getString("last_name").getBytes(encoding));
            builder.append("\n".getBytes(encoding));
//            builder.appendFontStyle(ICommandBuilder.FontStyleType.B);
            builder.append("Email: ".getBytes(encoding));
//            builder.appendFontStyle(ICommandBuilder.FontStyleType.A);
            builder.append(General.detailData.getJSONObject("billing").getString("email").getBytes(encoding));
            builder.append("\n".getBytes(encoding));
//            builder.appendFontStyle(ICommandBuilder.FontStyleType.B);
            builder.append("Phone: ".getBytes(encoding));
//            builder.appendFontStyle(ICommandBuilder.FontStyleType.A);
            builder.append(General.detailData.getJSONObject("billing").getString("phone").getBytes(encoding));
            builder.append("\n".getBytes(encoding));

            builder.append(("\n").getBytes(encoding));
            JSONArray itemArray = General.detailData.getJSONArray("line_items");
            for(int i = 0; i < itemArray.length(); i++) {
                JSONObject item = itemArray.getJSONObject(i);
                builder.appendFontStyle(ICommandBuilder.FontStyleType.A);
//                String lineStr = item.getString("name");
////                lineStr += " --- ";
//                lineStr += StringUtils.rightPad(String.format("  (%s) ",item.getString("quantity")), lineLength - lineStr.length()-10, ' ');
//
                String lineStr = String.format("(%s)",item.getString("quantity"));
//                lineStr += " --- ";
                lineStr += StringUtils.rightPad(item.getString("name"), lineLength - lineStr.length()-10, ' ');

                String price = String.format("$%.2f", item.getDouble("price"));
                lineStr += StringUtils.rightPad(price, lineLength - lineStr.length(), ' ');
                builder.append(lineStr.getBytes(encoding));
                builder.append("\n".getBytes(encoding));
                StringBuilder strDetails = new StringBuilder();
                StringBuilder productNote = new StringBuilder();
                for( int j = 0; j < item.getJSONArray("meta_data").length(); j++) {
                    if (strDetails.length() == 0 && item.getJSONArray("meta_data").getJSONObject(j).getString("key").equals("_ywapo_meta_data")) {
                        JSONArray detailData = item.getJSONArray("meta_data").getJSONObject(j).getJSONArray("value");
                        for(int k = 0; k < detailData.length(); k++ ) {
                            JSONObject detailItem = detailData.getJSONObject(k);
                            double detailPrice = detailItem.getDouble("price");
                            detailPrice = (double)Math.round(detailPrice * 100) / 100.0;
                            if(detailPrice==0) {
                                strDetails.append("    ").append(detailItem.getString("value"));
//                                strDetails.append("    ").append(detailItem.getString("name")).append(": ").append(detailItem.getString("value"));
                            }
                            else {
                                strDetails.append("    ").append("($").append(detailPrice).append("): ").append(detailItem.getString("value"));
//                                strDetails.append("    ").append(detailItem.getString("name")).append("($").append(detailPrice).append("): ").append(detailItem.getString("value"));
                            }
                            strDetails.append("\n");
                        }
                    }
                    else if(productNote.length() == 0 && item.getJSONArray("meta_data").getJSONObject(j).getString("key").equals("Product Note")){
//                        productNote.append("    Product Note: ").append(item.getJSONArray("meta_data").getJSONObject(j).getString("value"));
                        productNote.append("    ").append(item.getJSONArray("meta_data").getJSONObject(j).getString("value"));
                    }
                }
                builder.appendFontStyle(ICommandBuilder.FontStyleType.B);
                strDetails.append(productNote.toString()).append("\n");
                builder.append(strDetails.toString().getBytes(encoding));
            }
            builder.append("\n".getBytes(encoding));
            double subTotal = 0;
            for (int i = 0; i < itemArray.length(); i ++ ) {
                subTotal += itemArray.getJSONObject(i).getDouble("subtotal");
            }
            builder.appendFontStyle(ICommandBuilder.FontStyleType.A);
            builder.append("Subtotal      ".getBytes(encoding));
            builder.append(StringUtils.rightPad(String.format("$%.2f", subTotal), lineLength - 14, ' ').getBytes(encoding));
            builder.append("\n".getBytes(encoding));
            builder.append("Tax & Fees    ".getBytes(encoding));
            builder.append(StringUtils.rightPad("$" + General.detailData.getString("total_tax"), lineLength - 14, ' ').getBytes(encoding));
            builder.append("\n".getBytes(encoding));
            builder.append("Total         ".getBytes(encoding));
            builder.append(StringUtils.rightPad("$" + General.detailData.getString("total"), lineLength - 14, ' ').getBytes(encoding));
            builder.append("\n\n".getBytes(encoding));

//            Log.d("Print String", outputStr.toString());
        } catch (ParseException | JSONException e) {
            e.printStackTrace();
        }
    }
    @SuppressLint({"DefaultLocale","SimpleDateFormat"})
    static public void receiptFormatRaster(ICommandBuilder builder, int lineLength, int textSize, int printWidth) {
        //Looks Bigger!
        builder.appendAlignment(ICommandBuilder.AlignmentPosition.Center);
        builder.appendBitmap(BitmapFactory.decodeResource(General.instanceOfApplication.getResources(), R.drawable.slogo), false);

        textSize *= General.printerFontSize;
        int smallTextSize = (int) (textSize * 0.8);

        try {
            Typeface typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
            Bitmap onlineOrder = createBitmapFromText("\n\nONLINE ORDER   " + General.orderId, smallTextSize, PrinterSettingConstant.PAPER_SIZE_ESCPOS_THREE_INCH, typeface);
            builder.appendBitmap(onlineOrder,false);

            Bitmap restaurantName = createBitmapFromText(
                    General.restaurantName + "\n\n", textSize,
                    printWidth,
                    Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
            builder.appendBitmap(restaurantName,false);

            builder.appendAlignment(ICommandBuilder.AlignmentPosition.Left);
            String strDate = General.detailData.getString("date_modified");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
            SimpleDateFormat outputDateFormat = new SimpleDateFormat("d MMMM, yyyy hh:mm a");
            Date d = dateFormat.parse(strDate);

            Bitmap orderDate = createBitmapFromText(
                    StringUtils.rightPad(String.format("%s",outputDateFormat.format(d)), lineLength, ' ') + "\n" +
                            "Name:  " + General.detailData.getJSONObject("billing").getString("first_name") + " " +
                            General.detailData.getJSONObject("billing").getString("last_name") + "\n" +
                            "Email:  " + General.detailData.getJSONObject("billing").getString("email") + "\n" +
                            "Phone:  " + General.detailData.getJSONObject("billing").getString("phone") + "\n\n",
                    smallTextSize,
                    printWidth,
                    Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
            builder.appendBitmap(orderDate,false);

            JSONArray itemArray = General.detailData.getJSONArray("line_items");
            for(int i = 0; i < itemArray.length(); i++) {
                JSONObject item = itemArray.getJSONObject(i);
//                String lineStr = item.getString("name");
//                lineStr += StringUtils.rightPad(String.format("  (%s) ",item.getString("quantity")), lineLength - lineStr.length()-10, ' ');

                String lineStr = String.format("(%s)",item.getString("quantity"));
                lineStr += StringUtils.rightPad(item.getString("name"), lineLength - lineStr.length()-10, ' ');

                String price = String.format("$%.2f", item.getDouble("price"));
                lineStr += StringUtils.rightPad(price, lineLength - lineStr.length(), ' ');
//                lineStr += "\n";

                builder.appendBitmap(createBitmapFromText(
                        lineStr,
                        textSize,
                        printWidth,
                        Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)),
                        false);

                StringBuilder strDetails = new StringBuilder();
                StringBuilder productNote = new StringBuilder();
                for( int j = 0; j < item.getJSONArray("meta_data").length(); j++) {
                    if (strDetails.length() == 0 && item.getJSONArray("meta_data").getJSONObject(j).getString("key").equals("_ywapo_meta_data")) {
                        JSONArray detailData = item.getJSONArray("meta_data").getJSONObject(j).getJSONArray("value");
                        for(int k = 0; k < detailData.length(); k++ ) {
                            JSONObject detailItem = detailData.getJSONObject(k);
                            double detailPrice = detailItem.getDouble("price");
                            detailPrice = (double)Math.round(detailPrice * 100) / 100.0;
                            if(detailPrice==0) {
                                strDetails.append("    ").append(detailItem.getString("value"));
//                                strDetails.append("    ").append(detailItem.getString("name")).append(": ").append(detailItem.getString("value"));
                            }
                            else {
                                strDetails.append("    ").append("($").append(detailPrice).append("): ").append(detailItem.getString("value"));
//                                strDetails.append("    ").append(detailItem.getString("name")).append("($").append(detailPrice).append("): ").append(detailItem.getString("value"));
                            }
                            strDetails.append("\n");
                        }
                    }
                    else if(productNote.length() == 0 && item.getJSONArray("meta_data").getJSONObject(j).getString("key").equals("Product Note")){
                        productNote.append("    ").append(item.getJSONArray("meta_data").getJSONObject(j).getString("value"));
//                        productNote.append("    Product Note: ").append(item.getJSONArray("meta_data").getJSONObject(j).getString("value"));
                    }
                }
                strDetails.append(productNote.toString()).append("\n");
                builder.appendBitmap(createBitmapFromText(
                        strDetails.toString(),
                        smallTextSize,
                        printWidth,
                        Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)),
                        false);
            }
//            outputStr.append("\n");
            double subTotal = 0;
            for (int i = 0; i < itemArray.length(); i ++ ) {
                subTotal += itemArray.getJSONObject(i).getDouble("subtotal");
            }

            String totalStr = "\nSubtotal      " +
                    StringUtils.rightPad(String.format("$%.2f", subTotal), lineLength - 14, ' ') +
                    "\n" +
                    "Tax & Fees    " +
                    StringUtils.rightPad("$" + General.detailData.getString("total_tax"), lineLength - 14, ' ') +
                    "\n" +
                    "Total         " +
                    StringUtils.rightPad("$" + General.detailData.getString("total"), lineLength - 14, ' ') +
                    "\n\n\n";
            builder.appendBitmap(createBitmapFromText(
                    totalStr,
                    textSize,
                    printWidth,
                    Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)),
                    false);

//            builder.appendBitmap(createBitmapFromText(
//                    "Printed by ReceiptFormatRaster ("+textSize+","+smallTextSize+")",
//                    smallTextSize,
//                    printWidth,
//                    Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)),
//                    false);
//            Log.d("Print String", outputStr.toString());

        } catch (ParseException | JSONException e) {
            e.printStackTrace();
        }
    }
    static public void closeOutFormat(ICommandBuilder builder, int lineLength, Charset encoding) {
        builder.appendAlignment(ICommandBuilder.AlignmentPosition.Center);
        builder.appendBitmap(BitmapFactory.decodeResource(General.instanceOfApplication.getResources(), R.drawable.slogo), false);
        builder.append(("\n\n\n").getBytes(encoding));

        builder.appendFontStyle(ICommandBuilder.FontStyleType.B);
        builder.append(("CLOSE OUT SHEET\n").getBytes(encoding));
        builder.appendFontStyle(ICommandBuilder.FontStyleType.A);
        builder.append(General.restaurantName.getBytes(encoding));
        builder.append(("\n\n\n").getBytes(encoding));

        builder.appendAlignment(ICommandBuilder.AlignmentPosition.Left);
        builder.appendFontStyle(ICommandBuilder.FontStyleType.B);
        builder.append(StringUtils.rightPad("Reports for Period: " + CloseOutData.period, lineLength, ' ').getBytes(encoding));
        builder.append(("\n\n").getBytes(encoding));

        builder.appendFontStyle(ICommandBuilder.FontStyleType.A);
        builder.appendAlignment(ICommandBuilder.AlignmentPosition.Center);
        String report = "";
        report += "       Daily Sales: " + StringUtils.rightPad(CloseOutData.dailySales,     lineLength - 18, ' ') + "\n";
        report += "         Discounts: " + StringUtils.rightPad(CloseOutData.discounts,      lineLength - 18, ' ') + "\n";
        report += "Credit/Debit Cards: " + StringUtils.rightPad(CloseOutData.cardPayment,    lineLength - 18, ' ') + "\n";
        report += "              Cash: " + StringUtils.rightPad(CloseOutData.cashTotal,      lineLength - 18, ' ') + "\n";
        report += "     GiftCard Sold: " + StringUtils.rightPad(CloseOutData.giftPayment,    lineLength - 18, ' ') + "\n";
        report += "\n";
        report += "         Uber Eats: " + StringUtils.rightPad(CloseOutData.uberEat,        lineLength - 18, ' ') + "\n";
        report += "           GrubHub: " + StringUtils.rightPad(CloseOutData.grubHub,        lineLength - 18, ' ') + "\n";
        report += "          DoorDash: " + StringUtils.rightPad(CloseOutData.doorDash,       lineLength - 18, ' ') + "\n";
        report += "         Postmates: " + StringUtils.rightPad(CloseOutData.postmates,      lineLength - 18, ' ') + "\n";
        report += "   Online Ordering: " + StringUtils.rightPad(CloseOutData.onlineOrdering, lineLength - 18, ' ') + "\n";
        report += "              Tips: " + StringUtils.rightPad(CloseOutData.tips,           lineLength - 18, ' ') + "\n";
        report += "           Refunds: " + StringUtils.rightPad(CloseOutData.refunds,        lineLength - 18, ' ') + "\n";
        report += "\n\n\n";

        builder.append(report.getBytes(encoding));
    }

    static public void closeOutFormatRaster(ICommandBuilder builder, int lineLength, int textSize, int printWidth) {
        builder.appendAlignment(ICommandBuilder.AlignmentPosition.Center);
        builder.appendBitmap(BitmapFactory.decodeResource(General.instanceOfApplication.getResources(), R.drawable.slogo), false);
        textSize *= General.printerFontSize;
        int smallTextSize = (int) (textSize * 0.8);

        Typeface typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
        Bitmap onlineOrder = createBitmapFromText("\n\nCLOSE OUT SHEET", smallTextSize, PrinterSettingConstant.PAPER_SIZE_ESCPOS_THREE_INCH, typeface);
        builder.appendBitmap(onlineOrder,false);

        Bitmap restaurantName = createBitmapFromText(
                General.restaurantName + "\n\n", textSize + 3,
                printWidth,
                Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        builder.appendBitmap(restaurantName,false);

//        builder.appendAlignment(ICommandBuilder.AlignmentPosition.Left);

        builder.appendBitmap(createBitmapFromText(
                StringUtils.rightPad("Reports for Period: " + CloseOutData.period, lineLength, ' ') + "\n",
                smallTextSize,
                printWidth,
                Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)),
                false);

//        builder.appendAlignment(ICommandBuilder.AlignmentPosition.Center);

        String report = "";

        report += "       Daily Sales: " + StringUtils.rightPad(CloseOutData.dailySales,     lineLength - 18, ' ') + "\n";
        report += "         Discounts: " + StringUtils.rightPad(CloseOutData.discounts,      lineLength - 18, ' ') + "\n";
        report += "Credit/Debit Cards: " + StringUtils.rightPad(CloseOutData.cardPayment,    lineLength - 18, ' ') + "\n";
        report += "              Cash: " + StringUtils.rightPad(CloseOutData.cashTotal,      lineLength - 18, ' ') + "\n";
        report += "     GiftCard Sold: " + StringUtils.rightPad(CloseOutData.giftPayment,    lineLength - 18, ' ') + "\n";
        report += "\n";
        report += "         Uber Eats: " + StringUtils.rightPad(CloseOutData.uberEat,        lineLength - 18, ' ') + "\n";
        report += "           GrubHub: " + StringUtils.rightPad(CloseOutData.grubHub,        lineLength - 18, ' ') + "\n";
        report += "          DoorDash: " + StringUtils.rightPad(CloseOutData.doorDash,       lineLength - 18, ' ') + "\n";
        report += "         Postmates: " + StringUtils.rightPad(CloseOutData.postmates,      lineLength - 18, ' ') + "\n";
        report += "   Online Ordering: " + StringUtils.rightPad(CloseOutData.onlineOrdering, lineLength - 18, ' ') + "\n";
        report += "              Tips: " + StringUtils.rightPad(CloseOutData.tips,           lineLength - 18, ' ') + "\n";
        report += "           Refunds: " + StringUtils.rightPad(CloseOutData.refunds,        lineLength - 18, ' ') + "\n";
        report += "\n\n\n";

        builder.appendBitmap(createBitmapFromText(
                report,
                textSize,
                printWidth,
                Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)),
                false);
    }

    @Override
    public void append2inchTextReceiptData(ICommandBuilder builder, boolean utf8) {
        Charset encoding;

        if (utf8) {
            encoding = Charset.forName("UTF-8");

            builder.appendCodePage(CodePageType.UTF8);
        }
        else {
            encoding = Charset.forName("US-ASCII");

            builder.appendCodePage(CodePageType.CP998);
        }

        builder.appendInternational(InternationalType.USA);

        builder.appendCharacterSpace(0);

        receiptFormat(builder,32,encoding);

        builder.appendCharacterSpace(0);
    }

    @Override
    public void append3inchTextReceiptData(ICommandBuilder builder, boolean utf8) {
        Charset encoding;

        if (utf8) {
            encoding = Charset.forName("UTF-8");

            builder.appendCodePage(CodePageType.UTF8);
        }
        else {
            encoding = Charset.forName("US-ASCII");

            builder.appendCodePage(CodePageType.CP998);
        }

        builder.appendInternational(InternationalType.USA);

        builder.appendCharacterSpace(0);
        receiptFormat(builder,48,encoding);
        builder.appendCharacterSpace(0);
    }

    @Override
    public void append4inchTextReceiptData(ICommandBuilder builder, boolean utf8) {
        Charset encoding;

        if (utf8) {
            encoding = Charset.forName("UTF-8");

            builder.appendCodePage(CodePageType.UTF8);
        }
        else {
            encoding = Charset.forName("US-ASCII");

            builder.appendCodePage(CodePageType.CP998);
        }

        builder.appendInternational(InternationalType.USA);

        builder.appendCharacterSpace(0);
        receiptFormat(builder,69,encoding);
        builder.appendCharacterSpace(0);
    }
    ////CloseOut
    @Override
    public void append2inchTextCloseOutData(ICommandBuilder builder, boolean utf8) {
        Charset encoding;

        if (utf8) {
            encoding = Charset.forName("UTF-8");

            builder.appendCodePage(CodePageType.UTF8);
        }
        else {
            encoding = Charset.forName("US-ASCII");

            builder.appendCodePage(CodePageType.CP998);
        }

        builder.appendInternational(InternationalType.USA);

        builder.appendCharacterSpace(0);

        closeOutFormat(builder,32,encoding);

        builder.appendCharacterSpace(0);
    }

    @Override
    public void append3inchTextCloseOutData(ICommandBuilder builder, boolean utf8) {
        Charset encoding;

        if (utf8) {
            encoding = Charset.forName("UTF-8");

            builder.appendCodePage(CodePageType.UTF8);
        }
        else {
            encoding = Charset.forName("US-ASCII");

            builder.appendCodePage(CodePageType.CP998);
        }

        builder.appendInternational(InternationalType.USA);

        builder.appendCharacterSpace(0);
        closeOutFormat(builder,48,encoding);
        builder.appendCharacterSpace(0);
    }

    @Override
    public void append4inchTextCloseOutData(ICommandBuilder builder, boolean utf8) {
        Charset encoding;

        if (utf8) {
            encoding = Charset.forName("UTF-8");

            builder.appendCodePage(CodePageType.UTF8);
        }
        else {
            encoding = Charset.forName("US-ASCII");

            builder.appendCodePage(CodePageType.CP998);
        }

        builder.appendInternational(InternationalType.USA);

        builder.appendCharacterSpace(0);
        closeOutFormat(builder,69,encoding);
        builder.appendCharacterSpace(0);
    }

    @Override
    public void create2inchRasterReceiptImage(ICommandBuilder builder,Resources resources) {
        int textSize = 22;
        receiptFormatRaster(builder,29,textSize,PrinterSettingConstant.PAPER_SIZE_TWO_INCH);
    }

    @Override
    public void create3inchRasterReceiptImage(ICommandBuilder builder,Resources resources) {
//        String textToPrint = receiptFormat(38);
                /*
                "        Star Clothing Boutique\n" +
                "             123 Star Road\n" +
                "           City, State 12345\n" +
                "\n" +
                "Date:MM/DD/YYYY          Time:HH:MM PM\n" +
                "--------------------------------------\n" +
                "SALE\n" +
                "SKU            Description       Total\n" +
                "300678566      PLAIN T-SHIRT     10.99\n" +
                "300692003      BLACK DENIM       29.99\n" +
                "300651148      BLUE DENIM        29.99\n" +
                "300642980      STRIPED DRESS     49.99\n" +
                "30063847       BLACK BOOTS       35.99\n" +
                "\n" +
                "Subtotal                        156.95\n" +
                "Tax                               0.00\n" +
                "--------------------------------------\n" +
                "Total                          $156.95\n" +
                "--------------------------------------\n" +
                "\n" +
                "Charge\n" +
                "156.95\n" +
                "Visa XXXX-XXXX-XXXX-0123\n" +
                "Refunds and Exchanges\n" +
                "Within 30 days with receipt\n" +
                "And tags attached\n";


                 */
        int      textSize = 25;
//        Typeface typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
        receiptFormatRaster(builder,38,textSize,PrinterSettingConstant.PAPER_SIZE_THREE_INCH);
//        return createBitmapFromText(textToPrint, textSize, PrinterSettingConstant.PAPER_SIZE_THREE_INCH, typeface);
    }

    @Override
    public void create4inchRasterReceiptImage(ICommandBuilder builder,Resources resources) {
        int textSize = 23;
        receiptFormatRaster(builder,57,textSize,PrinterSettingConstant.PAPER_SIZE_THREE_INCH);
    }

    @Override
    public void create2inchRasterCloseOutImage(ICommandBuilder builder, Resources resources) {
        int textSize = 22;
        closeOutFormatRaster(builder,29,textSize,PrinterSettingConstant.PAPER_SIZE_TWO_INCH);
    }

    @Override
    public void create3inchRasterCloseOutImage(ICommandBuilder builder, Resources resources) {
        int textSize = 25;
        closeOutFormatRaster(builder,38,textSize,PrinterSettingConstant.PAPER_SIZE_THREE_INCH);
    }

    @Override
    public void create4inchRasterCloseOutImage(ICommandBuilder builder, Resources resources) {
        int textSize = 23;
        closeOutFormatRaster(builder,57,textSize,PrinterSettingConstant.PAPER_SIZE_THREE_INCH);
    }

    @Override
    public Bitmap createCouponImage(Resources resources) {
        return BitmapFactory.decodeResource(resources, R.drawable.slogo);
    }

    @Override
    public void createEscPos3inchRasterReceiptImage(ICommandBuilder builder,Resources resources) {
        int textSize = 24;
        receiptFormatRaster(builder,35,textSize,PrinterSettingConstant.PAPER_SIZE_ESCPOS_THREE_INCH);
    }

    @Override
    public void createEscPos3inchRasterCloseOutImage(ICommandBuilder builder, Resources resources) {
        int      textSize = 24;
        closeOutFormatRaster(builder,35,textSize,PrinterSettingConstant.PAPER_SIZE_ESCPOS_THREE_INCH);
    }

    @Override
    public void appendEscPos3inchTextReceiptData(ICommandBuilder builder, boolean utf8) {
        Charset encoding;

        if (utf8) {
            encoding = Charset.forName("UTF-8");

            builder.appendCodePage(CodePageType.UTF8);
        }
        else {
            encoding = Charset.forName("US-ASCII");

            builder.appendCodePage(CodePageType.CP998);
        }

        builder.appendInternational(InternationalType.USA);

        receiptFormat(builder,42, encoding);

        builder.appendCharacterSpace(0);
        /*
        builder.appendCharacterSpace(0);

        builder.appendAlignment(AlignmentPosition.Center);

        builder.append((
                "\n" +
                "Star Clothing Boutique\n" +
                        "123 Star Road\n" +
                        "City, State 12345\n" +
                        "\n").getBytes(encoding));

        builder.appendAlignment(AlignmentPosition.Left);

        builder.append((
                "Date:MM/DD/YYYY              Time:HH:MM PM\n" +
                "------------------------------------------\n" +
                "\n").getBytes(encoding));

        builder.appendEmphasis(("SALE \n").getBytes(encoding));

        builder.append((
                "SKU            Description           Total\n" +
                        "300678566      PLAIN T-SHIRT         10.99\n" +
                        "300692003      BLACK DENIM           29.99\n" +
                        "300651148      BLUE DENIM            29.99\n" +
                        "300642980      STRIPED DRESS         49.99\n" +
                        "300638471      BLACK BOOTS           35.99\n" +
                        "\n" +
                        "Subtotal                            156.95\n" +
                        "Tax                                   0.00\n" +
                        "------------------------------------------\n").getBytes(encoding));

        builder.append(("Total                 ").getBytes(encoding));

        builder.appendMultiple(("   $156.95\n").getBytes(encoding), 2, 2);

        builder.append((
                "------------------------------------------\n" +
                        "\n" +
                        "Charge\n" +
                        "156.95\n" +
                        "Visa XXXX-XXXX-XXXX-0123\n" +
                        "\n").getBytes(encoding));

        builder.appendInvert(("Refunds and Exchanges\n").getBytes(encoding));

        builder.append(("Within ").getBytes(encoding));

        builder.appendUnderLine(("30 days").getBytes(encoding));

        builder.append((" with receipt\n").getBytes(encoding));

        builder.append((
                "And tags attached\n" +
                        "\n").getBytes(encoding));

        builder.appendAlignment(AlignmentPosition.Center);

        builder.appendBarcode(("{BStar.").getBytes(Charset.forName("US-ASCII")), BarcodeSymbology.Code128, BarcodeWidth.Mode2, 40, true);

         */
    }

    @Override
    public void appendDotImpact3inchTextReceiptData(ICommandBuilder builder, boolean utf8) {
        Charset encoding;

        if (utf8) {
            encoding = Charset.forName("UTF-8");

            builder.appendCodePage(CodePageType.UTF8);
        }
        else {
            encoding = Charset.forName("US-ASCII");

            builder.appendCodePage(CodePageType.CP998);
        }

        builder.appendInternational(InternationalType.USA);

        builder.appendCharacterSpace(0);

        receiptFormat(builder,42, encoding);

        builder.appendCharacterSpace(0);
        /*
        builder.appendAlignment(AlignmentPosition.Center);

        builder.append((
                "Star Clothing Boutique\n" +
                        "123 Star Road\n" +
                        "City, State 12345\n" +
                        "\n").getBytes(encoding));

        builder.appendAlignment(AlignmentPosition.Left);

        builder.append((
                "Date:MM/DD/YYYY              Time:HH:MM PM\n" +
                        "------------------------------------------\n" +
                        "\n").getBytes(encoding));

        builder.appendEmphasis(("SALE \n").getBytes(encoding));

        builder.append((
                "SKU             Description          Total\n" +
                        "300678566       PLAIN T-SHIRT        10.99\n" +
                        "300692003       BLACK DENIM          29.99\n" +
                        "300651148       BLUE DENIM           29.99\n" +
                        "300642980       STRIPED DRESS        49.99\n" +
                        "300638471       BLACK BOOTS          35.99\n" +
                        "\n" +
                        "Subtotal                            156.95\n" +
                        "Tax                                   0.00\n" +
                        "------------------------------------------\n" +
                        "Total                              $156.95\n" +
                        "------------------------------------------\n" +
                        "\n" +
                        "Charge\n" +
                        "156.95\n" +
                        "Visa XXXX-XXXX-XXXX-0123\n" +
                        "\n").getBytes(encoding));

        builder.appendInvert(("Refunds and Exchanges\n").getBytes(encoding));

        builder.append(("Within ").getBytes(encoding));

        builder.appendUnderLine(("30 days").getBytes(encoding));

        builder.append((" with receipt\n").getBytes(encoding));


         */

    }

    @Override
    public void appendEscPos3inchTextCloseOutData(ICommandBuilder builder, boolean utf8) {
        Charset encoding;

        if (utf8) {
            encoding = Charset.forName("UTF-8");

            builder.appendCodePage(CodePageType.UTF8);
        }
        else {
            encoding = Charset.forName("US-ASCII");

            builder.appendCodePage(CodePageType.CP998);
        }

        builder.appendInternational(InternationalType.USA);

        closeOutFormat(builder,42, encoding);

        builder.appendCharacterSpace(0);
    }

    @Override
    public void appendDotImpact3inchTextCloseOutData(ICommandBuilder builder, boolean utf8) {
        Charset encoding;

        if (utf8) {
            encoding = Charset.forName("UTF-8");

            builder.appendCodePage(CodePageType.UTF8);
        }
        else {
            encoding = Charset.forName("US-ASCII");

            builder.appendCodePage(CodePageType.CP998);
        }

        builder.appendInternational(InternationalType.USA);

        builder.appendCharacterSpace(0);

        closeOutFormat(builder,42, encoding);

        builder.appendCharacterSpace(0);
    }

    @Override
    public void createSk12inchRasterReceiptImage(ICommandBuilder builder,Resources resources) {
//        String textToPrint = receiptFormat(33);
        /*
                "     Star Clothing Boutique\n" +
                        "          123 Star Road\n" +
                        "        City, State 12345\n" +
                        "\n" +
                        "Date:MM/DD/YYYY     Time:HH:MM PM\n" +
                        "---------------------------------\n" +
                        "SALE\n" +
                        "SKU         Description     Total\n" +
                        "300678566   PLAIN T-SHIRT   10.99\n" +
                        "300692003   BLACK DENIM     29.99\n" +
                        "300651148   BLUE DENIM      29.99\n" +
                        "300642980   STRIPED DRESS   49.99\n" +
                        "30063847    BLACK BOOTS     35.99\n" +
                        "\n" +
                        "Subtotal                   156.95\n" +
                        "Tax                          0.00\n" +
                        "---------------------------------\n" +
                        "Total                     $156.95\n" +
                        "---------------------------------\n" +
                        "\n" +
                        "Charge\n" +
                        "156.95\n" +
                        "Visa XXXX-XXXX-XXXX-0123\n" +
                        "Refunds and Exchanges\n" +
                        "Within 30 days with receipt\n" +
                        "And tags attached\n";

         */

        int      textSize = 22;
//        Typeface typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
        receiptFormatRaster(builder,33,textSize,PrinterSettingConstant.PAPER_SIZE_SK1_TWO_INCH);
//        return createBitmapFromText(textToPrint, textSize, PrinterSettingConstant.PAPER_SIZE_SK1_TWO_INCH, typeface);
    }

    @Override
    public void createSk12inchRasterCloseOutImage(ICommandBuilder builder, Resources resources) {
        int      textSize = 22;
        receiptFormatRaster(builder,33,textSize,PrinterSettingConstant.PAPER_SIZE_SK1_TWO_INCH);
    }

    @Override
    public void appendSk12inchTextReceiptData(ICommandBuilder builder, boolean utf8) {
        Charset encoding;

        if (utf8) {
            encoding = Charset.forName("UTF-8");

            builder.appendCodePage(CodePageType.UTF8);
        }
        else {
            encoding = Charset.forName("US-ASCII");

            builder.appendCodePage(CodePageType.CP998);
        }

        builder.appendInternational(InternationalType.USA);

        receiptFormat(builder,36, encoding);

        builder.appendCharacterSpace(0);
        /*
        builder.appendCharacterSpace(0);

        builder.appendAlignment(AlignmentPosition.Center);

        builder.append((
                "Star Clothing Boutique\n" +
                        "123 Star Road\n" +
                        "City, State 12345\n" +
                        "\n").getBytes(encoding));

        builder.appendAlignment(AlignmentPosition.Left);

        builder.append((
                "Date:MM/DD/YYYY        Time:HH:MM PM\n" +
                        "------------------------------------\n" +
                        "\n").getBytes(encoding));

        builder.append((
                "SKU           Description      Total\n" +
                        "300678566     PLAIN T-SHIRT    10.99\n" +
                        "300692003     BLACK DENIM      29.99\n" +
                        "300651148     BLUE DENIM       29.99\n" +
                        "300642980     STRIPED DRESS    49.99\n" +
                        "300638471     BLACK BOOTS      35.99\n" +
                        "\n" +
                        "Subtotal                      156.95\n" +
                        "Tax                             0.00\n" +
                        "------------------------------------\n").getBytes(encoding));

        builder.append(("Total     ").getBytes(encoding));

        builder.appendMultiple(("      $156.95\n").getBytes(encoding), 2, 2);

        builder.append((
                "------------------------------------\n" +
                        "\n" +
                        "Charge\n" +
                        "156.95\n" +
                        "Visa XXXX-XXXX-XXXX-0123\n" +
                        "\n").getBytes(encoding));

        builder.appendInvert(("Refunds and Exchanges\n").getBytes(encoding));

        builder.append(("Within ").getBytes(encoding));

        builder.appendUnderLine(("30 days").getBytes(encoding));

        builder.append((" with receipt\n").getBytes(encoding));

        builder.append((
                "And tags attached\n" +
                        "\n").getBytes(encoding));

        builder.appendAlignment(AlignmentPosition.Center);

        builder.appendBarcode(("{BStar.").getBytes(Charset.forName("US-ASCII")), BarcodeSymbology.Code128, BarcodeWidth.Mode2, 40, true);


         */
    }

    @Override
    public void appendSk12inchTextCloseOutData(ICommandBuilder builder, boolean utf8) {
        Charset encoding;

        if (utf8) {
            encoding = Charset.forName("UTF-8");

            builder.appendCodePage(CodePageType.UTF8);
        }
        else {
            encoding = Charset.forName("US-ASCII");

            builder.appendCodePage(CodePageType.CP998);
        }

        builder.appendInternational(InternationalType.USA);

        closeOutFormat(builder,36, encoding);

        builder.appendCharacterSpace(0);
    }

    @Override
    public void appendTextLabelData(ICommandBuilder builder, boolean utf8) {
        Charset encoding;

        if (utf8) {
            encoding = Charset.forName("UTF-8");

            builder.appendCodePage(CodePageType.UTF8);
        }
        else {
            encoding = Charset.forName("US-ASCII");

            builder.appendCodePage(CodePageType.CP998);
        }

        builder.appendInternational(InternationalType.USA);

        builder.appendCharacterSpace(0);

        builder.appendUnitFeed(20 * 2);

        builder.appendMultipleHeight(2);

        builder.append(("Star Micronics America, Inc.").getBytes(encoding));

        builder.appendUnitFeed(64);

        builder.append(("65 Clyde Road Suite G").getBytes(encoding));

        builder.appendUnitFeed(64);

        builder.append(("Somerset, NJ 08873-3485 U.S.A").getBytes(encoding));

        builder.appendUnitFeed(64);

        builder.appendMultipleHeight(1);
    }

    @Override
    public String createPasteTextLabelString() {
        return "Star Micronics America, Inc.\n" +
                "65 Clyde Road Suite G\n" +
                "Somerset, NJ 08873-3485 U.S.A";
    }

    @Override
    public void appendPasteTextLabelData(ICommandBuilder builder, String pasteText, boolean utf8) {
        Charset encoding;

        if (utf8) {
            encoding = Charset.forName("UTF-8");

            builder.appendCodePage(CodePageType.UTF8);
        }
        else {
            encoding = Charset.forName("US-ASCII");

            builder.appendCodePage(CodePageType.CP998);
        }

        builder.appendInternational(InternationalType.USA);

        builder.appendCharacterSpace(0);

        builder.append(pasteText.getBytes(encoding));
    }


}
