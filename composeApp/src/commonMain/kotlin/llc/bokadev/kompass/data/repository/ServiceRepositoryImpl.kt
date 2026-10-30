package llc.bokadev.kompass.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.core.utils.networking.safeSupabaseCall
import llc.bokadev.kompass.data.mapper.toDomain
import llc.bokadev.kompass.data.remote.dto.ServiceDto
import llc.bokadev.kompass.domain.model.Service
import llc.bokadev.kompass.domain.repository.ServiceRepository

class ServiceRepositoryImpl(
    private val supabase: SupabaseClient
) : ServiceRepository {

    override suspend fun getServices(): Resource<List<Service>, NetworkError, String?> = safeSupabaseCall {
        supabase.from("services")
            .select {
                filter { eq("is_active", true) }
                order(column = "sort_order", order = Order.ASCENDING)
            }
            .decodeList<ServiceDto>()
            .map { it.toDomain() }
    }

    override suspend fun getServiceById(id: String): Resource<Service, NetworkError, String?> = safeSupabaseCall {
        supabase.from("services")
            .select {
                filter {
                    eq("id", id)
                    eq("is_active", true)
                }
                limit(1)
            }
            .decodeSingle<ServiceDto>()
            .toDomain()
    }
}
