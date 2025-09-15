package io.github.classops.urouter.plugin

import io.github.classops.urouter.plugin.transform.RouterRegister
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.lang.module.ModuleDescriptor

class RouterPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // 注入代码
        val clazz = if (isAGP8) {
            Class.forName("io.github.classops.urouter.plugin.transform.v8.AGP8RouterRegister")
        } else {
            Class.forName("io.github.classops.urouter.plugin.transform.v4.AGP4RouterRegister")
        }
        val constructor = clazz.getConstructor(Project::class.java)
        val register: RouterRegister = constructor.newInstance(project) as RouterRegister
        register.registerTransform()
    }

    private val isAGP8: Boolean
        get() =
            try {
                @Suppress("DEPRECATION")
                val currVersion = ModuleDescriptor.Version.parse(com.android.builder.model.Version.ANDROID_GRADLE_PLUGIN_VERSION)
                val compVersion = ModuleDescriptor.Version.parse("8.0.0")
                currVersion >= compVersion
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
                true
            }
}