package repo

import javax.inject.Inject
import model._
import com.typesafe.scalalogging.LazyLogging

import anorm.SqlParser.get
import anorm._
import play.api.db.DBApi

import scala.collection.immutable.ListMap
import scala.concurrent.{ExecutionContext, Future}

trait LeagueRepository {

  def findPlayerById(id: Long): Future[Option[Player2]]

  def insertPlayer(player: Player2): Future[Option[Long]]

  def addPlayer(player: Player): Future[Unit]

  def deletePlayer(player: Player): Future[Unit]

  def addGame(game: Game): Future[Unit]

  def deleteGame(game: Game): Future[Unit]

  def allGames: Future[List[Game]]

  def gamesForPlayer(player: Player): Future[List[Game]]

  def allPlayersWithElos: Future[ListMap[Player, Int]]

  def findPlayerByID(id: String): Future[Either[String, Player]]
}

class LeagueRepositoryImpl @Inject()(dbApi: DBApi)(implicit ec: ExecutionContext) extends LeagueRepository with LazyLogging {
  var league = League.empty
  val db = dbApi.database("default")

  val playerParser = {
    (get[Option[Long]]("player.id") ~
      get[String]("player.name")). map {
      case id ~ name =>
        Player2(id, name)
    }
  }

  override def findPlayerById(id: Long) = Future {
    db.withConnection { implicit connection =>
      SQL"select * from player where id = $id".as(playerParser.singleOpt)
    }
  }(ec)

  override def insertPlayer(player: Player2) = Future {
    db.withConnection { implicit connection =>
      implicit val toParams = Macro.toParameters[Player2]
      SQL(
        """insert into player (id, name) values (nextval('player_id_seq'), {name});"""
      ).bind(player).executeInsert()
    }
  }(ec)

  override def addPlayer(player: Player): Future[Unit] =
    Future.successful {
      val playerNames = league.players.keys.map(player => player.name.toLowerCase).toList
      if(playerNames.contains(player.name.toLowerCase)) {
        logger.warn("Could not add this player to the league as the name is already taken")
        league = league
      }
      else
        league = League(league.games, league.players + (player -> 1500))
    }

  override def deletePlayer(player: Player): Future[Unit] =
    Future.successful {
      val activatedPlayers = league.games.flatMap { game =>
        game.leftSide ++ game.rightSide
      }
      if (activatedPlayers.contains(player)) {
        logger.warn("Can not delete a player who has already played a game")
        league = league
      }
      else
        league = League(league.games, league.players.filter(p => p._1 != player))
    }

  override def addGame(game: Game): Future[Unit] =
    Future.successful {
      league = League.addGame(game, league)
    }

  override def deleteGame(game: Game): Future[Unit] = {
    def refillLeague(games: List[Game], league: League): League = games match {
      case List() => league
      case x => refillLeague(games.tail, League.addGame(games.head, league))
    }
    val gameList = league.games.filter(g => g != game)
    val leveledElos = for ((player,elo) <- league.players) yield player -> 1500
    val emptiedLeague = League(List.empty, leveledElos)
    Future.successful {
      league = refillLeague(gameList, emptiedLeague)
    }
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

  override def findPlayerByID(id: String): Future[Either[String, Player]] =
    Future.successful {
      league.players.keys.filter(player => player.id == id).toList match {
        case List() => Left("Can not find player with this id")
        case x => Right(x.head)
      }
    }
}
