package io.github.classops.urouter.plugin.asm

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

/**
 * RouteInit类 加载路由类的增量修改
 */
class RouteClassVisitor(api: Int, cv: ClassVisitor?, private val classes: List<String>, private val deletedClasses: List<String>) :
    ClassVisitor(api, cv) {

    override fun visitMethod(
        access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?
    ): MethodVisitor? {
        val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (name == "load") {
            return RouteMethodVisitor(api, mv, classes, deletedClasses)
        }
        return mv
    }
}