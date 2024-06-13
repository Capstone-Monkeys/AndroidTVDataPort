package com.example.androidtvdataport.manager;

import android.util.Log;

import com.example.androidtvdataport.message.SimpleMessageOuterClass;
import com.example.androidtvdataport.message.SimpleMessageOuterClass.SimpleMessage;

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
                        Socket socket = mServerSocket.accept();
                        Log.d(TAG, "Client connected: " + socket.getInetAddress());
                        new Thread(new ReadTask(socket)).start();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error starting server", e);
                }
            }
        });
    }

    public void sendMessage(final SimpleMessage message) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mServerSocket == null || mServerSocket.isClosed()) {
                        Log.e(TAG, "Server socket is not initialized or closed");
                        return;
                    }
                    Socket socket = mServerSocket.accept();
                    new Thread(new WriteTask(socket, message)).start();
                } catch (IOException e) {
                    Log.e(TAG, "Error sending message", e);
                }
            }
        });
    }

    class ReadTask implements Runnable {
        private Socket socket;

        public ReadTask(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (InputStream inputStream = socket.getInputStream()) {
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
        private Socket socket;
        private SimpleMessage message;

        public WriteTask(Socket socket, SimpleMessage message) {
            this.socket = socket;
            this.message = message;
        }

        @Override
        public void run() {
            try (OutputStream outputStream = socket.getOutputStream()) {
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
        void onMessageReceived(SimpleMessage message);
    }
}
