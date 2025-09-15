package io.github.classops.urouter.plugin.transform.v4

import com.android.build.api.transform.*
import io.github.classops.urouter.plugin.GENERATED_ROUTE
import io.github.classops.urouter.plugin.PLUGIN_NAME
import io.github.classops.urouter.plugin.ROUTER_PKG_PATH
import io.github.classops.urouter.plugin.transform.RouterInitGen
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap


class RouterTransformImpl4(logger: Logger) : BaseTransform(logger) {

    private val classes = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())
    private val deletedClasses = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())

    override fun getName(): String {
        return PLUGIN_NAME
    }

    override fun classFilter(name: String): Boolean {
        return name.startsWith(GENERATED_ROUTE)
    }

    override fun onClassAdd(className: String) {
        this.classes.add(className)
    }

    override fun onClassDelete(className: String) {
        this.deletedClasses.add(className)
    }

    @Suppress("DEPRECATION")
    override fun transform(
        context: Context?,
        inputs: MutableCollection<TransformInput>,
        referencedInputs: MutableCollection<TransformInput>,
        outputProvider: TransformOutputProvider?,
        isIncremental: Boolean
    ) {
        super.transform(context, inputs, referencedInputs, outputProvider, isIncremental)
        // 列表处理
        for (s in this.classes) {
            logger.log(LogLevel.INFO, "class: $s")
        }
        for (s in this.deletedClasses) {
            logger.log(LogLevel.INFO, "deleted class: $s")
        }

        outputProvider ?: return

        val routerDir = outputProvider.getContentLocation(
            "router",
            setOf(QualifiedContent.DefaultContentType.CLASSES),
            mutableSetOf(QualifiedContent.Scope.PROJECT),
            Format.DIRECTORY
        )
        // full class
        val file = File(routerDir, "$ROUTER_PKG_PATH/init/RouteInit.class")

        // add/delete incremental class by asm
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }

        // 获取类文件
        if (isIncremental) {
            logger.log(LogLevel.INFO, "generate incremental route init class.")
            file.inputStream().use { fis ->
                val out = RouterInitGen.modifyRouteClasses(
                    fis,
                    classes.filter { it.startsWith(GENERATED_ROUTE) }
                        .map { it.removeSuffix(".class").replace("/", ".") },
                    deletedClasses.filter { it.startsWith(GENERATED_ROUTE) }
                        .map { it.removeSuffix(".class").replace("/", ".") }
                )

                file.outputStream().use { os ->
                    os.write(out)
                }
            }
        } else {
            logger.log(LogLevel.INFO, "generate route init class.")
            val out = RouterInitGen.createRouteInitClass(
                classes.filter { it.startsWith(GENERATED_ROUTE) }
                    .map { it.removeSuffix(".class").replace("/", ".") }
            )
            file.outputStream().use { os ->
                os.write(out)
            }
        }
    }

}