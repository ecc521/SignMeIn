package com.example.signmein;

import android.content.Context;
import android.os.Bundle;
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

public class HubFragment extends Fragment {
    private String TAG = "HubFragment";

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
        deviceConnector.startAdvertising("Test Hub");
    }

    public void onPause() {
        super.onPause();
        DeviceConnector deviceConnector = new DeviceConnector(getContext());
        deviceConnector.stopAdvertising();
    }

    public void userSignedIn(String name, String deviceID) {
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
        DocumentReference attendance = FirebaseFirestore.getInstance().collection("Teachers").document("Bob").collection("Classes").document("History").collection("Students").document(name);

        String date = new SimpleDateFormat("MM-dd-yyyy").format(new Date());

        attendance.update(date, "Present at " + time + ".")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setTitle("Student " + name + " not found in class. Please contact teacher.");
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