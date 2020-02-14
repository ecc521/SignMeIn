package com.example.signmein;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ClassViewHolder extends RecyclerView.ViewHolder {

    private TextView classView;

    public ClassViewHolder(@NonNull View itemView){
        super(itemView);
        classView = itemView.findViewById(R.id.class_ClassName);
    }

    public void bind(Class _class){
        classView.setText(_class.className);
    }
}
