package main.net.server;

import main.event.TcpEvent;
import main.net.packet.CommandType;
import main.net.packet.CommonPacket;
import main.net.packet.HeaderType;
import main.net.packet.commandParameters.ConnectGameParameter;
import main.net.packet.commandParameters.DisconnectParameter;
import main.net.packet.commandParameters.FinishGameParameter;

import java.io.*;
import java.net.Socket;

public class Connect6Worker extends Thread {
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;

    private boolean runnable = true;

    public final TcpEvent clientPlaceChipTcpEvent = new TcpEvent();

    public Connect6Worker(Socket socket) throws IOException {
        this.socket = socket;
        in = new ObjectInputStream(this.socket.getInputStream());
        out = new ObjectOutputStream(this.socket.getOutputStream());

        start();
    }

    @Override
    public void run() {
        try {
            while (runnable) {
                CommonPacket packet = (CommonPacket)in.readObject();

                System.out.println("Received packet:");
                System.out.println(packet.command);
                System.out.println(packet.commandParameter);

                handleCommonPacket(packet);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ClassNotFoundException cne) {
            System.out.println("Wanted class CommonPacket, but got class " + cne);
            cne.printStackTrace();
        }
    }

    public void connectToGame(int fieldSize, String chipColor) throws IOException {
        out.writeObject(new CommonPacket(
                HeaderType.TO_CLIENT,
                CommandType.CONNECT_GAME,
                new ConnectGameParameter(chipColor, fieldSize))
        );
    }

    public void finishGame(FinishGameParameter parameter) throws IOException {
        out.writeObject(new CommonPacket(
                HeaderType.TO_CLIENT,
                CommandType.FINISH_GAME,
                parameter)
        );
    }

    public void placeChip(CommonPacket packet) {
        try {
            packet.header = HeaderType.TO_CLIENT;
            out.writeObject(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleCommonPacket(CommonPacket packet) throws IOException {
        if (packet.isValidPacket() && packet.isServerPacket()) {
            switch (packet.command) {
                case PLACE_CHIP -> clientPlaceChipTcpEvent.invoke(packet);
                case DISCONNECT -> {
                    runnable = false;

                    out.writeObject(new CommonPacket(
                            HeaderType.TO_CLIENT,
                            CommandType.DISCONNECT,
                            new DisconnectParameter())
                    );

                    out.close();
                    in.close();
                    socket.close();
                }
            }
        }
    }
}