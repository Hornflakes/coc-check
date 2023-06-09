package client.models.location

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val id: Int = 0,
    val name: String? = null,
    val isCountry: Boolean = false,
    val countryCode: String? = null
)
