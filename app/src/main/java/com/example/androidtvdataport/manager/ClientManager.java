package com.example.androidtvdataport.manager;

import android.util.Log;

import com.example.androidtvdataport.listener.BrowserDataListener;
import com.example.androidtvdataport.message.BrowserData;
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
    private InputStream mInputStream;
    private OutputStream mOutputStream;

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
                    mOutputStream = socket.getOutputStream();
                    mInputStream = socket.getInputStream();
                    BrowserDataListener listener = new BrowserDataListener(mInputStream, new BrowserDataListener.OnMessageReceivedListener() {
                        @Override
                        public void onRequestReceived(BrowserData.FetchDataRequest request) {
                            Log.d(TAG, "Received request: " + request);
                            processRequest(request);
                        }

                        @Override
                        public void onResponseReceived(BrowserData.FetchDataResponse response) {
                            Log.d(TAG, "Received response: " + response);
                        }

                        @Override
                        public void onError(Error e) {
                            Log.e(TAG, "Error: " + e);
                        }
                    });
                    Thread listenerThread = new Thread(listener);
                    listenerThread.start();

                } catch (Exception e) {
                    Log.e(TAG, "Error handling client connection", e);
                }
            }
        });
    }

    public void sendMessage(OutputStream outputStream, final BrowserData.WrapperMessage message) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    message.writeDelimitedTo(outputStream);
                    outputStream.flush();
                    Log.d(TAG, "Message sent: " + message);
                } catch (IOException e) {
                    Log.e(TAG, "Error sending message", e);
                }
            }
        });
    }

    private void processRequest(BrowserData.FetchDataRequest request) {
        BrowserData.WrapperMessage.Builder responseBuilder = BrowserData.WrapperMessage.newBuilder().setType(BrowserData.WrapperMessage.MessageType.RESPONSE);
        BrowserData.FetchDataResponse.Builder response = BrowserData.FetchDataResponse.newBuilder();
        // Process request
        if (request.getFetchBookmarks()) {
            String dummyBookmarkJson = "[\n" +
                    "  {\n" +
                    "    \"title\": \"Bookmark Title Kathrine\",\n" +
                    "    \"url\": \"https://exampleFarley.com\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"title\": \"Bookmark Title Imogene\",\n" +
                    "    \"url\": \"https://exampleBowman.com\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"title\": \"Bookmark Title Krista\",\n" +
                    "    \"url\": \"https://exampleNorman.com\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"title\": \"Bookmark Title Albert\",\n" +
                    "    \"url\": \"https://exampleLewis.com\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"title\": \"Bookmark Title Fulton\",\n" +
                    "    \"url\": \"https://exampleKinney.com\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"title\": \"Bookmark Title Lambert\",\n" +
                    "    \"url\": \"https://exampleBeck.com\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"title\": \"Bookmark Title Pierce\",\n" +
                    "    \"url\": \"https://exampleAllen.com\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"title\": \"Bookmark Title Buck\",\n" +
                    "    \"url\": \"https://exampleEverett.com\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"title\": \"Bookmark Title Josefina\",\n" +
                    "    \"url\": \"https://exampleBryant.com\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"title\": \"Bookmark Title Wilda\",\n" +
                    "    \"url\": \"https://exampleBarlow.com\"\n" +
                    "  }\n" +
                    "]";
            response.setBookmarks(dummyBookmarkJson);
        }
        if (request.getFetchHistory()) {
            String dummyHistoryJson = "[\n" +
                    "  {\n" +
                    "    \"title\": \"History Title Briana\",\n" +
                    "    \"url\": \"https://exampleGreen.com\",\n" +
                    "    \"timestamp\": 1621502400000\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"title\": \"History Title Erickson\",\n" +
                    "    \"url\": \"https://exampleClements.com\",\n" +
                    "    \"timestamp\": 1621502400000\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"title\": \"History Title Darlene\",\n" +
                    "    \"url\": \"https://exampleMathis.com\",\n" +
                    "    \"timestamp\": 1621502400000\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"title\": \"History Title Myrna\",\n" +
                    "    \"url\": \"https://exampleCaldwell.com\",\n" +
                    "    \"timestamp\": 1621502400000\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"title\": \"History Title Norman\",\n" +
                    "    \"url\": \"https://exampleMcknight.com\",\n" +
                    "    \"timestamp\": 1621502400000\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"title\": \"History Title Fannie\",\n" +
                    "    \"url\": \"https://exampleCraft.com\",\n" +
                    "    \"timestamp\": 1621502400000\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"title\": \"History Title Weaver\",\n" +
                    "    \"url\": \"https://exampleCarlson.com\",\n" +
                    "    \"timestamp\": 1621502400000\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"title\": \"History Title Rosie\",\n" +
                    "    \"url\": \"https://exampleGraham.com\",\n" +
                    "    \"timestamp\": 1621502400000\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"title\": \"History Title Gallegos\",\n" +
                    "    \"url\": \"https://exampleWood.com\",\n" +
                    "    \"timestamp\": 1621502400000\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"title\": \"History Title Hannah\",\n" +
                    "    \"url\": \"https://exampleNicholson.com\",\n" +
                    "    \"timestamp\": 1621502400000\n" +
                    "  }\n" +
                    "]";
            response.setHistory(dummyHistoryJson);
        }
        responseBuilder.setResponse(response);
        sendMessage(mOutputStream, responseBuilder.build());
    }
}
