package io.github.classops.urouter.plugin.transform.v8.task

import io.github.classops.urouter.plugin.ROUTER_PKG_PATH
import io.github.classops.urouter.plugin.transform.RouterInitGen
import io.github.classops.urouter.plugin.util.forEachZipEntry
import io.github.classops.urouter.plugin.util.isClassFile
import io.github.classops.urouter.plugin.util.isJarFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.InputChanges
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.InputStream
import java.util.zip.ZipInputStream

/**
 * 从 classpath 收集所有 路由类，生成 [io/github/classops/urouter/init/RouteInit.class]
 */
@CacheableTask
abstract class AggregateDepsTask : DefaultTask() {

    @get:Classpath
    abstract val compileClasspath: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    private val classVisitor = RouterClassVisitor(logger)

    private val classes: List<String>
        get() = classVisitor.classes

    @TaskAction
    internal fun taskAction(@Suppress("UNUSED_PARAMETER") inputs: InputChanges) {
        compileClasspath.forEach {
            if (it.isDirectory) {
                it.listFiles()?.forEach {
                    logger.info("aggregate compileClasspath: {}", it.absolutePath)
                }
            } else {
                logger.info("aggregate compileClasspath: {}", it.absolutePath)
            }
        }

        process(compileClasspath)
        genRouterInitClass()
    }

    private fun process(files: Iterable<File>) {
        files.forEach { file ->
            when {
                file.isFile -> visitFile(file)
                file.isDirectory -> file.walkTopDown().filter { it.isFile }.forEach { visitFile(it) }
                else -> logger.warn("Can't process file/directory that doesn't exist: $file")
            }
        }
    }

    private fun visitFile(file: File) {
        when {
            file.isJarFile() -> {
                ZipInputStream(file.inputStream()).forEachZipEntry { inputStream, entry ->
                    logger.info("URouter jar entry: {}", entry.name)
                    if (entry.isClassFile()) {
                        visitClass(inputStream)
                    }
                }
            }

            file.isClassFile() -> {
                logger.info("URouter class: {}", file)
                file.inputStream().use { visitClass(it) }
            }

            else -> logger.debug("Don't know how to process file: {}", file)
        }
    }

    private fun visitClass(classFileInputStream: InputStream) {
        ClassReader(classFileInputStream)
            .accept(
                classVisitor,
                ClassReader.SKIP_CODE and ClassReader.SKIP_DEBUG and ClassReader.SKIP_FRAMES
            )
    }

    /**
     * 收集路由类
     */
    private class RouterClassVisitor(private val logger: Logger) : ClassVisitor(Opcodes.ASM9) {

        val classes = mutableListOf<String>()

        override fun visit(
            version: Int,
            access: Int,
            name: String?,
            signature: String?,
            superName: String?,
            interfaces: Array<out String>?
        ) {
            super.visit(version, access, name, signature, superName, interfaces)
            // AggregatedPackagesTransform的jar 只包含 路由类，无需其他判断
            if (name != null) {
                logger.info("AggregateDepsTask visit class: {}", name)
                classes.add(name.replace("/", "."))
            }
        }

    }

    /**
     * 生成 io/github/classops/urouter/init/RouteInit.class
     */
    private fun genRouterInitClass() {
        outputDir.file("$ROUTER_PKG_PATH/init/RouteInit.class").get().asFile.apply {
            parentFile.mkdirs()
            writeBytes(
                RouterInitGen.createRouteInitClass(classes.toSet().toList())
            )
        }
    }

}