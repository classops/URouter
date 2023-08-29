package io.github.classops.urouter

import io.github.classops.urouter.plugin.ROUTER_PKG_PATH
import io.github.classops.urouter.plugin.asm.RouteClassVisitor
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File

class TestGenClass {

    @Test
    fun testGenClass() {
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
            Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC,
            "load",
            "()V",
            null,
            null
        )

        mv.visitCode()


        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "$ROUTER_PKG_PATH/Router",
            "get",
            "()L${ROUTER_PKG_PATH}/Router",
            false
        )

//        mv.visitMethodInsn(
//            Opcodes.INVOKESTATIC,
//            "java/util/Calendar",
//            "getInstance",
//            "()Ljava/util/Calendar",
//            false
//        )
//        mv.visitInsn(Opcodes.DUP)
//        mv.visitInsn(Opcodes.POP)
        mv.visitVarInsn(Opcodes.ASTORE, 0)
//        mv.visitVarInsn(Opcodes.ALOAD, 1)

//        mv.visitVarInsn(Opcodes.ALOAD, 0)

        mv.visitVarInsn(Opcodes.ALOAD, 0)
//        mv.visitInsn(Opcodes.POP)

        mv.visitTypeInsn(
            Opcodes.NEW,
            "java/lang/Thread"
        )
        mv.visitInsn(Opcodes.DUP)
        mv.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            "java/lang/Thread",
            "<init>",
            "()V",
            false
        )

        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "$ROUTER_PKG_PATH/Router",
            "registerTable",
            "(Ljava/lang/Thread;)V",
            false
        )

        mv.visitVarInsn(Opcodes.ALOAD, 0)
//        mv.visitInsn(Opcodes.POP)

        mv.visitTypeInsn(
            Opcodes.NEW,
            "java/lang/Thread"
        )
        mv.visitInsn(Opcodes.DUP)
        mv.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            "java/lang/Thread",
            "<init>",
            "()V",
            false
        )

        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "$ROUTER_PKG_PATH/Router",
            "registerTable",
            "(Ljava/lang/Thread;)V",
            false
        )

//        for (i in 0..10) {
//            mv.visitTypeInsn(
//                Opcodes.NEW,
//                "java/lang/Integer"
//            )
//            mv.visitMethodInsn(
//                Opcodes.INVOKESPECIAL,
//                "${ROUTER_PKG_PATH}/Router",
//                "registerTable",
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


    @Test
    fun genRouteClass() {
        // 加载
        val f = File("./RouteInit.class")

        val classes = arrayListOf(
            "$ROUTER_PKG_PATH/TestUriInjector.class",
            "$ROUTER_PKG_PATH/route/IRouteTable.class",
        )

        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        val cv = object : ClassVisitor(Opcodes.ASM5, cw) {

        }

        cv.visit(
            Opcodes.V1_6,
            Opcodes.ACC_PUBLIC,
            "$ROUTER_PKG_PATH/init/RouteInit",
            null,
            "java/lang/Object",
            null
        )

        cv.visitMethod(
            Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC,
            "load",
            "()V",
            null,
            null
        )?.let { mv ->
            mv.visitCode()
            for (clazz in classes) {
                mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    clazz.removeSuffix(".class"),
                    "load",
                    "()V",
                    false
                )
            }
            // stacks
            mv.visitInsn(Opcodes.RETURN)
            mv.visitMaxs(4, 3)
            mv.visitEnd()
        }
        cv.visitEnd()

        f.outputStream().use {
            it.write(cw.toByteArray())
        }
    }

    @Test
    fun modifyClassTest() {
        val f = File("./RouteInit.class")

        val classes = setOf(
            "$ROUTER_PKG_PATH/DDD.class",
        ).map {
            it.removeSuffix(".class")
        }

        val deletedClasses = setOf(
            "$ROUTER_PKG_PATH/route/IRouteTable.class",
        ).map {
            it.removeSuffix(".class")
        }

        modifyClass(f, classes, deletedClasses)
    }

    private fun modifyClass(file: File, classes: List<String>, deletedClasses: List<String>) {
        // 修改类
        file.inputStream().use {
            val cr = ClassReader(it)
            val cw = ClassWriter(cr, ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
            val cv = RouteClassVisitor(Opcodes.ASM5, cw, classes, deletedClasses)
            cr.accept(cv, ClassReader.EXPAND_FRAMES)
            file.outputStream().use {
                it.write(cw.toByteArray())
            }
        }
    }

}