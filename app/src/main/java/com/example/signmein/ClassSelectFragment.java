package com.example.signmein;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;

import static androidx.paging.PagedList.Config.MAX_SIZE_UNBOUNDED;


public class ClassSelectFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private FirestorePagingAdapter<Class, ClassViewHolder> mAdapter;
    //Will need to change document "Bob" based on the teacher logged in.
    private CollectionReference classes = FirebaseFirestore.getInstance().collection("Teachers").document("Bob").collection("Classes");
    private Query query = classes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inputView = inflater.inflate(R.layout.fragment_class_select, container, false);

        mRecyclerView = inputView.findViewById(R.id.recyclerView);

        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext().getApplicationContext()));

        setupAdapter();

        return inputView;
    }

    private void setupAdapter(){
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(2)
                .setPageSize(10)
                .setMaxSize(MAX_SIZE_UNBOUNDED)
                .build();

        FirestorePagingOptions options = new FirestorePagingOptions.Builder<Class>()
                .setLifecycleOwner(this)
                .setQuery(query, config, Class.class)
                .build();

        mAdapter = new FirestorePagingAdapter<Class, ClassViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ClassViewHolder viewHolder, int i, @NonNull Class _class) {
                viewHolder.bind(_class);
            }

            @NonNull
            @Override
            public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = getLayoutInflater().inflate(R.layout.item_class, parent,false);
                return new ClassViewHolder(view);
            }
        };

        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    public void onStop(){
        super.onStop();
        mAdapter.stopListening();
    }

}
