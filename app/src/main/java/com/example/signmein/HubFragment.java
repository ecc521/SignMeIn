package com.example.signmein;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
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
        deviceConnector.startAdvertising();
    }

    public void onPause() {
        super.onPause();
        DeviceConnector deviceConnector = new DeviceConnector(getContext());
        deviceConnector.stopAdvertising();
    }
}