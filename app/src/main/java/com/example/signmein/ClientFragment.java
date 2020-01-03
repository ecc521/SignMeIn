package com.example.signmein;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.signmein.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class ClientFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Draw client_fragment XML.
        return inflater.inflate(R.layout.client_fragment, container, false);
    }

    public void onStart() {
        super.onStart();
        String[] strArray1 = {"Device A","Device B","Device C"};
        String[] strArray2 = {"Device 1","Device 2","Device 3"};
        createDeviceOptionsList(strArray1);
        createDeviceOptionsList(strArray2);
    }

    private void createDeviceOptionsList(String[] items) {
        LinearLayout ll = getActivity().findViewById(R.id.deviceSelector);
        ll.removeAllViews();
        final RadioButton[] rb = new RadioButton[5];
        RadioGroup rg = new RadioGroup(getActivity());
        rg.setOrientation(RadioGroup.VERTICAL);
        for(int i=0; i<items.length; i++){
            rb[i]  = new RadioButton(getActivity());
            rb[i].setText(items[i]);
            rb[i].setId(i + 100);
            rg.addView(rb[i]);
        }
        ll.addView(rg);
    }
}