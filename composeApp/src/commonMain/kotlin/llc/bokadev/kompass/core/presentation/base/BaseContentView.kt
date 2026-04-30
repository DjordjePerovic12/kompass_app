package llc.bokadev.kompass.core.presentation.base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import llc.bokadev.kompass.core.util.noRippleClickable
import llc.bokadev.kompass.presentation.theme.KompassTheme

@Composable
fun BaseContentView(
    modifier: Modifier = Modifier,
    state: BaseState,
    topBar: @Composable () -> Unit = {},
    fab: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .noRippleClickable { focusManager.clearFocus() },
        topBar = topBar,
        floatingActionButton = fab,
        containerColor = KompassTheme.colors.colorSurface
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            content()

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(KompassTheme.colors.colorSurface)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = KompassTheme.colors.colorNavy
                    )
                }
            }
        }
    }
}
