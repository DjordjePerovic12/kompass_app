package llc.bokadev.kompass.domain.model

enum class PlaceCategory {
    EAT_AND_DRINK,
    SEE_AND_VISIT,
    ACTIVITIES,
    HIDDEN_GEMS,
    PRACTICAL
}

enum class PriceIndicator {
    BUDGET,       // 1
    MODERATE,     // 2
    EXPENSIVE     // 3
}

enum class BestTime {
    MORNING,
    AFTERNOON,
    EVENING,
    ANYTIME
}

enum class EventCategory {
    MUSIC,
    FESTIVAL,
    SPORT,
    THEATER,
    CULTURAL,
    OTHER
}

enum class EssentialCategory {
    TRANSPORT,
    CUSTOMS,
    EMERGENCY,
    TIPS,
    PRACTICAL
}

enum class ExperienceCategory {
    TOURS,
    WATER_SPORTS,
    FOOD_AND_DRINK,
    CULTURAL,
    ADVENTURE,
    OTHER
}
