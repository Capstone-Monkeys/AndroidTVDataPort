package com.example.androidtvdataport.manager;

import android.util.Log;

import com.example.androidtvdataport.message.SimpleMessageOuterClass.SimpleMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
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
                        mExecutorService.execute(new ReadTask(socket));
                        mExecutorService.execute(new WriteTask(socket, SimpleMessage.newBuilder()
                                .setMessage("Hello from server")
                                .build()));
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
        private Socket mSocket;

        public ReadTask(Socket socket) {
            mSocket = socket;
        }

        @Override
        public void run() {
            try (InputStream is = mSocket.getInputStream()) {
                while (!mSocket.isClosed()) {
                    SimpleMessage message = SimpleMessage.parseDelimitedFrom(is);
                    if (message != null) {
                        if (mMessageReceivedListener != null) {
                            mMessageReceivedListener.onMessageReceived(message);
                        }
                        Log.d(TAG, "Received message: " + message);
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error reading from client", e);
            } finally {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing socket", e);
                }
            }
        }
    }

    class WriteTask implements Runnable {
        private Socket mSocket;
        private SimpleMessage mMessage;

        public WriteTask(Socket socket, SimpleMessage message) {
            mSocket = socket;
            mMessage = message;
        }

        @Override
        public void run() {
            try (OutputStream os = mSocket.getOutputStream()) {
                mMessage.writeDelimitedTo(os);
                os.flush();
            } catch (IOException e) {
                Log.e(TAG, "Error writing to client", e);
            } finally {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing socket", e);
                }
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
