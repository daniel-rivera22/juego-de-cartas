package es.daniylorena.juegodecartas.logic;

import es.daniylorena.juegodecartas.display.GameDisplay;
import es.daniylorena.juegodecartas.display.GameDisplayInterface;
import es.daniylorena.juegodecartas.state.*;
import es.daniylorena.juegodecartas.utilities.CircularList;

import java.util.*;


public class GameController implements GameControllerInterface {

    private static final GameController instance = new GameController();

    private GameDisplayInterface gameDisplay;
    private Game currentGame;
    private final RoleAssigner roleAssigner;
    private final Dealer dealer;

    private GameController() {
        this.roleAssigner = new RoleAssigner();
        this.dealer = new Dealer();
    }

    public static GameController getInstance() {
        return instance;
    }

    public GameDisplayInterface getGameDisplay() {
        return gameDisplay;
    }

    public void setGameDisplay(GameDisplayInterface gameDisplay) {
        this.gameDisplay = gameDisplay;
    }

    public Game getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(Game currentGame) {
        this.currentGame = currentGame;
    }

    public RoleAssigner getRoleAssigner() {
        return roleAssigner;
    }

    @Override
    public void launchGame(ArrayList<String> playersNames) {
        boolean rematch;
        do {
            ArrayList<Player> players = initializePlayers(playersNames);
            Deck deck = new Deck();
            this.currentGame = new Game(players, deck);
            this.roleAssigner.initializeRoles(players.size());
            this.currentGame.shuffleDeck();
            this.dealer.divideCards(players, deck);
            this.dealer.applyRolesIfDefined(players);
            singleGameLoop();
            rematch = this.gameDisplay.askForRematch();
        } while (rematch);
    }

    private ArrayList<Player> initializePlayers(ArrayList<String> playersNames) {
        ArrayList<Player> players = new ArrayList<>();
        for (String name : playersNames) {
            Player player = new Player(name);
            players.add(player);
        }
        return players;
    }

    private void singleGameLoop() {
        boolean endGame;
        do {
            Round round = new Round(generateRoundPlayers(this.currentGame.getRounds().size()));
            this.currentGame.addRound(round);
            roundLoop();
            endGame = this.currentGame.checkEndGame();
        } while (!endGame);
    }

    private CircularList<Player> generateRoundPlayers(int i) {
        CircularList<Player> roundPlayers;
        if (i == 0) {
            roundPlayers = new CircularList<>(this.currentGame.getPlayers());
        } else {
            Player roundWinner = this.currentGame.getCurrentRound().getWinner();
            List<Player> previousRoundPlayers = this.currentGame.getCurrentRound().getSimpleListOfSubplayers();
            List<Player> actualRoundPlayers = new ArrayList<>(previousRoundPlayers);
            actualRoundPlayers.remove(roundWinner);
            roundPlayers = new CircularList<>(actualRoundPlayers);
        }
        return roundPlayers;
    }

    private void roundLoop() {
        Round round = this.currentGame.getCurrentRound();

        Player player;
        Move move;
        boolean endOfRound = false;
        do {
            player = round.getActualRoundPlayers().next();
            move = executeTurn(player);
            if (move.isCloseMove()) {
                round.setWinner(player);
                endOfRound = true;
            }
            if (player.getHand().isEmpty()) {
                this.roleAssigner.assignRole(player);
            }
        } while (!endOfRound && this.currentGame.checkEndGame());
    }

    private Move executeTurn(Player player) {
        Round round = this.currentGame.getCurrentRound();
        boolean invalidMove = true;
        Move move;
        do {
            move = this.gameDisplay.askForAMove(player);
            if (round.playMove(move)) {
                invalidMove = false;
                if (round.isPlin()) {
                    Player skipped = round.getActualRoundPlayers().next();
                    this.gameDisplay.notifyPlin(skipped.getName());
                }
            } else gameDisplay.notifyInvalidMove(move);
        } while (invalidMove);
        return move;
    }
}
