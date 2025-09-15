package io.github.classops.urouter.plugin.util

@Suppress("DEPRECATION") // Older variant API is deprecated
internal fun getKaptConfigName(variant: com.android.build.gradle.api.BaseVariant)
  = getConfigName(variant, "kapt")

@Suppress("DEPRECATION") // Older variant API is deprecated
internal fun getKspConfigName(variant: com.android.build.gradle.api.BaseVariant)
  = getConfigName(variant, "ksp")

@Suppress("DEPRECATION") // Older variant API is deprecated
internal fun getConfigName(
  variant: com.android.build.gradle.api.BaseVariant,
  prefix: String
): String {
  // Config names don't follow the usual task name conventions:
  // <Variant Name>   -> <Config Name>
  // debug            -> <prefix>Debug
  // debugAndroidTest -> <prefix>AndroidTestDebug
  // debugUnitTest    -> <prefix>TestDebug
  // release          -> <prefix>Release
  // releaseUnitTest  -> <prefix>TestRelease
  return when (variant) {
    is com.android.build.gradle.api.TestVariant ->
      "${prefix}AndroidTest${variant.name.substringBeforeLast("AndroidTest").capitalize()}"
    is com.android.build.gradle.api.UnitTestVariant ->
      "${prefix}Test${variant.name.substringBeforeLast("UnitTest").capitalize()}"
    else ->
      "${prefix}${variant.name.capitalize()}"
  }
}