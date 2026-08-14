package snakeandladder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// SNAKE AND LADDER — key insight: a snake and a ladder are the same mechanic
// (land here, get moved there), just opposite direction, so both are stored
// in one merged "jumps" map instead of two separate structures. Also covers
// the classic "must land exactly on the final cell to win" overshoot rule.

// A single player's identity and current board position.
class Player {
    private final String id;
    private final String name;
    private int position;

    public Player(String id, String name) { this.id = id; this.name = name; this.position = 0; }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
}

// The board: size, plus every snake head and ladder bottom merged into one
// start-cell -> end-cell map, since resolving either is the same lookup.
class Board {
    private final int size;
    private final Map<Integer, Integer> jumps;

    public Board(int size, Map<Integer, Integer> snakes, Map<Integer, Integer> ladders) {
        this.size = size;
        this.jumps = new HashMap<>();

        for (Map.Entry<Integer, Integer> entry : snakes.entrySet()) {
            if (entry.getKey() <= entry.getValue()) {
                throw new IllegalArgumentException("Snake head " + entry.getKey() + " must be above its tail " + entry.getValue());
            }
            addJump(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Integer, Integer> entry : ladders.entrySet()) {
            if (entry.getKey() >= entry.getValue()) {
                throw new IllegalArgumentException("Ladder bottom " + entry.getKey() + " must be below its top " + entry.getValue());
            }
            addJump(entry.getKey(), entry.getValue());
        }
    }

    private void addJump(int from, int to) {
        if (jumps.containsKey(from)) {
            throw new IllegalArgumentException("Cell " + from + " already has a snake or ladder starting there");
        }
        jumps.put(from, to);
    }

    public int getSize() { return size; }

    // Resolves a landing spot: applies a snake/ladder if one starts there, otherwise no-op.
    public int getFinalPosition(int position) { return jumps.getOrDefault(position, position); }
}

// Rolls 1..sides. Seedable so a demo/test can get reproducible rolls.
class Dice {
    private final int sides;
    private final Random random;

    public Dice(int sides) { this(sides, System.nanoTime()); }
    public Dice(int sides, long seed) { this.sides = sides; this.random = new Random(seed); }

    public int roll() { return random.nextInt(sides) + 1; }
}

// What happened on one turn - who moved, what they rolled, and where they ended up.
class TurnResult {
    private final Player player;
    private final int diceRoll;
    private final int positionBefore;
    private final int positionAfter;
    private final boolean won;

    public TurnResult(Player player, int diceRoll, int positionBefore, int positionAfter, boolean won) {
        this.player = player;
        this.diceRoll = diceRoll;
        this.positionBefore = positionBefore;
        this.positionAfter = positionAfter;
        this.won = won;
    }

    public Player getPlayer() { return player; }
    public int getDiceRoll() { return diceRoll; }
    public int getPositionBefore() { return positionBefore; }
    public int getPositionAfter() { return positionAfter; }
    public boolean isWon() { return won; }
}

// Orchestrator: owns the board, dice, and turn order; plays one turn at a time.
public class SnakeAndLadderGame {
    private final Board board;
    private final Dice dice;
    private final List<Player> players;
    private int currentPlayerIndex;
    private Player winner;

    public SnakeAndLadderGame(Board board, Dice dice, List<Player> players) {
        this.board = board;
        this.dice = dice;
        this.players = players;
        this.currentPlayerIndex = 0;
        this.winner = null;
    }

    public TurnResult playTurn() {
        if (winner != null) {
            throw new IllegalStateException("Game is already over");
        }

        Player player = players.get(currentPlayerIndex);
        int roll = dice.roll();
        int positionBefore = player.getPosition();
        int tentative = positionBefore + roll;

        // Overshoot: a real roll of the dice that would go past the last cell
        // doesn't count - the player stays put and the turn passes on.
        int positionAfter = (tentative > board.getSize()) ? positionBefore : board.getFinalPosition(tentative);
        player.setPosition(positionAfter);

        boolean won = positionAfter == board.getSize();
        if (won) {
            winner = player;
        } else {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }

        return new TurnResult(player, roll, positionBefore, positionAfter, won);
    }

    public boolean isGameOver() { return winner != null; }
    public Player getWinner() { return winner; }

    public static void main(String[] args) {
        Map<Integer, Integer> snakes = new HashMap<>();
        snakes.put(16, 6);
        snakes.put(47, 26);
        snakes.put(49, 11);
        snakes.put(56, 53);
        snakes.put(62, 19);
        snakes.put(64, 60);
        snakes.put(87, 24);
        snakes.put(93, 73);
        snakes.put(95, 75);
        snakes.put(98, 78);

        Map<Integer, Integer> ladders = new HashMap<>();
        ladders.put(1, 38);
        ladders.put(4, 14);
        ladders.put(9, 31);
        ladders.put(21, 42);
        ladders.put(28, 84);
        ladders.put(36, 44);
        ladders.put(51, 67);
        ladders.put(71, 91);
        ladders.put(80, 100);

        Board board = new Board(100, snakes, ladders);

        // Deterministic checks on the jump-resolution algorithm itself, independent of dice randomness.
        System.out.println("getFinalPosition(16) [snake head] = " + board.getFinalPosition(16) + " (expect 6)");
        System.out.println("getFinalPosition(1) [ladder bottom] = " + board.getFinalPosition(1) + " (expect 38)");
        System.out.println("getFinalPosition(50) [plain cell] = " + board.getFinalPosition(50) + " (expect 50)");

        try {
            new Board(100, Map.of(10, 20), Map.of()); // "snake" head below its tail - invalid
        } catch (IllegalArgumentException e) {
            System.out.println("Rejected bad snake: " + e.getMessage());
        }

        Dice dice = new Dice(6, 42L); // fixed seed for a reproducible run
        List<Player> players = List.of(new Player("p1", "Alice"), new Player("p2", "Bob"));
        SnakeAndLadderGame game = new SnakeAndLadderGame(board, dice, players);

        System.out.println("\nPlaying...");
        while (!game.isGameOver()) {
            TurnResult result = game.playTurn();
            // A real move always changes position (dice rolls are >= 1), so positionAfter
            // == positionBefore can only mean the overshoot rule kicked in.
            String jumpNote;
            if (result.getPositionAfter() == result.getPositionBefore()) {
                jumpNote = " (overshoot, stayed put)";
            } else if (result.getPositionAfter() != result.getPositionBefore() + result.getDiceRoll()) {
                jumpNote = " (snake/ladder!)";
            } else {
                jumpNote = "";
            }
            System.out.println(result.getPlayer().getName() + " rolled " + result.getDiceRoll()
                    + ": " + result.getPositionBefore() + " -> " + result.getPositionAfter() + jumpNote);
        }
        System.out.println(game.getWinner().getName() + " wins!");

        // Bounds check: every roll must be within [1, 6].
        Dice boundsCheckDice = new Dice(6);
        boolean allInBounds = true;
        for (int i = 0; i < 1000; i++) {
            int roll = boundsCheckDice.roll();
            if (roll < 1 || roll > 6) allInBounds = false;
        }
        System.out.println("\n1000 dice rolls all within [1,6]: " + allInBounds);
    }
}
