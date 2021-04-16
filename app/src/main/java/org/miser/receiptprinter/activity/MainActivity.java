package org.miser.receiptprinter.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rt.printerlibrary.bean.BluetoothEdrConfigBean;
import com.rt.printerlibrary.connect.PrinterInterface;
import com.rt.printerlibrary.enumerate.CommonEnum;
import com.rt.printerlibrary.factory.connect.BluetoothFactory;
import com.rt.printerlibrary.factory.connect.PIFactory;
import com.rt.printerlibrary.factory.printer.PrinterFactory;
import com.rt.printerlibrary.factory.printer.UniversalPrinterFactory;
import com.rt.printerlibrary.observer.PrinterObserver;
import com.rt.printerlibrary.observer.PrinterObserverManager;
import com.rt.printerlibrary.printer.RTPrinter;
import com.rt.printerlibrary.rpp02.IPrintInfo;
import com.rt.printerlibrary.rpp02.IPrinterStatus;
import com.rt.printerlibrary.rpp02.RTPrintHelp;
import com.rt.printerlibrary.utils.ConnectListener;
import com.rt.printerlibrary.utils.FuncUtils;

import org.miser.receiptprinter.R;
import org.miser.receiptprinter.app.BaseActivity;
import org.miser.receiptprinter.model.Receipt;
import org.miser.receiptprinter.utils.ReceiptEnum;
import org.miser.receiptprinter.utils.ReceiptTemplet;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements PrinterObserver {
    private static final String TAG = "PrinterMainActivity";
    private final PrinterInterface printerInterface = null;
    private final String[] NEED_PERMISSION = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private final List<String> NO_PERMISSION = new ArrayList<String>();
    private final int PERMISSION_REQUEST_CODE = 100;
    private int iPrintTimes = 0;
    private boolean isPrintEnable = false;
    private BluetoothAdapter bluetoothAdapter;
    private Handler handler;
    private RTPrinter rtPrinter;
    private PrinterInterface curPrinterInterface;
    private ProgressBar pb_connect;
    private Object configObj;
    private FloatingActionButton bPrint;

    private void requestAllPermission() {
        ActivityCompat.requestPermissions(this, NEED_PERMISSION, PERMISSION_REQUEST_CODE);
    }

    /**
     * 设置是否可进行打印操作
     *
     * @param isEnable
     */
    private void setPrintEnable(boolean isEnable) {
        this.isPrintEnable = isEnable;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pb_connect = findViewById(R.id.pb_connect);
        bPrint = findViewById(R.id.btn_print);
        bPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bPrint.setEnabled(false);
                if (!isPrintEnable) {
                    doConnect();
                } else {
                    doPrint();
                }
            }
        });
        requestAllPermission();
        init();
        initPrinterListener();//printer status Listener
    }

    public void init() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 11:
                        String address = (String) msg.obj;
                        connectBluetoothByMac(address);
                }
                return true;
            }
        });
        PrinterFactory printerFactory = new UniversalPrinterFactory();
        rtPrinter = printerFactory.create();
    }

    private void initPrinterListener() {
        PrinterObserverManager.getInstance().add(this);
        RTPrintHelp.getInstance().setiPrintInfo(new IPrintInfo() {
            @Override
            public void opetate_batvol(String device_batvol) {
                showToast(device_batvol);
            }
        });
        RTPrintHelp.getInstance().setiPrinterStatus(new IPrinterStatus() {
            @Override
            public void status(int status, String msg) {
                //02n&328 0:打印机正常 1：打印完成 :2：机芯错误 3:开盖/缺纸 4:低电量 5:正在打印
                // 6：头片过热7:切刀错误   8:纸将尽  9:缺纸状态 10:钱箱状态
                showToast(msg);
            }

            @Override
            public void status_pager(String msg) {
                showToast(msg);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent("org.miser.receiptprinter.activity.SettingsActivity");
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private FirstFragment getFirstFragment() {
        NavHostFragment navHostFragment = (NavHostFragment) this.getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        Fragment fragment = navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
        if (fragment instanceof FirstFragment) {
            return (FirstFragment) fragment;
        }
        return null;
    }

    private void doConnect() {
        showToast(getString(R.string.device_connected));
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String address = preferences.getString("bluetooth_device", null);
        if (!TextUtils.isEmpty(address)) {
            Log.i(TAG, "Start to connect device:" + address + ", timestamp: " + System.currentTimeMillis());
            iPrintTimes = 0;
            pb_connect.setVisibility(View.VISIBLE);
            connectBluetoothByMac(address);
        } else {
            Log.i(TAG, "No device");
            showToast(getString(R.string.set_device));
        }
    }

    private void doPrint() {
        showToast(getString(R.string.device_print));
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isTemplate = preferences.getBoolean("template", false);
        if (isTemplate) {
            ReceiptTemplet receiptTemplet = ReceiptTemplet.getInstance(rtPrinter, MainActivity.this);
            boolean isMerchantTemplate = preferences.getBoolean("template_merchant", false);
            boolean isCustomTemplate = preferences.getBoolean("template_custom", false);
            int stubType = (isMerchantTemplate ? ReceiptEnum.STUB_TYPE_MERCHANT : 0x00) | (isCustomTemplate ? ReceiptEnum.STUB_TYPE_CUSTOM : 0x00);
            FirstFragment fragment = getFirstFragment();
            if (fragment != null) {
                Receipt receipt = fragment.getReceipt();
                if (receipt != null) {
                    try {
                        receiptTemplet.init(stubType,
                                TextUtils.isEmpty(receipt.getMerchantName()) ? getString(R.string.merchant_name_default) : receipt.getMerchantName(),
                                TextUtils.isEmpty(receipt.getAmount()) ? getString(R.string.amount_default) : receipt.getAmount(),
                                TextUtils.isEmpty(receipt.getDate()) ? getString(R.string.date_default) : receipt.getDate());
                        receiptTemplet.receiptTemplet();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            iPrintTimes++;
        }
    }

    private void connectBluetoothByMac(final String address) {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        BluetoothEdrConfigBean bluetoothEdrConfigBean = new BluetoothEdrConfigBean(device);
        PIFactory piFactory = new BluetoothFactory();
        PrinterInterface printerInterface = piFactory.create();
        printerInterface.setConfigObject(bluetoothEdrConfigBean);
        rtPrinter.setPrinterInterface(printerInterface);

        rtPrinter.setConnectListener(new ConnectListener() {
            @Override
            public void onPrinterConnected(Object configObj) {
                doPrint();
            }

            @Override
            public void onPrinterDisconnect(Object configObj) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bPrint.setEnabled(true);
                    }
                });
                showToast(getString(R.string.device_diconnect));
            }

            @Override
            public void onPrinterWritecompletion(Object configObj) {
                try {
                    Thread.sleep(500);
                    showToast(getString(R.string.device_print_completion));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                rtPrinter.disConnect();
            }
        });
        try {
            rtPrinter.connect(bluetoothEdrConfigBean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void printerObserverCallback(final PrinterInterface printerInterface, final int state) {
        Log.i(TAG, "printerObserverCallback: " + printerInterface.getConfigObject().toString() + ", State: " + state);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pb_connect.setVisibility(View.GONE);
                switch (state) {
                    case CommonEnum.CONNECT_STATE_SUCCESS:
                        Log.i(TAG, getString(R.string.device_connected) + printerInterface.getConfigObject().toString() + ", timestamp: " + System.currentTimeMillis());
                        //showToast(getString(R.string.deivce_connected) + ", " + printerInterface.getConfigObject().toString());
                        curPrinterInterface = printerInterface;
                        rtPrinter.setPrinterInterface(printerInterface);
                        setPrintEnable(true);
                        RTPrintHelp.getInstance().setRtPrinter(rtPrinter);
                        break;
                    case CommonEnum.CONNECT_STATE_INTERRUPTED:
                        if (printerInterface != null && printerInterface.getConfigObject() != null) {
                            Log.i(TAG, getString(R.string.device_interrupted) + printerInterface.getConfigObject().toString() + ", timestamp: " + System.currentTimeMillis());
                            //showToast(getString(R.string.deivce_interrupted) + ", " + printerInterface.getConfigObject().toString());
                        } else {
                            showToast(getString(R.string.no_device));
                        }
                        Log.i(TAG, "Disconnected device:" + printerInterface.getConfigObject().toString() + ", timestamp: " + System.currentTimeMillis());
                        curPrinterInterface = null;
                        setPrintEnable(false);
                        RTPrintHelp.getInstance().setRtPrinter(null);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void printerReadMsgCallback(PrinterInterface printerInterface, final byte[] bytes) {
        Log.i(TAG, "printerReadMsgCallback: " + FuncUtils.ByteArrToHex(bytes));
    }

}