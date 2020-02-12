package com.example.signmein;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;


public class ClassSelectFragment extends Fragment {


    public ClassSelectFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        CollectionReference classes = FirebaseFirestore.getInstance().collection("Teachers").document("Bob").collection("Classes");
        classes.get();
        return inflater.inflate(R.layout.fragment_class_select, container, false);
    }

}
