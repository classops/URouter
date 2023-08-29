package io.github.classops.urouter

import org.apache.maven.artifact.versioning.ComparableVersion
import org.gradle.internal.impldep.org.junit.Assert
import org.junit.jupiter.api.Test
import java.lang.module.ModuleDescriptor

class TestVersion {
    @Test
    fun testAGPVersion() {
        try {
            val currVersion = ModuleDescriptor.Version.parse("4.2.1")
            val compVersion = ModuleDescriptor.Version.parse("7.2.0")
            Assert.assertTrue("4.2.1 >= 7.2.0", currVersion >= compVersion)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            true
        }
    }

    @Test
    fun testVersion() {
        val version = ModuleDescriptor.Version.parse("1.1.0")
        val version2 = ModuleDescriptor.Version.parse("1.2.0")
        val version3 = ModuleDescriptor.Version.parse("1.0.1-SNAPSHOT")
        val version4 = ModuleDescriptor.Version.parse("0.9")
        val version5 = ModuleDescriptor.Version.parse("2.1.0-ALPHA")
        Assert.assertTrue(version.compareTo(version2) < 0)
        Assert.assertTrue(version.compareTo(version3) > 0)
        Assert.assertTrue(version.compareTo(version4) > 0)
        Assert.assertTrue(version.compareTo(version5) < 0)
    }

    @Test
    fun testVersion2() {
        val version = ComparableVersion("1.1.0")
        val version2 = ComparableVersion("1.2.0")
        val version3 = ComparableVersion("1.0.1-SNAPSHOT")
        val version4 = ComparableVersion("0.9")
        val version5 = ComparableVersion("2.1.0-ALPHA")
        Assert.assertTrue(version.compareTo(version2) < 0)
        Assert.assertTrue(version.compareTo(version3) > 0)
        Assert.assertTrue(version.compareTo(version4) > 0)
        Assert.assertTrue(version.compareTo(version5) < 0)
    }
}