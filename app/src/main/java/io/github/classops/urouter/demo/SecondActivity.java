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

import io.github.classops.urouter.Router;
import io.github.classops.urouter.annotation.Param;
import io.github.classops.urouter.annotation.Route;
import io.github.classops.urouter.demo.bean.A;
import io.github.classops.urouter.demo.bean.B;
import io.github.classops.urouter.demo.bean.PA;
import io.github.classops.urouter.demo.bean.SA;


/**
 * 测试 app 页面跳转、列表和对象参数、返回值
 */
@Route(path = "/test/test")
public class SecondActivity extends AppCompatActivity {

    @Param
    public boolean enabled;

    @Param(name = "progress")
    public int progress;

    @Param(name = "toast")
    public String toast;

    @Param(name = "sa")
    private SA sa;

    @Param(name = "pa")
    private PA pa;

    @Param(name = "a")
    public A<B> a;

    @Param(name = "list")
    public ArrayList<String> list;

    @Param
    public CharSequence seq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Router.get().inject(this);
        setContentView(R.layout.activity_second);
        Toast.makeText(this, toast + ":" + progress + ", a = " + a, Toast.LENGTH_LONG)
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

    public SA getSa() {
        return sa;
    }

    public void setSa(SA sa) {
        this.sa = sa;
    }

    public PA getPa() {
        return pa;
    }

    public void setPa(PA pa) {
        this.pa = pa;
    }
}