package com.slpolice.reporting.data

/** Access level of a signed-in account. */
enum class UserRole { CITIZEN, OFFICER;
    companion object {
        fun from(value: String): UserRole = entries.firstOrNull { it.name == value } ?: CITIZEN
    }
}

/** Offence categories a citizen can file evidence against. */
enum class ReportCategory(val label: String) {
    TRAFFIC_OFFENCE("Traffic offence"),
    RECKLESS_DRIVING("Reckless driving"),
    ROAD_ACCIDENT("Road accident"),
    PUBLIC_NUISANCE("Public nuisance"),
    THEFT_OR_ROBBERY("Theft or robbery"),
    ASSAULT("Assault"),
    DRUG_ACTIVITY("Drug activity"),
    ENVIRONMENTAL("Environmental damage"),
    CORRUPTION("Bribery or corruption"),
    OTHER("Other incident");

    companion object {
        fun from(value: String): ReportCategory = entries.firstOrNull { it.name == value } ?: OTHER
    }
}

/** Lifecycle of a filed report inside the police workflow. */
enum class ReportStatus(val label: String) {
    SUBMITTED("Submitted"),
    UNDER_REVIEW("Under review"),
    ACTION_TAKEN("Action taken"),
    REJECTED("Rejected");

    companion object {
        fun from(value: String): ReportStatus = entries.firstOrNull { it.name == value } ?: SUBMITTED
    }
}

enum class Priority(val label: String) {
    LOW("Low"), NORMAL("Normal"), HIGH("High"), CRITICAL("Critical");

    companion object {
        fun from(value: String): Priority = entries.firstOrNull { it.name == value } ?: NORMAL
    }
}

enum class EvidenceType { IMAGE, VIDEO;
    companion object {
        fun from(value: String): EvidenceType = entries.firstOrNull { it.name == value } ?: IMAGE
    }
}

/** Result wrapper used between the repository and the view models. */
sealed interface Outcome<out T> {
    data class Success<T>(val data: T) : Outcome<T>
    data class Failure(val message: String) : Outcome<Nothing>
}

/** Who sent an inbox message. */
enum class MessageKind(val label: String) {
    SYSTEM("Department"),
    OFFICER("Officer");

    companion object {
        fun from(value: String): MessageKind = entries.firstOrNull { it.name == value } ?: SYSTEM
    }
}

/**
 * Divisions the department works by. Each offence category belongs to exactly one group, which
 * is what gives the admin panel its separate dashboards.
 */
enum class CaseGroup(val label: String, val blurb: String) {
    TRAFFIC("Traffic", "Road offences, dangerous driving and collisions"),
    CRIMINAL("Criminal", "Theft, assault and narcotics"),
    PUBLIC_ORDER("Public order", "Nuisance and disturbance in public places"),
    ENVIRONMENTAL("Environmental", "Dumping, pollution and damage to land"),
    INTEGRITY("Bribery and corruption", "Public sector integrity matters"),
    GENERAL("General", "Anything not otherwise classified");

    companion object {
        fun from(value: String): CaseGroup = entries.firstOrNull { it.name == value } ?: GENERAL
    }
}

/** Which division handles this category. */
val ReportCategory.group: CaseGroup
    get() = when (this) {
        ReportCategory.TRAFFIC_OFFENCE,
        ReportCategory.RECKLESS_DRIVING,
        ReportCategory.ROAD_ACCIDENT -> CaseGroup.TRAFFIC

        ReportCategory.THEFT_OR_ROBBERY,
        ReportCategory.ASSAULT,
        ReportCategory.DRUG_ACTIVITY -> CaseGroup.CRIMINAL

        ReportCategory.PUBLIC_NUISANCE -> CaseGroup.PUBLIC_ORDER
        ReportCategory.ENVIRONMENTAL -> CaseGroup.ENVIRONMENTAL
        ReportCategory.CORRUPTION -> CaseGroup.INTEGRITY
        ReportCategory.OTHER -> CaseGroup.GENERAL
    }

/** The categories a given division is responsible for. */
fun CaseGroup.categories(): List<ReportCategory> =
    ReportCategory.entries.filter { it.group == this }
