package io.github.classops.urouter.plugin.transform.v7

import io.github.classops.urouter.plugin.GENERATED_ROUTE
import io.github.classops.urouter.plugin.ROUTER_CLASS
import io.github.classops.urouter.plugin.ROUTER_PKG_PATH
import io.github.classops.urouter.plugin.ROUTER_ROUTE_INIT
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.*
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
        println("RouterClassesTask")

        val classes = ArrayList<String>()

        val jarOutput = JarOutputStream(
            BufferedOutputStream(
                FileOutputStream(
                    output.get().asFile
                )
            )
        )
        println("jar out file: ${output.get().asFile}")

        allDirectories.get().forEach { directory ->
            println("dir handling " + directory.asFile.canonicalPath)
            directory.asFile.walk().filter(File::isFile).forEach { file ->
//                println("class file: ${file.canonicalPath}")

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
            println("jar handling " + file.asFile.canonicalPath)
            JarFile(file.asFile).use { jarFile ->
                jarFile.entries().iterator().forEach { jarEntry ->
                    if (!jarEntry.isDirectory && !jarEntry.name.startsWith(META_INF)) {
                        try {
                            jarOutput.putNextEntry(JarEntry(jarEntry.name))
                            if (jarEntry.name.startsWith(GENERATED_ROUTE)) {
                                // route class
                                val clazz = jarEntry.name.removeSuffix(".class")
                                    .replace("/", ".")
                                classes.add(clazz)
                            }
                            jarFile.getInputStream(jarEntry).use {
                                it.copyTo(jarOutput)
                            }

                            jarOutput.closeEntry()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        // 添加 RouteInit.class
        genTableClass(jarOutput, classes)

        jarOutput.close()

        println("output jar file: ${output.get().asFile}")
    }

    /**
     * 生成Class文件
     *
     * @param classes com/xxx/xxx/XXX 形式，没有.class后缀
     */
    private fun genTableClass(jarOutput: JarOutputStream, classes: List<String>) {
        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        val cv = object : ClassVisitor(Opcodes.ASM5, cw) {

        }
        cv.visit(
            Opcodes.V1_6,
            Opcodes.ACC_PUBLIC,
            ROUTER_ROUTE_INIT,
            null,
            "java/lang/Object",
            null
        )

        val mv = cv.visitMethod(
            Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC,
            "load",
            "(L$ROUTER_PKG_PATH/Router;)V",
            null,
            null
        )
        mv.visitCode()

        for (s in classes) {
            registerTable(mv, s)
        }

        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(100, 100)
        mv.visitEnd()
        cv.visitEnd()

        jarOutput.putNextEntry(JarEntry("$ROUTER_ROUTE_INIT.class"))
        jarOutput.write(cw.toByteArray())
    }

    private fun registerTable(mv: MethodVisitor, clazz: String) {
        mv.visitVarInsn(Opcodes.ALOAD, 0)
        mv.visitLdcInsn(clazz)
        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            ROUTER_CLASS.replace(".", "/"),
            "register",
            "(Ljava/lang/String;)V",
            false
        )
    }

}