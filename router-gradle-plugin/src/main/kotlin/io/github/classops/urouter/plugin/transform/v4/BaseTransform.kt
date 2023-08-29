package io.github.classops.urouter.plugin.transform.v4

import com.android.build.api.transform.*
import com.android.build.api.transform.QualifiedContent.DefaultContentType
import io.github.classops.urouter.plugin.CLASS_SUFFIX
import io.github.classops.urouter.plugin.jar.getClasses
import java.io.*
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

@Suppress("DEPRECATION")
abstract class BaseTransform : Transform() {

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        // TransformManager.CONTENT_CLASS
        return mutableSetOf(DefaultContentType.CLASSES)
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        // TransformManager.SCOPE_FULL_PROJECT
        return mutableSetOf(
            QualifiedContent.Scope.PROJECT,
            QualifiedContent.Scope.SUB_PROJECTS,
            QualifiedContent.Scope.EXTERNAL_LIBRARIES
        )
    }

    override fun isIncremental(): Boolean {
        return true
    }

    override fun transform(
        context: Context?,
        inputs: MutableCollection<TransformInput>,
        referencedInputs: MutableCollection<TransformInput>,
        outputProvider: TransformOutputProvider?,
        isIncremental: Boolean
    ) {
        if (!isIncremental) {
            outputProvider?.deleteAll()
        }

        outputProvider ?: return

        for (input in inputs) {
            // jar
            for (jarInput in input.jarInputs) {
                val inputJar = jarInput.file
                val outputJar = outputProvider.getContentLocation(
                    jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.JAR
                )
                if (isIncremental) {
                    when (jarInput.status) {
                        Status.ADDED -> {
                            // transform jar
                            transformJar(inputJar, outputJar)
                        }

                        Status.CHANGED -> {
                            // scan old and new class, diff
                            val oldClasses = JarFile(outputJar).use {
                                it.getClasses()
                            }
                            val newClasses = JarFile(inputJar).use {
                                it.getClasses()
                            }
                            // class diff
                            oldClasses.subtract(newClasses).filter {
                                classFilter(it)
                            }.forEach { className ->
                                onClassDelete(className)
                            }
                            if (outputJar.exists()) {
                                outputJar.delete()
                            }
                            // transform jar
                            transformJar(inputJar, outputJar)
                        }

                        Status.REMOVED -> {
                            // delete file
                            JarFile(outputJar).use {
                                it.getClasses().filter {
                                    classFilter(it)
                                }.forEach { className ->
                                    onClassDelete(className)
                                }
                            }
                            outputJar.delete()
                        }

                        else -> {
                            // do nothing
                        }
                    }
                } else {
                    // transform class
                    transformJar(inputJar, outputJar)
                }
            }
            // directory
            for (directoryInput in input.directoryInputs) {
                val outputDir = outputProvider.getContentLocation(
                    directoryInput.name,
                    directoryInput.contentTypes,
                    directoryInput.scopes,
                    Format.DIRECTORY
                )

                if (isIncremental) {
                    for ((file, status) in directoryInput.changedFiles) {
                        // 文件状态
                        when (status) {
                            Status.ADDED -> {
                                // transform class
                                val classFile = getOutputFile(outputDir, directoryInput.file, file)
                                transformClassFile(file, classFile)
                            }

                            Status.CHANGED -> {
                                // transform class
                                val output = getOutputFile(outputDir, directoryInput.file, file)
                                if (output.file.exists()) {
                                    output.file.delete()
                                }
                                transformClassFile(file, output)
                            }

                            Status.REMOVED -> {
                                // delete file
                                val classFile = getOutputFile(outputDir, directoryInput.file, file)
                                onClassDelete(classFile.className)
                                classFile.file.delete()
                            }

                            else -> {
                                // do nothing
                            }
                        }
                    }
                } else {
                    // AGP低版本 stdlib walk 方法有兼容问题
                    walk(directoryInput.file) { file ->
                        // output file
                        val classFile = getOutputFile(outputDir, directoryInput.file, file)
                        transformClassFile(file, classFile)
                    }
                }
            }
        }
    }

     private fun walk(dir: File, callback: (File) -> Unit) {
        dir.listFiles()?.forEach {
            if (it.isFile) {
                callback.invoke(it)
            } else {
                walk(it, callback)
            }
        }
    }

    protected fun getOutputFile(outputDir: File, inputBaseDir: File, input: File): ClassFile {
        val dest = inputBaseDir.toURI().relativize(input.toURI()).path
        return ClassFile(
            dest,
            File(outputDir, dest)
        )
    }

    protected open fun classFilter(name: String): Boolean {
        return name.endsWith(CLASS_SUFFIX)
    }

    protected fun transformJar(input: File, output: File) {
        JarFile(input).use { jarFile ->
            JarOutputStream(FileOutputStream(output)).use { jos ->
                for (entry in jarFile.entries()) {
                    if (!entry.isDirectory && isValidZipEntryName(entry)) {
                        jos.putNextEntry(ZipEntry(entry.name))
                        jarFile.getInputStream(entry).use { eis ->
                            if (classFilter(entry.name)) {
                                onClassAdd(entry.name)
                                transformClass(eis, jos)
                            } else {
                                eis.copyTo(jos)
                            }
                        }
                        jos.closeEntry()
                    }
                }
            }
        }
    }

    /**
     * class文件转换
     */
    protected fun transformClassFile(input: File, output: ClassFile) {
        output.file.parentFile?.let { dir ->
            if (!dir.exists()) {
                dir.mkdirs()
            }
        }
        if (classFilter(output.className)) {
            onClassAdd(output.className)
            transformClass(input, output.file)
        } else {
            input.copyTo(output.file)
        }
    }

    protected fun transformClass(inputClass: File, outputClass: File) {
        FileInputStream(inputClass).use {
            FileOutputStream(outputClass).use { os ->
                transformClass(it, os)
            }
        }
    }

    abstract fun transformClass(input: InputStream, output: OutputStream)

    protected open fun onClassAdd(className: String) {
        println("on class add: $className")
    }

    protected open fun onClassDelete(className: String) {
        println("on class delete: $className")
    }

    private fun isValidZipEntryName(entry: ZipEntry): Boolean {
        return !entry.name.contains("../")
    }

    /**
     * 保存class名和file
     *
     * @param className  com/xxx/xxx.class 格式
     */
    data class ClassFile(val className: String, val file: File)

}