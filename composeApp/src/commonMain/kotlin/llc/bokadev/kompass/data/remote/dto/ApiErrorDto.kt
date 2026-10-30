package llc.bokadev.kompass.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
    Error body shape of Supabase REST and Edge Function errors
 **/

@Serializable
data class ApiErrorDto(
    @SerialName("message")
    val message: String = ""
)
