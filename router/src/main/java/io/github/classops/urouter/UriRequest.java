package io.github.classops.urouter;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;

import java.io.Serializable;
import java.util.List;

import io.github.classops.urouter.route.RouteType;
import io.github.classops.urouter.service.SerializationService;

/**
 * 路由请求
 *
 * @author wangmingshuo
 * @since 2022/11/17 19:26
 */
public class UriRequest {

    @NonNull
    private final Uri uri;
    @Nullable
    private final Bundle extras;
    private final int routeType;
    private final int flags;
    private final int enterAnim;
    private final int exitAnim;
    private final Bundle activityOptions;
    private final boolean startActivity;
    private final int requestCode;
    private final boolean ignoreInterceptor;

    public UriRequest(@NonNull Uri uri, @Nullable Bundle extras, int routeType, int flags,
                      int enterAnim, int exitAnim, Bundle activityOptions, int requestCode,
                      boolean startActivity, boolean ignoreInterceptor) {
        this.uri = uri;
        this.extras = extras;
        this.routeType = routeType;
        this.flags = flags;
        this.enterAnim = enterAnim;
        this.exitAnim = exitAnim;
        this.activityOptions = activityOptions;
        this.requestCode = requestCode;
        this.startActivity = startActivity;
        this.ignoreInterceptor = ignoreInterceptor;
    }

    public UriRequest(@NonNull String url, @Nullable Bundle extras, int routeType, int flags,
                      int enterAnim, int exitAnim, Bundle activityOptions, int requestCode,
                      boolean startActivity, boolean ignoreInterceptor) {
        this(Uri.parse(url), extras, routeType, flags, enterAnim, exitAnim, activityOptions,
                requestCode, startActivity, ignoreInterceptor);
    }

    public UriRequest(String scheme, String host, @NonNull String path, @Nullable Bundle extras,
                      int routeType, int flags, int enterAnim, int exitAnim, Bundle activityOptions,
                      int requestCode, boolean startActivity, boolean ignoreInterceptor) {
        this(new Uri.Builder()
                .scheme(scheme)
                .authority(host)
                .path(path)
                .build(), extras, routeType, flags, enterAnim, exitAnim, activityOptions,
                requestCode, startActivity, ignoreInterceptor);
    }

    @NonNull
    public Uri getUri() {
        return uri;
    }

    public String getHost() {
        return this.uri.getHost();
    }

    public String getPath() {
        return this.uri.getPath();
    }

    @Nullable
    public Bundle getExtras() {
        return extras;
    }

    public int getFlags() {
        return flags;
    }

    public int getEnterAnim() {
        return enterAnim;
    }

    public int getExitAnim() {
        return exitAnim;
    }

    public Bundle getActivityOptions() {
        return activityOptions;
    }

    public int getRouteType() {
        return routeType;
    }

    public boolean isStartActivity() {
        return startActivity;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public boolean isIgnoreInterceptor() {
        return ignoreInterceptor;
    }

    public Builder newBuilder(@NonNull Router router) {
        return new Builder(router, this);
    }

    public static class Builder {

        private final Router router;
        private final SerializationService service;
        @NonNull
        private final Uri uri;
        @NonNull
        private final Bundle extras;
        private int flags;
        private int enterAnim = -1;
        private int exitAnim = -1;
        private Bundle activityOptions;
        private int routeType = RouteType.UNKNOWN;
        private boolean startActivity = true;

        public Builder(@NonNull Uri uri) {
            this(Router.get(), uri);
        }

        public Builder(@NonNull String uri) {
            this(Uri.parse(uri));
        }

        public Builder(@NonNull Router router, @NonNull Uri uri) {
            this.router = router;
            this.service = router.navigate(SerializationService.class);
            this.uri = uri;
            this.extras = new Bundle();
        }

        public Builder(@NonNull UriRequest request) {
            this(Router.get(), request);
        }

        public Builder(@NonNull Router router, @NonNull UriRequest request) {
            this.router = router;
            this.service = router.navigate(SerializationService.class);
            this.uri = request.getUri();
            Bundle extras = request.getExtras();
            this.extras = extras != null ? extras : new Bundle();
            this.flags = request.getFlags();
            this.exitAnim = request.getExitAnim();
            this.enterAnim = request.getEnterAnim();
            this.activityOptions = request.getActivityOptions();
            this.routeType = request.getRouteType();
            this.startActivity = request.isStartActivity();
        }

        public Builder withBoolean(String key, boolean value) {
            this.extras.putBoolean(key, value);
            return this;
        }

        public Builder withByte(String key, byte value) {
            this.extras.putByte(key, value);
            return this;
        }

        public Builder withShort(String key, short value) {
            this.extras.putShort(key, value);
            return this;
        }

        public Builder withInt(String key, int value) {
            this.extras.putInt(key, value);
            return this;
        }

        public Builder withLong(String key, long value) {
            this.extras.putLong(key, value);
            return this;
        }

        public Builder withFloat(String key, float value) {
            this.extras.putFloat(key, value);
            return this;
        }

        public Builder withDouble(String key, double value) {
            this.extras.putDouble(key, value);
            return this;
        }

        public Builder withString(String key, String value) {
            this.extras.putString(key, value);
            return this;
        }

        public Builder withCharSequence(String key, String value) {
            this.extras.putCharSequence(key, value);
            return this;
        }

        public Builder withObject(String key, Object obj) {
            SerializationService service = router.navigate(SerializationService.class);
            if (service != null) {
                this.extras.putString(key, service.toJson(obj));
            }
            return this;
        }

        public Builder withObjectList(@Nullable String key, @Nullable List<?> list) {
            SerializationService service = this.router.navigate(SerializationService.class);
            if (service != null) {
                this.extras.putString(key, service.toJson(list));
            }
            return this;
        }

        public Builder withBooleanArray(@Nullable String key, @Nullable boolean[] value) {
            this.extras.putBooleanArray(key, value);
            return this;
        }

        public Builder withByteArray(@Nullable String key, @Nullable byte[] value) {
            this.extras.putByteArray(key, value);
            return this;
        }

        public Builder withShortArray(@Nullable String key, @Nullable short[] value) {
            this.extras.putShortArray(key, value);
            return this;
        }

        public Builder withCharArray(@Nullable String key, @Nullable char[] value) {
            this.extras.putCharArray(key, value);
            return this;
        }

        public Builder withIntArray(@Nullable String key, @Nullable int[] value) {
            this.extras.putIntArray(key, value);
            return this;
        }

        public Builder withLongArray(@Nullable String key, @Nullable long[] value) {
            this.extras.putLongArray(key, value);
            return this;
        }

        public Builder withFloatArray(@Nullable String key, @Nullable float[] value) {
            this.extras.putFloatArray(key, value);
            return this;
        }

        public Builder withDoubleArray(@Nullable String key, @Nullable double[] value) {
            this.extras.putDoubleArray(key, value);
            return this;
        }

        public Builder withStringArray(@Nullable String key, @Nullable String[] value) {
            this.extras.putStringArray(key, value);
            return this;
        }

        public Builder withCharSequenceArray(@Nullable String key, @Nullable String[] value) {
            this.extras.putCharSequenceArray(key, value);
            return this;
        }

        public Builder withParcelable(@Nullable String key, @Nullable Parcelable value) {
            this.extras.putParcelable(key, value);
            return this;
        }

        public Builder withSerializable(@Nullable String key, @Nullable Serializable value) {
            this.extras.putSerializable(key, value);
            return this;
        }

        public Builder withFlags(int flags) {
            this.flags = flags;
            return this;
        }

        public Builder withTransition(int enterAnim, int exitAnim) {
            this.enterAnim = enterAnim;
            this.exitAnim = exitAnim;
            return this;
        }

        public Builder withActivityOptions(@Nullable Bundle options) {
            this.activityOptions = options;
            return this;
        }

        public Builder withActivityOptions(@Nullable ActivityOptionsCompat optionsCompat) {
            this.activityOptions = optionsCompat != null ? optionsCompat.toBundle() : null;
            return this;
        }

        public Builder routeType(int type) {
            this.routeType = type;
            return this;
        }

        public Builder routeOnly() {
            this.startActivity = false;
            return this;
        }

        public Builder addFlags(int flags) {
            this.flags |= flags;
            return this;
        }

        public UriRequest build() {
            return build(-1, false);
        }

        UriRequest build(int requestCode) {
            return build(requestCode, false);
        }

        UriRequest build(int requestCode, boolean ignoreInterceptor) {
            return new UriRequest(this.uri, this.extras, this.routeType, this.flags, this.enterAnim,
                    this.exitAnim, this.activityOptions, requestCode, startActivity, ignoreInterceptor);
        }

        @Nullable
        public Object navigate() {
            return this.navigate(null);
        }

        @Nullable
        public Object navigate(@Nullable Context context) {
            return navigate(context, -1);
        }

        @Nullable
        public Object navigate(@Nullable Context context, int requestCode) {
            return navigate(context, requestCode, null);
        }

        @Nullable
        public Object navigate(@Nullable Context context, @Nullable NavigationCallback callback) {
            return navigate(context, -1, callback);
        }

        @Nullable
        public Object navigate(@Nullable Context context, int requestCode,
                               @Nullable NavigationCallback callback) {
            return router.route(context, build(requestCode), callback);
        }

    }

}
