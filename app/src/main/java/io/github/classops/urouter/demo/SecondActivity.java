package io.github.classops.urouter.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import io.github.classops.urouter.Router;
import io.github.classops.urouter.annotation.Param;
import io.github.classops.urouter.annotation.Route;
import io.github.classops.urouter.demo.bean.A;
import io.github.classops.urouter.demo.bean.B;
import io.github.classops.urouter.demo.bean.PA;
import io.github.classops.urouter.demo.bean.SA;


@Route(path = "/test/test2")
public class SecondActivity extends AppCompatActivity {

    @Param
    public boolean enabled;

    @Param(name = "progress")
    public int progress;

    @Param(name = "toast")
    public String toast;

    @Param(name = "sa")
    public SA sa;

    @Param(name = "pa")
    public PA pa;

    @Param(name = "a")
    public A<B> a;

    @Param(name = "list")
    public ArrayList<String> list;

    @Param
    public List<A<B>> listA;

    @Param
    public CharSequence seq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Router.get().inject(this);

//        MyInjector injector = new MyInjector();
//        injector.inject(this);

        setContentView(R.layout.activity_second);
        Toast.makeText(this, toast + ":" + progress + ", a = " + a, Toast.LENGTH_LONG)
//        Toast.makeText(this, toast + ":" + progress, Toast.LENGTH_LONG)
                .show();

        Log.e("Test", "list: " + TextUtils.join(",", list));
        Log.e("Test", "enabled: " + enabled);

        Button btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> {

//            Router.get().build("/test/test")
//                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                    .navigate(SecondActivity.this);

            Intent result = new Intent();
            result.putExtra("text", "123");
            setResult(Activity.RESULT_OK, result);
            finish();
        });
    }

}