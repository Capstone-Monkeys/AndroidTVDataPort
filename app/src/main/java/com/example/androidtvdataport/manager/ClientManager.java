package com.example.androidtvdataport.manager;

import android.util.Log;

import com.example.androidtvdataport.message.SimpleMessageOuterClass;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
                        handleClientConnection(socket);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error starting server", e);
                }
            }
        });
    }

    private void handleClientConnection(Socket socket) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    OutputStream outputStream = socket.getOutputStream();
                    InputStream inputStream = socket.getInputStream();

                    // Start threads to read from and write to the socket
                    new Thread(new ReadTask(inputStream)).start();

                    //send message to client every 5 seconds
                    while (true) {
                        Thread.sleep(5000);
                        sendMessage(outputStream, SimpleMessageOuterClass.SimpleMessage.newBuilder().setMessage("Hello hello").build());
                    }

                } catch (IOException e) {
                    Log.e(TAG, "Error handling client connection", e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void sendMessage(OutputStream outputStream, final SimpleMessageOuterClass.SimpleMessage message) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    message.writeDelimitedTo(outputStream);
                    outputStream.flush();
                    Log.d(TAG, "Message sent: " + message.getMessage());
                } catch (IOException e) {
                    Log.e(TAG, "Error sending message", e);
                }
            }
        });
    }

    class ReadTask implements Runnable {
        private InputStream inputStream;

        public ReadTask(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    SimpleMessageOuterClass.SimpleMessage message = SimpleMessageOuterClass.SimpleMessage.parseDelimitedFrom(inputStream);
                    if (message != null) {
                        Log.d(TAG, "Received: " + message.getMessage());
                        if (mMessageReceivedListener != null) {
                            mMessageReceivedListener.onMessageReceived(message);
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error reading message", e);
            }
        }
    }

    public void setOnMessageReceivedListener(OnMessageReceivedListener listener) {
        mMessageReceivedListener = listener;
    }

    public interface OnMessageReceivedListener {
        void onMessageReceived(SimpleMessageOuterClass.SimpleMessage message);
    }
}
