import kotlin.random.Random

data class Domino(val left: Int, val right: Int) {
    override fun toString(): String = "[ $left | $right ]"
}

class DominoGame(private val players: List<String>) {
    private val handSize: Int = if (players.size == 2) 7 else 5
    private val dominos = mutableListOf<Domino>()
    private val hands = mutableMapOf<String, MutableList<Domino>>() // players hands
    private val stock = mutableListOf<Domino>() // bazar
    private var leftExtreme: Int = -1
    private var rightExtreme: Int = -1
    var currentPlayerIndex = 0
    var gameOver = false

    init {
        (0..6).forEach { i ->
            (i..6).forEach { j ->
                dominos.add(Domino(i, j))
            }
        }
    }

    fun startGame() {
        dominos.shuffle()
        initializeHands()
        stock.addAll(dominos)
        determineStartingPlayer()
    }

    private fun initializeHands() {
        players.forEach {
            hands[it] = dominos.subList(0, handSize).toMutableList()
            dominos.subList(0, handSize).clear()
        }
    }

    private fun determineStartingPlayer(): Pair<Int, Domino>? {
        val allDominos = hands.values.flatten() //unzip collection of collection, take only values (dominos)
        val doubleSix = allDominos.find { it.left == 6 && it.right == 6 } // 6 6
        val nonZeroDoubles = allDominos.filter { it.left == it.right && it.left != 0 } // doubles without 0 0
        val maxDomino = allDominos.filter { it.left != 0 && it.right != 0 }.maxByOrNull { maxOf(it.left, it.right) } //highest

        val startingDomino = doubleSix ?: nonZeroDoubles.maxByOrNull { it.left } ?: maxDomino

        if (startingDomino != null) {
            currentPlayerIndex = players.indexOfFirst { hands[it]?.contains(startingDomino) == true } //finding player with it
            return currentPlayerIndex to startingDomino
        }
        return null
    }

    fun playDomino(player: String, domino: Domino) {
        if (hands[player]?.contains(domino) == true) {
            //print("$player plays: $domino edges: ($leftExtreme ; $rightExtreme) -> ")
            println("$player plays: $domino")
            hands[player]?.remove(domino)
            updateEdges(domino)
            //val cnt = hands[player]?.size
            //println("($leftExtreme ; $rightExtreme) cnt: ($cnt)")
        }
    }

    fun nextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size
    }

    fun checkGameEnd(): Boolean {
        return hands.any { it.value.isEmpty() }
    }

    fun displayGameState() {
        println("======= Game State =======")
        hands.forEach { (player, hand) ->
            println("$player's hand: $hand")
        }
        println("Stock size: ${stock.size}")
        println("Current player: ${players[currentPlayerIndex]}\n")
    }

    fun isDominoPlayable(domino: Domino): Boolean {
        return domino.left == leftExtreme || domino.right == rightExtreme ||
                domino.left == rightExtreme || domino.right == leftExtreme
    }

    fun updateEdges(domino: Domino) {
        // Update leftExtreme and rightExtreme according to the played domino
        when {
            leftExtreme == -1 && rightExtreme == -1 -> {
                leftExtreme  = domino.left
                rightExtreme = domino.right
            }
            domino.left == rightExtreme -> rightExtreme = domino.right
            domino.right == leftExtreme -> leftExtreme = domino.left
            domino.left == leftExtreme -> leftExtreme = domino.right
            domino.right == rightExtreme -> rightExtreme = domino.left
        }
    }
    fun calculateHandScore(hand: List<Domino>): Int {
        var score = 0
        hand.forEach {
            score += if (it.left == 0 && it.right == 0) {
                25
            } else {
                it.left + it.right
            }
        }
        return score
    }

    fun playRandomDomino(player: String) {
        val playerHand = hands[player]

        if (playerHand.isNullOrEmpty()) {
            println("$player does not have dominoes to play.")
            return
        }

        val playableDominos = playerHand.filter { isDominoPlayable(it) }

        if (playableDominos.isEmpty()) {
            if (stock.isEmpty()) {
                val minScorePlayer = hands
                    .map { (player, hand) -> player to calculateHandScore(hand) }
                    .minByOrNull { it.second }?.first

                println("All players are in a stalemate. $minScorePlayer wins with the lowest hand score.")
                gameOver = true
            } else {
                while (stock.isNotEmpty()) {
                    val drawnDomino = stock.removeAt(Random.nextInt(stock.size))
                    println("$player draws: $drawnDomino")
                    hands[player]?.add(drawnDomino)

                    if (isDominoPlayable(drawnDomino)) {
                        playDomino(player, drawnDomino)
                        return
                    }
                }
            }
        } else {
            val domino = playableDominos.random()
            playDomino(player, domino)
        }
    }


    fun randomSimulate() {
        startGame()
        displayGameState()

        val startingDomino = determineStartingPlayer()
        if (startingDomino != null) {
            val (playerIndex, domino) = startingDomino
            val firstPlayer = players[playerIndex]
            println("$firstPlayer starts the game with domino: $domino!")
            playDomino(firstPlayer, domino)
            nextPlayer()
        }

        while (!gameOver) {
            val currentPlayer = players[currentPlayerIndex]
            playRandomDomino(currentPlayer)
            nextPlayer()

            if (checkGameEnd()) {
                gameOver = true
                println("$currentPlayer won, game over!")
            }
        }
    }
}

