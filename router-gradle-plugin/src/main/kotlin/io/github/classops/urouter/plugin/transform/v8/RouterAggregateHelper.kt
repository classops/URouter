package io.github.classops.urouter.plugin.transform.v8

import com.android.build.gradle.BaseExtension
import io.github.classops.urouter.plugin.transform.v8.task.AggregateDepsTask
import io.github.classops.urouter.plugin.util.capitalize
import io.github.classops.urouter.plugin.util.forEachRootVariant
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.type.ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE
import org.gradle.configurationcache.extensions.capitalized

class RouterAggregateHelper {

    companion object {
        const val ROUTER_ARTIFACT_TYPE_VALUE = "jar-for-router"
        const val AGGREGATED_ARTIFACT_TYPE_VALUE = "aggregated-jar-for-router"

        private fun Project.isGradleSyncRunning() =
            gradleSyncProps.any { property ->
                providers.gradleProperty(property).map { it.toBoolean() }.orElse(false).get()
            }

        private val gradleSyncProps by lazy {
            listOf(
                "android.injected.build.model.v2",
                "android.injected.build.model.only",
                "android.injected.build.model.only.advanced",
            )
        }
    }

    fun registerAggregateTransform(project: Project) {
        // 复制 RouteInit 相关类
        project.dependencies.registerTransform(CopyTransform::class.java) { spec ->
//            spec.from.attribute(ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
            spec.from.attribute(ARTIFACT_TYPE_ATTRIBUTE, "jar")
            spec.from.attribute(ARTIFACT_TYPE_ATTRIBUTE, "android-classes")
            spec.to.attribute(ARTIFACT_TYPE_ATTRIBUTE, ROUTER_ARTIFACT_TYPE_VALUE)
        }

        project.dependencies.registerTransform(CopyTransform::class.java) { spec ->
//            spec.from.attribute(ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.DIRECTORY_TYPE)
            spec.from.attribute(ARTIFACT_TYPE_ATTRIBUTE, "directory")
            spec.to.attribute(ARTIFACT_TYPE_ATTRIBUTE, ROUTER_ARTIFACT_TYPE_VALUE)
        }

        // 收集
        project.dependencies.registerTransform(AggregatedPackagesTransform::class.java) { spec ->
            spec.from.attribute(ARTIFACT_TYPE_ATTRIBUTE, ROUTER_ARTIFACT_TYPE_VALUE)
            spec.to.attribute(ARTIFACT_TYPE_ATTRIBUTE, AGGREGATED_ARTIFACT_TYPE_VALUE)
        }
    }

    fun configureAggregatingTask(project: Project) {
        val androidExtension =
            project.extensions.findByType(BaseExtension::class.java)
                ?: error("Android BaseExtension not found.")
        androidExtension.forEachRootVariant { variant ->
            configureVariantAggregatingTask(project, androidExtension, variant)
        }
    }

    private fun configureVariantAggregatingTask(
        project: Project,
        androidExtension: BaseExtension,
        @Suppress("DEPRECATION") variant: com.android.build.gradle.api.BaseVariant
    ) {
        if (project.isGradleSyncRunning()) {
            return
        }

        val routerCompileConfiguration =
            project.configurations.create("routerCompileOnly${variant.name.capitalize()}")
                .apply {
                    description =
                        "Router aggregated compile only dependencies for '${variant.name}'"
                    isCanBeConsumed = false
                    isCanBeResolved = true
                    isVisible = false
                }

        project.dependencies.add(
            routerCompileConfiguration.name,
            project.files(variant.javaCompileProvider.map { it.classpath }),
        )
        project.dependencies.add(
            routerCompileConfiguration.name,
            project.files(variant.javaCompileProvider.map {
                it.destinationDirectory.get()
            }),
        )

        fun getInputClasspath(artifactAttributeValue: String) =
            buildList<Configuration> {
                @Suppress("DEPRECATION") // Older variant API is deprecated
                if (variant is com.android.build.gradle.api.TestVariant) {
                    add(variant.testedVariant.runtimeConfiguration)
                }
                add(variant.runtimeConfiguration)
                add(routerCompileConfiguration)
            }.map { configuration ->
                configuration.incoming
                    .artifactView { view ->
                        view.attributes.attribute(ARTIFACT_TYPE_ATTRIBUTE, artifactAttributeValue)
                    }.files
            }.let { project.files(*it.toTypedArray()) }

        // 收集 route 信息
        val aggregatingTask = project.tasks.register(
            "routerAggregate${variant.name.capitalized()}Task",
            AggregateDepsTask::class.java,
        ) {
            it.compileClasspath.setFrom(getInputClasspath(AGGREGATED_ARTIFACT_TYPE_VALUE))
            @Suppress("DEPRECATION")
            it.outputDir.set(
                project.file(project.buildDir.resolve("intermediates/urouter/component_classes/${variant.name}/"))
            )
        }

        @Suppress("DEPRECATION")
        val componentClasses = project.files(
            project.buildDir.resolve("intermediates/urouter/component_classes/${variant.name}/")
        )

        componentClasses.builtBy(aggregatingTask)
        variant.registerPostJavacGeneratedBytecode(componentClasses)
    }

    fun configureCompileClasspath(project: Project) {
        val androidExtension =
            project.extensions.findByType(BaseExtension::class.java)
                ?: error("Android BaseExtension not found.")
        androidExtension.forEachRootVariant { variant ->
            configureVariantCompileClasspath(project, variant)
        }
    }

    private fun configureVariantCompileClasspath(
        project: Project,
        @Suppress("DEPRECATION") variant: com.android.build.gradle.api.BaseVariant,
    ) {
        @Suppress("DEPRECATION") // Older variant API is deprecated
        val runtimeConfiguration =
            if (variant is com.android.build.gradle.api.TestVariant) {
                // For Android test variants, the tested runtime classpath is used since the test app has
                // tested dependencies removed.
                variant.testedVariant.runtimeConfiguration
            } else {
                variant.runtimeConfiguration
            }
        val artifactView =
            runtimeConfiguration.incoming.artifactView { view ->
                view.attributes.attribute(ARTIFACT_TYPE_ATTRIBUTE, ROUTER_ARTIFACT_TYPE_VALUE)
                view.componentFilter { identifier ->
                    // Filter out the project's classes from the aggregated view since this can cause
                    // issues with Kotlin internal members visibility. b/178230629
                    if (identifier is ProjectComponentIdentifier) {
                        identifier.projectName != project.name
                    } else {
                        true
                    }
                }
            }

        // CompileOnly config names don't follow the usual convention:
        // <Variant Name>   -> <Config Name>
        // debug            -> debugCompileOnly
        // debugAndroidTest -> androidTestDebugCompileOnly
        // debugUnitTest    -> testDebugCompileOnly
        // release          -> releaseCompileOnly
        // releaseUnitTest  -> testReleaseCompileOnly
        @Suppress("DEPRECATION") // Older variant API is deprecated
        val compileOnlyConfigName =
            when (variant) {
                is com.android.build.gradle.api.TestVariant ->
                    "androidTest${variant.name.substringBeforeLast("AndroidTest").capitalize()}CompileOnly"

                is com.android.build.gradle.api.UnitTestVariant ->
                    "test${variant.name.substringBeforeLast("UnitTest").capitalize()}CompileOnly"

                else -> "${variant.name}CompileOnly"
            }
        project.dependencies.add(compileOnlyConfigName, artifactView.files)
    }

}