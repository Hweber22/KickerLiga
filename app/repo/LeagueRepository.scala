package repo

import java.util.UUID

import javax.inject.Inject
import model._

import scala.collection.immutable.ListMap
import scala.concurrent.{ExecutionContext, Future}

trait LeagueRepository {
  def addPlayer(player: Player): Future[Unit]

  def deletePlayer(player: Player): Future[Unit]

  def addGame(game: Game): Future[Unit]

  def allGames: Future[List[Game]]

  def gamesForPlayer(player: Player): Future[List[Game]]

  def allPlayersWithElos: Future[ListMap[Player, Int]]
}

class LeagueRepositoryImpl @Inject()(implicit ec: ExecutionContext) extends LeagueRepository {
  var league = League.empty

  override def addPlayer(player: Player): Future[Unit] =
    Future.successful {
      league = League(league.games, league.players + (player -> 1500))
    }

  override def deletePlayer(player: Player): Future[Unit] =
    Future.successful {
      val activatedPlayers = league.games.flatMap { game =>
        game.leftSide ++ game.rightSide
      }
      if (activatedPlayers.contains(player))
        league = league
      else
        league = League(league.games, league.players.filter(p => p._1 != player))
    }

  override def addGame(game: Game): Future[Unit] =
    Future.successful {
      league = League.addGame(game, league)
    }

  override def allGames: Future[List[Game]] = Future.successful(league.games)

  override def gamesForPlayer(player: Player): Future[List[Game]] =
    Future.successful {
      val playersGames = league.games.filter(game => (game.leftSide ::: game.rightSide).contains(player))
      playersGames.map { game =>
        if (game.leftSide.contains(player)) game
        else Game(game.time, game.rightSide, game.leftSide, GameResult(game.result.rightSets, game.result.leftSets))
      }
    }

  override def allPlayersWithElos: Future[ListMap[Player, Int]] = Future.successful(League.sortTable(league.players))
}
