package com.github.kr328.clash.glue.util

import com.github.kr328.clash.common.util.PatternFileName
import com.github.kr328.clash.core.model.isHttpProfileSource
import com.github.kr328.clash.core.model.isProfileAutoUpdateIntervalMinutesInput

typealias Validator = (String) -> Boolean

val ValidatorAcceptAll: Validator = { true }

val ValidatorFileName: Validator = { PatternFileName.matches(it) && it.isNotBlank() }

val ValidatorNotBlank: Validator = { it.isNotBlank() }

val ValidatorHttpUrl: Validator = ::isHttpProfileSource

val ValidatorAutoUpdateInterval: Validator = ::isProfileAutoUpdateIntervalMinutesInput
