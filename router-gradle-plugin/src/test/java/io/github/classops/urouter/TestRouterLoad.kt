package io.github.classops.urouter

import io.github.classops.urouter.plugin.ROUTER_PKG
import io.github.classops.urouter.plugin.ROUTER_PKG_PATH
import io.github.classops.urouter.plugin.transform.RouterInitGen
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.io.File

class TestRouterLoad {

    @Test
    fun testInitClass() {
        val classes = arrayListOf(
                "$ROUTER_PKG.TestUriInjector",
                "$ROUTER_PKG.route.IRouteTable",
        )
        val out = RouterInitGen.addRouteClasses(classes)

        val f = File("./rouder.class")
        f.outputStream().use {
            it.write(out)
        }
        println("file: ${f.canonicalPath}")
    }

    @Test
    fun testModifyClasses() {
        val classes = arrayListOf(
            "$ROUTER_PKG.TestUriInjector",
        )
        val deleteClasses = arrayListOf(
            "$ROUTER_PKG.route.IRouteTable",
        )

        val f = File("./rouder.class")

        val out = RouterInitGen.modifyRouteClasses(
            f.inputStream(),
            classes,
            deleteClasses
        )
        f.outputStream().use {
            it.write(out)
        }

        println("file: ${f.canonicalPath}")
    }

    fun testGenClass() {

        val classes = arrayListOf(
                "$ROUTER_PKG.TestUriInjector",
                "$ROUTER_PKG.route.IRouteTable",
        )

        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        val cv = object : ClassVisitor(Opcodes.ASM5, cw) {

        }
        cv.visit(
                Opcodes.V1_6,
                Opcodes.ACC_PUBLIC,
                "$ROUTER_PKG_PATH/ServiceLoader",
                null,
                "java/lang/Object",
                null
        )

        val mv = cv.visitMethod(
                Opcodes.ACC_PUBLIC,
                "load",
                "(L${ROUTER_PKG_PATH}/Router;)V",
                null,
                null
        )

        mv.visitCode()


//        mv.visitMethodInsn(
//                Opcodes.INVOKESTATIC,
//                "${ROUTER_PKG_PATH}/Router",
//                "get",
//                "()L${ROUTER_PKG_PATH}/Router;",
//                false
//        )
//        mv.visitVarInsn(Opcodes.ASTORE, 0)

        /*
        mv.visitTypeInsn(
                Opcodes.NEW,
                "java/lang/String"
        )
        mv.visitInsn(Opcodes.DUP)
        mv.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                "java/lang/String",
                "<init>",
                "()V",
                false
        )
        */

        for (s in classes) {
            registerTable(mv, s)
        }

//        mv.visitVarInsn(Opcodes.ALOAD, 0)
//        mv.visitInsn(Opcodes.POP)

//        for (i in 0..10) {
//            mv.visitTypeInsn(
//                Opcodes.NEW,
//                "java/lang/Integer"
//            )
//            mv.visitMethodInsn(
//                Opcodes.INVOKESPECIAL,
//                "${ROUTER_PKG_PATH}/Router",
//                "register",
//                "(L${ROUTER_PKG_PATH}/IRouteTable;)V",
//                false
//            )
//        }

        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(100, 100)
        mv.visitEnd()
        cv.visitEnd()

        val f = File("./rouder.class")
//        val f = File.createTempFile("rouder_test", ".class")
        f.outputStream().use {
            it.write(cw.toByteArray())
        }
        println("file: ${f.canonicalPath}")

//        print("class: \n" + String(cw.toByteArray()))
    }

    private fun registerTable(mv: MethodVisitor, clazz: String) {
        mv.visitVarInsn(Opcodes.ALOAD, 1)
        mv.visitLdcInsn(clazz)
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "$ROUTER_PKG_PATH/Router",
                "register",
                "(Ljava/lang/String;)V",
                false
        )
    }

}