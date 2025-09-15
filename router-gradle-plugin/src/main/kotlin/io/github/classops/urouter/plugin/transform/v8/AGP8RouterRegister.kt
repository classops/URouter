package io.github.classops.urouter.plugin.transform.v8

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.api.AndroidBasePlugin
import io.github.classops.urouter.plugin.transform.RouterRegister
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import java.util.concurrent.atomic.AtomicBoolean


/**
 * AGP8使用 Gradle 的 Transform API，而不是 AGP 的。
 */
class AGP8RouterRegister(private val project: Project) : RouterRegister {

    private val extension: AndroidComponentsExtension<*, *, *> = project.extensions.getByType(
            AndroidComponentsExtension::class.java
    )

    private val routerHelper = RouterAggregateHelper()

    override val pluginVersion: String
        get() = extension.pluginVersion.toString()

    val logger: Logger
        get() = this.project.logger

    override fun registerTransform() {
        val configured = AtomicBoolean(false)

        project.plugins.withId("com.android.base") {
            if (configured.compareAndSet(false, true)) {
                configureURouter(project)
            }
        }

        project.plugins.withType(AndroidBasePlugin::class.java) {
            if (configured.compareAndSet(false, true)) {
                configureURouter(project)
            }
        }
    }

    private fun configureURouter(project: Project) {
        println("Configure URouter")
        routerHelper.registerAggregateTransform(project)
        routerHelper.configureCompileClasspath(project)
        routerHelper.configureAggregatingTask(project)
    }

}