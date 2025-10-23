import kotlin.random.Random

// Оружие
data class Weapon(val name: String, val attack: Int)

// Броня
class Armor(val name: String, val defense: Int, var durability: Int) {
    fun takeDamage(damage: Int): Int {
        if (durability <= 0) return damage // броня сломана

        val blocked = minOf(defense, damage)
        durability -= blocked / 2 // прочность снижается

        return damage - blocked
    }

    fun isBroken(): Boolean = durability <= 0
}

// Игровой класс
abstract class PlayerClass(
    val name: String,
    val maxHealth: Int,
    val armor: Armor,
    val weapon: Weapon
) {
    var health: Int = maxHealth
        private set

    fun takeDamage(damage: Int) {
        val actualDamage = armor.takeDamage(damage)
        health -= actualDamage
        if (health < 0) health = 0
    }

    fun attack(): Int {
        // Бросок кубика 1-6 добавляется к атаке оружия
        return weapon.attack + Random.nextInt(1, 7)
    }

    fun isAlive(): Boolean = health > 0

    fun reset() {
        health = maxHealth
        armor.durability = 50 // восстанавливаем прочность брони
    }

    fun getStatus(): String {
        val armorStatus = if (armor.isBroken()) "СЛОМАНА" else "${armor.durability}"
        return "$name: Здоровье $health/$maxHealth, Броня: $armorStatus"
    }
}

// Конкретные классы
class Paladin : PlayerClass(
    "Паладин",
    120,
    Armor("Тяжелая броня", 8, 50),
    Weapon("Меч", 12)
)

class Rogue : PlayerClass(
    "Разбойник",
    90,
    Armor("Кожаная броня", 5, 50),
    Weapon("Кинжалы", 18)
)

class Archer : PlayerClass(
    "Лучник",
    70,
    Armor("Легкая броня", 3, 50),
    Weapon("Лук", 22)
)

// Игрок
class Player(val name: String, val playerClass: PlayerClass) {
    fun makeTurn(opponent: Player): String {
        val attackValue = playerClass.attack()
        val opponentDefense = opponent.playerClass.armor.defense

        opponent.playerClass.takeDamage(attackValue)

        return "$name (${playerClass.name}) атакует на $attackValue урона. " +
                "${opponent.name} получает ${attackValue - opponentDefense} урона после брони."
    }

    fun isAlive(): Boolean = playerClass.isAlive()

    fun getStatus(): String = playerClass.getStatus()

    fun reset() {
        playerClass.reset()
    }
}

// Игра
class DuelGame {
    private lateinit var player1: Player
    private lateinit var player2: Player
    private var currentPlayer: Player? = null

    fun startGame() {
        println("Дуэль")
        println("Выберите классы для игроков:")
        println("1. Паладин (много здоровья, слабая атака, тяжелая броня)")
        println("2. Разбойник (среднее здоровье, сильная атака, средняя броня)")
        println("3. Лучник (мало здоровья, очень сильная атака, легкая броня)")

        player1 = createPlayer(1)
        player2 = createPlayer(2)

        // Случайно выбираем, кто ходит первым
        currentPlayer = if (Random.nextBoolean()) player1 else player2

        println("\nНачинается дуэль между:")
        println("${player1.name} - ${player1.playerClass.name}")
        println("${player2.name} - ${player2.playerClass.name}")
        println("Первым ходит: ${currentPlayer!!.name}")
        println("−".repeat(50))

        gameLoop()
    }

    private fun createPlayer(playerNumber: Int): Player {
        print("Введите имя игрока $playerNumber: ")
        val name = readLine() ?: "Игрок$playerNumber"

        var classChoice: Int
        do {
            print("Выберите класс для $name (1-3): ")
            classChoice = readLine()?.toIntOrNull() ?: 0
        } while (classChoice !in 1..3)

        val playerClass = when (classChoice) {
            1 -> Paladin()
            2 -> Rogue()
            3 -> Archer()
            else -> Paladin()
        }

        return Player(name, playerClass)
    }

    private fun gameLoop() {
        var turn = 1

        while (player1.isAlive() && player2.isAlive()) {
            println("\n--- Ход $turn ---")
            println(player1.getStatus())
            println(player2.getStatus())
            println()

            val attacker = currentPlayer!!
            val defender = if (currentPlayer == player1) player2 else player1

            // Ход текущего игрока
            val result = attacker.makeTurn(defender)
            println(result)

            // Проверяем, не умер ли защитник
            if (!defender.isAlive()) {
                println("\n⚔️ ${defender.name} пал в бою!")
                println("🏆 ${attacker.name} побеждает!")
                break
            }

            // Меняем текущего игрока
            currentPlayer = defender
            turn++

            // Пауза между ходами
            println("Нажмите Enter для продолжения...")
            readLine()
        }

        if (!player1.isAlive() && !player2.isAlive()) {
            println("\n💀 Оба игрока пали в бою! Ничья!")
        }

        askForRematch()
    }

    private fun askForRematch() {
        println("\nХотите сыграть еще раз? (да/нет)")
        val answer = readLine()?.lowercase()

        if (answer == "да" || answer == "д" || answer == "y" || answer == "yes") {
            player1.reset()
            player2.reset()
            println("\n" + "=".repeat(50))
            startGame()
        } else {
            println("Спасибо за игру!")
        }
    }
}

// Главная функция
fun main() {
    val game = DuelGame()
    game.startGame()
}