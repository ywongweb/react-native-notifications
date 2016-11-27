package com.wix.reactnativenotifications.core.notification;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.react.bridge.ReactContext;
import com.wix.reactnativenotifications.core.AppLaunchHelper;
import com.wix.reactnativenotifications.core.AppLifecycleFacade;
import com.wix.reactnativenotifications.core.AppLifecycleFacade.AppVisibilityListener;
import com.wix.reactnativenotifications.core.ReactContextAdapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PushNotificationTest {

    @Mock private ReactContext mReactContext;
    @Mock private Context mContext;

    @Mock private Bundle mDefaultBundle;
    @Mock private Intent mLaunchIntent;
    @Mock private AppLifecycleFacade mAppLifecycleFacade;
    @Mock private AppLaunchHelper mAppLaunchHelper;
    @Mock private ReactContextAdapter mReactContextAdapter;

    @Before
    public void setup() throws Exception {
        when(mDefaultBundle.getString(eq("title"))).thenReturn("Notification-title");
        when(mDefaultBundle.getString(eq("body"))).thenReturn("Notification-body");
        when(mDefaultBundle.clone()).thenReturn(mDefaultBundle);

        when(mAppLaunchHelper.getLaunchIntent(eq(mContext))).thenReturn(mLaunchIntent);
        when(mReactContextAdapter.getRunningReactContext(mContext)).thenReturn(mReactContext);
    }

    @Test
    public void onOpened_noReactContext_launchApp() throws Exception {
        when(mAppLifecycleFacade.isReactInitialized()).thenReturn(false);

        final PushNotification uut = createUUT();
        uut.onOpened();

        verify(mContext).startActivity(eq(mLaunchIntent));
    }

    @Test
    public void onOpened_appInvisible_resumeAppWaitForVisibility() throws Exception {
        when(mAppLifecycleFacade.isReactInitialized()).thenReturn(true);
        when(mAppLifecycleFacade.isAppVisible()).thenReturn(false);

        final PushNotification uut = createUUT();
        uut.onOpened();

        verify(mContext).startActivity(any(Intent.class));
        verify(mAppLifecycleFacade).addVisibilityListener(any(AppVisibilityListener.class));
    }

    @Test
    public void onOpened_appGoesVisible_resumeAppAndNotifyJs() throws Exception {

        // Arrange

        when(mAppLifecycleFacade.isReactInitialized()).thenReturn(true);
        when(mAppLifecycleFacade.isAppVisible()).thenReturn(false);

        // Act

        final PushNotification uut = createUUT();
        uut.onOpened();

        // Hijack and invoke visibility listener
        ArgumentCaptor<AppVisibilityListener> listenerCaptor = ArgumentCaptor.forClass(AppVisibilityListener.class);
        verify(mAppLifecycleFacade).addVisibilityListener(listenerCaptor.capture());
        AppVisibilityListener listener = listenerCaptor.getValue();
        listener.onAppVisible();

        // Assert

        verify(mReactContextAdapter).sendEventToJS(eq("notificationOpened"), eq(mDefaultBundle), eq(mContext));
    }

    @Test
    public void onOpened_appVisible_notifyJS() throws Exception {
        when(mAppLifecycleFacade.isReactInitialized()).thenReturn(true);
        when(mAppLifecycleFacade.isAppVisible()).thenReturn(true);

        final PushNotification uut = createUUT();
        uut.onOpened();

        verify(mContext, never()).startActivity(any(Intent.class));
        verify(mReactContextAdapter).sendEventToJS(eq("notificationOpened"), eq(mDefaultBundle), eq(mContext));
    }

    protected PushNotification createUUT() {
        return new PushNotification(mContext, mDefaultBundle, mAppLifecycleFacade, mAppLaunchHelper, mReactContextAdapter);
    }
}
