fun main() {
    val players = listOf("Alice", "Bob"/*, "Wawa", "Oleg"*/)

    val game = DominoGame(players)
    game.randomSimulate()
}