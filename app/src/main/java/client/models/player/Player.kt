package client.models.player

import client.models.common.Label
import client.models.league.League
import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val clan: PlayerClan? = null,
    val league: League? = null,
    val role: String? = null,
    val warPreference: String? = null,
    val attackWins: Int = 0,
    val defenseWins: Int = 0,
    val townHallLevel: Int = 0,
    val townHallWeaponLevel: Int = 0,
    val versusBattleWins: Int = 0,
    val legendStatistics: PlayerLegendStatistics? = null,
    val troops: List<Troop>,
    val heroes: List<Troop>,
    val spells: List<Troop>,
    val labels: List<Label>,
    val tag: String,
    val name: String,
    val expLevel: Int = 0,
    val trophies: Int = 0,
    val bestTrophies: Int = 0,
    val donations: Int = 0,
    val donationsReceived: Int = 0,
    val builderHallLevel: Int = 0,
    val versusTrophies: Int = 0,
    val bestVersusTrophies: Int = 0,
    val warStars: Int = 0,
    val achievements: List<Achievement>,
    val versusBattleWinCount: Int = 0
)
