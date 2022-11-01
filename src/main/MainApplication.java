package main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.controllers.GameController;
import main.net.client.Connect6Client;
import main.net.packet.CommonPacket;
import main.net.packet.commandParameters.ConnectGameParameter;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.*;

public class MainApplication extends Application {
    private static Scene mainScene;
    private static final Map<String, FXMLLoader> mapOfLoader = new HashMap<>();

    public static Connect6Client connect6Client;

    @Override
    public void start(Stage primaryStage) {
        try {
            mapOfLoader.put("Launcher", new FXMLLoader(getClass().getResource("views/Launcher.fxml")));
            mapOfLoader.put("Game", new FXMLLoader(getClass().getResource("views/Game.fxml")));

            mainScene = new Scene(mapOfLoader.get("Launcher").load(), 500, 600);

            primaryStage.setTitle("Connect6");
            primaryStage.setResizable(false);
            primaryStage.setScene(mainScene);
            primaryStage.show();
        }
        catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void setSceneParent(String key) {
        if (mapOfLoader.containsKey(key)) {
            try {
                if (key.equals("Game")) {
                    connect6Client = new Connect6Client();
                    Task<Void> task = new Task<>() {
                        @Override public Void call() {
                            CommonPacket packet = connect6Client.waitConnectionToGame();
                            Platform.runLater(() -> {
                                try {
                                    mainScene.setRoot(mapOfLoader.get(key).load());
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                mapOfLoader.get(key).<GameController>getController().initializeController((ConnectGameParameter) packet.commandParameter);
                            });
                            return null;
                        }
                    };
                    new Thread(task).start();
                }
                else mainScene.setRoot(mapOfLoader.get(key).load());
            } catch (IOException e) {
                System.out.println("Can't setRoot for mainScene!");
                e.printStackTrace();
            }
        }
        else {
            throw new InvalidParameterException();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}