package com.example.androidtvdataport.listener;

import android.util.Log;

import com.example.androidtvdataport.message.BrowserData;

import java.io.IOException;
import java.io.InputStream;

public class BrowserDataListener implements Runnable {
    private String TAG = "BrowserDataListener";

    private boolean running = true;

    private InputStream mInputStream;

    private OnMessageReceivedListener mListener;

    public BrowserDataListener(InputStream inputStream, OnMessageReceivedListener listener) {
        mInputStream = inputStream;
        mListener = listener;
    }
    @Override
    public void run() {
        try {
            while (running) {
                BrowserData.WrapperMessage message = BrowserData.WrapperMessage.parseDelimitedFrom(mInputStream);
                if (message != null) {
                    Log.d(TAG, "Received message: " + message);
                    if (message.getType().equals(BrowserData.WrapperMessage.MessageType.REQUEST)){
                        mListener.onRequestReceived(message.getRequest());
                    } else {
                        mListener.onResponseReceived(message.getResponse());
                    }
                }

            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading message", e);
        }
    }
    public interface OnMessageReceivedListener {
        void onRequestReceived(BrowserData.FetchDataRequest request);
        void onResponseReceived(BrowserData.FetchDataResponse response);
        void onError(Error e);
    }
}
