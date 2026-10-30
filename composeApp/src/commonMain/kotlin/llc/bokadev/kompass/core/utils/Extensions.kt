package llc.bokadev.kompass.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

/**
  Composable function that observes a Flow with lifecycle awareness.
  Automatically handles collection and cancellation based on the lifecycle state.

  Collection is tied to the composition (unlike launching into lifecycleScope): a tab's back stack
  entry outlives its composition when another tab is selected, and returning must not add a
  second collector.

  @param T The type of data flowing through the Flow
  @param lifecycleOwner The lifecycle owner to respect (defaults to LocalLifecycleOwner)
  @param minActiveState Minimum lifecycle state when collection should occur (defaults to STARTED)
  @param action The action to perform when a value is collected
 **/
@Composable
fun <T> Flow<T>.observeWithLifecycle(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit
) {
    LaunchedEffect(this, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(minActiveState) {
            collect(action)
        }
    }
}
