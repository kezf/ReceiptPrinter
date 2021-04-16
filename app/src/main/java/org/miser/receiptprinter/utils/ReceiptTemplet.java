/**
 * 模板打印，都要移动此单元
 */


package org.miser.receiptprinter.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.rt.printerlibrary.cmd.Cmd;
import com.rt.printerlibrary.cmd.EscFactory;
import com.rt.printerlibrary.enumerate.CommonEnum;
import com.rt.printerlibrary.enumerate.SettingEnum;
import com.rt.printerlibrary.exception.SdkException;
import com.rt.printerlibrary.factory.cmd.CmdFactory;
import com.rt.printerlibrary.printer.RTPrinter;
import com.rt.printerlibrary.setting.BitmapSetting;
import com.rt.printerlibrary.setting.CommonSetting;
import com.rt.printerlibrary.setting.TextSetting;

import org.miser.receiptprinter.R;
import org.miser.receiptprinter.utils.ReceiptEnum.ACQUIRER;
import org.miser.receiptprinter.utils.ReceiptEnum.STUB_TYPE;
import org.miser.receiptprinter.utils.ReceiptEnum.TRANS_TYPE;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class ReceiptTemplet {

    private static ReceiptTemplet receiptTemplet;
    @STUB_TYPE
    int stubType;
    @ACQUIRER
    int acquirer;
    @TRANS_TYPE
    int transType;
    int batchNo, voucherNo;
    String stubTypeDesc, merchantName, merchantNo, terminalNo, operatorNo, acquirerName, transTypeDesc,
            orderNo, referNo, dateTime, amount, reference;
    private RTPrinter rtPrinter;
    private Cmd escCmd;
    private Context context;

    public ReceiptTemplet(RTPrinter rtPrinter, Context context) {
        this.rtPrinter = rtPrinter;
        this.context = context;
    }

    public static ReceiptTemplet getInstance(RTPrinter rtPrinter, Context context) {
        if (receiptTemplet == null)
            receiptTemplet = new ReceiptTemplet(rtPrinter, context);
        else {
            receiptTemplet.rtPrinter = rtPrinter;
            receiptTemplet.context = context;
        }
        receiptTemplet.init();
        return receiptTemplet;
    }

    public void init() {
        int stubType = ReceiptEnum.STUB_TYPE_MERCHANT | ReceiptEnum.STUB_TYPE_CUSTOM;
        int acquirer = ReceiptEnum.ACQUIRER_SPDB;
        int transType = ReceiptEnum.TRANS_TYPE_WX;
        String merchantName = "Test merchant";
        String amount = "0.00";
        String date = "20210101";
        init(stubType, acquirer, transType, merchantName, amount, date);
    }

    public void init(int stubType, String merchantName, String amount, String date) {
        int acquirer = ReceiptEnum.ACQUIRER_SPDB;
        int transType = ReceiptEnum.TRANS_TYPE_WX;
        init(stubType, acquirer, transType, merchantName, amount, date);
    }

    public void init(int stubType, @ACQUIRER int acquirer, @TRANS_TYPE int transType,
                     String merchantName, String amount, String date) {
        this.stubType = stubType;
        this.acquirer = acquirer;
        this.acquirerName = ReceiptEnum.ACQUIRER_NAME[acquirer];
        this.merchantName = merchantName;
        this.merchantNo = ReceiptEnum.MERCHANT_NO_PREFIX[acquirer] + getRandomString(13);
        this.terminalNo = ReceiptEnum.TERMINAL_NO_PREFIX[acquirer] + getRandomString(6);
        this.operatorNo = "01";
        this.transType = transType;
        this.transTypeDesc = ReceiptEnum.TRANS_TYPE_DESC[transType];
        this.orderNo = ReceiptEnum.ORDER_NO_PREFIX[acquirer] + getRandomString(10);
        this.batchNo = getRandomNumber(100);
        this.voucherNo = getRandomNumber(100);
        this.referNo = ReceiptEnum.REFER_NO_PREFIX[acquirer] + getRandomString(10);
        try {
            DateFormat df1 = new SimpleDateFormat("yyyyMMdd");
            Date day = df1.parse(date);
            df1 = new SimpleDateFormat("yyyy/MM/dd");
            Date now = new Date();
            DateFormat df2 = new SimpleDateFormat("HH:mm:ss");
            this.dateTime = df1.format(day) + " " + df2.format(now);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.amount = "RMB " + amount;
        this.reference = "";
    }

    public void receiptTemplet() throws UnsupportedEncodingException, SdkException {
        escInit();
        if ((this.stubType & ReceiptEnum.STUB_TYPE_MERCHANT) != 0) {
            merchantTemplet();
        }
        if ((this.stubType & ReceiptEnum.STUB_TYPE_CUSTOM) != 0) {
            customTemplet();
        }
        escWrite();
    }

    private void merchantTemplet() throws UnsupportedEncodingException, SdkException {
        int i = 0;
        this.stubTypeDesc = ReceiptEnum.STUB_TYPE_DESC[0];

        TextSetting textSetting = new TextSetting();
        textSetting.setAlign(CommonEnum.ALIGN_MIDDLE);//对齐方式-左对齐，居中，右对齐
        textSetting.setBold(SettingEnum.Enable);//加粗
        textSetting.setUnderline(SettingEnum.Disable);//下划线
        textSetting.setIsAntiWhite(SettingEnum.Disable);//反白
        textSetting.setDoubleHeight(SettingEnum.Enable);//倍高
        textSetting.setDoubleWidth(SettingEnum.Enable);//倍宽
        textSetting.setItalic(SettingEnum.Disable);//斜体
        textSetting.setIsEscSmallCharactor(SettingEnum.Disable);//小字体
        escCmd.append(escCmd.getHeaderCmd());//初始化

        CommonSetting commonSetting = new CommonSetting();
        commonSetting.setAlign(CommonEnum.ALIGN_MIDDLE);
        escCmd.append(escCmd.getCommonSettingCmd(commonSetting));

        BitmapSetting bitmapSetting = new BitmapSetting();
        bitmapSetting.setBimtapLimitWidth(40 * 8);//限制图片最大宽度 58打印机=48mm， 80打印机=72mm

        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.spdb);
        escCmd.append(escCmd.getBitmapCmd(bitmapSetting, Bitmap.createBitmap(bmp)));
        escCmd.append(escCmd.getLFCRCmd());

        textSetting.setIsEscSmallCharactor(SettingEnum.Enable);
        textSetting.setBold(SettingEnum.Disable);
        textSetting.setDoubleHeight(SettingEnum.Disable);
        textSetting.setDoubleWidth(SettingEnum.Disable);

        escCmd.append(escCmd.getTextCmd(textSetting, this.stubTypeDesc));
        escCmd.append(escCmd.getLFCRCmd());

        textSetting.setAlign(CommonEnum.ALIGN_LEFT);

        escCmd.append(escCmd.getTextCmd(textSetting, ReceiptEnum.TITLES[i++]));
        escCmd.append(escCmd.getLFCRCmd());

        textSetting.setIsEscSmallCharactor(SettingEnum.Disable);
        textSetting.setDoubleHeight(SettingEnum.Enable);

        escCmd.append(escCmd.getTextCmd(textSetting, this.merchantName));
        escCmd.append(escCmd.getLFCRCmd());

        textSetting.setIsEscSmallCharactor(SettingEnum.Enable);
        textSetting.setDoubleHeight(SettingEnum.Disable);

        escCmd.append(escCmd.getTextCmd(textSetting, ReceiptEnum.TITLES[i++] + this.merchantNo));
        escCmd.append(escCmd.getLFCRCmd());

        escCmd.append(escCmd.getTextCmd(textSetting, ReceiptEnum.TITLES[i++] + this.terminalNo));
        escCmd.append(escCmd.getLFCRCmd());

        escCmd.append(escCmd.getTextCmd(textSetting, ReceiptEnum.TITLES[i++] + this.operatorNo));
        escCmd.append(escCmd.getLFCRCmd());

        escCmd.append(escCmd.getTextCmd(textSetting, ReceiptEnum.TITLES[i++] + this.acquirerName));
        escCmd.append(escCmd.getLFCRCmd());

        escCmd.append(escCmd.getTextCmd(textSetting, ReceiptEnum.TITLES[i++]));
        escCmd.append(escCmd.getLFCRCmd());

        textSetting.setAlign(CommonEnum.ALIGN_MIDDLE);
        textSetting.setIsEscSmallCharactor(SettingEnum.Disable);
        textSetting.setDoubleHeight(SettingEnum.Enable);

        escCmd.append(escCmd.getTextCmd(textSetting, ReceiptEnum.TRANS_TYPE_DESC[this.transType]));
        escCmd.append(escCmd.getLFCRCmd());

        textSetting.setAlign(CommonEnum.ALIGN_LEFT);
        textSetting.setIsEscSmallCharactor(SettingEnum.Enable);
        textSetting.setDoubleHeight(SettingEnum.Disable);

        escCmd.append(escCmd.getTextCmd(textSetting, ReceiptEnum.TITLES[i++] + this.orderNo));
        escCmd.append(escCmd.getLFCRCmd());

        this.batchNo = getRealNumber(this.batchNo, 6);
        String batchNoStr = getFixedNumberString(this.batchNo, 6);
        escCmd.append(escCmd.getTextCmd(textSetting, ReceiptEnum.TITLES[i++] + batchNoStr));
        escCmd.append(escCmd.getLFCRCmd());

        this.voucherNo = getRealNumber(this.voucherNo, 6);
        String voucherNoStr = getFixedNumberString(this.voucherNo, 6);
        escCmd.append(escCmd.getTextCmd(textSetting, ReceiptEnum.TITLES[i++] + voucherNoStr));
        escCmd.append(escCmd.getLFCRCmd());

        escCmd.append(escCmd.getTextCmd(textSetting, ReceiptEnum.TITLES[i++] + this.referNo));
        escCmd.append(escCmd.getLFCRCmd());

        escCmd.append(escCmd.getTextCmd(textSetting, ReceiptEnum.TITLES[i++] + dateTime));
        escCmd.append(escCmd.getLFCRCmd());

        escCmd.append(escCmd.getTextCmd(textSetting, ReceiptEnum.TITLES[i++]));
        escCmd.append(escCmd.getLFCRCmd());

        textSetting.setAlign(CommonEnum.ALIGN_MIDDLE);
        textSetting.setIsEscSmallCharactor(SettingEnum.Disable);
        textSetting.setDoubleHeight(SettingEnum.Enable);

        escCmd.append(escCmd.getTextCmd(textSetting, this.amount));
        escCmd.append(escCmd.getLFCRCmd());

        textSetting.setAlign(CommonEnum.ALIGN_LEFT);
        textSetting.setIsEscSmallCharactor(SettingEnum.Enable);
        textSetting.setDoubleHeight(SettingEnum.Disable);

        escCmd.append(escCmd.getTextCmd(textSetting, ReceiptEnum.TITLES[i++] + this.reference));
        escCmd.append(escCmd.getLFCRCmd());

        escCmd.append(escCmd.getLFCRCmd());
        escCmd.append(escCmd.getLFCRCmd());
        escCmd.append(escCmd.getLFCRCmd());
    }

    private void customTemplet() throws UnsupportedEncodingException, SdkException {
        int i = 0;
        this.stubTypeDesc = ReceiptEnum.STUB_TYPE_DESC[1];

        TextSetting textSetting = new TextSetting();
        textSetting.setAlign(CommonEnum.ALIGN_MIDDLE);//对齐方式-左对齐，居中，右对齐
        textSetting.setBold(SettingEnum.Enable);//加粗
        textSetting.setUnderline(SettingEnum.Disable);//下划线
        textSetting.setIsAntiWhite(SettingEnum.Disable);//反白
        textSetting.setDoubleHeight(SettingEnum.Enable);//倍高
        textSetting.setDoubleWidth(SettingEnum.Enable);//倍宽
        textSetting.setItalic(SettingEnum.Disable);//斜体
        textSetting.setIsEscSmallCharactor(SettingEnum.Disable);//小字体
        escCmd.append(escCmd.getHeaderCmd());//初始化

        CommonSetting commonSetting = new CommonSetting();
        commonSetting.setAlign(CommonEnum.ALIGN_MIDDLE);
        escCmd.append(escCmd.getCommonSettingCmd(commonSetting));

        BitmapSetting bitmapSetting = new BitmapSetting();
        bitmapSetting.setBimtapLimitWidth(40 * 8);//限制图片最大宽度 58打印机=48mm， 80打印机=72mm

        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.spdb);
        escCmd.append(escCmd.getBitmapCmd(bitmapSetting, Bitmap.createBitmap(bmp)));
        escCmd.append(escCmd.getLFCRCmd());

        textSetting.setIsEscSmallCharactor(SettingEnum.Enable);
        textSetting.setBold(SettingEnum.Disable);
        textSetting.setDoubleHeight(SettingEnum.Disable);
        textSetting.setDoubleWidth(SettingEnum.Disable);

        escCmd.append(escCmd.getTextCmd(textSetting, this.stubTypeDesc));
        escCmd.append(escCmd.getLFCRCmd());

        textSetting.setAlign(CommonEnum.ALIGN_LEFT);

        escCmd.append(escCmd.getTextCmd(textSetting, ReceiptEnum.TITLES[i++]));
        escCmd.append(escCmd.getLFCRCmd());

        textSetting.setIsEscSmallCharactor(SettingEnum.Disable);
        textSetting.setDoubleHeight(SettingEnum.Enable);

        escCmd.append(escCmd.getTextCmd(textSetting, this.merchantName));
        escCmd.append(escCmd.getLFCRCmd());

        textSetting.setIsEscSmallCharactor(SettingEnum.Enable);
        textSetting.setDoubleHeight(SettingEnum.Disable);

        escCmd.append(escCmd.getTextCmd(textSetting, ReceiptEnum.TITLES[i++] + this.merchantNo));
        escCmd.append(escCmd.getLFCRCmd());

        escCmd.append(escCmd.getTextCmd(textSetting, ReceiptEnum.TITLES[i++] + this.terminalNo));
        escCmd.append(escCmd.getLFCRCmd());

        escCmd.append(escCmd.getTextCmd(textSetting, ReceiptEnum.TITLES[i++] + this.operatorNo));
        escCmd.append(escCmd.getLFCRCmd());

        escCmd.append(escCmd.getTextCmd(textSetting, ReceiptEnum.TITLES[i++] + this.acquirerName));
        escCmd.append(escCmd.getLFCRCmd());

        escCmd.append(escCmd.getTextCmd(textSetting, ReceiptEnum.TITLES[i++]));
        escCmd.append(escCmd.getLFCRCmd());

        textSetting.setAlign(CommonEnum.ALIGN_MIDDLE);
        textSetting.setIsEscSmallCharactor(SettingEnum.Disable);
        textSetting.setDoubleHeight(SettingEnum.Enable);

        escCmd.append(escCmd.getTextCmd(textSetting, ReceiptEnum.TRANS_TYPE_DESC[this.transType]));
        escCmd.append(escCmd.getLFCRCmd());

        textSetting.setAlign(CommonEnum.ALIGN_LEFT);
        textSetting.setIsEscSmallCharactor(SettingEnum.Enable);
        textSetting.setDoubleHeight(SettingEnum.Disable);

        escCmd.append(escCmd.getTextCmd(textSetting, ReceiptEnum.TITLES[i++] + this.orderNo));
        escCmd.append(escCmd.getLFCRCmd());

        this.batchNo = getRealNumber(this.batchNo, 6);
        String batchNoStr = getFixedNumberString(this.batchNo, 6);
        escCmd.append(escCmd.getTextCmd(textSetting, ReceiptEnum.TITLES[i++] + batchNoStr));
        escCmd.append(escCmd.getLFCRCmd());

        this.voucherNo = getRealNumber(this.voucherNo, 6);
        String voucherNoStr = getFixedNumberString(this.voucherNo, 6);
        escCmd.append(escCmd.getTextCmd(textSetting, ReceiptEnum.TITLES[i++] + voucherNoStr));
        escCmd.append(escCmd.getLFCRCmd());

        escCmd.append(escCmd.getTextCmd(textSetting, ReceiptEnum.TITLES[i++] + this.referNo));
        escCmd.append(escCmd.getLFCRCmd());

        escCmd.append(escCmd.getTextCmd(textSetting, ReceiptEnum.TITLES[i++] + dateTime));
        escCmd.append(escCmd.getLFCRCmd());

        escCmd.append(escCmd.getTextCmd(textSetting, ReceiptEnum.TITLES[i++]));
        escCmd.append(escCmd.getLFCRCmd());

        textSetting.setAlign(CommonEnum.ALIGN_MIDDLE);
        textSetting.setIsEscSmallCharactor(SettingEnum.Disable);
        textSetting.setDoubleHeight(SettingEnum.Enable);

        escCmd.append(escCmd.getTextCmd(textSetting, this.amount));
        escCmd.append(escCmd.getLFCRCmd());

        textSetting.setAlign(CommonEnum.ALIGN_LEFT);
        textSetting.setIsEscSmallCharactor(SettingEnum.Enable);
        textSetting.setDoubleHeight(SettingEnum.Disable);

        escCmd.append(escCmd.getTextCmd(textSetting, ReceiptEnum.TITLES[i++] + this.reference));
        escCmd.append(escCmd.getLFCRCmd());

        escCmd.append(escCmd.getLFCRCmd());
        escCmd.append(escCmd.getLFCRCmd());
        escCmd.append(escCmd.getLFCRCmd());
    }

    private void escInit() {
        CmdFactory escFac = new EscFactory();
        escCmd = escFac.create();
        escCmd.setChartsetName("UTF-8");
    }

    private void escWrite() {
        rtPrinter.writeMsgAsync(escCmd.getAppendCmds());
    }

    private int getRandomNumber(int bound) {
        Random random = new Random();
        //random.nextInt(x)会生成一个范围在0~x（不包含x）内的任意正整数
        return random.nextInt(bound);
    }

    private String getRandomString(int length) {
        String str = "0123456789";
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            //random.nextInt(x)会生成一个范围在0~x（不包含x）内的任意正整数
            int num = random.nextInt(10);
            stringBuilder.append(str.charAt(num));
        }
        return stringBuilder.toString();
    }

    private int getRealNumber(int number, int length) {
        int a = ++number;
        String b = Integer.toString(a);
        if (b.length() > length) {
            a = 0;
        }
        return a;
    }

    private String getFixedNumberString(int number, int length) {
        String a = Integer.toString(number);
        StringBuffer buffer = new StringBuffer();
        buffer.setLength(length);
        if (a.length() > length) {
            buffer.append(a.substring(a.length() - length, length));
        } else {
            for (int i = 0; i < length - a.length(); i++) {
                buffer.append("0");
            }
            buffer.append(a);
        }
        return buffer.toString();
    }

}