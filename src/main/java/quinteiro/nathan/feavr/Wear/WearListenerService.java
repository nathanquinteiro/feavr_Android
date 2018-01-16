package quinteiro.nathan.feavr.Wear;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import quinteiro.nathan.feavr.BuildConfig;

public class WearListenerService extends WearableListenerService {

    public static final String ACTION_SEND_HEART_RATE = "ACTION_SEND_HEART_RATE";
    public static final String INT_HEART_RATE = "INT_HEART_RATE";

    // Tag for Logcat
    private static final String TAG = "WearListenerService";

    // Constants
    public static final String ACTION_SEND_MESSAGE = "ACTION_SEND_MESSAGE";
    public static final String ACTION_SEND_DATAMAP = "ACTION_SEND_DATAMAP";
    public static final String MESSAGE = "MESSAGE";
    public static final String DATAMAP_INT = "DATAMAP_INT";
    public static final String DATAMAP_INT_ARRAYLIST = "DATAMAP_INT_ARRAYLIST";

    public static final String PATH = "PATH";

    // Member for the Wear API handle
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        // Start the Wear API connection
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        String action = intent.getAction();
        if(action == null) return START_NOT_STICKY;

        switch(action) {
            case ACTION_SEND_MESSAGE:
                sendMessage(intent.getStringExtra(MESSAGE), intent.getStringExtra(PATH));
                break;
            case ACTION_SEND_DATAMAP:
                PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(BuildConfig.another_path);
                putDataMapRequest.getDataMap().putInt(BuildConfig.a_key, intent.getIntExtra(DATAMAP_INT, -1));
                putDataMapRequest.getDataMap().putIntegerArrayList(BuildConfig.some_other_key, intent.getIntegerArrayListExtra(DATAMAP_INT_ARRAYLIST));
                sendPutDataMapRequest(putDataMapRequest);
                break;
            default:
                Log.w(TAG, "Unknown action");
                break;
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.v(TAG, "onDataChanged: " + dataEvents);

        for (DataEvent event : dataEvents) {

            // Get the URI of the event
            Uri uri = event.getDataItem().getUri();

            // Test if data has changed or has been removed
            if (event.getType() == DataEvent.TYPE_CHANGED) {

                // Extract the dataMap from the event
                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());

                Log.v(TAG, "DataItem Changed: " + event.getDataItem().toString() + "\n"
                        + "\tPath: " + uri
                        + "\tDatamap: " + dataMapItem.getDataMap() + "\n");

                Intent intent;

                switch (uri.getPath()) {
                    case BuildConfig.heart_rate_path:
                        int heartRate = dataMapItem.getDataMap().getInt(BuildConfig.heart_rate_key);
                        Log.i(TAG, "Got heart rate: " + heartRate);
                        intent = new Intent(ACTION_SEND_HEART_RATE);
                        intent.putExtra(INT_HEART_RATE, heartRate);
                        sendBroadcast(intent);
                        break;
                    default:
                        Log.v(TAG, "Data changed for unrecognized path: " + uri);
                        break;
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.w(TAG, "DataItem deleted: " + event.getDataItem().toString());
            }

            // For demo, send a acknowledgement message back to the node that created the data item
            String payload = "Received data OK!";
            String path = BuildConfig.acknowledge;
            String nodeId = uri.getHost();
            sendMessage(payload, path, nodeId);
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        // A message has been received from the Wear API
        Log.v(TAG, "onMessageReceived: " + messageEvent);

        // Get the URI of the event
        String path = messageEvent.getPath();

        switch (path) {
            case BuildConfig.start_activity:
                break;
            case BuildConfig.some_path:
                Log.v(TAG, "Received a message for path " + BuildConfig.some_path + " : " + new String(messageEvent.getData()));
                // For demo, send back a dataMap
                int some_value = 42;
                ArrayList<Integer> arrayList = new ArrayList<>();
                Collections.addAll(arrayList, 5, 7, 9, 10);
                sendSpecificDatamap(some_value, arrayList);
                break;
            case BuildConfig.another_path:
                Log.v(TAG, "Received a message for path " + BuildConfig.another_path + " : " + new String(messageEvent.getData()));
                break;
            default:
                Log.w(TAG, "Received a message for unknown path " + path + " : " + new String(messageEvent.getData()));
        }
    }

    private void sendMessage(String message, String path, final String nodeId) {
        // Sends a message through the Wear API
        Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId, path, message.getBytes())
                .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                        Log.v(TAG, "Sent message to " + nodeId + ". Result = " + sendMessageResult.getStatus());
                    }
                });
    }

    private void sendMessage(String message, String path) {
        // Send message to ALL connected nodes
        sendMessageToNodes(message, path);
    }

    void sendAsset(Asset asset, String path, String key) {
        // Sends data (an asset) through the Wear API
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(path);
        putDataMapRequest.getDataMap().putAsset(key, asset);
        sendPutDataMapRequest(putDataMapRequest);
    }

    void sendSpecificDatamap(int value, ArrayList<Integer> arrayList) {
        // Sends data (a datamap) through the Wear API
        // It's specific to a datamap containing an int and an arraylist. Duplicate and change
        // according to your needs
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(BuildConfig.another_path);
        putDataMapRequest.getDataMap().putInt(BuildConfig.a_key, value);
        putDataMapRequest.getDataMap().putIntegerArrayList(BuildConfig.some_other_key, arrayList);
        sendPutDataMapRequest(putDataMapRequest);
    }

    void sendPutDataMapRequest(PutDataMapRequest putDataMapRequest) {
        putDataMapRequest.getDataMap().putLong("time", System.nanoTime());
        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        request.setUrgent();
        Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                        Log.v(TAG, "Sent datamap. Result = " + dataItemResult.getStatus());

                    }
                });
    }

    void sendMessageToNodes(final String message, final String path) {
        Log.v(TAG, "Sending message " + message);
        // Lists all the nodes (devices) connected to the Wear API
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(@NonNull NodeApi.GetConnectedNodesResult connectedNodes) {
                for (Node node : connectedNodes.getNodes()) {
                    sendMessage(message, path, node.getId());
                }
            }
        });
    }

    private Bitmap bitmapFromAsset(Asset asset) {
        // Reads an asset from the Wear API and parse it as an image
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        ConnectionResult result = mGoogleApiClient.blockingConnect(10, TimeUnit.SECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        // Convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleApiClient, asset).await().getInputStream();
        mGoogleApiClient.disconnect();

        if (assetInputStream == null) {
            Log.w(TAG, "Requested an unknown Asset.");
            return null;
        }

        // Decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }
}
