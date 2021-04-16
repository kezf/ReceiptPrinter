package org.miser.receiptprinter.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.miser.receiptprinter.R;
import org.miser.receiptprinter.model.Receipt;

public class FirstFragment extends Fragment {
    private Receipt mReceipt;
    private EditText mMerchantNameField;
    private EditText mAmountField;
    private EditText mDateField;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_first, container, false);

        mMerchantNameField = (EditText) v.findViewById(R.id.merchant_name);
        mMerchantNameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mReceipt.setMerchantName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mReceipt = new Receipt();
        mAmountField = (EditText) v.findViewById(R.id.amount);
        mAmountField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mReceipt.setAmount(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mDateField = (EditText) v.findViewById(R.id.date);
        mDateField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mReceipt.setDate(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return v;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public Receipt getReceipt() {
        return mReceipt;
    }

    public void setReceipt(Receipt receipt) {
        mReceipt = receipt;
    }
}