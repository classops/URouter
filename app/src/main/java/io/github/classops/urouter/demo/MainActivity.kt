package io.github.classops.urouter.demo

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import io.github.classops.urouter.NavigationCallback
import io.github.classops.urouter.Router
import io.github.classops.urouter.UriRequest
import io.github.classops.urouter.Utils
import io.github.classops.urouter.annotation.Param
import io.github.classops.urouter.annotation.Route
import io.github.classops.urouter.demo.bean.A
import io.github.classops.urouter.demo.bean.B
import io.github.classops.urouter.demo.module_a.service.TestService

@Route(path = "/test/test")
class MainActivity : BaseActivity() {

    @Param(desc = "测试字段")
    lateinit var test: String

    @Param(desc = "测试字段")
    var test0: Boolean = false

    @Param(desc = "测试字段")
    var test1: Byte = 0

    @Param(desc = "测试字段")
    var test2: Int = 0

    @Param(desc = "测试字段")
    var test3: Long = 0

    @Param(desc = "测试字段")
    var test6: Short = 0

    @Param(desc = "测试字段")
    var test4: Char = '2'

    @Param(desc = "dd")
    var dddd: String? = null

    var isTopVisible: Boolean = false
    var isTop: Int = 0


    private lateinit var launcher: ActivityResultLauncher<UriRequest?>

    private val testService by lazy {
        Router.get().build("/service/test")
            .navigate(this) as TestService
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Router.get().inject(this)

        launcher = Router.get().registerForResult(
            this,
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback<ActivityResult?> {
                Toast.makeText(this, "String: $it", Toast.LENGTH_SHORT)
                    .show()
            }
        )

        findViewById<Button>(R.id.btnStart).setOnClickListener {
//            routeToTest2()
//            testUriQueryParams()

//            Router.get().route(TestService::class.java)?.log()

//            testService.toast("测试")

//            launcher.launch(
//                UriRequest.Builder("/test/atest2")
//                    .withString("ds", "2312")
//                    .withInt("progress",80)
//                    .withString("toast", "传递toast参数")
//                    .build()

            launcher.launch(
                UriRequest.Builder("/test/test2")
                    .withBoolean("enabled", true)
                    .withString("ds", "2312")
                    .withInt("progress",80)
                    .withObjectList("list", listOf("1", "2", "3"))
                    .withString("toast", "传递toast参数")
                    .withString("seq", "test seq")
                    .build()
            )
        }

        Utils.getQueryParameters(
            Uri.parse("https://www.baidu.com/test")
                .buildUpon()
                .appendQueryParameter("b", "4")
                .appendQueryParameter("b", "3")
                .appendQueryParameter("a", "1")
                .appendQueryParameter("a", "2")
                .appendQueryParameter("a", "3")
                .appendQueryParameter("a", "1")
                .appendQueryParameter("b", "2")
                .appendQueryParameter("b", "1")

                .appendQueryParameter("ba", "4321")
                .appendQueryParameter("ba", "432")
                .appendQueryParameter("ab", "1234")
                .appendQueryParameter("ab", "123")
                .appendQueryParameter("ab", "12")
                .appendQueryParameter("ab", "1")
                .appendQueryParameter("ba", "43")
                .appendQueryParameter("ba", "4")
                .build()
        ).let {
            Log.d("Test", "result: ${it}")
        }


        val fragment = Router.get().build("/frag/test")
            .withString("content", "测试Fragment内容")
            .navigate()
        if (fragment is Fragment) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }
    }

    private fun routeToTest2() {
        Router.get().build("/test/test2")
            .withString("ds", "2312")
            .withInt("progress", 80)
            .withString("toast", "传递toast参数")
            .withObject("list", arrayListOf("1", "2", "3"))
            .withActivityOptions(
                ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_from_bottom,
                    R.anim.slide_out_to_bottom
                )
            )
            .withObject("a", A("a", B("1")))

            .navigate(this, object :
                NavigationCallback {
                override fun onFound(request: UriRequest?) {
                    Toast.makeText(this@MainActivity, "onFound", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onLost(request: UriRequest?) {
                    Toast.makeText(this@MainActivity, "onLost", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onArrival(request: UriRequest?) {
                    Toast.makeText(this@MainActivity, "onArrival", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onInterrupt(request: UriRequest?) {
                    Toast.makeText(this@MainActivity, "onInterrupt", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

}