package com.example.signmein;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.signmein.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import static android.content.Context.MODE_PRIVATE;

public class ClientFragment extends Fragment {

    private String TAG = "Client Fragment";
    private SharedPreferences sharedPrefs;
    private String userNameKey = "Client Name";

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
                deviceConnector.connectToEndpoint(sharedPrefs.getString(userNameKey, "Click to Choose Name"), hubId, new SignInCompletedCallback() {
                    @Override
                    public void SignInCompleted(String endpointId) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setTitle("You have been signed in to " + selectedHub.getText());
                        builder.show();
                    }
                });
            }
        });

        sharedPrefs = getContext().getSharedPreferences("settings", MODE_PRIVATE);

        TextView userName = inputView.findViewById(R.id.clientName);
        userName.setPaintFlags(userName.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        userName.setText(sharedPrefs.getString(userNameKey, "Click to Choose Name"));


        userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText taskEditText = new EditText(getContext());
                AlertDialog dialog = new AlertDialog.Builder(getContext())
                        .setTitle("Enter New Username: ")
                        .setView(taskEditText)
                        .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newName = taskEditText.getText().toString();
                                sharedPrefs.edit().putString(userNameKey, newName).commit();
                                userName.setText(newName);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();
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
        String[] emptyArr = {};
        createDeviceOptionsList(emptyArr, emptyArr);
        DeviceConnector deviceConnector = new DeviceConnector(getContext());

        AvailableDevicesChangedCallback callback = new AvailableDevicesChangedCallback() {
            @Override
            public void AvailableDevicesChanged(String[] endpointIds, String[] hubNames) {
                Log.i(TAG, "Callback Called. ");
                createDeviceOptionsList(endpointIds, hubNames);
            }
        };
        deviceConnector.startDiscovery(sharedPrefs.getString(userNameKey, "Click to Choose Name"), callback);
    }

    public void onPause() {
        super.onPause();
        DeviceConnector deviceConnector = new DeviceConnector(getContext());
        deviceConnector.stopDiscovery();
    }
}