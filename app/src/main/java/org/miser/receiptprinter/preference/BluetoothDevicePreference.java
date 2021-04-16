package org.miser.receiptprinter.preference;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.DialogPreference;
import androidx.preference.EditTextPreference;

import org.miser.receiptprinter.R;

public class BluetoothDevicePreference extends DialogPreference {
    private static final String TAG = "BluetoothDevice";

    private final BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;

    @SuppressLint("RestrictedApi")
    public BluetoothDevicePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.EditTextPreference, defStyleAttr, defStyleRes);

        if (TypedArrayUtils.getBoolean(a, R.styleable.BluetoothDevicePreference_useSimpleSummaryProvider,
                R.styleable.BluetoothDevicePreference_useSimpleSummaryProvider, false)) {
            setSummaryProvider(BluetoothDevicePreference.SimpleSummaryProvider.getInstance());
        }

        a.recycle();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public BluetoothDevicePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @SuppressLint("RestrictedApi")
    public BluetoothDevicePreference(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, R.attr.dialogPreferenceStyle,
                android.R.attr.dialogPreferenceStyle));
    }

    public BluetoothDevicePreference(Context context) {
        this(context, null);
    }

    public BluetoothDevice getBluetoothDevice() {
        return mBluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice device) {
        if (device == null)
            return;

        final boolean wasBlocking = shouldDisableDependents();

        mBluetoothDevice = device;

        persistString(device.getAddress());

        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }

        notifyChanged();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        String address = getPersistedString((String) defaultValue);
        try {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            setBluetoothDevice((device));
        } catch (Exception e) {
            Log.e(TAG, "Don't set initial value, bluetooth device [" + address + "] is not exist.", e);
        }
    }

    @Override
    public boolean shouldDisableDependents() {
        return getBluetoothDevice() == null || super.shouldDisableDependents();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.value = getBluetoothDevice().getAddress();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());

        String address = myState.value;
        try {
            setBluetoothDevice(mBluetoothAdapter.getRemoteDevice(address));
        } catch (Exception e) {
            Log.e(TAG, "Don't restore instance state, bluetooth device [" + address + "] is not exist.", e);
        }
    }

    private static class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };

        String value;

        SavedState(Parcel source) {
            super(source);
            value = source.readString();
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(value);
        }
    }

    /**
     * A simple {@link androidx.preference.Preference.SummaryProvider} implementation for an
     * {@link EditTextPreference}. If no value has been set, the summary displayed will be 'Not
     * set', otherwise the summary displayed will be the value set for this preference.
     */
    public static final class SimpleSummaryProvider implements SummaryProvider<BluetoothDevicePreference> {

        private static SimpleSummaryProvider sSimpleSummaryProvider;

        private SimpleSummaryProvider() {
        }

        /**
         * Retrieve a singleton instance of this simple
         * {@link androidx.preference.Preference.SummaryProvider} implementation.
         *
         * @return a singleton instance of this simple
         * {@link androidx.preference.Preference.SummaryProvider} implementation
         */
        public static SimpleSummaryProvider getInstance() {
            if (sSimpleSummaryProvider == null) {
                sSimpleSummaryProvider = new SimpleSummaryProvider();
            }
            return sSimpleSummaryProvider;
        }

        @Override
        public CharSequence provideSummary(BluetoothDevicePreference preference) {
            final BluetoothDevice device = preference.getBluetoothDevice();
            if (device == null) {
                return (preference.getContext().getString(R.string.no_device));
            } else {
                if (TextUtils.isEmpty(device.getName())) {
                    return "[" + device.getAddress() + "]";
                } else {
                    return device.getName() + " [" + device.getAddress() + "]";
                }
            }
        }
    }

}
