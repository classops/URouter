package io.github.classops.urouter.plugin.asm

import io.github.classops.urouter.plugin.ROUTER_METHOD_LOAD_ROUTE
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

/**
 * Router
 * 完成字节码逻辑
 */
class RouterClassVisitor : ClassVisitor {

    constructor(api: Int) : super(api)

    constructor(api: Int, classVisitor: ClassVisitor?) : super(api, classVisitor)

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String?,
        signature: String?,
        exceptions: Array<String>?
    ): MethodVisitor? {
        val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (name == ROUTER_METHOD_LOAD_ROUTE) {
            println("visit loadRoute method")
            return RouterMethodVisitor(api, mv)
        }
        return mv
    }
}