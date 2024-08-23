package org.dreamabout.sw.game.sudoku.dlx;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import static org.dreamabout.sw.game.sudoku.dlx.Constant.N;
import static org.dreamabout.sw.game.sudoku.dlx.Constant.SIZE;

@Data
@RequiredArgsConstructor
public class SudokuGrid {

    /* The grid contains all the numbers in the Sudoku puzzle.  Numbers which have
     * not yet been revealed are stored as 0. */
    private final int[][] grid;

    /* The print() function outputs the Sudoku grid to the standard output, using
     * a bit of extra formatting to make the result clearly readable. */
    public void print() {
        // Compute the number of digits necessary to print out each number in the Sudoku puzzle
        int digits = (int) Math.floor(Math.log(N) / Math.log(10)) + 1;

        // Create a dashed line to separate the boxes
        int lineLength = (digits + 1) * N + 2 * SIZE - 3;
        var line = new StringBuilder();
        for (int lineInit = 0; lineInit < lineLength; lineInit++)
            line.append('-');

        // Go through the Grid, printing out its values separated by spaces
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                printFixedWidth(String.valueOf(grid[i][j]), digits);
                // Print the vertical lines between boxes
                if ((j < N - 1) && ((j + 1) % SIZE == 0))
                    System.out.print(" |");
                System.out.print(" ");
            }
            System.out.println();

            // Print the horizontal line between boxes
            if ((i < N - 1) && ((i + 1) % SIZE == 0))
                System.out.println(line.toString());
        }
    }

    /* Helper function for the printing of Sudoku puzzle.  This function will print
     * out text, preceded by enough ' ' characters to make sure that the printint out
     * takes at least width characters.  */
    void printFixedWidth(String text, int width) {
        for (int i = 0; i < width - text.length(); i++)
            System.out.print(" ");
        System.out.print(text);
    }

}
