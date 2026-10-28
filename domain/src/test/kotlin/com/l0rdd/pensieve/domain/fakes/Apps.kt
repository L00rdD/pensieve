package com.l0rdd.pensieve.domain.fakes

import com.l0rdd.pensieve.domain.entities.apps.App
import com.l0rdd.pensieve.domain.entities.apps.AppId
import com.l0rdd.pensieve.domain.entities.apps.AppKind

/** An app for tests, its label taken from its package. */
fun app(pkg: String, kind: AppKind = AppKind.APP) = App(AppId(pkg), pkg.substringAfterLast('.'), artwork = null, kind = kind)
