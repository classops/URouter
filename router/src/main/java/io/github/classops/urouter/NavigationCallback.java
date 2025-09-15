package io.github.classops.urouter;

import androidx.annotation.MainThread;

/**
 * 导航回调
 *
 * @author classops
 * @since 2023/04/20 09:47
 */
public interface NavigationCallback {

    @MainThread
    void onFound(UriRequest request);

    @MainThread
    void onLost(UriRequest request);

    @MainThread
    void onArrival(UriRequest request);

    @MainThread
    void onInterrupt(UriRequest request);

}
