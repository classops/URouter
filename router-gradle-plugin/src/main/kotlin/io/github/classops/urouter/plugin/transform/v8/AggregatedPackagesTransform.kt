package io.github.classops.urouter.plugin.transform.v8

import io.github.classops.urouter.plugin.util.forEachZipEntry
import io.github.classops.urouter.plugin.util.isClassFile
import io.github.classops.urouter.plugin.util.isJarFile
import io.github.classops.urouter.plugin.util.walkInPlatformIndependentOrder
import org.gradle.api.artifacts.transform.CacheableTransform
import org.gradle.api.artifacts.transform.InputArtifact
import org.gradle.api.artifacts.transform.TransformAction
import org.gradle.api.artifacts.transform.TransformOutputs
import org.gradle.api.artifacts.transform.TransformParameters
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Classpath
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * A transform that outputs classes and jars containing only classes in key aggregating Hilt
 * packages that are used to pass dependencies between compilation units.
 */
@CacheableTransform
abstract class AggregatedPackagesTransform : TransformAction<TransformParameters.None> {

    @get:Classpath
    @get:InputArtifact
    abstract val input: Provider<FileSystemLocation>

    override fun transform(outputs: TransformOutputs) {
        val input = input.get().asFile
        when {
            input.isFile -> transformFile(outputs, input)
            input.isDirectory -> input.walkInPlatformIndependentOrder().filter { it.isFile }.forEach {
                transformFile(outputs, it)
            }
            else -> error("File/directory does not exist: ${input.absolutePath}")
        }
//        println("URouter aggregate packages time: ${costTime}ms")
    }

    private fun transformFile(outputs: TransformOutputs, file: File) {
        if (file.isJarFile()) {
            var atLeastOneEntry = false
            val tmpOutputStream = ByteArrayOutputStream()
            ZipOutputStream(tmpOutputStream).use { outputStream ->
                ZipInputStream(file.inputStream()).forEachZipEntry { inputStream, inputEntry ->
                    if (inputEntry.isClassFile()) {
                        val parentDirectory = inputEntry.name.substringBeforeLast('/')

                        // URouter生成类都在 ROUTER_PACKAGE 目录
                        val match = parentDirectory.endsWith(RouterAnnotation.ROUTER_PACKAGE)
                        if (match) {
                            outputStream.putNextEntry(ZipEntry(inputEntry.name))
                            inputStream.copyTo(outputStream)
                            outputStream.closeEntry()
                            atLeastOneEntry = true
                        }
                    }
                }
            }
            if (atLeastOneEntry) {
                outputs.file(JAR_NAME).outputStream().use { tmpOutputStream.writeTo(it) }
            }
        } else if (file.isClassFile()) {
            // If transforming a file, check if the parent directory matches one of the known aggregated
            // packages structure. File and Path APIs are used to avoid OS-specific issues when comparing
            // paths.
            val parentDirectory: File = file.parentFile

            // URouter生成类都在 ROUTER_PACKAGE 目录
            val match = parentDirectory.endsWith(RouterAnnotation.ROUTER_PACKAGE)
            if (match) {
                outputs.file(file)
            }
        }
    }

    companion object {
        // The output file name containing classes in the aggregated packages.
        const val JAR_NAME = "routerAggregated.jar"
    }
}
