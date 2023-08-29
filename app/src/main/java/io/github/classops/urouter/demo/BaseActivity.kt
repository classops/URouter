package io.github.classops.urouter.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.classops.urouter.Router

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Router.get().inject(this)
    }
}