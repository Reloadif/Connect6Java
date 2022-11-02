package main.net.server;

import main.infrastructure.Pair;
import main.net.packet.CommonPacket;
import main.net.packet.commandParameters.FinishGameParameter;
import main.net.packet.commandParameters.PlaceChipParameter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Connect6GameField {
    public final int FIELD_SIZE = 9;
    public final int[][] gameField = new int[FIELD_SIZE][FIELD_SIZE];
    public Pair<Connect6Worker, Connect6Worker> players;

    private final ReentrantLock reentrantLock = new ReentrantLock();

    public Connect6GameField(Pair<Connect6Worker, Connect6Worker> players) {
        for (int[] ints : gameField) {
            Arrays.fill(ints, -1);
        }

        this.players = players;
    }

    public void connectPlayersToGame() throws IOException {
        Random random = new Random();
        boolean randomValue = random.nextBoolean();

        players.getFirst().clientPlaceChipTcpEvent.addListener(this::onClientPlaceChipEvent);
        players.getSecond().clientPlaceChipTcpEvent.addListener(this::onClientPlaceChipEvent);

        players.getFirst().clientPlaceChipTcpEvent.addListener(players.getSecond()::placeChip);
        players.getSecond().clientPlaceChipTcpEvent.addListener(players.getFirst()::placeChip);

        players.getFirst().connectToGame(FIELD_SIZE, randomValue ? "0xffffffff" : "0x000000ff");
        players.getSecond().connectToGame(FIELD_SIZE, randomValue ? "0x000000ff" : "0xffffffff");
    }

    private void onClientPlaceChipEvent(CommonPacket packet) {
        reentrantLock.lock();

        PlaceChipParameter parameter = (PlaceChipParameter)packet.commandParameter;
        gameField[parameter.row][parameter.column] = parameter.chipColor.equals("0x000000ff") ? 0 : 1;

        Pair<Boolean, FinishGameParameter> pair = isGameFinish(parameter.chipColor);
        if (pair.getFirst()) {
            finishGame(pair.getSecond());
        }

        reentrantLock.unlock();
    }

    private Pair<Boolean, FinishGameParameter> isGameFinish(String chipColor) {
        Pair<Boolean, FinishGameParameter> result = new Pair<>(false, null);

        if(areSixConnected(chipColor.equals("0x000000ff") ? 0 : 1)) {
            result.setFirst(true);
            result.setSecond(new FinishGameParameter(chipColor));
        }

        return result;
    }

    public boolean areSixConnected(int player){
        // horizontalCheck
        for (int j = 0; j < FIELD_SIZE - 5; ++j) {
            for (int i = 0; i < FIELD_SIZE; ++i) {
                if (gameField[i][j] == player && gameField[i][j+1] == player &&
                    gameField[i][j+2] == player && gameField[i][j+3] == player &&
                    gameField[i][j+4] == player && gameField[i][j+5] == player) {
                    return true;
                }
            }
        }
        // verticalCheck
        for (int i = 0; i < FIELD_SIZE - 5; ++i) {
            for (int j = 0; j < FIELD_SIZE; ++j) {
                if (gameField[i][j] == player && gameField[i+1][j] == player &&
                    gameField[i+2][j] == player && gameField[i+3][j] == player &&
                    gameField[i+4][j] == player && gameField[i+5][j] == player) {
                    return true;
                }
            }
        }
        // ascendingDiagonalCheck
        for (int i = 5; i < FIELD_SIZE; ++i) {
            for (int j = 0; j < FIELD_SIZE - 5; ++j) {
                if (gameField[i][j] == player && gameField[i-1][j+1] == player &&
                    gameField[i-2][j+2] == player && gameField[i-3][j+3] == player &&
                    gameField[i-4][j+4] == player && gameField[i-5][j+5] == player)
                    return true;
            }
        }
        // descendingDiagonalCheck
        for (int i = 5; i < FIELD_SIZE; ++i) {
            for (int j = 3; j < FIELD_SIZE; ++j) {
                if (gameField[i][j] == player && gameField[i-1][j+1] == player &&
                    gameField[i-2][j+2] == player && gameField[i-3][j+3] == player &&
                    gameField[i-4][j+4] == player && gameField[i-5][j+5] == player)
                    return true;
            }
        }

        return false;
    }

    private void finishGame(FinishGameParameter parameter) {
        try {
            players.getFirst().finishGame(parameter);
            players.getSecond().finishGame(parameter);

            players.setFirst(null);
            players.setSecond(null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
