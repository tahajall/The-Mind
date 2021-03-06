package Server;

import Game.Game;
import Model.Player.Bot;
import Model.Player.Human;
import Model.Player.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class GameHandler {
    private final int numberOfPlayers;
    private Game game;
    private final ArrayList<ClientHandler> clientHandlers;
    private final HashMap<Player,ClientHandler> humanHashMap;
    private final ArrayList<Player> players;
    private boolean isFull;
    private final int number;

    GameHandler(int numberOfPlayers, int number){
        this.numberOfPlayers = numberOfPlayers;
        clientHandlers = new ArrayList<>();
        humanHashMap = new HashMap<>();
        isFull = false;
        this.number = number;
        players = new ArrayList<>();
    }

    public void addPlayer(ClientHandler clientHandler){
        if (!isFull) {
            clientHandlers.add(clientHandler);
            Human human = new Human(clientHandler);
            players.add(human);
            humanHashMap.put(human ,clientHandler);
            if (clientHandlers.size() == numberOfPlayers){
                isFull = true;
            }
        }
    }

    public void startGame(){
        int numberOfHumans = clientHandlers.size();
        if (numberOfHumans < numberOfPlayers){
            for (int i = 0; i < numberOfPlayers - numberOfHumans; i++){
                Bot bot = new Bot(i);
                players.add(bot);
            }
        }
        game = new Game(players);
        game.setGameNumber(number);
        for (ClientHandler clientHandler:clientHandlers){
            Connection connection = clientHandler.getConnection();
            connection.send(new Message("The game is Started.", clientHandler.getToken(),"0"));
        }
        isFull=true;
        GameInterface gameInterface = new GameInterface(game,this, humanHashMap);
        gameInterface.runGame();
        game.startPlayers();
    }

    public void MessageToHost(String message){
        ClientHandler host = null;
        for (ClientHandler m:clientHandlers){
            if (m.isHost()){
                host=m;
            }
        }
        assert host != null;
        Connection connection = host.getConnection();
        connection.send(new Message(message,host.getToken(),"0"));
    }

    public void MessageToAllClients(String message , String senderName){
        for (ClientHandler m:clientHandlers){
            if (!m.getName().equals(senderName)){
                Connection connection = m.getConnection();
                connection.send(new Message(senderName + ": " + message, m.getToken(),"0"));
            }
        }
    }

    public boolean isFull(){
        return isFull;
    }

    public Game getGame() {
        return game;
    }
}
