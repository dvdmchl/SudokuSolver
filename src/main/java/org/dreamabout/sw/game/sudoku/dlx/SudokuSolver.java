package org.dreamabout.sw.game.sudoku.dlx;

// Shivan Kaul Sahib

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class SudokuSolver {

    /* SIZE is the size parameter of the Sudoku puzzle, and N is the square of the size.  For
     * a standard Sudoku puzzle, SIZE is 3 and N is 9. */
    int SIZE, N;

    /* The grid contains all the numbers in the Sudoku puzzle.  Numbers which have
     * not yet been revealed are stored as 0. */
    int Grid[][];

    /* The solve() method should remove all the unknown characters ('x') in the Grid
     * and replace them with the numbers from 1-9 that satisfy the Sudoku puzzle. */


    /************************************************************************************************************************************************************/
    // DOCUMENTATION -- ALGORITHM X, EXACT COVER PROBLEM AND DANCING LINKS IMPLEMENTATION

    // My class AlgorithmXSolver takes an unsolved Sudoku puzzled as int[][] (the Grid) and outputs the solved Sudoku puzzle.
    // I convert the Sudoku puzzle into an Exact Cover problem, solve that using the Dancing Links algorithm as described by Dr Donald Knuth,
    // and then get the solution and map it onto the Grid.

    // EXACT COVER AND DANCING LINKS
 /*An Exact Cover problem can be represented as a sparse matrix where the rows represent possibilities, and the columns
  * represent constraints. Every row will have a 1 in every column (constraint) that it satisfies, and a 0 otherwise. A set
  * of rows that together have exactly one 1 for each column can be said to be the solution set of the Exact Cover problem. Now,
  * Dancing Links is an efficient way of solving such a problem. The idea is to take the Exact Cover matrix and put it into a
  * a toroidal circular doubly-linked list. Thus, every node in such a list will be connected to 4 other nodes and the list will be circular
  * i.e. the last element will point to the first one. In the case of Dancing Links, for every column of the linked list, there is a
  * special ColumnNode (which extends the normal Node) that contains identifying information about that particular column as well as
  * the size of the column i.e. the number of nodes in it. Each Node points to four other nodes as mentioned, as well as its ColumnNode.
  *
   // SOLVING
  * To solve the Exact Cover problem i.e. come up with a set of rows that contain exactly one 1 for every column/constraint, we search
  * recursively using the principles of backtracking. It chooses a column, 'covers' it i.e. removes that column from the linked list completely,
  * store it in a solution list (which I implemented using an ArrayList), and then try to recursively solve the rest of the table. If
  * it's not possible, backtrack, restore the column (uncover it), and try a different column. For this assignment I assumed that the
  * Sudoku problem being provided has a solution.
  *
   // SUDOKU APPLICATION
  * For Sudoku, there are 4 constraints. Only 1 instance of a number can be in a row, in a column, and in a block. In addition, there can
  * be only one number in a cell. The rows represent every single possible position for every number. Every row would have 4 1s, representing
  * one possible place for the number (satisfying all 4 constraints).
  * To implement my solution, I created a class AlgorithmXSolver that contained all the methods and the data structures required to solve
  * the problem. I instantiated an instance of this class in the solve() method, and then ran it.
  * I had to convert the given Grid into a sparse matrix, accounting for the given clues (filled in values). Then, this matrix
  * is converted into a linked list as talked about above and solved using the Dancing Links approach. We store
  * possible solutions in an ArrayList 'solution'. Once we get a set of Nodes that solves the problem, we take the solution list
  * and iterate over every single Node and map the solution over the original Grid.
  *
  // TESTING
  * I tested my solver using the puzzles provided by Prof Blanchette by passing the sudoku text file as the args[] variable
  * of the main method. I did this in Eclipse by editing the Run Configuration (and providing the full path to the text file
  * in the Arguments tab).
  */

    // CREDITS:
    // (1) Dr Donald Knuth's original paper (http://www.ocf.berkeley.edu/~jchu/publicportal/sudoku/0011047.pdf) on Dancing Links
    // (2) Jonathan Chu's paper for the pseudocode for the Dancing Links implementation (http://www.ocf.berkeley.edu/~jchu/publicportal/sudoku/sudoku.paper.html)
    // (3) The Wikipedia pages on Dancing Links, Exact Cover problem, Algorithm X for helping to understand Knuth's paper
    // (4) This StackOverflow discussion to intuitively understand the Dancing Links implementation: http://stackoverflow.com/questions/1518335/the-dancing-links-algorithm-an-explanation-that-is-less-explanatory-but-more-o
    // (5) Xi Chen's implementation in C to get an understanding of the data structures (http://uaa.wtf.im/?page_id=27)
    // (6) Alex Rudnick's implementation in Python for getting ideas on how to implement some of the methods (https://code.google.com/p/narorumo/wiki/SudokuDLX)

    /************************************************************************************************************************************************************/
    public void solve() {
        AlgorithmXSolver solver = new AlgorithmXSolver();
        solver.N = N;
        solver.Grid = Grid;
        solver.SIZE = SIZE;
        solver.run(solver.Grid);
    }


    /*****************************************************************************/
    /* NOTE: YOU SHOULD NOT HAVE TO MODIFY ANY OF THE FUNCTIONS BELOW THIS LINE. */

    /*****************************************************************************/

    /* Default constructor.  This will initialize all positions to the default 0
     * value.  Use the read() function to load the Sudoku puzzle from a file or
     * the standard input. */
    public SudokuSolver(int size) {
        SIZE = size;
        N = size * size;

        Grid = new int[N][N];
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++)
                Grid[i][j] = 0;
    }



    /* Helper function for the printing of Sudoku puzzle.  This function will print
     * out text, preceded by enough ' ' characters to make sure that the printint out
     * takes at least width characters.  */
    void printFixedWidth(String text, int width) {
        for (int i = 0; i < width - text.length(); i++)
            System.out.print(" ");
        System.out.print(text);
    }


    /* The print() function outputs the Sudoku grid to the standard output, using
     * a bit of extra formatting to make the result clearly readable. */
    public void print() {
        // Compute the number of digits necessary to print out each number in the Sudoku puzzle
        int digits = (int) Math.floor(Math.log(N) / Math.log(10)) + 1;

        // Create a dashed line to separate the boxes
        int lineLength = (digits + 1) * N + 2 * SIZE - 3;
        StringBuffer line = new StringBuffer();
        for (int lineInit = 0; lineInit < lineLength; lineInit++)
            line.append('-');

        // Go through the Grid, printing out its values separated by spaces
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                printFixedWidth(String.valueOf(Grid[i][j]), digits);
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


    private static int[][] EASY_SUDOKU = new int[][]{
            {0, 0, 0, 2, 6, 0, 7, 0, 1},
            {6, 8, 0, 0, 7, 0, 0, 9, 0},
            {1, 9, 0, 0, 0, 4, 5, 0, 0},
            {8, 2, 0, 1, 0, 0, 0, 4, 0},
            {0, 0, 4, 6, 0, 2, 9, 0, 0},
            {0, 5, 0, 0, 0, 3, 0, 2, 8},
            {0, 0, 9, 3, 0, 0, 0, 7, 4},
            {0, 4, 0, 0, 5, 0, 0, 3, 6},
            {7, 0, 3, 0, 1, 8, 0, 0, 0}
    };

    /* The main function reads in a Sudoku puzzle from the standard input,
     * unless a file name is provided as a run-time argument, in which case the
     * Sudoku puzzle is loaded from that file.  It then solves the puzzle, and
     * outputs the completed puzzle to the standard output. */
    public static void main(String args[]) throws Exception {
//        InputStream in;
//        if( args.length > 0 )
//            in = new FileInputStream( args[0] );
//        else
//            in = System.in;
//
//        // The first number in all Sudoku files must represent the size of the puzzle.  See
//        // the example files for the file format.
//        int puzzleSize = readInteger( in );
//        if( puzzleSize > 100 || puzzleSize < 1 ) {
//            System.out.println("Error: The Sudoku puzzle size must be between 1 and 100.");
//            System.exit(-1);
//        }

        var puzzleSize = 3;
        SudokuSolver s = new SudokuSolver(puzzleSize);

        // read the rest of the Sudoku puzzle
//        s.read( in );

        // Solve the puzzle.  We don't currently check to verify that the puzzle can be
        // successfully completed.  You may add that check if you want to, but it is not
        // necessary.
        long startTime = System.currentTimeMillis();
        s.Grid = EASY_SUDOKU; // test
        s.solve();
        long endTime = System.currentTimeMillis(); // test
        System.out.println(endTime - startTime); // test
        //  s.solve();

        // Print out the (hopefully completed!) puzzle
        s.print();
    }

    /**
     * Load the Sudoku puzzle from the given input stream.
     * The input stream has the following format:
     * 9 rows of 9 numbers (0-9)
     * @param sudokuInputStream
     */
    public void loadSudokuFromStream(InputStream sudokuInputStream) {
        try(var reader = new InputStreamReader(sudokuInputStream)) {
            var bufferedReader = new BufferedReader(reader);
            var sudoku = new int[9][9];
            for (int i = 0; i < 9; i++) {
                var line = bufferedReader.readLine();
                var numbers = line.split("");
                for (int j = 0; j < 9; j++) {
                    sudoku[i][j] = Integer.parseInt(numbers[j]);
                }
            }
            Grid = sudoku;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int[][] getCurrentGrid() {
        return Grid;
    }
}

