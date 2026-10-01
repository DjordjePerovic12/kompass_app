package llc.bokadev.kompass

import androidx.compose.ui.window.ComposeUIViewController
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import llc.bokadev.kompass.di.appModule
import org.koin.core.context.startKoin
import platform.UIKit.UIViewController

private var koinStarted = false

fun MainViewController(): UIViewController {
    if (!koinStarted) {
        koinStarted = true
        Napier.base(DebugAntilog())
        startKoin {
            modules(appModule)
        }
    }
    return ComposeUIViewController { App() }
}
