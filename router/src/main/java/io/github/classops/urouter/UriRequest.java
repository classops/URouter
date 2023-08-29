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
    private final Bundle extras;
    private int flags;
    private int enterAnim = -1;
    private int exitAnim = -1;

    private Bundle activityOptions;

    private int routeType = RouteType.UNKNOWN;

    private boolean startActivity = true;
    private int requestCode = -1;

    private boolean ignoreInterceptor;

    public UriRequest(@NonNull Uri uri, Bundle extras) {
        this.uri = uri;
        this.extras = extras;
    }

    public UriRequest(@NonNull String url, Bundle extras) {
        this.uri = Uri.parse(url);
        this.extras = extras;
    }

    public UriRequest(String scheme, String host, @NonNull String path, Bundle extras) {
        this(new Uri.Builder()
                .scheme(scheme)
                .authority(host)
                .path(path)
                .build(), extras);
    }

    public String getHost() {
        return this.uri.getHost();
    }

    public String getPath() {
        return this.uri.getPath();
    }

    @NonNull
    public Uri getUri() {
        return uri;
    }

    public Bundle getExtras() {
        return extras;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
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

    public void setActivityOptions(Bundle activityOptions) {
        this.activityOptions = activityOptions;
    }

    public int getRouteType() {
        return routeType;
    }

    public void setRouteType(int routeType) {
        this.routeType = routeType;
    }

    public boolean isStartActivity() {
        return startActivity;
    }

    public void setStartActivity(boolean startActivity) {
        this.startActivity = startActivity;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public boolean isIgnoreInterceptor() {
        return ignoreInterceptor;
    }

    public void setIgnoreInterceptor(boolean ignoreInterceptor) {
        this.ignoreInterceptor = ignoreInterceptor;
    }

    public static class Builder {

        private final Uri uri;
        private final Bundle extras;
        private int flags;
        private int enterAnim = -1;
        private int exitAnim = -1;
        private Bundle activityOptions;
        private int routeType = RouteType.UNKNOWN;
        private boolean startActivity = true;

        public Builder(Uri uri) {
            this.uri = uri;
            this.extras = new Bundle();
        }

        public Builder(String uri) {
            this(Uri.parse(uri));
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
            SerializationService service = Router.get().route(SerializationService.class);
            if (service != null) {
                this.extras.putString(key, service.toJson(obj));
            }
            return this;
        }

        public Builder withObjectList(@Nullable String key, @Nullable List<?> list) {
            SerializationService service = Router.get().route(SerializationService.class);
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
            UriRequest request = new UriRequest(this.uri, this.extras);
            request.flags = flags;
            request.enterAnim = enterAnim;
            request.exitAnim = exitAnim;
            request.activityOptions = activityOptions;
            request.routeType = this.routeType;
            request.startActivity = this.startActivity;
            return request;
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
            UriRequest request = build();
            request.requestCode = requestCode;
            return Router.get().route(context, request, callback);
        }

    }

}
