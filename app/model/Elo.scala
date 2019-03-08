package model

import scala.collection.immutable.ListMap

object Elo {

  val baseValue = 16
  val stretchParam = 200

  def updateEloForPlayer(game: Game, player: Player, league: League): Int = {
    val playersSide = if (game.leftSide.contains(player)) game.leftSide else game.rightSide
    val otherSide = if (playersSide == game.leftSide) game.rightSide else game.leftSide
    val diff: Int = (League.getEloForSide(otherSide, league) - League.getEloForSide(playersSide, league)) / playersSide.length
    val winProb = 1.0 / (1 + Math.pow(10, diff.toDouble / stretchParam))
    val numberOfGames = game.result.leftSets + game.result.rightSets
    val expectedWins = numberOfGames * winProb
    val actualWins = if (playersSide == game.leftSide) game.result.leftSets else game.result.rightSets

    Math.round(League.getEloForPlayer(player, league) + (actualWins - expectedWins) * baseValue).toInt
  }

  def updateAllElos(game: Game, league: League): ListMap[Player, Int] = {
    val allPlayers = game.leftSide ::: game.rightSide
    val newElos = allPlayers.map(player => player -> updateEloForPlayer(game, player, league)).toMap
    league.players ++ newElos
  }
}
