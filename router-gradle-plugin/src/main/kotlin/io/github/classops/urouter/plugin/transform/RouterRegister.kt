package io.github.classops.urouter.plugin.transform

interface RouterRegister {
    fun registerTransform()

    /**
     * 获取 AGP 的版本
     */
    val pluginVersion: String?
}