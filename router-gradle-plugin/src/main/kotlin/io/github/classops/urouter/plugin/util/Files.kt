package io.github.classops.urouter.plugin.util

import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/* Checks if a file is a .class file. */
fun File.isClassFile() = this.isFile && this.extension == "class"

/* Checks if a Zip entry is a .class file. */
fun ZipEntry.isClassFile() = !this.isDirectory && this.name.endsWith(".class")

/* Checks if a file is a .jar file. */
fun File.isJarFile() = this.isFile && this.extension == "jar"

/**
 * Get a sequence of files in a platform independent order from walking this
 * file/directory recursively.
 */
fun File.walkInPlatformIndependentOrder() = this.walkTopDown().sortedBy {
  it.toRelativeString(this).replace(File.separatorChar, '/')
}

/* Executes the given [block] function over each [ZipEntry] in this [ZipInputStream]. */
fun ZipInputStream.forEachZipEntry(block: (InputStream, ZipEntry) -> Unit) = use {
  var inputEntry = nextEntry
  while (inputEntry != null) {
    block(this, inputEntry)
    inputEntry = nextEntry
  }
}
