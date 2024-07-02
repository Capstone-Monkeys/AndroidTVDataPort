package com.example.androidtvdataport.manager;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.androidtvdataport.listener.BrowserDataListener;
import com.example.androidtvdataport.message.BrowserData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientManager {
    private static final String TAG = "ClientManager";
    private static final int SERVER_PORT = 9867;

    // Singleton instance
    private static ClientManager sInstance;

    private ServerSocket mServerSocket;
    private final ExecutorService mExecutorService;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private Context mContext;

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
        mExecutorService.execute(() -> {
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
        });
    }

    private void handleClientConnection(Socket socket) {
        mExecutorService.execute(() -> {
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
                    public void onError(Exception e) {
                        Log.e(TAG, "Error: " + e);
                    }
                });
                Thread listenerThread = new Thread(listener);
                listenerThread.start();

            } catch (Exception e) {
                Log.e(TAG, "Error handling client connection", e);
            }
        });
    }

    public void sendMessage(OutputStream outputStream, final BrowserData.WrapperMessage message) {
        mExecutorService.execute(() -> {
            try {
                message.writeDelimitedTo(outputStream);
                outputStream.flush();
                Log.d(TAG, "Message sent: " + message);
            } catch (IOException e) {
                Log.e(TAG, "Error sending message", e);
            }
        });
    }

    private void processRequest(BrowserData.FetchDataRequest request) {
        BrowserData.WrapperMessage.Builder responseBuilder = BrowserData.WrapperMessage.newBuilder().setType(BrowserData.WrapperMessage.MessageType.RESPONSE);
        BrowserData.FetchDataResponse.Builder response = BrowserData.FetchDataResponse.newBuilder();
        // Process request
        if (request.getFetchBookmarks()) {
            String dummyBookmarkJson = createBookmarkDummy();
            response.setBookmarks(dummyBookmarkJson);
        }
        if (request.getFetchHistory()) {
            String dummyHistoryJson = createHistoryDummy();
            response.setHistory(dummyHistoryJson);
        }
        Log.d(TAG, "Action code: " + request.hasActionCodeInject());
        if (request.hasActionCodeInject()) {
            // TODO: Handle action code
            BrowserData.ActionCode actionCode = request.getActionCodeInject().getActionCode();
            Handler handler = new Handler(mContext.getMainLooper());
            handler.post(() -> {
                Toast.makeText(mContext, "Action code: " + actionCode, Toast.LENGTH_SHORT).show();
            });
            response.setActionResponse(BrowserData.ActionResponse.newBuilder().setStatusCode(200).setMessage("OPENED CINEMA MODE").build());
        }
        responseBuilder.setResponse(response);
        sendMessage(mOutputStream, responseBuilder.build());
    }

    public void setContext(Context context) {
        mContext = context;
    }
    private String createHistoryDummy() {

        return "[\n" +
                "  {\n" +
                "    \"imageUrl\": \"https://cdn.coccoc.com/browser_images/thumbnail/android-tv/coc-coc-phim-truc-tuyen.png\",\n" +
                "    \"title\": \"History Title Briana\",\n" +
                "    \"url\": \"https://exampleGreen.com\",\n" +
                "    \"timestamp\": 1621502400001\n" +
                "  },\n" +
                "  {\n" +
                "    \"title\": \"History Title Erickson\",\n" +
                "    \"url\": \"https://exampleClements.com\",\n" +
                "    \"timestamp\": 1621502400002\n" +
                "  },\n" +
                "  {\n" +
                "    \"title\": \"History Title Darlene\",\n" +
                "    \"url\": \"https://exampleMathis.com\",\n" +
                "    \"timestamp\": 1621502400003\n" +
                "  },\n" +
                "  {\n" +
                "    \"title\": \"History Title Myrna\",\n" +
                "    \"url\": \"https://exampleCaldwell.com\",\n" +
                "    \"timestamp\": 1621502400004\n" +
                "  },\n" +
                "  {\n" +
                "    \"title\": \"History Title Norman\",\n" +
                "    \"url\": \"https://exampleMcknight.com\",\n" +
                "    \"timestamp\": 1621502400005\n" +
                "  },\n" +
                "  {\n" +
                "    \"title\": \"History Title Fannie\",\n" +
                "    \"url\": \"https://exampleCraft.com\",\n" +
                "    \"timestamp\": 1621502400006\n" +
                "  },\n" +
                "  {\n" +
                "    \"title\": \"History Title Weaver\",\n" +
                "    \"url\": \"https://exampleCarlson.com\",\n" +
                "    \"timestamp\": 1621502400007\n" +
                "  },\n" +
                "  {\n" +
                "    \"title\": \"History Title Rosie\",\n" +
                "    \"url\": \"https://exampleGraham.com\",\n" +
                "    \"timestamp\": 1621502400008\n" +
                "  },\n" +
                "  {\n" +
                "    \"title\": \"History Title Gallegos\",\n" +
                "    \"url\": \"https://exampleWood.com\",\n" +
                "    \"timestamp\": 1621502400009\n" +
                "  },\n" +
                "  {\n" +
                "    \"title\": \"History Title Hannah\",\n" +
                "    \"url\": \"https://exampleNicholson.com\",\n" +
                "    \"timestamp\": 1621502400010\n" +
                "  }\n" +
                "]";
    }

    private String createBookmarkDummy() {
        return "[\n" +
                "  {\n" +
                "    \"imageUrl\": \"https://cdn.coccoc.com/browser_images/thumbnail/android-tv/coc-coc-phim-truc-tuyen.png\",\n" +
                "    \"title\": \"Bookmark Title Kathrine\",\n" +
                "    \"url\": \"https://facebook.com\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"imageUrl\": \"https://cdn.coccoc.com/browser_images/thumbnail/android-tv/coc-coc-phim-truc-tuyen.png\",\n" +
                "    \"title\": \"Bookmark Title Imogene\",\n" +
                "    \"url\": \"https://youtube.com\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"imageUrl\": \"https://cdn.coccoc.com/browser_images/thumbnail/android-tv/coc-coc-phim-truc-tuyen.png\",\n" +
                "    \"title\": \"Bookmark Title Krista\",\n" +
                "    \"url\": \"https://google.com\"\n" +
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
    }
}
