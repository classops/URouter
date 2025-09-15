package io.github.classops.urouter.plugin.transform.v8

import org.gradle.api.artifacts.transform.InputArtifact
import org.gradle.api.artifacts.transform.TransformAction
import org.gradle.api.artifacts.transform.TransformOutputs
import org.gradle.api.artifacts.transform.TransformParameters
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Classpath
import org.gradle.work.DisableCachingByDefault
import kotlin.system.measureTimeMillis

/**
 * A transform that registers the input file (usually a jar or a class) as an output and thus
 * changing from one artifact type to another.
 */
@DisableCachingByDefault(because = "Copying files does not benefit from caching")
abstract class CopyTransform : TransformAction<TransformParameters.None> {
    @get:Classpath
    @get:InputArtifact
    abstract val inputArtifactProvider: Provider<FileSystemLocation>

    override fun transform(outputs: TransformOutputs) {
        val input = inputArtifactProvider.get().asFile
        when {
            input.isDirectory -> outputs.dir(input)
            input.isFile -> outputs.file(input)
            else -> {
                error("File/directory does not exist: ${input.absolutePath}")
            }
        }
    }
}
