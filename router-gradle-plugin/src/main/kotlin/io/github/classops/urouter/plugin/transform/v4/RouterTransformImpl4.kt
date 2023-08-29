package io.github.classops.urouter.plugin.transform.v4

import com.android.build.api.transform.*
import io.github.classops.urouter.plugin.GENERATED_ROUTE
import io.github.classops.urouter.plugin.PLUGIN_NAME
import io.github.classops.urouter.plugin.ROUTER_PKG_PATH
import io.github.classops.urouter.plugin.asm.RouterClassVisitor
import io.github.classops.urouter.plugin.transform.RouterInitGen
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class RouterTransformImpl4 : BaseTransform() {

    private val classes = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())
    private val deletedClasses = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())

    override fun getName(): String {
        return PLUGIN_NAME
    }

    override fun classFilter(name: String): Boolean {
        return name.startsWith(GENERATED_ROUTE)
    }

    override fun transformClass(input: InputStream, output: OutputStream) {
        // 转换class文件
        val cr = ClassReader(input)
        val cw = ClassWriter(cr, ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        cr.accept(RouterClassVisitor(Opcodes.ASM5, cw), ClassReader.EXPAND_FRAMES)
        try {
            output.write(cw.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onClassAdd(className: String) {
        this.classes.add(className)
    }

    override fun onClassDelete(className: String) {
        this.deletedClasses.add(className)
    }

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
            println("class: $s")
        }
        for (s in this.deletedClasses) {
            println("deleted class: $s")
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
            println("generate incremental route init class.")

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
            println("generate route init class.")
            val out = RouterInitGen.addRouteClasses(
                classes.filter { it.startsWith(GENERATED_ROUTE) }
                    .map { it.removeSuffix(".class").replace("/", ".") }
            )
            file.outputStream().use { os ->
                os.write(out)
            }
        }
    }

}