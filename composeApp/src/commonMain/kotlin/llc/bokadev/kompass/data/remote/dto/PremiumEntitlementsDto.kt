package llc.bokadev.kompass.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import llc.bokadev.kompass.domain.model.PremiumEntitlements

@Serializable
data class PremiumEntitlementsDto(
    @SerialName("audio_pass")
    val audioPass: Boolean = false,
    @SerialName("explorer_pass")
    val explorerPass: Boolean = false,
    @SerialName("perks_pass")
    val perksPass: Boolean = false
)

fun PremiumEntitlementsDto.toDomain(): PremiumEntitlements = PremiumEntitlements(
    audioPass = audioPass,
    explorerPass = explorerPass,
    perksPass = perksPass
)
