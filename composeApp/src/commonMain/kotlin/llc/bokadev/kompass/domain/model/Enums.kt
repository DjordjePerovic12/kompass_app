package llc.bokadev.kompass.domain.model

enum class PlaceCategory(val uiText: String) {
    EAT_AND_DRINK(uiText = "EAT AND DRINK"),
    SEE_AND_VISIT("SEE AND VISIT"),
    ACTIVITIES("ACTIVITIES"),
    HIDDEN_GEMS("HIDDEN GEMS"),
    PRACTICAL("PRACTICAL")
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
