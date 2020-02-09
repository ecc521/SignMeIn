package com.example.signmein;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class DeviceConnector {
    private static final Strategy STRATEGY = Strategy.P2P_STAR;

    private static final String SERVICE_ID = BuildConfig.APPLICATION_ID;

    private String TAG = "DeviceConnector";

    private Context context;

    private String userNickname;

    //Android ID is used to try and detect sign-in fraud, where one person signs in for somebody else.
    private String ANDROID_ID;

    private List<String> availableHubNames = new ArrayList<>();
    private List<String> availableHubs = new ArrayList<>();



    public DeviceConnector(Context context) {
        this.context = context;
        this.ANDROID_ID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.i(TAG, "Android ID for current device is " + ANDROID_ID);
    }

    //No-op. We don't need it to actually do anything yet.
    private PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {

        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {

        }
    };

    public void startAdvertising(String userNickname, IncomingConnectionCallback callback) {
        this.userNickname = userNickname;

        ConnectionLifecycleCallback connectionLifecycleCallback =
                new ConnectionLifecycleCallback() {
                    @Override
                    public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                        callback.IncomingConnection(endpointId, connectionInfo.getEndpointName());
                        Log.i(TAG, "Accepting connection to " + endpointId);
                        Nearby.getConnectionsClient(context).acceptConnection(endpointId, payloadCallback);
                    }

                    @Override
                    public void onConnectionResult(String endpointId, ConnectionResolution result) {
                        switch (result.getStatus().getStatusCode()) {
                            case ConnectionsStatusCodes.STATUS_OK:
                                Log.i(TAG, "Successfully connected to " + endpointId);
                                // We're connected! Can now start sending and receiving data.
                                break;
                            case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                                Log.i(TAG, "Connection rejected for endpoint " + endpointId);
                                // The connection was rejected by one or both sides.
                                break;
                            case ConnectionsStatusCodes.STATUS_ERROR:
                                Log.e(TAG, "Connection errored for endpoint " + endpointId);
                                // The connection broke before it was able to be accepted.
                                break;
                            default:
                                // Unknown status code
                        }
                    }

                    @Override
                    public void onDisconnected(String endpointId) {
                        Log.i(TAG, "Disconnected from " + endpointId);
                        // We've been disconnected from this endpoint. No more data can be
                        // sent or received.
                    }
                };

        AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder().setStrategy(STRATEGY).build();
        Nearby.getConnectionsClient(context)
                .startAdvertising(
                        userNickname, SERVICE_ID, connectionLifecycleCallback, advertisingOptions)
                .addOnSuccessListener(
                        (Void unused) -> {
                            // We're advertising!
                            Log.i(TAG, "Started Advertising");
                        })
                .addOnFailureListener(
                        (Exception e) -> {
                            // We were unable to start advertising.
                            Log.e(TAG, "Error Starting Advertising " + e);
                        });
    }

    public void stopDiscovery() {
        Nearby.getConnectionsClient(context).stopDiscovery();
        //TODO: Do we need listeners?
    }

    public void stopAdvertising() {
        Nearby.getConnectionsClient(context).stopAdvertising();
        //TODO: Do we need listeners?
    }

    private AvailableDevicesChangedCallback callback;

    public void startDiscovery(String userNickname, AvailableDevicesChangedCallback callback) {
        this.callback = callback;
        this.userNickname = userNickname;
        DiscoveryOptions discoveryOptions =
                new DiscoveryOptions.Builder().setStrategy(STRATEGY).build();
        Nearby.getConnectionsClient(context)
                .startDiscovery(SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
                .addOnSuccessListener(
                        (Void unused) -> {
                            Log.i(TAG, "Started Discovery");
                            // We're discovering!
                        })
                .addOnFailureListener(
                        (Exception e) -> {
                            // We're unable to start discovering.
                            Log.e(TAG, "Error Starting Discovery " + e);
                        });
    }

    public void connectToEndpoint(String userNickname, String endpointId, SignInCompletedCallback callback) {
    //TODO: We need to disconnect from the endpoint so that we can connect again in the future without restarting the app.
         ConnectionLifecycleCallback connectionLifecycleCallback =
                new ConnectionLifecycleCallback() {
                    @Override
                    public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                        // Automatically accept the connection on both sides.
                        Log.i(TAG, "Connection initiated by " + endpointId);
                        Log.i(TAG, "Endpoint name is " + connectionInfo.getEndpointName());
                        Nearby.getConnectionsClient(context).acceptConnection(endpointId, payloadCallback);
                    }

                    @Override
                    public void onConnectionResult(String endpointId, ConnectionResolution result) {
                        switch (result.getStatus().getStatusCode()) {
                            case ConnectionsStatusCodes.STATUS_OK:
                                // We're connected! Can now start sending and receiving data.
                                Log.i(TAG, "Successfully connected to " + endpointId);
                                //Disconnect from the endpoint.
                                Nearby.getConnectionsClient(context)
                                        .disconnectFromEndpoint(endpointId);
                                callback.SignInCompleted(endpointId);
                                break;
                            case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                                Log.i(TAG, "Connection rejected for endpoint " + endpointId);
                                // The connection was rejected by one or both sides.
                                break;
                            case ConnectionsStatusCodes.STATUS_ERROR:
                                Log.e(TAG, "Connection errored for endpoint " + endpointId);
                                // The connection broke before it was able to be accepted.
                                break;
                            default:
                                // Unknown status code
                        }
                    }

                    @Override
                    public void onDisconnected(String endpointId) {
                        Log.i(TAG, "Disconnected from " + endpointId);
                        // We've been disconnected from this endpoint. No more data can be
                        // sent or received.
                    }
                };

        Nearby.getConnectionsClient(context)
                .requestConnection(userNickname, endpointId, connectionLifecycleCallback)
                .addOnSuccessListener(
                        (Void unused) -> {
                            Log.i(TAG, "Requested Connection to " + endpointId);
                            // We successfully requested a connection. Now both sides
                            // must accept before the connection is established.
                        })
                .addOnFailureListener(
                        (Exception e) -> {
                            // Nearby Connections failed to request the connection.
                            Log.e(TAG, "Failed to Initiate Connection to " + endpointId + ". " + e);
                        });
    }

    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {

                    availableHubs.add(endpointId);
                    availableHubNames.add(info.getEndpointName());

                    String[] hubsForCallback = availableHubs.toArray(new String[0]);
                    String[] hubNamesForCallback = availableHubNames.toArray(new String[0]);

                    callback.AvailableDevicesChanged(hubsForCallback, hubNamesForCallback);
                }

                @Override
                public void onEndpointLost(String endpointId) {
                    // A previously discovered endpoint has gone away.
                    Log.i(TAG, "Endpoint Disappeared " + endpointId);

                    availableHubNames.remove(availableHubs.indexOf(endpointId));
                    availableHubs.remove(endpointId);
                    Log.i(TAG, "List of Endpoints: " + TextUtils.join(", ", availableHubs));
                }
            };

}
