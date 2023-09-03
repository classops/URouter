package io.github.classops.urouter.result;

import android.content.Context;
import android.content.Intent;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;


public abstract class RouterResultContract<T> extends ActivityResultContract<Intent, T> {

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Intent intent) {
        return intent;
    }

}
