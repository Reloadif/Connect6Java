package main.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import main.MainApplication;
import main.infrastructure.Pair;
import main.net.packet.CommandType;
import main.net.packet.CommonPacket;
import main.net.packet.HeaderType;
import main.net.packet.commandParameters.ConnectGameParameter;
import main.net.packet.commandParameters.DisconnectParameter;
import main.net.packet.commandParameters.FinishGameParameter;
import main.net.packet.commandParameters.PlaceChipParameter;

public class GameController {
    @FXML
    private Text ChipColor;
    @FXML
    private Text CurrentMoveName;
    @FXML
    private Text CurrentMoveCounter;
    @FXML
    private GridPane GameField;

    private Color chipColor;
    private int fieldSize;

    private StackPane[][] tiles;

    private boolean isMoving = true;
    private int currentMove = 0;

    public void initializeController(ConnectGameParameter parameter) {
        chipColor = Color.valueOf(parameter.chipColor);
        fieldSize = parameter.fieldSize;

        ChipColor.setText(hexColorToString(chipColor.toString()));
        CurrentMoveName.setText("You");
        CurrentMoveCounter.setText("2");

        tiles = new StackPane[fieldSize][fieldSize];

        for (int i = 0; i < fieldSize; ++i) {
            for (int j = 0; j < fieldSize; ++j) {
                Rectangle rectangle = new Rectangle(50, 50, Color.LIGHTGRAY);
                Ellipse ellipse = new Ellipse(12.5, 12.5);

                rectangle.setStroke(Color.DARKGRAY);
                rectangle.setOnMouseClicked(event -> drawEllipse(ellipse));
                ellipse.setFill(Color.TRANSPARENT);
                ellipse.setMouseTransparent(true);

                StackPane tile = new StackPane(rectangle, ellipse);
                tile.setDisable(true);

                GameField.add(tile, j, i);
                tiles[i][j] = tile;
            }
        }

        MainApplication.connect6Client.serverPlaceChipTcpEvent.addListener(this::drawEllipseFromServer);
        MainApplication.connect6Client.serverFinishGameTcpEvent.addListener(this::finishGame);

        if(chipColor.equals(Color.BLACK)) {
            drawEllipse((Ellipse) tiles[fieldSize/2][fieldSize/2].getChildren().get(1));
            changeGameFieldState(false);
            currentMove = 2;
        }
    }

    private void drawEllipse(Ellipse ellipse) {
        if (isMoving) {
            Pair<Integer, Integer> pair = getEllipseIndexes(ellipse);
            MainApplication.connect6Client.sendRequest(new CommonPacket(
                    HeaderType.TO_SERVER,
                    CommandType.PLACE_CHIP,
                    new PlaceChipParameter(chipColor.toString(), pair.getFirst(), pair.getSecond()))
            );

            ellipse.setFill(chipColor);
            updateAvailableTile(ellipse);

            currentMove += 1;
            CurrentMoveCounter.setText(String.valueOf(Integer.parseInt(CurrentMoveCounter.getText()) - 1));
            if(currentMove == 2) {
                changeGameFieldState(false);
            }
        }
    }

    private void drawEllipseFromServer(CommonPacket packet) {
        PlaceChipParameter parameter = (PlaceChipParameter) packet.commandParameter;
        StackPane Stack = (StackPane) getNodeFromGridPane(parameter.column, parameter.row);

        if (Stack != null) {
           Ellipse ellipse = (Ellipse) Stack.getChildren().get(1);
           if (ellipse != null) {
               ellipse.setFill(Color.valueOf(parameter.chipColor));
               updateAvailableTile(ellipse);

               currentMove -= 1;
               CurrentMoveCounter.setText(String.valueOf(Integer.parseInt(CurrentMoveCounter.getText()) - 1));
               if(currentMove == 0) {
                   changeGameFieldState(true);
               }
               else if (currentMove < 0) {
                   currentMove = 0;
                   CurrentMoveCounter.setText("2");
               }
           }
        }
    }

    private void finishGame(CommonPacket packet) {
        System.out.println("FINISH_GAME");
        FinishGameParameter parameter = (FinishGameParameter) packet.commandParameter;

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText("Game is finished");
            alert.setContentText(chipColor.toString().contains(parameter.winnerChipColor) ? "You are winner!" : "You are loser :(");
            alert.showAndWait();

            MainApplication.connect6Client.sendRequest(new CommonPacket(
                    HeaderType.TO_SERVER,
                    CommandType.DISCONNECT,
                    new DisconnectParameter())
            );
            MainApplication.setSceneParent("Launcher");
        });
    }

    private Node getNodeFromGridPane(int column, int row) {
        for (Node node : GameField.getChildren()) {
            if (GridPane.getColumnIndex(node) == column && GridPane.getRowIndex(node) == row) {
                return node;
            }
        }
        return null;
    }
    private Pair<Integer, Integer> getEllipseIndexes(Ellipse ellipse) {
        return new Pair<>(GridPane.getColumnIndex(ellipse.getParent()), GridPane.getRowIndex(ellipse.getParent()));
    }

    private void updateAvailableTile(Ellipse ellipse) {
        Pair<Integer, Integer> pair = getEllipseIndexes(ellipse);
        tiles[pair.getSecond()][pair.getFirst()].setDisable(true);

        int minRows = Math.max(pair.getSecond() - 1, 0);
        int minColumns = Math.max(pair.getFirst() - 1, 0);

        int maxRows = Math.min(pair.getSecond() + 1, fieldSize - 1);
        int maxColumns = Math.min(pair.getFirst() + 1, fieldSize - 1);

        for (int i = minRows; i <= maxRows; ++i) {
            for (int j = minColumns; j <= maxColumns; ++j){
                if (((Ellipse) tiles[i][j].getChildren().get(1)).getFill().equals(Color.TRANSPARENT)) {
                    tiles[i][j].setDisable(false);
                }
            }
        }
    }

    private String hexColorToString(String hex) {
        return hex.equals("0x000000ff") ? "BLACK" : "WHITE";
    }

    private void changeGameFieldState(boolean state) {
        if (state) {
            isMoving = true;
            GameField.setDisable(false);
            CurrentMoveName.setText("You");
            CurrentMoveCounter.setText("2");
        }
        else {
            isMoving = false;
            GameField.setDisable(true);
            CurrentMoveName.setText("Opponent");
            CurrentMoveCounter.setText("2");
        }
    }
}
