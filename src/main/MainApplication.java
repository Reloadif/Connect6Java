package main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.controllers.GameController;
import main.controllers.LauncherController;
import main.net.client.Connect6Client;
import main.net.packet.CommonPacket;
import main.net.packet.commandParameters.ConnectGameParameter;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.*;

public class MainApplication extends Application {
    private static Scene mainScene;
    private static final Map<String, FXMLLoader> mapOfLoader = new HashMap<>();
    private static final Map<String, Parent> mapOfParent = new HashMap<>();

    public static Connect6Client connect6Client;

    @Override
    public void start(Stage primaryStage) {
        try {
            mapOfLoader.put("Launcher", new FXMLLoader(getClass().getResource("views/Launcher.fxml")));
            mapOfLoader.put("Game", null);

            mapOfParent.put("Launcher", mapOfLoader.get("Launcher").load());
            mapOfParent.put("Game", null);

            mainScene = new Scene(mapOfParent.get("Launcher"), 500, 600);

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
            if (key.equals("Game")) {
                mapOfLoader.put("Game", new FXMLLoader(MainApplication.class.getResource("views/Game.fxml")));
                try {
                    mapOfParent.put("Game", mapOfLoader.get("Game").load());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                connect6Client = new Connect6Client();
                mapOfLoader.get("Launcher").<LauncherController>getController().StartButton.setDisable(true);
                Task<Void> task = new Task<>() {
                    @Override public Void call() {
                        CommonPacket packet = connect6Client.waitConnectionToGame();
                        Platform.runLater(() -> {
                            mainScene.setRoot(mapOfParent.get(key));
                            mapOfLoader.get(key).<GameController>getController().initializeController((ConnectGameParameter) packet.commandParameter);
                            mapOfLoader.get("Launcher").<LauncherController>getController().StartButton.setDisable(false);
                        });
                        return null;
                    }
                };
                new Thread(task).start();
            }
            else mainScene.setRoot(mapOfParent.get(key));
        }
        else {
            throw new InvalidParameterException();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}