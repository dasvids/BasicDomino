import kotlin.random.Random

data class Domino(val left: Int, val right: Int){
    override fun toString(): String = "[ $left | $right ]"
}

class DominoGame(private val players: List<String>){
    private val handSize: Int = if (players.size == 2) 7 else 5
    private val dominos = mutableListOf<Domino>()
    val hands = mutableMapOf<String, MutableList<Domino>>() // players hands
    val stock = mutableListOf<Domino>() // bazar
    var currentPlayerIndex = 0
    var gameOver = false

    init{
        /*for (i in 0..6){
            for (j in i..6){
                dominos.add(Domino(i,j))
            }
        }*/
        (0..6).forEach { i ->
            (i..6).forEach{
                j -> dominos.add(Domino(i,j))
            }
        }
    }

    fun startGame(){
        dominos.shuffle()
        players.forEach {//player ->
//            hands[player] = mutableListOf()
//            hands[player]?.addAll(dominos.subList(0, handSize)) //adding dominos from stock
            hands[it] = dominos.subList(0, handSize).toMutableList()
            dominos.subList(0, handSize).clear() //removing dominos from stock
        }

        stock.addAll(dominos) // put all rest dominos to stock
    }

    fun drawFromStock(player: String){
        val drawnDomino = stock.removeAt(Random.nextInt(stock.size))
        println("$player draws: $drawnDomino")
        hands[player]?.add(drawnDomino)
    }

    fun playDomino(player: String, domino: Domino){
        println("$player plays: $domino")
        hands[player]?.remove(domino)
    }
}