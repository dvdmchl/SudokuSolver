package org.dreamabout.sw.game.sudoku.dlx;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SudokuSolverTest {

    @ParameterizedTest
    @CsvSource({
            "sudoku_easy_1.txt, sudoku_easy_1_solution.txt",
            "sudoku_intermediate_1.txt, sudoku_intermediate_1_solution.txt",
            "sudoku_difficult_1.txt, sudoku_difficult_1_solution.txt",
            "sudoku_not_fun_1.txt, sudoku_not_fun_1_solution.txt"
    })
    void solverTest(String toSolveFileName, String solutionFileName) {
        var sudokuSolver = createSolverFromResourceName(toSolveFileName);
        sudokuSolver.solve();
        var solvedGrid = sudokuSolver.getCurrentGrid();

        var sudokuSolution = createSolverFromResourceName(solutionFileName);
        var solutionGrid = sudokuSolution.getCurrentGrid();

        // assert that the solved grid is equal to the solution grid
        assertTrue(Arrays.deepEquals(solvedGrid, solutionGrid));
    }

    private SudokuSolver createSolverFromResourceName(String resourceName) {
        var sudokuInputStream = getClass().getClassLoader().getResourceAsStream(resourceName);
        var puzzleSize = 3;
        var sudokuSolver = new SudokuSolver(puzzleSize);
        sudokuSolver.loadSudokuFromStream(sudokuInputStream);
        return sudokuSolver;
    }
}
