package com.eevee.monstertalk;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;

import com.eevee.monstertalk.monstertalk.R;

public class NewChatDialog extends DialogFragment {
    public interface OnSuccessListener {
        void onSuccess(DialogFragment dialog, Bundle bundle);
    }
    public interface OnDismissListener {
        void onDismiss(DialogFragment dialog);
    }

    private OnSuccessListener mSuccessListener;
    private OnDismissListener mDismissListener;

    private EditText mDestEditText;
    private Switch mCryptoSwitch;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_new_chat, null);

        mDestEditText = (EditText) view.findViewById(R.id.destEditText);
        mCryptoSwitch = (Switch) view.findViewById(R.id.cryptoSwitch);

        builder.setView(view)
            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (mSuccessListener != null) {
                        Bundle data = new Bundle();
                        data.putString("to", mDestEditText.getText().toString());
                        data.putBoolean("crypto", mCryptoSwitch.isChecked());

                        mSuccessListener.onSuccess(NewChatDialog.this, data);
                    }
                }
            })
            .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (mDismissListener != null) {
                        mDismissListener.onDismiss(NewChatDialog.this);
                    }
                }
            });

        return builder.create();
    }

    public void setOnSuccessListener(OnSuccessListener listener) {
        mSuccessListener = listener;
    }

    public void setOnDismissListener(OnDismissListener listener) {
        mDismissListener = listener;
    }
}
