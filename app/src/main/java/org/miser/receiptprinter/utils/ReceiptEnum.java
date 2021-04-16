package org.miser.receiptprinter.utils;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ReceiptEnum {
    public static final short STUB_TYPE_MERCHANT = 0x01, STUB_TYPE_CUSTOM = 0x02;
    public static final int ACQUIRER_CCB = 0, ACQUIRER_SPDB = 1;
    public static final int TRANS_TYPE_WX = 0, TRANS_TYPE_ALI = 1, TRANS_TYPE_MCARD = 2;
    public static final String[] STUB_TYPE_DESC = {"--------------商户存根---------------", "--------------持卡人存根--------------"};
    public static final String[] ACQUIRER_NAME = {"建设银行", "浦发银行"};
    public static final String[] MERCHANT_NO_PREFIX = {"30", "31"};
    public static final String[] TERMINAL_NO_PREFIX = {"70", "71"};
    public static final String[] ORDER_NO_PREFIX = {"20", "21"};
    public static final String[] REFER_NO_PREFIX = {"60", "61"};
    public static final String[] TRANS_TYPE_DESC = {"微信扫码", "支付宝扫码", "银行卡支付"};
    public static final String[] TITLES = {"商户名(MERCHANT NAME):", "商户号(MERCHANT NO.):",
            "终端号(TERMINAL NO.):", "操作员号(OPERATOR NO.):", "收单行(ACQUIRER):", "交易类型(TRANS TYPE):",
            "订单号(ORDER NO.):", "批次号(BATCH NO.):", "凭证号(VOUCHER NO.):", "参考号(REFER NO.):",
            "日期/时间(DATE/TIME):", "金额(AMOUNT):", "备注(REFERENCE):"};

    @IntDef({STUB_TYPE_MERCHANT, STUB_TYPE_CUSTOM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface STUB_TYPE {
    }

    @IntDef({ACQUIRER_CCB, ACQUIRER_SPDB})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ACQUIRER {
    }

    @IntDef({TRANS_TYPE_WX, TRANS_TYPE_ALI})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TRANS_TYPE {
    }

}