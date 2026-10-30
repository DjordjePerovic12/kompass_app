package llc.bokadev.kompass.domain.repository

interface RecentSearchesRepository {
    fun get(): List<String>
    fun add(query: String)
    fun clear()
}
