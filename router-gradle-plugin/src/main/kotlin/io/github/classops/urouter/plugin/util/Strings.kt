package io.github.classops.urouter.plugin.util

import java.util.Locale

fun String.capitalize(
  locale: Locale = Locale.getDefault()
): String = if (isNotEmpty() && this[0].isLowerCase()) {
  substring(0, 1).uppercase(locale) + substring(1)
} else {
  this
}
