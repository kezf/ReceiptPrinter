package org.miser.receiptprinter.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import org.miser.receiptprinter.R;
import org.miser.receiptprinter.preference.BluetoothDevicePreference;
import org.miser.receiptprinter.preference.BluetoothDevicePreferenceDialogFragmentCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item != null && item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
        private final int REQUEST_ENABLE_BT = 101;
        private SwitchPreferenceCompat mBluetoothPreference;
        private BluetoothAdapter mBluetoothAdapter;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            mBluetoothPreference = findPreference("bluetooth");
            if (mBluetoothPreference != null) {
                if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
                    mBluetoothPreference.setChecked(false);
                }
                mBluetoothPreference.setOnPreferenceClickListener(this);
            }
        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            if (preference instanceof BluetoothDevicePreference) {
                BluetoothDevicePreferenceDialogFragmentCompat f = BluetoothDevicePreferenceDialogFragmentCompat.newInstance(preference.getKey());
                f.setTargetFragment(this, 0);
                f.show(getParentFragmentManager(), null);
                return;
            }
            super.onDisplayPreferenceDialog(preference);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (mBluetoothPreference != null && mBluetoothPreference.equals(preference)) {
                if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled() && mBluetoothPreference.isChecked()) {
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
            }

            return true;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQUEST_ENABLE_BT && resultCode != RESULT_OK) {
                mBluetoothPreference.setChecked(false);
            }
        }
    }
}