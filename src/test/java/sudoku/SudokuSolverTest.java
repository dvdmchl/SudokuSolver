package sudoku;

import org.dreamabout.sw.game.sudoku.dlx.SudokuSolver;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SudokuSolverTest {

    @Test
    void easy1Test() {
        var sudokuSolver = createSolverFromResourceName("sudoku_easy_1.txt");
        sudokuSolver.solve();
        var solvedGrid = sudokuSolver.getCurrentGrid();

        var sudokuSolution = createSolverFromResourceName("sudoku_easy_1_solution.txt");
        var solutionGrid = sudokuSolution.getCurrentGrid();

        // assert that the solved grid is equal to the solution grid
        assertTrue(Arrays.deepEquals(solvedGrid, solutionGrid));
    }


    @Test
    void intermediate1Test() {
        var sudokuSolver = createSolverFromResourceName("sudoku_intermediate_1.txt");
        sudokuSolver.solve();
        var solvedGrid = sudokuSolver.getCurrentGrid();

        var sudokuSolution = createSolverFromResourceName("sudoku_intermediate_1_solution.txt");
        var solutionGrid = sudokuSolution.getCurrentGrid();

        // assert that the solved grid is equal to the solution grid
        assertTrue(Arrays.deepEquals(solvedGrid, solutionGrid));
    }

    @Test
    void difficult1Test() {
        var sudokuSolver = createSolverFromResourceName("sudoku_difficult_1.txt");
        sudokuSolver.solve();
        var solvedGrid = sudokuSolver.getCurrentGrid();

        var sudokuSolution = createSolverFromResourceName("sudoku_difficult_1_solution.txt");
        var solutionGrid = sudokuSolution.getCurrentGrid();

        // assert that the solved grid is equal to the solution grid
        assertTrue(Arrays.deepEquals(solvedGrid, solutionGrid));
    }

    @Test
    void notFun1Test() {
        var sudokuSolver = createSolverFromResourceName("sudoku_not_fun_1.txt");
        sudokuSolver.solve();
        var solvedGrid = sudokuSolver.getCurrentGrid();

        var sudokuSolution = createSolverFromResourceName("sudoku_not_fun_1_solution.txt");
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
