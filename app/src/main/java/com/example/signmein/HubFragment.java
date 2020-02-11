package com.example.signmein;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import static android.content.Context.MODE_PRIVATE;

public class HubFragment extends Fragment {
    private String TAG = "HubFragment";
    SharedPreferences sharedPrefs;
    private String hubNameKey = "hubname";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inputView = inflater.inflate(R.layout.hub_fragment, container, false);

        //If the user presses the sign me in button, sign them in.
        Button localSignInButton = inputView.findViewById(R.id.localSignInButton);
        localSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                localSignIn();
                hideKeyboard();
            }
        });


        EditText nameInputField = inputView.findViewById(R.id.studentName);

        //When the user presses enter after inputting their name, sign them in.
        nameInputField.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                //If the keyevent is a key-down event on the "enter" button
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    localSignIn();
                    hideKeyboard();
                    return true;
                }
                return false;
            }
        });

        sharedPrefs = getContext().getSharedPreferences("settings", MODE_PRIVATE);

        TextView hubName = inputView.findViewById(R.id.hubName);
        hubName.setText(sharedPrefs.getString(hubNameKey, "Click to Name Hub"));
        hubName.setPaintFlags(hubName.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);


        hubName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText taskEditText = new EditText(getContext());
                AlertDialog dialog = new AlertDialog.Builder(getContext())
                        .setTitle("Enter New Hub Name: ")
                        .setView(taskEditText)
                        .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newName = taskEditText.getText().toString();
                                sharedPrefs.edit().putString(hubNameKey, newName).commit();
                                hubName.setText(newName);
                                onPause();
                                onResume();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();
            }
        });

        return inputView;
    }


    public void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow((null == getActivity().getCurrentFocus())
                ? null : getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void onResume() {
        super.onResume();
        DeviceConnector deviceConnector = new DeviceConnector(getContext());
        deviceConnector.startAdvertising(sharedPrefs.getString(hubNameKey, "Click to Name Hub"), new IncomingConnectionCallback() {
            @Override
            public void IncomingConnection(String endpointId, String endpointName) {
                userSignedIn(endpointName, endpointId); //TODO: Transfer android ID so that we have an installation ID instead of something that changes every time the app opens.
            }
        });
    }

    public void onPause() {
        super.onPause();
        DeviceConnector deviceConnector = new DeviceConnector(getContext());
        deviceConnector.stopAdvertising();
    }

    public void userSignedIn(String signInName, String deviceID) {
        String name = signInName.trim();

        TextView signInHistory = getActivity().findViewById(R.id.history);
        String text = signInHistory.getText().toString();

        if (!text.contains("\n")) {
            //Clear the textbox. There is currently nothing in it.
            text = "";
        }

        DateFormat dateFormat = DateFormat.getTimeInstance();
        String time = dateFormat.format(new Date());

        text = name + " signed in at " + time + "\n" + text;

        signInHistory.setText(text);



        //Look for Document under teacher's name from login for the name entered in
        //Documents "Bob" and "History" will be changed to variables whenever the teacher login is implemented
        String databaseName = name.toLowerCase();

        DocumentReference attendance = FirebaseFirestore.getInstance().collection("Teachers").document("Bob").collection("Classes").document("History").collection("Students").document(databaseName);

        String date = new SimpleDateFormat("MM-dd-yyyy").format(new Date());

        Log.i(TAG, "Updating Attendance");
        attendance.update(date, "Present at " + time + ".")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Signed in " + databaseName);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "Failed to Sign In " + name);
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setTitle("Student " + databaseName + " not found in class. Please contact teacher.");
                        builder.show();
                    }
                });
    }

    public void localSignIn() {
        EditText studentName = getActivity().findViewById(R.id.studentName);

        String name = studentName.getText().toString();
        if (name.isEmpty()){
            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setTitle("Please enter a name.");
            builder.show();
        }
        else {
            userSignedIn(name, "local");
            studentName.setText("");
        }
    }
}