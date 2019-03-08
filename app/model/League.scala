package model

import scala.collection.immutable.ListMap
import play.api.libs.json.{JsValue, Json}

case class League(games: List[Game], players: ListMap[Player, Int])

object League {
  def empty = League(
    games = List.empty,
    players = ListMap.empty
  )

  def getEloForPlayer(player: Player, league: League) =
    league.players.getOrElse(player, 1500)

  def getEloForSide(side: List[Player], league: League) =
    side.map(player => getEloForPlayer(player, league)).sum

  def sortTable(players: ListMap[Player, Int]): ListMap[Player, Int] =
    ListMap(players.toSeq.sortWith(_._2 > _._2): _*)

  def addPlayer(league: League, player: Player): League = {
    League(league.games, league.players + (player -> 1500))
  }

  def addGame(game: Game, league: League): League = {
    val players = game.leftSide ::: game.rightSide
    players.foreach(player => if (!league.players.keySet.contains(player))
      addPlayer(league, player))
    League(league.games ::: List(game), Elo.updateAllElos(game, league))
  }
}

object LeagueFormat {

  import model.PlayerFormat._
  import model.GameFormat._
  import util.PlayerMapFormat.playerMapFormats

  implicit val leagueFormat = Json.format[League]

  implicit class LeagueOpsTo(league: League) {
    def toJson = Json.toJson(league)
  }

  implicit class LeagueOpsFrom(league: League.type) {
    def fromJson(json: JsValue) = Json.fromJson[League](json)
  }

}

