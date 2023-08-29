package io.github.classops.urouter.plugin.asm

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class RouterMethodVisitor(api: Int, mv: MethodVisitor?) : MethodVisitor(api, mv) {

    override fun visitInsn(opcode: Int) {
        if (opcode in Opcodes.IRETURN..Opcodes.RETURN) {
            /*
            for (routeClass in RouterManager.instance.routeClasses) {
                val clazz = routeClass.replace("/", ".")
                println("add $clazz")
                mv.visitVarInsn(Opcodes.ALOAD, 0)
                mv.visitTypeInsn(Opcodes.NEW, routeClass)
                mv.visitInsn(Opcodes.DUP)
                mv.visitMethodInsn(
                        Opcodes.INVOKESPECIAL,
                        routeClass,
                        "<init>",
                        "()V",
                        false
                )
//                mv.visitTypeInsn(Opcodes.CHECKCAST, ROUTE_TABLE_INTERFACE)
//                mv.visitLdcInsn(clazz)
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        ROUTER_CLASS.replace(".", "/"),
                        ROUTER_METHOD_REGISTER_TABLE,
                        "(L${ROUTE_TABLE_INTERFACE};)V",
                        false
                )
            }
             */
        }
        super.visitInsn(opcode)
    }

    override fun visitMaxs(maxStack: Int, maxLocals: Int) {
        super.visitMaxs(maxStack + 4, maxLocals)
    }
}