package com.xorz.didmyshitsell.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.xorz.didmyshitsell.R;
import com.xorz.didmyshitsell.ShitSellingApplication;

/**
 * Created by Adam on 10/31/2015.
 */
public class SettingsFragment extends Fragment {

    private TextView apiKey;
    private EditText apiKeyEditText;
    private View apiKeySaveCancelButtons;
    private Button apiKeyEditButton;
    private Button apiKeyEditSave;
    private Button apiKeyEditCancel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        apiKey = (TextView) root.findViewById(R.id.settings_current_api_key);
        apiKeyEditText = (EditText) root.findViewById(R.id.settings_edit_api_key);
        apiKeyEditButton = (Button) root.findViewById(R.id.settings_button_edit_api_key);
        apiKeySaveCancelButtons = root.findViewById(R.id.settings_buttons_save_cancel);
        apiKeyEditSave = (Button) root.findViewById(R.id.settings_button_edit_key_save);
        apiKeyEditCancel = (Button) root.findViewById(R.id.settings_button_edit_key_cancel);

        apiKeyEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setApiKeyEditable();
            }
        });

        apiKeyEditCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apiKeyEditText.setText("");
                setApiKeyUneditable();
            }
        });

        apiKeyEditSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = apiKeyEditText.getText().toString();
                if (!"".equals(key)) {
                    ((ShitSellingApplication) getActivity().getApplication()).setUserAPIKey(key);
                    apiKey.setText(key);
                } //TODO: show a warning when an empty or invalid key is input instead of silently discarding
                setApiKeyUneditable();
            }
        });

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void setApiKeyEditable() {
        apiKey.setVisibility(View.GONE);
        apiKeyEditText.setVisibility(View.VISIBLE);
        apiKeyEditButton.setVisibility(View.GONE);
        apiKeySaveCancelButtons.setVisibility(View.VISIBLE);
    }

    private void setApiKeyUneditable() {
        apiKey.setVisibility(View.VISIBLE);
        apiKeyEditText.setVisibility(View.GONE);
        apiKeyEditButton.setVisibility(View.VISIBLE);
        apiKeySaveCancelButtons.setVisibility(View.GONE);
    }
}
