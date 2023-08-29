package io.github.classops.urouter.demo.module_b;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import io.github.classops.urouter.Router;
import io.github.classops.urouter.annotation.Param;


public class BTestActivity extends AppCompatActivity {

    @Param(name = "progress")
    private int prog;

    @Param
    private String toast;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btest);
        Router.get().inject(this);
        Toast.makeText(this, toast, Toast.LENGTH_LONG).show();
    }
}
