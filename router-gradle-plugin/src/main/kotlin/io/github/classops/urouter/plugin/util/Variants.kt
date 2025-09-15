package io.github.classops.urouter.plugin.util

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Component
import com.android.build.api.variant.HasAndroidTest
//import com.android.build.api.variant.HasUnitTest
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestExtension

/**
 * Invokes the [block] function for each Android variant, including android instrumentation tests
 * and host unit tests.
 */
internal fun AndroidComponentsExtension<*, *, *>.onAllVariants(block: (Component) -> Unit) {
  this.onVariants { variant ->
    block(variant)
//    (variant as? HasUnitTest)?.unitTest?.let { block(it) }
    (variant as? HasAndroidTest)?.androidTest?.let { block(it) }
  }
}

/**
 * Invokes the [block] function for each Android variant that is considered a Hilt root, where
 * dependencies are aggregated and components are generated.
 */
internal fun BaseExtension.forEachRootVariant(
  @Suppress("DEPRECATION") block: (variant: com.android.build.gradle.api.BaseVariant) -> Unit
) {
  when (this) {
    is AppExtension -> {
      // For an app project we configure the app variant and both androidTest and unitTest
      // variants, Hilt components are generated in all of them.
      applicationVariants.all { block(it) }
      testVariants.all { block(it) }
      unitTestVariants.all { block(it) }
    }
    is LibraryExtension -> {
      // For a library project, only the androidTest and unitTest variant are configured since
      // Hilt components are not generated in a library.
      testVariants.all { block(it) }
      unitTestVariants.all { block(it) }
    }
    is TestExtension -> {
      applicationVariants.all { block(it) }
    }
    else -> error("Hilt plugin does not know how to configure '$this'")
  }
}
