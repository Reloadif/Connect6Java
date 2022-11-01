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

        Pair<Boolean, FinishGameParameter> pair = isGameFinish();
        if (pair.getFirst()) {
            finishGame(pair.getSecond());
        }

        reentrantLock.unlock();
    }

    private Pair<Boolean, FinishGameParameter> isGameFinish() {
        Pair<Boolean, FinishGameParameter> result = new Pair<>(false, null);

        int[] horizontalBlack = new int[FIELD_SIZE];
        int[] horizontalWhite = new int[FIELD_SIZE];

        int[] verticalBlack = new int[FIELD_SIZE];
        int[] verticalWhite = new int[FIELD_SIZE];

        for(int i = 0; i < FIELD_SIZE; i++) {
            for(int j = 0; j < FIELD_SIZE; j++) {
                if (gameField[i][j] != -1) {
                    if(gameField[i][j] == 0) {
                        horizontalBlack[i] += 1;
                        verticalBlack[j] += 1;
                    }
                    else {
                        horizontalWhite[i] += 1;
                        verticalWhite[j] += 1;
                    }
                }
            }
        }

        for(int i = 0; i < FIELD_SIZE; ++i) {
            System.out.println(horizontalBlack[i] + " " + horizontalWhite[i] + " " + verticalBlack[i] + " " + verticalWhite[i]);
        }

        if (Arrays.stream(horizontalBlack).filter(i -> i == 6).findAny().isPresent() || Arrays.stream(horizontalWhite).filter(i -> i == 6).findAny().isPresent()) {
            result.setFirst(true);
            result.setSecond(new FinishGameParameter("0x000000ff"));
        }
        else if(Arrays.stream(verticalBlack).filter(i -> i == 6).findAny().isPresent() || Arrays.stream(verticalWhite).filter(i -> i == 6).findAny().isPresent()) {
            result.setFirst(true);
            result.setSecond(new FinishGameParameter("0xffffffff"));
        }

        return result;
    }

    private void finishGame(FinishGameParameter parameter) {
        try {
            players.getFirst().finishGame(parameter);
            players.getSecond().finishGame(parameter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
