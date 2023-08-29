package io.github.classops.urouter.demo

import androidx.multidex.MultiDexApplication
import io.github.classops.urouter.Router

/**
 * Application
 *
 * @author wangmingshuo
 * @since 2023/04/26 16:04
 */
class App : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        Router.get().init(this)
        Router.get().log()
    }
}