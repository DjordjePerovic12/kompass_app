package llc.bokadev.kompass.core.utils

import llc.bokadev.kompass.domain.model.EssentialCategory
import llc.bokadev.kompass.domain.model.PlaceCategory
import llc.bokadev.kompass.domain.model.SearchTag
import llc.bokadev.kompass.domain.model.SearchVocabulary
import llc.bokadev.kompass.domain.model.UtilityCategory

/**
 * The reviewed search vocabulary: what a visitor might type for a category or CMS subtype, and a
 * short list of practical queries that should reach related content ("airport" → taxi, bus).
 *
 * Rules for editing: plain words only, keyed by *category/subtype*, never by a record's name;
 * every language gets the English words too (visitors often type English). Labels shown in the
 * app (category names, Essentials groups, utility tiles) are repeated here in every language, as
 * search data: Compose resources only resolve the current locale, and search matches all of them.
 * Keep them identical to the strings.xml values so the vocabulary can't drift from what the
 * screen says. Subtypes not listed here still match their own CMS value (e.g. "palace").
 */
class SearchVocabularyTable : SearchVocabulary {

    override fun terms(tag: SearchTag, lang: String): List<String> {
        val local = when (tag) {
            is SearchTag.Place -> listOf(placeCategoryLabel(tag.category, lang)) + placeTerms(tag.category, lang)
            is SearchTag.Subtype -> subtypeTerms(tag.value, lang)
            is SearchTag.Essential -> essentialLabel(tag.category, lang) + essentialTerms(tag.category, lang)
            is SearchTag.Utility -> utilityLabels(tag.category, lang) + utilityTerms(tag.category, lang)
        }
        val english = if (lang == "en") emptyList() else terms(tag, "en")
        return (local + english).filter { it.isNotBlank() }.distinct()
    }

    override fun related(lang: String): Map<String, List<String>> {
        val local = relatedTerms(lang)
        if (lang == "en") return local
        // Both tables apply: an English query still works in another app language.
        return (relatedTerms("en").keys + local.keys).associateWith { key ->
            (local[key].orEmpty() + relatedTerms("en")[key].orEmpty()).distinct()
        }
    }

    /** The category name the app shows (redesign_category_*). */
    private fun placeCategoryLabel(category: PlaceCategory, lang: String): String = when (category) {
        PlaceCategory.EAT_AND_DRINK -> when (lang) {
            "fr" -> "Manger et boire"; "de" -> "Essen & Trinken"; "es" -> "Comer y beber"; "tr" -> "Yeme & İçme"; else -> "Eat & Drink"
        }
        PlaceCategory.SEE_AND_VISIT -> when (lang) {
            "fr" -> "À voir"; "de" -> "Sehenswürdigkeiten"; "es" -> "Para ver"; "tr" -> "Gezilecek yer"; else -> "See & Visit"
        }
        PlaceCategory.ACTIVITIES -> when (lang) {
            "fr" -> "Activités"; "de" -> "Aktivitäten"; "es" -> "Actividades"; "tr" -> "Aktiviteler"; else -> "Activities"
        }
        PlaceCategory.HIDDEN_GEMS -> when (lang) {
            "fr" -> "Trésors cachés"; "de" -> "Versteckte Orte"; "es" -> "Joyas escondidas"; "tr" -> "Gizli hazineler"; else -> "Hidden Gems"
        }
        PlaceCategory.PRACTICAL -> when (lang) {
            "fr" -> "Pratique"; "de" -> "Praktisches"; "es" -> "Práctico"; "tr" -> "Pratik"; else -> "Practical"
        }
    }

    /** The Essentials group name the app shows (essentials_group_*), or the Emergency tile (essentials_tile_emergency). */
    private fun essentialLabel(category: EssentialCategory, lang: String): List<String> = listOf(
        when (category) {
            EssentialCategory.CUSTOMS -> when (lang) {
                "fr" -> "Enregistrement & taxe"; "de" -> "Anmeldung & Kurtaxe"; "es" -> "Registro y tasa"; "tr" -> "Kayıt ve vergi"; else -> "Registration & tax"
            }
            EssentialCategory.TRANSPORT -> when (lang) {
                "fr" -> "Se déplacer"; "de" -> "Unterwegs"; "es" -> "Moverse"; "tr" -> "Ulaşım"; else -> "Getting around"
            }
            EssentialCategory.PRACTICAL -> when (lang) {
                "fr" -> "Infos pratiques"; "de" -> "Praktisches"; "es" -> "Info práctica"; "tr" -> "Pratik bilgiler"; else -> "Practical info"
            }
            EssentialCategory.TIPS -> when (lang) {
                "fr" -> "Conseils locaux"; "de" -> "Lokale Tipps"; "es" -> "Consejos locales"; "tr" -> "Yerel ipuçları"; else -> "Local tips"
            }
            EssentialCategory.EMERGENCY -> utilityLabels(UtilityCategory.EMERGENCY, lang).first()
        }
    )

    /** The utility's tile label then its card title, as the app shows them (essentials_tile_* / essentials_title_*). */
    private fun utilityLabels(category: UtilityCategory, lang: String): List<String> = when (category) {
        UtilityCategory.ATM -> when (lang) {
            "fr" -> listOf("Argent & DAB", "Argent & DAB"); "de" -> listOf("Bargeld & Automaten", "Bargeld & Automaten")
            "es" -> listOf("Efectivo y cajeros", "Efectivo y cajeros"); "tr" -> listOf("Nakit ve ATM", "Nakit ve ATM")
            else -> listOf("Cash & ATMs", "Cash & ATMs")
        }
        UtilityCategory.PHARMACY -> when (lang) {
            "fr" -> listOf("Pharmacie", "Pharmacies"); "de" -> listOf("Apotheke", "Apotheken")
            "es" -> listOf("Farmacia", "Farmacias"); "tr" -> listOf("Eczane", "Eczaneler")
            else -> listOf("Pharmacy", "Pharmacies")
        }
        UtilityCategory.SUPERMARKET -> when (lang) {
            "fr" -> listOf("Supermarché", "Supermarchés"); "de" -> listOf("Supermarkt", "Supermärkte")
            "es" -> listOf("Supermercado", "Supermercados"); "tr" -> listOf("Market", "Marketler")
            else -> listOf("Supermarket", "Supermarkets")
        }
        UtilityCategory.SHOP -> when (lang) {
            "fr" -> listOf("Magasin", "Magasins"); "de" -> listOf("Geschäft", "Geschäfte")
            "es" -> listOf("Tienda", "Tiendas"); "tr" -> listOf("Mağaza", "Mağazalar")
            else -> listOf("Shop", "Shops")
        }
        UtilityCategory.PARKING -> when (lang) {
            "fr" -> listOf("Parking", "Parking"); "de" -> listOf("Parken", "Parken")
            "es" -> listOf("Aparcamiento", "Aparcamiento"); "tr" -> listOf("Otopark", "Otopark")
            else -> listOf("Parking", "Parking")
        }
        UtilityCategory.GAS_STATION -> when (lang) {
            "fr" -> listOf("Carburant", "Carburant"); "de" -> listOf("Tanken", "Tankstellen")
            "es" -> listOf("Gasolina", "Gasolineras"); "tr" -> listOf("Yakıt", "Akaryakıt")
            else -> listOf("Fuel", "Fuel")
        }
        UtilityCategory.EMERGENCY -> when (lang) {
            "fr" -> listOf("Urgence", "Urgence"); "de" -> listOf("Notfall", "Notfall")
            "es" -> listOf("Emergencias", "Emergencias"); "tr" -> listOf("Acil", "Acil")
            else -> listOf("Emergency", "Emergency")
        }
    }

    private fun placeTerms(category: PlaceCategory, lang: String): List<String> = when (category) {
        PlaceCategory.EAT_AND_DRINK -> when (lang) {
            "fr" -> listOf("restaurant", "manger", "boire", "dîner", "déjeuner", "bar", "café")
            "de" -> listOf("Restaurant", "Essen", "Trinken", "Abendessen", "Mittagessen", "Bar", "Café", "Lokal")
            "es" -> listOf("restaurante", "comer", "beber", "cena", "almuerzo", "bar", "cafetería")
            "tr" -> listOf("restoran", "yemek", "içecek", "akşam yemeği", "öğle yemeği", "bar", "kafe")
            else -> listOf("restaurant", "food", "eat", "drink", "dinner", "lunch", "bar", "cafe")
        }
        PlaceCategory.SEE_AND_VISIT -> when (lang) {
            "fr" -> listOf("à voir", "monument", "site", "visite")
            "de" -> listOf("Sehenswürdigkeit", "Denkmal", "besichtigen")
            "es" -> listOf("monumento", "lugar de interés", "visitar")
            "tr" -> listOf("görülecek yer", "anıt", "gezilecek yer")
            else -> listOf("sight", "sightseeing", "landmark", "attraction", "monument")
        }
        PlaceCategory.ACTIVITIES, PlaceCategory.HIDDEN_GEMS, PlaceCategory.PRACTICAL -> emptyList()
    }

    /** CMS subtype values seen in content (Oct 2026); unknown ones fall back to their own words. */
    private fun subtypeTerms(value: String, lang: String): List<String> {
        val church = when (lang) { "fr" -> "église"; "de" -> "Kirche"; "es" -> "iglesia"; "tr" -> "kilise"; else -> "church" }
        val seafood = when (lang) {
            "fr" -> listOf("fruits de mer", "poisson"); "de" -> listOf("Meeresfrüchte", "Fisch")
            "es" -> listOf("marisco", "pescado"); "tr" -> listOf("deniz ürünleri", "balık"); else -> listOf("seafood", "fish")
        }
        val fortress = when (lang) {
            "fr" -> listOf("forteresse", "fort", "remparts"); "de" -> listOf("Festung", "Burg", "Stadtmauer")
            "es" -> listOf("fortaleza", "murallas"); "tr" -> listOf("kale", "surlar"); else -> listOf("fortress", "fort", "castle", "walls")
        }
        val local: List<String> = when (value) {
            "destination overview" -> return emptyList()
            "church" -> listOf(church)
            "orthodox church" -> listOf(church, when (lang) { "fr" -> "orthodoxe"; "de" -> "orthodox"; "es" -> "ortodoxa"; "tr" -> "ortodoks"; else -> "orthodox" })
            "cathedral" -> listOf(church, when (lang) { "fr" -> "cathédrale"; "de" -> "Kathedrale"; "es" -> "catedral"; "tr" -> "katedral"; else -> "cathedral" }) +
                if (lang == "de") listOf("Dom") else emptyList()
            "sanctuary" -> listOf(church, when (lang) { "fr" -> "sanctuaire"; "de" -> "Heiligtum"; "es" -> "santuario"; "tr" -> "kutsal mekan"; else -> "sanctuary" })
            "seafood" -> seafood
            "fine dining seafood" -> seafood + when (lang) {
                "fr" -> "gastronomique"; "de" -> "Gourmet"; "es" -> "alta cocina"; "tr" -> "fine dining"; else -> "fine dining"
            }
            "pub" -> listOf("pub", "bar") + when (lang) { "fr" -> listOf("bière"); "de" -> listOf("Kneipe", "Bier"); "es" -> listOf("cerveza"); "tr" -> listOf("bira"); else -> listOf("beer") }
            "museum" -> listOf(when (lang) { "fr" -> "musée"; "de" -> "Museum"; "es" -> "museo"; "tr" -> "müze"; else -> "museum" })
            "fortress" -> fortress
            "historic building" -> listOf(when (lang) { "fr" -> "bâtiment historique"; "de" -> "historisches Gebäude"; "es" -> "edificio histórico"; "tr" -> "tarihi bina"; else -> "historic building" })
            "square" -> listOf(when (lang) { "fr" -> "place"; "de" -> "Platz"; "es" -> "plaza"; "tr" -> "meydan"; else -> "square" })
            "palace" -> listOf(when (lang) { "fr" -> "palais"; "de" -> "Palast"; "es" -> "palacio"; "tr" -> "saray"; else -> "palace" })
            "promenade" -> when (lang) {
                "fr" -> listOf("promenade", "bord de mer"); "de" -> listOf("Promenade", "Uferweg"); "es" -> listOf("paseo", "paseo marítimo")
                "tr" -> listOf("sahil yolu", "gezinti yolu"); else -> listOf("promenade", "waterfront", "seafront")
            }
            // Activity categories.
            "hike" -> when (lang) {
                "fr" -> listOf("randonnée"); "de" -> listOf("Wanderung", "wandern"); "es" -> listOf("senderismo", "caminata")
                "tr" -> listOf("doğa yürüyüşü", "trekking"); else -> listOf("hike", "hiking", "trek")
            }
            "walk" -> when (lang) {
                "fr" -> listOf("balade", "visite à pied"); "de" -> listOf("Spaziergang", "Rundgang"); "es" -> listOf("paseo", "a pie")
                "tr" -> listOf("yürüyüş", "yürüyüş turu"); else -> listOf("walk", "walking tour")
            }
            "boat" -> when (lang) {
                "fr" -> listOf("bateau", "excursion en bateau"); "de" -> listOf("Boot", "Bootsfahrt", "Schiff"); "es" -> listOf("barco", "paseo en barco")
                "tr" -> listOf("tekne", "tekne turu"); else -> listOf("boat", "boat trip", "cruise")
            }
            // Editorial collection membership, not a type: not searchable as a word.
            "worth the drive" -> return emptyList()
            else -> listOf(value)
        }
        return local
    }

    private fun essentialTerms(category: EssentialCategory, lang: String): List<String> = when (category) {
        EssentialCategory.TRANSPORT -> when (lang) {
            "fr" -> listOf("transport", "bus", "taxi", "navette", "ferry"); "de" -> listOf("Verkehr", "Bus", "Taxi", "Transfer", "Fähre")
            "es" -> listOf("transporte", "autobús", "taxi", "traslado", "ferry"); "tr" -> listOf("ulaşım", "otobüs", "taksi", "transfer", "feribot")
            else -> listOf("transport", "bus", "taxi", "transfer", "ferry")
        }
        EssentialCategory.CUSTOMS -> when (lang) {
            "fr" -> listOf("enregistrement", "taxe de séjour", "visa"); "de" -> listOf("Anmeldung", "Kurtaxe", "Visum")
            "es" -> listOf("registro", "tasa turística", "visado"); "tr" -> listOf("kayıt", "turist vergisi", "vize")
            else -> listOf("registration", "tourist tax", "visa")
        }
        EssentialCategory.EMERGENCY -> when (lang) {
            "fr" -> listOf("urgence", "police", "ambulance", "pompiers"); "de" -> listOf("Notfall", "Polizei", "Krankenwagen", "Feuerwehr")
            "es" -> listOf("emergencia", "policía", "ambulancia", "bomberos"); "tr" -> listOf("acil", "polis", "ambulans", "itfaiye")
            else -> listOf("emergency", "police", "ambulance", "fire")
        }
        EssentialCategory.TIPS, EssentialCategory.PRACTICAL -> emptyList()
    }

    private fun utilityTerms(category: UtilityCategory, lang: String): List<String> = when (category) {
        UtilityCategory.ATM -> when (lang) {
            "fr" -> listOf("DAB", "distributeur", "espèces", "banque"); "de" -> listOf("Geldautomat", "Bargeld", "Bank")
            "es" -> listOf("cajero", "efectivo", "banco"); "tr" -> listOf("ATM", "bankamatik", "nakit", "banka")
            else -> listOf("ATM", "cash machine", "cash", "bank")
        }
        UtilityCategory.PHARMACY -> when (lang) {
            "fr" -> listOf("pharmacie", "médicaments"); "de" -> listOf("Apotheke", "Medikamente")
            "es" -> listOf("farmacia", "medicamentos"); "tr" -> listOf("eczane", "ilaç")
            else -> listOf("pharmacy", "chemist", "drugstore", "medicine")
        }
        UtilityCategory.SUPERMARKET -> when (lang) {
            "fr" -> listOf("supermarché", "épicerie", "courses"); "de" -> listOf("Supermarkt", "Lebensmittel", "Einkaufen")
            "es" -> listOf("supermercado", "tienda de comestibles"); "tr" -> listOf("süpermarket", "market", "bakkal")
            else -> listOf("supermarket", "grocery", "groceries", "market")
        }
        UtilityCategory.SHOP -> when (lang) {
            "fr" -> listOf("magasin", "boutique"); "de" -> listOf("Geschäft", "Laden")
            "es" -> listOf("tienda"); "tr" -> listOf("mağaza", "dükkan"); else -> listOf("shop", "store")
        }
        UtilityCategory.PARKING -> when (lang) {
            "fr" -> listOf("parking", "stationnement"); "de" -> listOf("Parkplatz", "Parken", "Parkhaus")
            "es" -> listOf("aparcamiento", "parking"); "tr" -> listOf("otopark", "park yeri")
            else -> listOf("parking", "car park")
        }
        UtilityCategory.GAS_STATION -> when (lang) {
            "fr" -> listOf("station-service", "essence", "carburant"); "de" -> listOf("Tankstelle", "Benzin", "Diesel")
            "es" -> listOf("gasolinera", "gasolina", "combustible"); "tr" -> listOf("benzinlik", "akaryakıt", "benzin")
            else -> listOf("gas station", "petrol", "fuel", "diesel")
        }
        UtilityCategory.EMERGENCY -> essentialTerms(EssentialCategory.EMERGENCY, lang) + when (lang) {
            "fr" -> listOf("hôpital", "médecin"); "de" -> listOf("Krankenhaus", "Arzt"); "es" -> listOf("hospital", "médico")
            "tr" -> listOf("hastane", "doktor"); else -> listOf("hospital", "doctor")
        }
    }

    /** Whole-query → words to try. Keep short: each line must be obviously right to a local. */
    private fun relatedTerms(lang: String): Map<String, List<String>> = when (lang) {
        "fr" -> mapOf(
            "aéroport" to listOf("taxi", "bus", "navette", "transfert"),
            "paiement" to listOf("espèces", "carte", "DAB", "change"), "payer" to listOf("espèces", "carte", "DAB", "change"),
            "argent" to listOf("espèces", "DAB", "change"), "toilettes" to listOf("toilet", "WC"),
            "bagages" to listOf("luggage", "consigne"), "médecin" to listOf("pharmacie", "urgence", "ambulance"),
            "hôpital" to listOf("urgence", "ambulance", "pharmacie")
        )
        "de" -> mapOf(
            "Flughafen" to listOf("Taxi", "Bus", "Transfer"),
            "Bezahlen" to listOf("Bargeld", "Karte", "Geldautomat", "Wechsel"), "Zahlung" to listOf("Bargeld", "Karte", "Geldautomat", "Wechsel"),
            "Geld" to listOf("Bargeld", "Geldautomat", "Wechsel"), "Toilette" to listOf("toilet", "WC"),
            "Gepäck" to listOf("luggage", "Gepäckaufbewahrung"), "Arzt" to listOf("Apotheke", "Notfall", "Krankenwagen"),
            "Krankenhaus" to listOf("Notfall", "Krankenwagen", "Apotheke")
        )
        "es" -> mapOf(
            "aeropuerto" to listOf("taxi", "autobús", "traslado"),
            "pago" to listOf("efectivo", "tarjeta", "cajero", "cambio"), "pagar" to listOf("efectivo", "tarjeta", "cajero", "cambio"),
            "dinero" to listOf("efectivo", "cajero", "cambio"), "baño" to listOf("toilet", "WC", "aseo"),
            "equipaje" to listOf("luggage", "consigna"), "médico" to listOf("farmacia", "emergencia", "ambulancia"),
            "hospital" to listOf("emergencia", "ambulancia", "farmacia")
        )
        "tr" -> mapOf(
            "havalimanı" to listOf("taksi", "otobüs", "transfer"), "havaalanı" to listOf("taksi", "otobüs", "transfer"),
            "ödeme" to listOf("nakit", "kart", "ATM", "döviz"), "para" to listOf("nakit", "ATM", "döviz"),
            "tuvalet" to listOf("toilet", "WC"), "bagaj" to listOf("luggage", "emanet"),
            "doktor" to listOf("eczane", "acil", "ambulans"), "hastane" to listOf("acil", "ambulans", "eczane")
        )
        else -> mapOf(
            "airport" to listOf("taxi", "bus", "transfer"),
            "payment" to listOf("cash", "card", "ATM", "exchange", "paying"), "pay" to listOf("cash", "card", "ATM", "exchange", "paying"),
            "money" to listOf("cash", "ATM", "exchange"), "card" to listOf("cash", "paying"),
            "toilet" to listOf("toilet", "WC", "restroom"), "restroom" to listOf("toilet", "WC"), "wc" to listOf("toilet"),
            "luggage" to listOf("luggage", "baggage", "left luggage"), "baggage" to listOf("luggage"),
            "doctor" to listOf("pharmacy", "emergency", "ambulance"), "hospital" to listOf("emergency", "ambulance", "pharmacy"),
            "police" to listOf("emergency")
        )
    }
}
