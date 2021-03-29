@file:Suppress("unused")

package org.gradle.kotlin.dsl

import org.gradle.api.tasks.testing.Test
import com.felipefzdz.kubernetes.extension.KubernetesTestExtension

val Test.kubernetes: KubernetesTestExtension
    get() = the()

fun Test.kubernetes(configure: KubernetesTestExtension.() -> Unit) =
        extensions.configure(KubernetesTestExtension.NAME, configure)
