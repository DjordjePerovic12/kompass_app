package llc.bokadev.kompass.data.repository

import llc.bokadev.kompass.domain.model.PaymentCheckoutSession

class PaymentCheckoutSessionStore {
    private val sessions = mutableMapOf<String, PaymentCheckoutSession>()

    fun put(session: PaymentCheckoutSession) {
        sessions[session.sessionId] = session
    }

    fun get(sessionId: String): PaymentCheckoutSession? = sessions[sessionId]

    fun clear(sessionId: String) {
        sessions.remove(sessionId)
    }
}
