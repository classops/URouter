package io.github.classops.urouter.demo.module_a

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import io.github.classops.urouter.Router
import io.github.classops.urouter.annotation.Param
import io.github.classops.urouter.annotation.Route
import io.github.classops.urouter.demo.module_a.service.TestService

@Route(path = "/test/atest2")
class ATest2Activity : AppCompatActivity() {

    @Param(name = "progress")
    var prog = 0

    @Param
    var toast: String? = null

    @Param
    var list: MutableList<String>? = null

    private val testService by lazy {
        Router.get().build("/service/test")
            .navigate(this) as TestService
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_atest2)

        Router.get().inject(this)
        testService.toast("$toast:$prog")
        val btnClose = findViewById<Button>(R.id.btnClose)
        btnClose.setOnClickListener { v: View? ->
            val result = Intent()
            result.putExtra("text", "123")
            setResult(Activity.RESULT_OK, result)
            finish()
        }
    }
}