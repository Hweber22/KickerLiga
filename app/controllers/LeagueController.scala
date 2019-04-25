package controllers

import java.time.LocalDateTime
import java.util.UUID

import play.api.libs.json.{JsError, JsObject, JsSuccess, Json}
import play.api.mvc.{Action, InjectedController}
import javax.inject.Inject
import model._
import repo.LeagueRepository

import scala.concurrent.{ExecutionContext, Future}

class LeagueController @Inject()(leagueRepo: LeagueRepository)(implicit ec: ExecutionContext) extends InjectedController {

  def createPlayer2(name: String) = Action(parse.json).async {
    leagueRepo.insertPlayer(Player2(id = None, name)).map { id =>
      Created(Json.toJson(s"Player '$name' saved to league with id '$id'"))
    }
  }

  def getPlayer2(id: Long) = Action(parse.json).async {
    leagueRepo.findPlayerById(id).map { maybePlayer =>
      maybePlayer.map { player =>
        Ok(s"found $player")
      }.getOrElse {
        NotFound
      }
    }
  }

  def addPlayer(name: String) = Action(parse.json).async {

    leagueRepo.addPlayer(Player(UUID.randomUUID().toString, name)).map { _ =>
      Created(Json.toJson(s"Player '$name' saved to league"))
    }
  }

  def deletePlayer(id: String) = Action.async {

    leagueRepo.findPlayerByID(id).flatMap { stringOrPlayer =>
      stringOrPlayer.fold(
        error =>
          Future.successful(BadRequest(Json.toJson(error))),
        player =>
          leagueRepo.deletePlayer(player)
      )
    }.map { _ =>
      Ok(Json.toJson(s"Player was deleted from league"))
    }
  }

  def addGame = Action(parse.json).async { implicit request =>
    import model.GameFormat._

    Json.fromJson[Game](request.body) match {
      case JsSuccess(game, _) =>

        leagueRepo.addGame(game.copy(time = LocalDateTime.now)).map { _ =>
          Ok(Json.toJson(s"Game saved to league"))
        }

      case JsError(errors) =>
        Future.successful(BadRequest(Json.toJson(errors.mkString(","))))
    }
  }

  def deleteGame = Action(parse.json).async { implicit request =>
    import model.GameFormat._

    Json.fromJson[Game](request.body) match {
      case JsSuccess(game, _) =>

        leagueRepo.deleteGame(game).map { _ =>
          Ok(Json.toJson(s"Game deleted"))
        }

      case JsError(errors) =>
        Future.successful(BadRequest(Json.toJson(errors.mkString(","))))
    }
  }

  def getAllGames = Action.async { implicit request =>
    import model.GameFormat._

    leagueRepo.allGames
      .map { games =>
        Ok(Json.toJson(games))
      }
  }

  def gamesForPlayer(id: String) = Action.async { implicit request =>
    import model.GameFormat._

    leagueRepo.findPlayerByID(id).flatMap { stringOrPlayer =>
      stringOrPlayer.fold(
        error =>
          Future.successful(BadRequest(Json.toJson(error))),
        player =>
          leagueRepo.gamesForPlayer(player).
            map { games => Ok(Json.toJson(games)) }
      )
    }
  }

  def getLeagueStandings = Action.async { implicit request =>
    import util.PlayerMapFormat._

    leagueRepo.allPlayersWithElos
      .map { players =>
        Ok(Json.toJson(players))
      }
  }
}
