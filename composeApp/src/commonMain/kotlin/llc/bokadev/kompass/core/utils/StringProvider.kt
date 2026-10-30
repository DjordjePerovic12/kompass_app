package llc.bokadev.kompass.core.utils

import org.jetbrains.compose.resources.StringResource

/**
  Interface for providing localized strings throughout the application.

  This abstraction serves several purposes:
  1. Enables access to string resources in non-UI classes (e.g., ViewModels, UseCases)
  2. Facilitates string localization management
  3. Improves testability by allowing a FakeStringProvider in commonTest

  Compose Multiplatform resources are read with suspend functions, so these are suspend too.
 **/

interface StringProvider {
    /**
      Retrieves a string resource.

      @param res The Res.string resource to retrieve
      @return The localized string
     **/
    suspend fun getString(res: StringResource): String

    /**
      Retrieves a string resource with format arguments.
      Used for strings containing placeholders (e.g., "Hello, %1$s").

      Example usage:
      ```
      // For string resource: "%1$s must be at least %2$d characters"
      stringProvider.getStringWithArgs(Res.string.error_min_length, "Password", 8)
      ```
     **/
    suspend fun getStringWithArgs(res: StringResource, vararg args: Any): String
}
