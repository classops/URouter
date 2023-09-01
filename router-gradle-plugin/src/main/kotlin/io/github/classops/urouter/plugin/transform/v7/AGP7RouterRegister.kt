package io.github.classops.urouter.plugin.transform.v7

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.api.variant.Variant
import io.github.classops.urouter.plugin.transform.RouterRegister
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import kotlin.math.log


class AGP7RouterRegister(private val project: Project) : RouterRegister {

    private val extension: AndroidComponentsExtension<*, *, *> = project.extensions.getByType(
            AndroidComponentsExtension::class.java
    )

    override val pluginVersion: String
        get() = extension.pluginVersion.toString()

    val logger: Logger
        get() = this.project.logger

    override fun registerTransform() {
        extension.onVariants(extension.selector().all()) { variant ->
            // 获取所有.class，输出 classes.jar
            transformClasses(variant)
        }
    }

    private fun transformClasses(variant: Variant) {
        val taskProvider = project.tasks.register("${variant.name}RouterClasses", RouterClassesTask::class.java)
        variant.artifacts.forScope(ScopedArtifacts.Scope.ALL)
            .use<RouterClassesTask>(taskProvider)
            .toTransform(
                ScopedArtifact.CLASSES,
                RouterClassesTask::allJars,
                RouterClassesTask::allDirectories,
                RouterClassesTask::output
            )
    }

}