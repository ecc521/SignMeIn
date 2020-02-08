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
import com.google.android.gms.nearby.connection.Strategy;

import java.util.ArrayList;
import java.util.List;

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

    public void startAdvertising(String userNickname) {
        this.userNickname = userNickname;
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

    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                    // An endpoint was found. We request a connection to it.
                    Log.i(TAG, "Found Endpoint " + endpointId);
                    Log.i(TAG, "Endpoint Name is " + info.getEndpointName());

                    String hubIdentifier = endpointId; //TODO: We need a way to show the user friendly endpoint
                    //name instead of just providing the endpoint ID. Make sure to change onEndpointLost to also use a new system.
                    availableHubs.add(hubIdentifier);
                    availableHubNames.add(info.getEndpointName());

                    Log.i(TAG, "List of Endpoints: " + TextUtils.join(", ", availableHubs));

                    String[] hubsForCallback = availableHubs.toArray(new String[0]);
                    String[] hubNamesForCallback = availableHubNames.toArray(new String[0]);

                    callback.AvailableDevicesChanged(hubsForCallback, hubNamesForCallback);

                    /*Nearby.getConnectionsClient(context)
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
                                    });*/
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

    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    // Automatically accept the connection on both sides.
                    Log.i(TAG, "Connection initiated by " + endpointId);
                    Log.i(TAG, "Endpoint name is " + connectionInfo.getEndpointName());
                    //Nearby.getConnectionsClient(context).acceptConnection(endpointId, payloadCallback);
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

}
