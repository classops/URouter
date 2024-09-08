package io.github.classops.urouter.plugin.asm

import io.github.classops.urouter.plugin.transform.RouterInitGen
import org.objectweb.asm.MethodVisitor

/**
 * RouteInit.load(Router) 加载路由类的增量修改
 *
 * 因为只有 router.register("xxx"); 方法，所以 忽略 所有，
 * 使用 visitLdcInsn 获取 “xxx”，直接插入或忽略
 */
class RouteMethodVisitor(
    api: Int,
    mv: MethodVisitor?,
    private val classes: List<String>,
    private val deletedClasses: List<String>
) :
    MethodVisitor(api, mv) {

    override fun visitCode() {
        super.visitCode()
        // class load
        for (clazz in classes) {
            RouterInitGen.registerTable(mv, clazz)
        }
    }

    override fun visitVarInsn(opcode: Int, varIndex: Int) {

    }

    override fun visitLdcInsn(value: Any?) {
        if (value is String && isAddClasses(value)) {
            RouterInitGen.registerTable(mv, value)
        }
    }

    private fun isAddClasses(clazz: String): Boolean {
        return !classes.contains(clazz) && !deletedClasses.contains(clazz)
    }

    override fun visitMethodInsn(
        opcode: Int,
        owner: String?,
        name: String?,
        descriptor: String?,
        isInterface: Boolean
    ) {

    }
}