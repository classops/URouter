package io.github.classops.urouter.demo

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import io.github.classops.urouter.NavigationCallback
import io.github.classops.urouter.Router
import io.github.classops.urouter.UriRequest
import io.github.classops.urouter.demo.bean.A
import io.github.classops.urouter.demo.bean.B
import io.github.classops.urouter.demo.databinding.ActivityMainBinding
import io.github.classops.urouter.demo.module_a.service.TestService

class MainActivity : BaseActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var launcher: ActivityResultLauncher<UriRequest?>

    private val testService by lazy {
        Router.get().route(TestService::class.java) as TestService
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        launcher = Router.get().registerForResult(
            this,
            ActivityResultContracts.StartActivityForResult()
        ) {
            Toast.makeText(this, "result: $it", Toast.LENGTH_SHORT)
                .show()
        }

        // test fragment
        val fragment = Router.get().build("/frag/test")
            .withString("content", "test route fragment")
            .navigate()
        if (fragment is Fragment) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }

        binding.btnTestApp.setOnClickListener {
            routeToTest()

        }

        binding.btnTestA.setOnClickListener {
            launcher.launch(
                UriRequest.Builder("/test/a")
                    .withString("ds", "2312")
                    .withInt("progress",80)
                    .withString("toast", "hello world!")
                    .build()
            )
        }

        binding.btnTestB.setOnClickListener {
            launcher.launch(
                UriRequest.Builder("/test/b")
                    .withBoolean("enabled", true)
                    .withString("ds", "2312")
                    .withInt("progress", 80)
                    .withObjectList("list", listOf("1", "2", "3"))
                    .withString("toast", "hello world!")
                    .withString("seq", "test seq")
                    .build()
            )
        }

        binding.btnTestService.setOnClickListener {
            testService.toast("Service")
        }
    }

    private fun routeToTest() {
        Router.get().build("/test/test")
            .withString("ds", "2312")
            .withInt("progress", 80)
            .withString("toast", "hello world!")
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
                    Log.d("Router", "onFound")
                }

                override fun onLost(request: UriRequest?) {
                    Log.d("Router", "onLost")
                }

                override fun onArrival(request: UriRequest?) {
                    Log.d("Router", "onArrival")
                }

                override fun onInterrupt(request: UriRequest?) {
                    Log.d("Router", "onInterrupt")
                }
            })
    }

}