package com.example.signmein;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class AboutFragment extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.about_fragment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        //Add app version info.
        TextView versionInfo = getActivity().findViewById(R.id.app_version_info);
        String versionString = "SignMeIn version " + BuildConfig.VERSION_NAME + ", " + BuildConfig.BUILD_TYPE + " build, Android sdk " + android.os.Build.VERSION.SDK_INT;
        versionInfo.setText(versionString);
    }
}