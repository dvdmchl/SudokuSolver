package org.dreamabout.sw.game.sudoku.dlx;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SudokuSolverTest {

    @ParameterizedTest
    @CsvSource({
            "sudoku_easy_1.txt, sudoku_easy_1_solution.txt",
            "sudoku_intermediate_1.txt, sudoku_intermediate_1_solution.txt",
            "sudoku_difficult_1.txt, sudoku_difficult_1_solution.txt",
            "sudoku_not_fun_1.txt, sudoku_not_fun_1_solution.txt",
            "sudoku_hardest_ever_1.txt, sudoku_hardest_ever_1_solution.txt"
    })
    void solverTest(String toSolveFileName, String solutionFileName) {
        var sudokuSolver = createSolverFromResourceName(toSolveFileName);
        sudokuSolver.solve();
        var solvedGrid = sudokuSolver.getGridArray();

        var sudokuSolution = createSolverFromResourceName(solutionFileName);
        var solutionGrid = sudokuSolution.getGridArray();

        // assert that the solved grid is equal to the solution grid
        assertTrue(Arrays.deepEquals(solvedGrid, solutionGrid));
    }

    private SudokuSolver createSolverFromResourceName(String resourceName) {
        var sudokuInputStream = getClass().getClassLoader().getResourceAsStream(resourceName);
        var sudokuSolver = new SudokuSolver();
        sudokuSolver.loadSudokuFromStream(sudokuInputStream);
        return sudokuSolver;
    }

    @Test
    void parsesNineLinesOfDigits() {
        var input = """
                000260701
                680070090
                190004500
                820100040
                004602900
                050003028
                009300074
                040050036
                703018000
                """;
        var sudokuSolver = new SudokuSolver();
        sudokuSolver.loadSudokuFromString(input);
        var grid = sudokuSolver.getGridArray();
        assertTrue(grid[0][0] == 0 && grid[0][3] == 2 && grid[8][8] == 0);
    }

    @Test
    void rejectsInvalidLengthLines() {
        var input = """
                00026070
                680070090
                190004500
                820100040
                004602900
                050003028
                009300074
                040050036
                703018000
                """;
        var sudokuSolver = new SudokuSolver();
        assertThrows(IllegalArgumentException.class, () -> sudokuSolver.loadSudokuFromString(input));
    }

    @Test
    void rejectsNonDigits() {
        var input = """
                00026070A
                680070090
                190004500
                820100040
                004602900
                050003028
                009300074
                040050036
                703018000
                """;
        var sudokuSolver = new SudokuSolver();
        assertThrows(IllegalArgumentException.class, () -> sudokuSolver.loadSudokuFromString(input));
    }
}
