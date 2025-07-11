package net.azisaba.azisababot.util

import net.azisaba.azisababot.bundle
import java.text.MessageFormat

fun i18n(key: String, vararg args: Any?): String =
    MessageFormat.format(bundle.getString(key), *(args.map { it?.toString() }.toTypedArray()))