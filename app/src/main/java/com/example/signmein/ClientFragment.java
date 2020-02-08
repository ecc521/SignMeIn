package com.example.signmein;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.signmein.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class ClientFragment extends Fragment {

    private String TAG = "Client Fragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inputView = inflater.inflate(R.layout.client_fragment, container, false);

        Button signInButton = inputView.findViewById(R.id.signInButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RadioGroup rg = getActivity().findViewById(R.id.hubSelector);
                RadioButton selectedHub = getActivity().findViewById(rg.getCheckedRadioButtonId());
                String hubId = selectedHub.getTag().toString();
                Log.i(TAG, hubId);

                DeviceConnector deviceConnector = new DeviceConnector(getContext());
                deviceConnector.connectToEndpoint("Test User", hubId);
            }
        });

        return inputView;
    }

    public void onStart() {
        super.onStart();
    }

    private void createDeviceOptionsList(String[] endpointIds, String[] hubNames) {
        TextView numberOfHubs = getActivity().findViewById(R.id.numberOfHubs);
        numberOfHubs.setText(endpointIds.length + " Hubs Available:");

        LinearLayout ll = getActivity().findViewById(R.id.deviceSelector);
        ll.removeAllViews();
        final RadioButton[] rb = new RadioButton[5];
        RadioGroup rg = new RadioGroup(getActivity());
        rg.setId(R.id.hubSelector);
        rg.setOrientation(RadioGroup.VERTICAL);
        for(int i=0; i<endpointIds.length; i++){
            String endpointId = endpointIds[i];
            rb[i]  = new RadioButton(getActivity());
            rb[i].setText(hubNames[i] + " (" + endpointId + ")");
            rb[i].setTag(endpointId);
            rb[i].setId(rb[i].generateViewId());
            rg.addView(rb[i]);
        }
        ll.addView(rg);
    }

    public void onResume() {
        super.onResume();
        DeviceConnector deviceConnector = new DeviceConnector(getContext());

        AvailableDevicesChangedCallback callback = new AvailableDevicesChangedCallback() {
            @Override
            public void AvailableDevicesChanged(String[] endpointIds, String[] hubNames) {
                Log.i(TAG, "Callback Called. ");
                createDeviceOptionsList(endpointIds, hubNames);
            }
        };
        deviceConnector.startDiscovery("Test Client", callback);
    }

    public void onPause() {
        super.onPause();
        DeviceConnector deviceConnector = new DeviceConnector(getContext());
        deviceConnector.stopDiscovery();
    }
}