package model

import play.api.libs.json.{JsValue, Json}

case class Player(id: String, name: String)

case class Player2(id: Option[Long], name: String)

object PlayerFormat {

  implicit val playerFormat = Json.format[Player]

  implicit class PlayerOpsTo(player: Player) {
    def toJson = Json.toJson(player)
  }

  implicit class PlayerOpsFrom(player: Player.type) {
    def fromJson(json: JsValue) = Json.fromJson[Player](json)
  }

}
