package model

import java.time.LocalDateTime
import play.api.libs.json.{JsValue, Json}

case class Game(time: LocalDateTime, leftSide: List[Player], rightSide: List[Player], result: GameResult) {
  require(leftSide.length == rightSide.length && leftSide.length <= 2 && leftSide.nonEmpty)
}

object GameFormat {

  import PlayerFormat._
  import GameResultFormat._

  implicit val gameFormat = Json.format[Game]

  implicit class GameOpsTo(game: Game) {
    def toJson = Json.toJson(game)
  }

  implicit class GameOpsFrom(game: Game.type) {
    def fromJson(json: JsValue) = Json.fromJson[Game](json)
  }

}