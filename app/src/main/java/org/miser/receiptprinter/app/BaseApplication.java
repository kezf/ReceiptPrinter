package org.miser.receiptprinter.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.rt.printerlibrary.printer.RTPrinter;

import org.miser.receiptprinter.utils.BaseEnum;

public class BaseApplication extends Application {
    public static final String SP_NAME_SETTING = "setting";
    public static BaseApplication instance = null;
    public static String labelSizeStr = "80*40", labelWidth = "80", labelHeight = "40", labelSpeed = "2", labelType = "CPCL", labelOffset = "0";
    private final String TAG = getClass().getSimpleName();
    private RTPrinter rtPrinter;
    @BaseEnum.CmdType
    private int currentCmdType = BaseEnum.CMD_PIN;//默认为针打
    @BaseEnum.ConnectType
    private int currentConnectType = BaseEnum.NONE;//默认未连接

    public static BaseApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sp = getSharedPreferences(BaseApplication.SP_NAME_SETTING, Context.MODE_PRIVATE);
        String labelSize = labelSizeStr;
        String[] temp = labelSize.split("\\*");
        labelWidth = temp[0];
        labelHeight = temp[1];
        instance = this;
    }

    public RTPrinter getRtPrinter() {
        return rtPrinter;
    }

    public void setRtPrinter(RTPrinter rtPrinter) {
        this.rtPrinter = rtPrinter;
    }

    @BaseEnum.CmdType
    public int getCurrentCmdType() {
        return currentCmdType;
    }

    public void setCurrentCmdType(@BaseEnum.CmdType int currentCmdType) {
        this.currentCmdType = currentCmdType;
    }

    @BaseEnum.ConnectType
    public int getCurrentConnectType() {
        return currentConnectType;
    }

    public void setCurrentConnectType(@BaseEnum.ConnectType int currentConnectType) {
        this.currentConnectType = currentConnectType;
    }
}
