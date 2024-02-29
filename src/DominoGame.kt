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
        val doubleSix = hands.values.flatten().find { it.left == 6 && it.right == 6 }
        if (doubleSix != null) {
            currentPlayerIndex = players.indexOfFirst { hands[it]?.contains(doubleSix) == true }
            return Pair(currentPlayerIndex, doubleSix)
        }

        // check for doubles
        val doubles = hands.values.flatten().filter { it.left == it.right }
        val sortedDoubles = doubles.sortedByDescending { it.left }

        if (sortedDoubles.isNotEmpty()) {
            val startingDomino = sortedDoubles.first()
            currentPlayerIndex = players.indexOfFirst { hands[it]?.contains(startingDomino) == true }
            return Pair(currentPlayerIndex, startingDomino)
        }

        // if no doubles search highest
        val allDominos = hands.values.flatten()
        val maxDomino = allDominos.maxByOrNull { maxOf(it.left, it.right) }
        if (maxDomino != null) {
            currentPlayerIndex = players.indexOfFirst { hands[it]?.contains(maxDomino) == true }
            return Pair(currentPlayerIndex, maxDomino)
        }
        return null
    }

    fun drawFromStock(player: String) {
        if (stock.isNotEmpty()) {
            val drawnDomino = stock.removeAt(Random.nextInt(stock.size))
            println("$player draws: $drawnDomino")
            hands[player]?.add(drawnDomino)
        } else println("Stock is empty, $player cannot draw.")
    }

    fun playDomino(player: String, domino: Domino) {
        if (hands[player]?.contains(domino) == true) {
            print("$player plays: $domino edges: ($leftExtreme ; $rightExtreme) -> ")
            hands[player]?.remove(domino)
            updateEdges(domino)
            println("($leftExtreme ; $rightExtreme)")
        } else println("$player does not have $domino in hand.")
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
        if (leftExtreme == -1 && rightExtreme == -1) {
            leftExtreme = domino.left
            rightExtreme = domino.right
        } else if (domino.left == rightExtreme) {
            rightExtreme = domino.right
        } else if (domino.right == leftExtreme) {
            leftExtreme = domino.left
        } else if (domino.left == leftExtreme) {
            leftExtreme = domino.right
        } else if (domino.right == rightExtreme) {
            rightExtreme = domino.left
        }
    }

    fun playRandomDomino(player: String) {
        val playerHand = hands[player]

        if (playerHand.isNullOrEmpty()) {
            //println("$player does not have dominoes to play.")
            return
        }

        var playableDominos = playerHand.filter { isDominoPlayable(it) }

        if (playableDominos.isEmpty()) {
            //println("$player does not have dominoes to play.")
            while (stock.isNotEmpty()) {
                val drawnDomino = stock.removeAt(Random.nextInt(stock.size))
                println("$player draws: $drawnDomino")
                hands[player]?.add(drawnDomino)

                if (isDominoPlayable(drawnDomino)) {
                    print("$player plays: $drawnDomino edges: ($leftExtreme ; $rightExtreme) -> ")
                    hands[player]?.remove(drawnDomino)
                    updateEdges(drawnDomino)
                    println("($leftExtreme ; $rightExtreme)")
                    return
                }
            }
            println("Stock is empty, $player cannot draw.")
            return
        }

        playableDominos = playerHand.filter { isDominoPlayable(it) }

        val dominoIndex = Random.nextInt(playableDominos.size)
        val domino = playableDominos[dominoIndex]

        playDomino(player, domino)
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

