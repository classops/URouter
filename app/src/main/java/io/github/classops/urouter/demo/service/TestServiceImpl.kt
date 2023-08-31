package io.github.classops.urouter.demo.service

import android.content.Context
import android.util.Log
import android.widget.Toast
import io.github.classops.urouter.annotation.Route
import io.github.classops.urouter.demo.module_a.service.TestService

/**
 * 文件名：TestServiceImpl <br/>
 * 描述：测试
 *
 * @author wangmingshuo
 * @since 2023/04/07 17:18
 */
@Route(path = "/service/test")
class TestServiceImpl(private val context: Context) : TestService {
    override fun log() {
        Log.e("Test", "test log: ${context}")
    }

    override fun toast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT)
            .show()
    }
}