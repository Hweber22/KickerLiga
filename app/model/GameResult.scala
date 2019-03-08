package model
import play.api.libs.json.{JsValue, Json}

case class GameResult(leftSets: Int, rightSets: Int)

object GameResultFormat {

  implicit val gameresultFormat = Json.format[GameResult]

  implicit class GameResultOpsTo(gameresult: GameResult) {
    def toJson = Json.toJson(gameresult)
  }
  implicit class GameResultOpsFrom(gameresult: GameResult.type) {
    def fromJson(json: JsValue) = Json.fromJson[GameResult](json)
  }
}
