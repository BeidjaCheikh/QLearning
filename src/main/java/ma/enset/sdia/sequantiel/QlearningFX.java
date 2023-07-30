package ma.enset.sdia.sequantiel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.Random;

public class QlearningFX extends Application {
    private final double ALPHA = 0.1;
    private final double GAMMA = 0.9;
    private final int MAX_EPOCH = 2000;
    private final int GRID_SIZE = 6;
    private final int ACTION_SIZE = 4;
    private int[][] grid = new int[GRID_SIZE][GRID_SIZE];
    private double[][] qTable = new double[GRID_SIZE * GRID_SIZE][ACTION_SIZE];
    private int[][] actions;
    private int stateI;
    private int stateJ;
    private Random random = new Random();
    private GridPane gridPane;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        gridPane = createGridPane();
        initializeQLearning();
        runQLearningAndVisualize();
        primaryStage.setTitle("QLearning");
        primaryStage.setScene(new Scene(gridPane, 600, 600));
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                Rectangle rectangle = (Rectangle) getNodeFromGridPane(gridPane, j, i);
                if (grid[i][j] == -1) {
                    rectangle.setFill(Color.RED);
                }
            }
            Rectangle rect = (Rectangle) getNodeFromGridPane(gridPane, 0, 0);
            rect.setFill(Color.YELLOW);
        }

        primaryStage.show();
    }

    private GridPane createGridPane() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        gridPane.setAlignment(Pos.CENTER);

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                Rectangle rectangle = new Rectangle(35, 35);
                rectangle.setFill(Color.WHITE);
                rectangle.setStroke(Color.BLACK);
                GridPane.setHalignment(rectangle, HPos.CENTER);
                gridPane.add(rectangle, j, i);


            }
        }

        return gridPane;
    }

    private void initializeQLearning() {
        actions = new int[][]{
                {0, -1}, // left
                {0, 1},  // write
                {1, 0},  // down
                {-1, 0}  // up
        };

        grid = new int[][]{
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, -1, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, -1, -1, 0, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 1}
        };
    }

    private void runQLearningAndVisualize() {
        new Thread(() -> {
            runQLearning();
            resetState();

            while (!finiched()) {
                int action = chooseAction(0);
                int nextStateI = Math.max(0, Math.min(actions[action][0] + stateI, GRID_SIZE - 1));
                int nextStateJ = Math.max(0, Math.min(actions[action][1] + stateJ, GRID_SIZE - 1));

                Platform.runLater(() -> moveRobot(stateI, stateJ, nextStateI, nextStateJ));

                stateI = nextStateI;
                stateJ = nextStateJ;

                try {
                    Thread.sleep(1000);   // Pause between robot movements (milliseconds)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void runQLearning() {
        int it = 0;
        int currentState;
        int nextState;
        int action, action1;

        while (it < MAX_EPOCH) {
            resetState();
            while (!finiched()) {
                currentState = stateI * GRID_SIZE + stateJ;
                action = chooseAction(0.4);
                nextState = executeAction(action);
                action1 = chooseAction(0);
                qTable[currentState][action] = qTable[currentState][action] + ALPHA * (grid[stateI][stateJ] + GAMMA * qTable[nextState][action1] - qTable[currentState][action]);
                // Afficher les détails de chaque itération
                System.out.println("Epoch: " + it + " Current State: " + currentState + " Action: " + action + " Next State: " + nextState);

            }
            it++;
        }
        showResult();
    }

    private void resetState() {
        stateI = 0;
        stateJ = 0;
    }

    private boolean finiched() {
        return grid[stateI][stateJ] == 1;
    }

    private int chooseAction(double epsilon) {
        if (random.nextDouble() < epsilon) {
            // Exploration
            return random.nextInt(ACTION_SIZE);
        } else {
            // Exploitation
            int currentState = stateI * GRID_SIZE + stateJ;
            double bestQ = qTable[currentState][0];
            int bestAction = 0;
            for (int i = 1; i < ACTION_SIZE; i++) {
                if (qTable[currentState][i] > bestQ) {
                    bestQ = qTable[currentState][i];
                    bestAction = i;
                }
            }
            return bestAction;
        }
    }

    private int executeAction(int action) {
        stateI = Math.max(0, Math.min(actions[action][0] + stateI, GRID_SIZE - 1));
        stateJ = Math.max(0, Math.min(actions[action][1] + stateJ, GRID_SIZE - 1));
        return stateI * GRID_SIZE + stateJ;
    }

    private void showResult() {
        System.out.println("********qTable********");
        for (double[] line : qTable) {
            System.out.print("[");
            for (double qValue : line) {
                System.out.print(qValue + ", ");
            }
            System.out.println("]");
        }
        System.out.println();

        resetState();
        while (!finiched()) {
            int action = chooseAction(0);
            System.out.println("state: " +(stateI*GRID_SIZE+stateJ)+" action: " + action);
            executeAction(action);
        }
        System.out.println("Final state: " +(stateI*GRID_SIZE+stateJ));
    }
    private void moveRobot(int currentI, int currentJ, int nextI, int nextJ) {
        Rectangle currentRect = (Rectangle) getNodeFromGridPane(gridPane, currentJ, currentI);
        Rectangle nextRect = (Rectangle) getNodeFromGridPane(gridPane, nextJ, nextI);

        currentRect.setFill(Color.WHITE);

        if (grid[nextI][nextJ] == 1) {
            nextRect.setFill(Color.GREEN);

        } else {
            nextRect.setFill(Color.GRAY);


        }
    }


    private Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                return node;
            }
        }
        return null;
    }
}
