package com.example.signmein;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class HubFragment extends Fragment {
    private String TAG = "HubFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.hub_fragment, container, false);
    }

    public void onResume() {
        super.onResume();
        DeviceConnector deviceConnector = new DeviceConnector(getContext());
        deviceConnector.startAdvertising("Test Hub");
    }

    public void onPause() {
        super.onPause();
        DeviceConnector deviceConnector = new DeviceConnector(getContext());
        deviceConnector.stopAdvertising();
    }

    public void signIn(View view) {
        EditText studentName = (EditText) findViewbyId (R.id.studentName);
        
        if (studentName.getText().toString().isEmpty()){
            final AlertDialog.Builder builder = new AlertDialog.Builder(null);
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setTitle("Please enter a name.");
        } else {

        }
    }
}