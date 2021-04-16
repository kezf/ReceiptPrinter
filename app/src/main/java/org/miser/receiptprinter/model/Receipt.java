package org.miser.receiptprinter.model;

public class Receipt {

    private String mMerchantName;

    private String mAmount;

    private String mDate;

    public String getMerchantName() {
        return mMerchantName;
    }

    public void setMerchantName(String merchantName) {
        mMerchantName = merchantName;
    }

    public String getAmount() {
        return mAmount;
    }

    public void setAmount(String amount) {
        mAmount = amount;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }
}