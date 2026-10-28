package com.l0rdd.pensieve.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.l0rdd.pensieve.PensieveApplication
import com.l0rdd.pensieve.dependency_injector.injectSettingsKey
import com.l0rdd.pensieve.domain.usecases.NoParam
import kotlinx.coroutines.launch

/** Answers the remote's ⚙ key from any app: opens the projector's settings, then goes away. */
class SettingsKeyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val openSettings = injectSettingsKey((application as PensieveApplication).scope)
        lifecycleScope.launch {
            runCatching { openSettings(NoParam) }
            finish()
        }
    }
}
