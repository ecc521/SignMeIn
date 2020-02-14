package com.example.signmein;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Class {
    public String className;

    public Class() {
    }

    public Class(String className){
        this.className = className;
    }
}
