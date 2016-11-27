package com.wix.reactnativenotifications.core;

import android.content.Context;
import android.os.Bundle;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class ReactContextAdapter {
    public ReactContext getRunningReactContext(Context context) {
        final ReactNativeHost rnHost = ((ReactApplication) context.getApplicationContext()).getReactNativeHost();
        if (!rnHost.hasInstance()) {
            return null;
        }

        final ReactInstanceManager instanceManager = rnHost.getReactInstanceManager();
        final ReactContext reactContext = instanceManager.getCurrentReactContext();
        if (reactContext == null || !reactContext.hasActiveCatalystInstance()) {
            return null;
        }

        return reactContext;
    }

    public void sendEventToJS(String eventName, Bundle data, Context context) {
        final ReactContext reactContext = getRunningReactContext(context);
        if (reactContext != null) {
            sendEventToJS(eventName, data, reactContext);
        }
    }

    public void sendEventToJS(String eventName, WritableMap data, Context context) {
        final ReactContext reactContext = getRunningReactContext(context);
        if (reactContext != null) {
            sendEventToJS(eventName, data, reactContext);
        }
    }

    public void sendEventToJS(String eventName, Bundle data, ReactContext reactContext) {
        sendEventToJS(eventName, Arguments.fromBundle(data), reactContext);
    }

    public void sendEventToJS(String eventName, WritableMap data, ReactContext reactContext) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, data);
    }
}
