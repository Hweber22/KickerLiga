package util

import model.Player
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._

import scala.collection.immutable.ListMap

object PlayerMapFormat {

  implicit def playerMapFormats = new Format[ListMap[Player, Int]] {
    override def writes(o: ListMap[Player, Int]): JsValue = {
      val keysAsStrings = o.map { case (player, value) => (player.toString, value) }
      Json.toJson(keysAsStrings)
    }

    override def reads(json: JsValue): JsResult[ListMap[Player, Int]] = {
      for {
        keysAsStrings <- Json.fromJson[Map[String, Int]](json)
      } yield {
        val nonOrderedMap = keysAsStrings.map {
          case (key, value) =>
            key.split('-') match {
              case Array(id, name) => Player(id, name) -> value
            }
        }
        ListMap(nonOrderedMap.toSeq.sortWith(_._2 > _._2): _*)
      }
    }
  }
}
