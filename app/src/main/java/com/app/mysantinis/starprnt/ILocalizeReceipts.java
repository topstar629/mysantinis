package com.app.mysantinis.starprnt;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.starmicronics.starioextension.ICommandBuilder;
import com.starmicronics.starioextension.StarIoExt.CharacterCode;

public abstract class ILocalizeReceipts {
    private int mPaperSize;
    private int mLanguage;

    protected String mLanguageCode;
    private String mPaperSizeStr;
    private String mScalePaperSizeStr;
    protected CharacterCode mCharacterCode;

    public static ILocalizeReceipts createLocalizeReceipts(int paperSize) {
        ILocalizeReceipts localizeReceipts = new EnglishReceiptsImpl();

        switch (paperSize) {
            case PrinterSettingConstant.PAPER_SIZE_TWO_INCH:
            case PrinterSettingConstant.PAPER_SIZE_SK1_TWO_INCH:
                localizeReceipts.setPaperSizeStr("2\"");
                localizeReceipts.setScalePaperSizeStr("3\"");   // 3inch -> 2inch
                break;
            case PrinterSettingConstant.PAPER_SIZE_THREE_INCH:
            case PrinterSettingConstant.PAPER_SIZE_ESCPOS_THREE_INCH:
            case PrinterSettingConstant.PAPER_SIZE_DOT_THREE_INCH:
                localizeReceipts.setPaperSizeStr("3\"");
                localizeReceipts.setScalePaperSizeStr("4\"");   // 4inch -> 3inch
                break;
//          case PrinterSettingConstant.PAPER_SIZE_FOUR_INCH :
            default:
                localizeReceipts.setPaperSizeStr("4\"");
                localizeReceipts.setScalePaperSizeStr("3\"");   // 3inch -> 4inch
                break;
        }

        localizeReceipts.setLanguage(1);
        localizeReceipts.setPaperSize(paperSize);

        return localizeReceipts;
    }

    public void appendTextReceiptData(ICommandBuilder builder, boolean utf8) {
        switch (mPaperSize) {
            case PrinterSettingConstant.PAPER_SIZE_TWO_INCH:
                append2inchTextReceiptData(builder, utf8);
                break;
            case PrinterSettingConstant.PAPER_SIZE_THREE_INCH:
                append3inchTextReceiptData(builder, utf8);
                break;
            case PrinterSettingConstant.PAPER_SIZE_FOUR_INCH:
                append4inchTextReceiptData(builder, utf8);
                break;
            case PrinterSettingConstant.PAPER_SIZE_ESCPOS_THREE_INCH:
                appendEscPos3inchTextReceiptData(builder, utf8);
                break;
            case PrinterSettingConstant.PAPER_SIZE_SK1_TWO_INCH:
                appendSk12inchTextReceiptData(builder, utf8);
                break;
//          case PrinterSettingConstant.PAPER_SIZE_DOT_THREE_INCH:
            default:
                appendDotImpact3inchTextReceiptData(builder, utf8);
                break;
        }
    }

    public void appendTextCloseOutData(ICommandBuilder builder, boolean utf8) {
        switch (mPaperSize) {
            case PrinterSettingConstant.PAPER_SIZE_TWO_INCH:
                append2inchTextCloseOutData(builder, utf8);
                break;
            case PrinterSettingConstant.PAPER_SIZE_THREE_INCH:
                append3inchTextCloseOutData(builder, utf8);
                break;
            case PrinterSettingConstant.PAPER_SIZE_FOUR_INCH:
                append4inchTextCloseOutData(builder, utf8);
                break;
            case PrinterSettingConstant.PAPER_SIZE_ESCPOS_THREE_INCH:
                appendEscPos3inchTextCloseOutData(builder, utf8);
                break;
            case PrinterSettingConstant.PAPER_SIZE_SK1_TWO_INCH:
                appendSk12inchTextCloseOutData(builder, utf8);
                break;
//          case PrinterSettingConstant.PAPER_SIZE_DOT_THREE_INCH:
            default:
                appendDotImpact3inchTextCloseOutData(builder, utf8);
                break;
        }
    }
    public void createRasterReceiptImage(ICommandBuilder builder,Resources resources) {
        switch (mPaperSize) {
            case PrinterSettingConstant.PAPER_SIZE_TWO_INCH:
                create2inchRasterReceiptImage( builder, resources);
                break;
            case PrinterSettingConstant.PAPER_SIZE_THREE_INCH:
                create3inchRasterReceiptImage( builder, resources);
                break;
            case PrinterSettingConstant.PAPER_SIZE_FOUR_INCH:
                create4inchRasterReceiptImage( builder, resources);
                break;
            case PrinterSettingConstant.PAPER_SIZE_ESCPOS_THREE_INCH:
                createEscPos3inchRasterReceiptImage( builder, resources);
                break;
            case PrinterSettingConstant.PAPER_SIZE_SK1_TWO_INCH:
                createSk12inchRasterReceiptImage( builder, resources);
                break;
//          case PrinterSettingConstant.PAPER_SIZE_DOT_THREE_INCH:
            default :
                create3inchRasterReceiptImage( builder, resources);
                break;
        }

    }
    public void createRasterCloseOutImage(ICommandBuilder builder,Resources resources) {
        switch (mPaperSize) {
            case PrinterSettingConstant.PAPER_SIZE_TWO_INCH:
                create2inchRasterCloseOutImage( builder, resources);
                break;
            case PrinterSettingConstant.PAPER_SIZE_THREE_INCH:
                create3inchRasterCloseOutImage( builder, resources);
                break;
            case PrinterSettingConstant.PAPER_SIZE_FOUR_INCH:
                create4inchRasterCloseOutImage( builder, resources);
                break;
            case PrinterSettingConstant.PAPER_SIZE_ESCPOS_THREE_INCH:
                createEscPos3inchRasterCloseOutImage( builder, resources);
                break;
            case PrinterSettingConstant.PAPER_SIZE_SK1_TWO_INCH:
                createSk12inchRasterCloseOutImage( builder, resources);
                break;
//          case PrinterSettingConstant.PAPER_SIZE_DOT_THREE_INCH:
            default :
                create3inchRasterCloseOutImage( builder, resources);
                break;
        }

    }
//    public Bitmap createScaleRasterReceiptImage(Resources resources) {
//        Bitmap image;
//
//        switch (mPaperSize) {
//            case PrinterSettingConstant.PAPER_SIZE_TWO_INCH:
//            case PrinterSettingConstant.PAPER_SIZE_SK1_TWO_INCH:
//                image = create3inchRasterReceiptImage(resources);      // 3inch -> 2inch
//                break;
//            case PrinterSettingConstant.PAPER_SIZE_THREE_INCH:
//            case PrinterSettingConstant.PAPER_SIZE_ESCPOS_THREE_INCH :
//                image = create4inchRasterReceiptImage(resources);      // 4inch -> 3inch
//                break;
//            case PrinterSettingConstant.PAPER_SIZE_FOUR_INCH:
//                image = create3inchRasterReceiptImage(resources);      // 3inch -> 4inch
//                break;
////          case PrinterSettingConstant.PAPER_SIZE_DOT_THREE_INCH:
//            default                                :
//                image = createCouponImage(resources);
//                break;
//        }
//
//        return image;
//    }

    public int getLanguage() {
        return mLanguage;
    }

    public void setLanguage(int language) {
        mLanguage = language;
    }

    public void setPaperSize(int paperSize) {
        mPaperSize = paperSize;
    }

    public String getLanguageCode() {
        return mLanguageCode;
    }

    public String getPaperSizeStr() {
        return mPaperSizeStr;
    }

    public void setPaperSizeStr(String paperSizeStr){
        mPaperSizeStr = paperSizeStr;
    }

    public String getScalePaperSizeStr() {
        return mScalePaperSizeStr;
    }

    public void setScalePaperSizeStr(String scalePaperSizeStr){
        mScalePaperSizeStr = scalePaperSizeStr;
    }

    public CharacterCode getCharacterCode() {
        return mCharacterCode;
    }

    public abstract void append2inchTextReceiptData(ICommandBuilder builder, boolean utf8);

    public abstract void append3inchTextReceiptData(ICommandBuilder builder, boolean utf8);

    public abstract void append4inchTextReceiptData(ICommandBuilder builder, boolean utf8);

    public abstract void append2inchTextCloseOutData(ICommandBuilder builder, boolean utf8);

    public abstract void append3inchTextCloseOutData(ICommandBuilder builder, boolean utf8);

    public abstract void append4inchTextCloseOutData(ICommandBuilder builder, boolean utf8);

    public abstract void create2inchRasterReceiptImage(ICommandBuilder builder,Resources resources);

    public abstract void create3inchRasterReceiptImage(ICommandBuilder builder,Resources resources);

    public abstract void create4inchRasterReceiptImage(ICommandBuilder builder,Resources resources);

    public abstract void create2inchRasterCloseOutImage(ICommandBuilder builder,Resources resources);

    public abstract void create3inchRasterCloseOutImage(ICommandBuilder builder,Resources resources);

    public abstract void create4inchRasterCloseOutImage(ICommandBuilder builder,Resources resources);

    public abstract Bitmap createCouponImage(Resources resources);

    public abstract void createEscPos3inchRasterReceiptImage(ICommandBuilder builder,Resources resources);

    public abstract void createEscPos3inchRasterCloseOutImage(ICommandBuilder builder,Resources resources);

    public abstract void appendEscPos3inchTextReceiptData(ICommandBuilder builder, boolean utf8);

    public abstract void appendDotImpact3inchTextReceiptData(ICommandBuilder builder, boolean utf8);

    public abstract void appendEscPos3inchTextCloseOutData(ICommandBuilder builder, boolean utf8);

    public abstract void appendDotImpact3inchTextCloseOutData(ICommandBuilder builder, boolean utf8);

    public abstract void createSk12inchRasterReceiptImage(ICommandBuilder builder,Resources resources);

    public abstract void createSk12inchRasterCloseOutImage(ICommandBuilder builder,Resources resources);

    public abstract void appendSk12inchTextReceiptData(ICommandBuilder builder, boolean utf8);

    public abstract void appendSk12inchTextCloseOutData(ICommandBuilder builder, boolean utf8);

    public abstract void appendTextLabelData(ICommandBuilder builder, boolean utf8);

    public abstract String createPasteTextLabelString();

    public abstract void appendPasteTextLabelData(ICommandBuilder builder, String pasteText, boolean utf8);

    static public Bitmap createBitmapFromText(String printText, int textSize, int printWidth, Typeface typeface) {
        Paint paint = new Paint();
        Bitmap bitmap;
        Canvas canvas;

        paint.setTextSize(textSize);
        paint.setTypeface(typeface);

        paint.getTextBounds(printText, 0, printText.length(), new Rect());

        TextPaint textPaint = new TextPaint(paint);
        android.text.StaticLayout staticLayout = new StaticLayout(printText, textPaint, printWidth, Layout.Alignment.ALIGN_NORMAL, 1, 0, false);

        // Create bitmap
        bitmap = Bitmap.createBitmap(staticLayout.getWidth(), staticLayout.getHeight(), Bitmap.Config.ARGB_8888);

        // Create canvas
        canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        canvas.translate(0, 0);
        staticLayout.draw(canvas);

        return bitmap;
    }
}
