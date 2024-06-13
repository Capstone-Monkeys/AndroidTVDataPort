package com.example.androidtvdataport.manager;

import android.util.Log;

import com.example.androidtvdataport.message.SimpleMessageOuterClass;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Handler;

public class ClientManager {
    private static final String TAG = "ClientManager";
    private static final int SERVER_PORT = 9866;

    // Singleton instance
    private static ClientManager sInstance;

    private ServerSocket mServerSocket;
    private Socket socket;
    private ExecutorService mExecutorService;
    private OnMessageReceivedListener mMessageReceivedListener;

    private ClientManager() {
        mExecutorService = Executors.newCachedThreadPool();
    }

    public static ClientManager getInstance() {
        if (sInstance == null) {
            sInstance = new ClientManager();
        }
        return sInstance;
    }

    public void start() {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mServerSocket = new ServerSocket(SERVER_PORT);
                    Log.d(TAG, "Server started on port: " + SERVER_PORT);
                    while (!mServerSocket.isClosed()) {
                        socket = mServerSocket.accept();
                        Log.d(TAG, "Client connected: " + socket.getInetAddress());
                        try {
                            OutputStream outputStream = socket.getOutputStream();
                            InputStream inputStream = socket.getInputStream();
                            new Thread(new ReadTask(inputStream)).start();
                            SimpleMessageOuterClass.SimpleMessage message = SimpleMessageOuterClass.SimpleMessage.newBuilder().setMessage("Hello hello").build();
                            message.writeDelimitedTo(outputStream);
                            outputStream.flush();
                            Log.d("Sent", "Closed:" + mServerSocket.isClosed());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Log.d("Sent", "Closed:" + mServerSocket.isClosed());

//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                try {
//                                    Thread.sleep(5000);
////                                    sendMessage(SimpleMessageOuterClass.SimpleMessage.newBuilder().setMessage("Hello hello").build());
//                                    try (OutputStream outputStream = socket.getOutputStream()) {
//                                        SimpleMessageOuterClass.SimpleMessage message = SimpleMessageOuterClass.SimpleMessage.newBuilder().setMessage("Hello hello").build();
//                                        message.writeDelimitedTo(outputStream);
//                                        outputStream.flush();
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                    }
//                                    Log.d("Send", "Sent msg");
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }).start();
                    }

                } catch (IOException e) {
                    Log.e(TAG, "Error starting server", e);
                }
            }
        });
    }

    public void sendMessage(final SimpleMessageOuterClass.SimpleMessage message) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mServerSocket == null || mServerSocket.isClosed()) {
                        Log.e(TAG, "Server socket is not initialized or closed");
                        return;
                    }
//                    Socket socket = mServerSocket.accept();
//                    new Thread(new WriteTask(socket, message)).start();
                } catch (Exception e) {
                    Log.e(TAG, "Error sending message", e);
                }
            }
        });
    }

    class ReadTask implements Runnable {
        private InputStream inputStream;

        public ReadTask(InputStream i) {
            this.inputStream = i;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    SimpleMessageOuterClass.SimpleMessage message = SimpleMessageOuterClass.SimpleMessage.parseDelimitedFrom(inputStream);
                    if (message != null) {
                        System.out.println("Received: " + message.getMessage());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class WriteTask implements Runnable {
        private OutputStream outputStream;
        private SimpleMessageOuterClass.SimpleMessage message;

        public WriteTask(OutputStream outputStream, SimpleMessageOuterClass.SimpleMessage message) {
            this.outputStream = outputStream;
            this.message = message;
        }

        @Override
        public void run() {
            try {
                    message.writeDelimitedTo(outputStream);
                    outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setOnMessageReceivedListener(OnMessageReceivedListener listener) {
        mMessageReceivedListener = listener;
    }

    interface OnMessageReceivedListener {
        void onMessageReceived(SimpleMessageOuterClass.SimpleMessage message);
    }
}
