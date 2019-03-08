package repo

import java.util.UUID

import javax.inject.Inject
import model._

import scala.collection.immutable.ListMap
import scala.concurrent.{ExecutionContext, Future}

trait LeagueRepository {
  def addPlayer(player: Player): Future[Unit]

  def addGame(game: Game): Future[Unit]

  def allGames: Future[List[Game]]

  def allPlayersWithElos: Future[ListMap[Player, Int]]
}

class LeagueRepositoryImpl @Inject()(implicit ec: ExecutionContext) extends LeagueRepository {
  var league = League.empty

  override def addPlayer(player: Player): Future[Unit] =
    Future.successful {
      league = League(league.games, league.players + (player -> 1500))
    }

  override def addGame(game: Game): Future[Unit] =
    Future.successful {
      league = League.addGame(game, league)
    }

  override def allGames: Future[List[Game]] = Future.successful(league.games)

  override def allPlayersWithElos: Future[ListMap[Player, Int]] = Future.successful(League.sortTable(league.players))
}
