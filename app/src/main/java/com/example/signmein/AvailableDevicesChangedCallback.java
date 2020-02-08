package com.example.signmein;

import java.util.List;

public interface AvailableDevicesChangedCallback {
    void AvailableDevicesChanged(String[] endpointIds, String[] endpointNames);
}
