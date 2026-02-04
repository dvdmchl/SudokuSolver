package org.dreamabout.sw.game.sudoku.dlx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.function.UnaryOperator;

import static org.dreamabout.sw.game.sudoku.dlx.Constant.N;

public class SudokuSolverUI extends Application {

    private static SudokuGrid sudokuGrid;
    private static SudokuSolverUI activeInstance;

    public static boolean isInitialized = false;

    public static Thread uiThread;

    private final Label[][] outputGrid = new Label[N][N];
    private final TextField[][] inputFields = new TextField[N][N];
    private Button solveButton;
    private Label statusLabel;
    private int[][] initialGrid;

    public static void setSudokuGrid(SudokuGrid sudokuGrid) {
        SudokuSolverUI.sudokuGrid = sudokuGrid;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        activeInstance = this;
        var root = new VBox(10);
        root.setPadding(new Insets(10));

        var instruction = new Label("Enter Sudoku (digits 1-9, leave empty for blanks):");
        var inputGrid = createInputGrid();

        solveButton = new Button("Solve");
        solveButton.setOnAction(event -> handleSolve());

        statusLabel = new Label();
        statusLabel.setTextFill(Color.FIREBRICK);

        var gridPane = createOutputGrid();
        root.getChildren().addAll(instruction, inputGrid, solveButton, statusLabel, gridPane);

        if (sudokuGrid != null) {
            fillInputGrid(sudokuGrid.getGrid());
            setInputDisabled(true);
            solveButton.setDisable(true);
            statusLabel.setText("Solving...");
            statusLabel.setTextFill(Color.DARKGREEN);
            initializeFromGrid(sudokuGrid);
        }

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Sudoku Solver");
        var iconStream = getClass().getClassLoader().getResourceAsStream("app_icon.png");
        if (iconStream != null) {
            primaryStage.getIcons().add(new Image(iconStream));
        }
        primaryStage.show();

        isInitialized = true;
        if (sudokuGrid == null) {
            Platform.runLater(() -> inputFields[0][0].requestFocus());
        }
    }

    public static void notifySolveCompleted() {
        if (activeInstance == null) {
            return;
        }
        Platform.runLater(() -> {
            activeInstance.setInputDisabled(false);
            activeInstance.solveButton.setDisable(false);
            activeInstance.statusLabel.setText("Solved.");
            activeInstance.statusLabel.setTextFill(Color.DARKGREEN);
        });
    }

    private GridPane createInputGrid() {
        GridPane gridPane = new GridPane();
        gridPane.setGridLinesVisible(true);
        UnaryOperator<TextFormatter.Change> filter = change -> {
            var newText = change.getControlNewText();
            if (newText.isEmpty()) {
                return change;
            }
            return newText.matches("[1-9]") ? change : null;
        };
        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                TextField cell = new TextField();
                cell.setPrefSize(40, 40);
                cell.setFont(new Font("Arial", 12));
                cell.setAlignment(Pos.CENTER);
                cell.setTextFormatter(new TextFormatter<>(filter));
                cell.setOnAction(event -> handleSolve());
                inputFields[row][col] = cell;
                gridPane.add(cell, col, row);
            }
        }
        return gridPane;
    }

    private GridPane createOutputGrid() {
        GridPane gridPane = new GridPane();
        gridPane.setGridLinesVisible(true);
        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                Label cell = new Label();
                cell.setMinSize(40, 40);
                cell.setFont(new Font("Arial", 18));
                cell.setStyle("-fx-border-color: black; -fx-alignment: center;");
                outputGrid[row][col] = cell;
                gridPane.add(cell, col, row);
            }
        }
        return gridPane;
    }

    private void handleSolve() {
        statusLabel.setText("");
        statusLabel.setTextFill(Color.FIREBRICK);
        clearOutputGrid();
        solveButton.setDisable(true);

        var solver = new SudokuSolver();
        solver.setVisualize(false);
        int[][] originalGrid;
        try {
            solver.loadSudokuFromString(buildSudokuText());
            originalGrid = copyGrid(solver.getGridArray());
        } catch (IllegalArgumentException e) {
            statusLabel.setText(e.getMessage());
            solveButton.setDisable(false);
            return;
        }

        var thread = new Thread(() -> {
            try {
                var solved = solver.solve();
                Platform.runLater(() -> {
                    if (solved) {
                        var solvedGrid = solver.getGridArray();
                        fillOutputGrid(solvedGrid, originalGrid);
                        statusLabel.setText("Solved.");
                        statusLabel.setTextFill(Color.DARKGREEN);
                    } else {
                        statusLabel.setText("No solution found.");
                        statusLabel.setTextFill(Color.FIREBRICK);
                    }
                    solveButton.setDisable(false);
                });
            } catch (RuntimeException e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Failed to solve: " + e.getMessage());
                    solveButton.setDisable(false);
                });
            }
        }, "sudoku-solver-thread");
        thread.setDaemon(true);
        thread.start();
    }

    private String buildSudokuText() {
        var builder = new StringBuilder();
        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                var text = inputFields[row][col].getText().trim();
                builder.append(text.isEmpty() ? '0' : text);
            }
            if (row < N - 1) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    private void clearOutputGrid() {
        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                outputGrid[row][col].setText("");
                outputGrid[row][col].setTextFill(Color.BLACK);
            }
        }
    }

    private void fillOutputGrid(int[][] solvedGrid, int[][] originalGrid) {
        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                int value = solvedGrid[row][col];
                outputGrid[row][col].setText(String.valueOf(value));
                outputGrid[row][col].setTextFill(originalGrid[row][col] == 0 ? Color.BLUE : Color.BLACK);
            }
        }
    }

    private void fillInputGrid(int[][] grid) {
        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                int value = grid[row][col];
                inputFields[row][col].setText(value == 0 ? "" : String.valueOf(value));
            }
        }
    }

    private void initializeFromGrid(SudokuGrid grid) {
        initialGrid = new int[N][N];
        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                int value = grid.getGrid()[row][col];
                if (value != 0) {
                    outputGrid[row][col].setText(String.valueOf(value));
                    outputGrid[row][col].setTextFill(Color.BLACK);
                    initialGrid[row][col] = value;
                }
            }
        }

        grid.addListener((row, col, value) -> Platform.runLater(() -> {
            if (initialGrid[row][col] != 0) {
                return;
            }
            if (value == 0) {
                outputGrid[row][col].setText("");
            } else {
                outputGrid[row][col].setText(String.valueOf(value));
                outputGrid[row][col].setTextFill(Color.BLUE);
            }
        }));
    }

    private int[][] copyGrid(int[][] source) {
        var copy = new int[N][N];
        for (int row = 0; row < N; row++) {
            System.arraycopy(source[row], 0, copy[row], 0, N);
        }
        return copy;
    }

    private void setInputDisabled(boolean disabled) {
        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                inputFields[row][col].setDisable(disabled);
            }
        }
    }
}
