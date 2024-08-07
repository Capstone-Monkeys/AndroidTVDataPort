package com.example.androidtvdataport;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.example.androidtvdataport.manager.ClientManager;

public class MainActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_browse_fragment, new BlankFragment())
                    .commitNow();
        }

        // Mo server de lang nghe ket noi tu client
        ClientManager manager = ClientManager.getInstance();
        manager.setContext(this);
        manager.start();
    }
}