package llc.bokadev.kompass.core.utils

import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString as getResourceString

/**
  Implementation of [StringProvider] backed by Compose Multiplatform resources.
  Strings resolve against the current app locale (see AppLocale.kt).
 **/

class StringProviderImpl : StringProvider {
    override suspend fun getString(res: StringResource): String {
        return getResourceString(res)
    }

    override suspend fun getStringWithArgs(res: StringResource, vararg args: Any): String {
        return getResourceString(res, *args)
    }
}
