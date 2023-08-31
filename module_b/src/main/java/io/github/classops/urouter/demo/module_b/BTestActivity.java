package io.github.classops.urouter.demo.module_b;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import io.github.classops.urouter.Router;
import io.github.classops.urouter.annotation.Param;
import io.github.classops.urouter.annotation.Route;

@Route(path = "/test/b")
public class BTestActivity extends AppCompatActivity {

    @Param(name = "progress")
    public int prog;

    @Param
    public String toast;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btest);
        Router.get().inject(this);
        Toast.makeText(this, toast, Toast.LENGTH_LONG).show();

        Button btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener((v) -> {
            setResult(Activity.RESULT_OK);
            finish();
        });
    }
}
