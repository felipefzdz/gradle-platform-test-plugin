@file:Suppress("unused")

package org.gradle.kotlin.dsl

import org.gradle.api.tasks.testing.Test
import com.felipefzdz.platform.extension.PlatformTestExtension

val Test.platform: PlatformTestExtension
    get() = the()

fun Test.platform(configure: PlatformTestExtension.() -> Unit) =
        extensions.configure(PlatformTestExtension.NAME, configure)
