package main.net.server;

import main.infrastructure.Pair;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Connect6Server {
    public final static int PORT = 5050;
    private ServerSocket serverSocket;

    private final List<Connect6GameField> gameSessions = new ArrayList<>();

    public Connect6Server()
    {
        initServerSocket();
        try {
            while (true) {
                Socket socket = serverSocket.accept();

                gameSessions.removeIf(el -> el.players.getFirst() == null && el.players.getSecond() == null);
                Optional<Connect6GameField> emptySession = gameSessions.stream().filter(el -> el.players.getFirst() == null || el.players.getSecond() == null ).findFirst();

                if(gameSessions.isEmpty() || emptySession.isEmpty()) {
                    gameSessions.add(new Connect6GameField(new Pair<>(new Connect6Worker(socket), null)));
                }
                else {
                    Connect6GameField currentSession = emptySession.get();
                    if (currentSession.players.getFirst() == null) {
                        currentSession.players.setFirst(new Connect6Worker(socket));
                    }
                    else {
                        currentSession.players.setSecond(new Connect6Worker(socket));
                    }

                    currentSession.connectPlayersToGame();
                }
            }
        } catch (SecurityException se) {
            System.err.println("Unable to get host address due to security.");
            se.printStackTrace();
            System.exit(1);
        } catch (IOException ioe) {
            System.err.println("Unable to read data from an open socket.");
            ioe.printStackTrace();
            System.exit(1);
        } finally {
            try {
                serverSocket.close();
            } catch (IOException ioe) {
                System.err.println("Unable to close an open socket.");
                ioe.printStackTrace();
                System.exit(1);
            }
        }
    }

    private void initServerSocket() {
        try {
            serverSocket = new java.net.ServerSocket(PORT);
            if (this.serverSocket.isBound()) {
                System.out.println("SERVER inbound data port " + serverSocket.getLocalPort() + " is ready and waiting for client to connect...");
            }
        } catch (SocketException se) {
            System.err.println("Unable to create socket.");
            se.printStackTrace();
            System.exit(1);
        } catch (IOException ioe) {
            System.err.println("Unable to read data from an open socket.");
            ioe.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        new Connect6Server();
    }
}
