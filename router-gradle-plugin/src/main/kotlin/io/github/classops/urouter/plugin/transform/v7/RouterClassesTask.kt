package io.github.classops.urouter.plugin.transform.v7

import io.github.classops.urouter.plugin.GENERATED_ROUTE
import io.github.classops.urouter.plugin.ROUTER_ROUTE_INIT
import io.github.classops.urouter.plugin.transform.RouterInitGen
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

/**
 * 生成路由类
 */
abstract class RouterClassesTask : DefaultTask() {

    companion object {
        private const val META_INF = "META-INF/"
    }

    @get:InputFiles
    abstract val allJars: ListProperty<RegularFile>

    @get:InputFiles
    abstract val allDirectories: ListProperty<Directory>

    @get:OutputFiles
    abstract val output: RegularFileProperty

    @TaskAction
    fun taskAction() {
        val classes = ArrayList<String>()
        val jarOutput = JarOutputStream(
            BufferedOutputStream(
                FileOutputStream(
                    output.get().asFile
                )
            )
        )
        logger.log(LogLevel.INFO, "jar out file: ${output.get().asFile}")
        allDirectories.get().forEach { directory ->
            logger.log(LogLevel.INFO, "process class dir: ${directory.asFile.canonicalPath}")
            directory.asFile.walk().filter(File::isFile).forEach { file ->
                logger.log(LogLevel.INFO, "process class file: ${file.canonicalPath}")
                val entryName = directory.asFile.toURI().relativize(file.toURI()).path
                    .replace(File.separatorChar, '/')
                jarOutput.putNextEntry(JarEntry(entryName))
                try {
                    if (entryName.startsWith(GENERATED_ROUTE)) {
                        file.inputStream().use { inputStream ->
                            inputStream.copyTo(jarOutput)
                        }
                        // route class
                        val clazz = entryName.removeSuffix(".class")
                            .replace("/", ".")
                        classes.add(clazz)
                    } else {
                        file.inputStream().use { inputStream ->
                            inputStream.copyTo(jarOutput)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    jarOutput.closeEntry()
                }
            }
        }

        allJars.get().forEach { file ->
            logger.log(LogLevel.INFO, "process jar: ${file.asFile.canonicalPath}")
            JarFile(file.asFile).use { jarFile ->
                jarFile.entries().iterator().forEach { jarEntry ->
                    if (!jarEntry.isDirectory && !jarEntry.name.startsWith(META_INF)) {
                        jarOutput.putNextEntry(JarEntry(jarEntry.name))
                        try {
                            if (jarEntry.name.startsWith(GENERATED_ROUTE)) {
                                // route class
                                val clazz = jarEntry.name.removeSuffix(".class")
                                    .replace("/", ".")
                                classes.add(clazz)
                            }
                            jarFile.getInputStream(jarEntry).use {
                                it.copyTo(jarOutput)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            jarOutput.closeEntry()
                        }
                    }
                }
            }
        }

        // 添加 RouteInit.class
        genTableClass(jarOutput, classes)

        jarOutput.close()

        logger.log(LogLevel.LIFECYCLE, "output jar file: ${output.get().asFile}")
    }

    /**
     * 生成 RouteInit.class
     *
     * @param classes com/xxx/xxx/XXX 形式，没有.class后缀
     */
    private fun genTableClass(jarOutput: JarOutputStream, classes: List<String>) {
        jarOutput.putNextEntry(JarEntry("$ROUTER_ROUTE_INIT.class"))
        jarOutput.write(
            RouterInitGen.addRouteClasses(classes)
        )
    }

}