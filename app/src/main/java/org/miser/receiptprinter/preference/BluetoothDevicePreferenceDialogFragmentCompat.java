package org.miser.receiptprinter.preference;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

import org.miser.receiptprinter.R;
import org.miser.receiptprinter.adapter.BluetoothDeviceAdapter;

import java.util.ArrayList;
import java.util.List;

public class BluetoothDevicePreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {
    private static final String TAG = "BluetoothDevice";
    private static final String SAVE_STATE_TEXT = "BluetoothDevicePreferenceDialogFragmentCompat.text";

    private final BluetoothAdapter mBluetoothAdapter;
    private final List<BluetoothDevice> mPairedDevices;
    private final List<BluetoothDevice> mFoundDevices;

    private BluetoothDeviceAdapter mPairedDeviceAdapter, mFoundDeviceAdapter;
    private ListView lvPairedDevices, lvFoundDevices;
    private TextView tvPairedDeviceEmpty, tvFoundDeviceEmpty, tvSearchDevice;
    private ProgressBar progressBar;
    private Button btn_hide;

    private BroadcastReceiver mBluetoothReceiver;

    private boolean mHidePaired = false;
    private boolean mDiscovered = false;
    private boolean mRegistered = false;

    private BluetoothDevicePreferenceDialogFragmentCompat() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mPairedDevices = new ArrayList<>(mBluetoothAdapter.getBondedDevices());
        mFoundDevices = new ArrayList<>();
    }

    public static BluetoothDevicePreferenceDialogFragmentCompat newInstance(String key) {
        final BluetoothDevicePreferenceDialogFragmentCompat fragment = new BluetoothDevicePreferenceDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    public boolean isHidePaired() {
        return mHidePaired;
    }

    public void setHidePaired(boolean hidePaired) {
        this.mHidePaired = hidePaired;
    }

    public boolean isDiscovered() {
        return mDiscovered;
    }

    public void setDiscovered(boolean discovered) {
        this.mDiscovered = discovered;
    }

    public boolean isRegistered() {
        return mRegistered;
    }

    public void setRegistered(boolean registered) {
        this.mRegistered = registered;
    }

    public void startDiscovery() {
        if (!isDiscovered()) {
            mFoundDevices.clear();
            mFoundDeviceAdapter.notifyDataSetChanged();
            mBluetoothAdapter.startDiscovery();
            setDiscovered(true);
        }
    }

    public void cancelDiscovery() {
        if (isDiscovered()) {
            mBluetoothAdapter.cancelDiscovery();
            setDiscovered(false);
        }
    }

    public void registerReceiver(Context context) {
        if (!isRegistered()) {
            mBluetoothReceiver = new BluetoothDeviceReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            context.registerReceiver(mBluetoothReceiver, filter);
            setRegistered(true);
        }
    }

    public void unregisterReceiver(Context context) {
        if (isRegistered()) {
            context.unregisterReceiver(mBluetoothReceiver);
            setRegistered(false);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {

        } else {

        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected View onCreateDialogView(Context context) {
        final int resId = R.layout.preference_dialog_bluetoothdevice;
        if (resId == 0) {
            return null;
        }
        return getLayoutInflater().inflate(resId, null);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        initView(view);
        setListener();
    }

    private BluetoothDevicePreference getBluetoothDeviceListPreference() {
        return (BluetoothDevicePreference) getPreference();
    }

    private void initView(View view) {
        lvPairedDevices = view.findViewById(R.id.lv_dialog_choose_bluetooth_device_paired_devices);
        lvFoundDevices = view.findViewById(R.id.lv_dialog_choose_bluetooth_device_found_devices);
        tvPairedDeviceEmpty = view.findViewById(R.id.tv_dialog_choose_bluetooth_device_paired_devices_empty);
        tvFoundDeviceEmpty = view.findViewById(R.id.tv_dialog_choose_bluetooth_device_found_devices_empty);
        tvSearchDevice = view.findViewById(R.id.tv_dialog_choose_bluetooth_device_search_device);
        progressBar = view.findViewById(R.id.pb_dialog_choose_bluetooth_device_progress_bar);
        btn_hide = view.findViewById(R.id.btn_hide);

        mPairedDeviceAdapter = new BluetoothDeviceAdapter(getActivity(), mPairedDevices);
        lvPairedDevices.setAdapter(mPairedDeviceAdapter);
        if (mPairedDevices.size() == 0) {
            tvPairedDeviceEmpty.setVisibility(View.VISIBLE);
        }
        mFoundDeviceAdapter = new BluetoothDeviceAdapter(getActivity(), mFoundDevices);
        lvFoundDevices.setAdapter(mFoundDeviceAdapter);
    }

    private void setListener() {
        tvSearchDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvSearchDevice.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                tvFoundDeviceEmpty.setVisibility(View.GONE);
                mFoundDevices.clear();
                mFoundDeviceAdapter.notifyDataSetChanged();
                registerReceiver(getActivity());
                startDiscovery();
            }
        });
        lvPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectBluetoothDevice((BluetoothDevice) parent.getAdapter().getItem(position));
            }
        });
        lvFoundDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectBluetoothDevice((BluetoothDevice) parent.getAdapter().getItem(position));
            }
        });
        btn_hide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isHidePaired()) {
                    lvPairedDevices.setVisibility(View.VISIBLE);
                    btn_hide.setText(R.string.btn_hide);
                    setHidePaired(false);
                } else {
                    lvPairedDevices.setVisibility(View.GONE);
                    btn_hide.setText(R.string.btn_show);
                    setHidePaired(true);
                }
            }
        });
    }

    private void selectBluetoothDevice(BluetoothDevice device) {
        getBluetoothDeviceListPreference().setBluetoothDevice(device);
        getDialog().dismiss();
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        cancelDiscovery();
        unregisterReceiver(getActivity());
    }

    private class BluetoothDeviceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int devType = device.getBluetoothClass().getMajorDeviceClass();
                if (devType != BluetoothClass.Device.Major.IMAGING) {
                    return;
                }
                if (!mFoundDevices.contains(device)) {
                    mFoundDevices.add(device);
                    mFoundDeviceAdapter.notifyDataSetChanged();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                cancelDiscovery();
                unregisterReceiver(context);
                tvSearchDevice.setEnabled(true);
                progressBar.setVisibility(View.GONE);
                if (mFoundDevices.size() == 0) {
                    tvFoundDeviceEmpty.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
