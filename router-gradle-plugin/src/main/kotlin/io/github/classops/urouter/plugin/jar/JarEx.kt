package io.github.classops.urouter.plugin.jar

import io.github.classops.urouter.plugin.CLASS_SUFFIX
import java.util.jar.JarFile

/**
 * 返回 com/xx/xx.class 格式的名
 */
fun JarFile.getClasses(): Set<String> {
    val set = HashSet<String>()
    for (entry in this.entries()) {
        val name = entry.name
        if (!entry.isDirectory && name.endsWith(CLASS_SUFFIX)) {
            set.add(name)
        }
    }
    return set
}