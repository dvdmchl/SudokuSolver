package org.dreamabout.sw.game.sudoku.dlx;

public interface SudokuSolverListener {
    void cellUpdated(int row, int col, int value);
}
