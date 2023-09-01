package io.github.classops.urouter.plugin.transform.v4

import com.android.build.gradle.AppExtension
import io.github.classops.urouter.plugin.transform.RouterRegister
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.compile.JavaCompile
import java.lang.reflect.Modifier

class AGP4RouterRegister(private val project: Project) : RouterRegister {

    private val extension = project.extensions.getByType(AppExtension::class.java)
    private val logger: Logger
        get() = project.logger
    private val transform = RouterTransformImpl4(logger)

    init {
        logger.log(LogLevel.INFO, "use agp4 api")
    }

    override fun registerTransform() {
        extension.registerTransform(transform)
        project.extensions
        project.tasks.withType(JavaCompile::class.java) {
            it.options
        }
    }

    override val pluginVersion: String?
        get() {
            try {
                val clazz = Class.forName("com.android.Version")
                for (f in clazz.fields) {
                    if ("ANDROID_GRADLE_PLUGIN_VERSION" == f.name && Modifier.isStatic(f.modifiers)) {
                        return f[clazz] as? String?
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
}