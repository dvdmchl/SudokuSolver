package org.dreamabout.sw.game.sudoku.dlx;

// Shivan Kaul Sahib

import com.github.rvesse.airline.SingleCommand;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

import java.io.*;

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
@Command(name = "sudoku-solver", description = "Solves a Sudoku puzzle")
public class SudokuSolver {

    @Option(name = {"-f", "--file"}, description = "Path to the Sudoku puzzle file")
    private String sudokuFilePath;

    private SudokuGrid grid;

    /**
     * Main method to run the Sudoku solver.
     * It takes the Sudoku puzzle as input and solves it.
     * The Sudoku puzzle is provided as a text file.
     * The text file has 9 rows of 9 numbers (0-9).
     * The numbers represent the Sudoku puzzle, with 0 representing an empty cell.
     */
    public static void main(String[] args) throws Exception {
        var argsParser = SingleCommand.singleCommand(SudokuSolver.class);
        var resultParser = argsParser.parseWithResult(args);
        if (resultParser.wasSuccessful()) {
            var cmd = resultParser.getCommand();
            cmd.initializeGrid();
            cmd.solve();
        } else {
            System.err.println(resultParser.getErrors());
        }
    }

    private void initializeGrid() throws IOException {
        if (sudokuFilePath == null) {
            throw new IllegalArgumentException("Sudoku file path is required");
        }
        var sudokuFile = new File(sudokuFilePath);
        if (!sudokuFile.exists()) {
            throw new IllegalArgumentException("File: %s does not exist".formatted(sudokuFilePath));
        }
        var is = sudokuFile.toURI().toURL().openStream();
        loadSudokuFromStream(is);
    }

    public void solve() {
        AlgorithmXSolver solver = new AlgorithmXSolver(grid.getGrid());
        var startTime = System.nanoTime();
        solver.run();
        var endTime = System.nanoTime() - startTime;
        System.out.println("Time taken to solve the Sudoku puzzle: " + endTime / 1000000 + " ms");
        grid.print();
    }

    /**
     * Load the Sudoku puzzle from the given input stream.
     * The input stream has the following format:
     * 9 rows of 9 numbers (0-9)
     */
    public void loadSudokuFromStream(InputStream sudokuInputStream) {
        try (var reader = new InputStreamReader(sudokuInputStream)) {
            var bufferedReader = new BufferedReader(reader);
            var sudoku = new int[9][9];
            for (int i = 0; i < 9; i++) {
                var line = bufferedReader.readLine();
                var numbers = line.split("");
                for (int j = 0; j < 9; j++) {
                    sudoku[i][j] = Integer.parseInt(numbers[j]);
                }
            }
            grid = new SudokuGrid(sudoku);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public int[][] getGridArray() {
        return grid.getGrid();
    }
}

