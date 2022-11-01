package main.net.client;

import main.event.TcpEvent;
import main.net.packet.CommandType;
import main.net.packet.CommonPacket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Connect6Client {
    public final static String SERVER_HOSTNAME = "localhost";
    public final static int PORT = 5050;
    public final TcpEvent serverPlaceChipTcpEvent = new TcpEvent();
    public final TcpEvent serverFinishGameTcpEvent = new TcpEvent();

    public final Reader reader = new Reader();

    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public Connect6Client() {
        try {
            socket = new Socket(SERVER_HOSTNAME, PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (UnknownHostException uhe) {
            System.out.println("Don't know about host: " + SERVER_HOSTNAME);
            uhe.printStackTrace();
        } catch (IOException ioe) {
            System.out.println("Couldn't get I/O for the connection to: " + SERVER_HOSTNAME + ":" + PORT);
            ioe.printStackTrace();
        }
    }

    public CommonPacket waitConnectionToGame() {
        CommonPacket receivedPacket = new CommonPacket();
        try {
            receivedPacket = (CommonPacket)in.readObject();
            reader.start();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ClassNotFoundException cne) {
            System.out.println("Wanted class CommonPacket, but got class " + cne);
            cne.printStackTrace();
        }
        return receivedPacket;
    }

    public void sendRequest(CommonPacket packet) {
        try {
            System.out.println("Sent packet:");
            System.out.println(packet.command);
            System.out.println(packet.commandParameter);

            if (packet.command == CommandType.DISCONNECT) {
                out.writeObject(packet);

                out.close();
                in.close();
                socket.close();
            }
            else {
                out.writeObject(packet);
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private class Reader extends Thread {
        private boolean runnable = true;

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

        private void handleCommonPacket(CommonPacket packet) throws IOException {
            if (packet.isValidPacket() && packet.isClientPacket()) {
                switch (packet.command) {
                    case PLACE_CHIP -> serverPlaceChipTcpEvent.invoke(packet);
                    case FINISH_GAME -> serverFinishGameTcpEvent.invoke(packet);
                    case DISCONNECT -> runnable = false;
                }
            }
        }
    }
}
