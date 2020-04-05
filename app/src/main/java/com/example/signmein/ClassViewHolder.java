package com.example.signmein;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ClassViewHolder extends RecyclerView.ViewHolder {

    private TextView classView;
    private View.OnClickListener mOnItemClickListener;

    public ClassViewHolder(@NonNull View itemView){
        super(itemView);
        classView = itemView.findViewById(R.id.class_ClassName);
        itemView.setTag(this);
        itemView.setOnClickListener(mOnItemClickListener);
    }

    public void bind(Class nameClass){
        classView.setText(nameClass.className);
    }
}
