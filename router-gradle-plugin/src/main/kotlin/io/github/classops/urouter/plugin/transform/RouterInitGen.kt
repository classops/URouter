package io.github.classops.urouter.plugin.transform

import io.github.classops.urouter.plugin.ROUTER_CLASS
import io.github.classops.urouter.plugin.ROUTER_PKG_PATH
import io.github.classops.urouter.plugin.ROUTER_ROUTE_INIT
import io.github.classops.urouter.plugin.asm.RouteClassVisitor
import org.objectweb.asm.*
import java.io.InputStream

object RouterInitGen {

    /**
     * 生成Class文件
     *
     * @param classes com.xxx.xxx.XXX 形式，没有.class后缀
     */
    fun addRouteClasses(classes: List<String>): ByteArray {
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
        mv.visitMaxs(0, 0)
        mv.visitEnd()

        cv.visitEnd()

        return cw.toByteArray()
    }

    fun modifyRouteClasses(
        inputStream: InputStream,
        classes: List<String>,
        deletedClasses: List<String>
    ): ByteArray {
        // 修改类
        val cr = ClassReader(inputStream)
        val cw = ClassWriter(cr, ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        val cv = RouteClassVisitor(Opcodes.ASM5, cw, classes, deletedClasses)
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        return cw.toByteArray()
    }

    fun registerTable(mv: MethodVisitor, clazz: String) {
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