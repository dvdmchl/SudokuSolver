package org.dreamabout.sw.game.sudoku.dlx;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static org.dreamabout.sw.game.sudoku.dlx.Constant.N;
import static org.dreamabout.sw.game.sudoku.dlx.Constant.SIZE;

@RequiredArgsConstructor
public class AlgorithmXSolver {

    /* The grid contains all the numbers in the Sudoku puzzle.  Numbers which have
     * not yet been revealed are stored as 0. */
    private final int[][] grid;


    private ColumnNode root = null; // this is the starting node of the linked list
    private List<Node> solution = new ArrayList<>(); // a raw Array List for dynamically storing the solutions. It slows things
    // down a bit, but this how I started and ran out of time before I could come up with a more efficient way to do it.

    // the run method. We pass the Grid[][] as input
    public void run() {
        byte[][] matrix = createMatrix(grid); // create the sparse matrix. We use the type byte to speed things up. I tried using
        // using all the primitive types, expecting the same results in terms
        // of speed; the only performance boost should have been in terms of space.
        // Yet, there was a marked difference in the running times. Hence, I used byte[][] whenever possible.
        createDoubleLinkedLists(matrix);   // create the circular doubly-linked toroidal list
        search(0); // start the Dancing Links process of searching and covering and uncovering recursively
    }

    // data structures


    // create a sparse matrix for Grid
    private byte[][] createMatrix(int[][] initialMatrix) {
        int[][] clues = null; // stores the numbers that are already given on the board i.e. the 'clues'
        var cluesList = new ArrayList<int[]>(); // the list used to get the clues. Because we use a raw ArrayList, we later have to cast to int[] before storing in clues
        int counter = 0;
        for (int r = 0; r < N; r++) // iterates over the rows of Grid
        {
            for (int c = 0; c < N; c++) // iterates over the columns of Grid
            {
                if (initialMatrix[r][c] > 0) // if the number on the Grid is != 0 (the number is a clue and not a blank space to solved for), then store it
                {
                    cluesList.add(new int[]{initialMatrix[r][c], r, c}); // store the number, the row number and the column number
                    counter++;
                }
            }
        }
        clues = new int[counter][]; // store the clues once we've gotten them
        for (int i = 0; i < counter; i++) {
            clues[i] = cluesList.get(i);
        }

        // Now, we build our sparse matrix
        byte[][] matrix = new byte[N * N * N][4 * N * N];
        // The rows of our matrix represent all the possibilities, whereas the columns represent the constraints.
        // Hence, there are N^3 rows (N rows * N columns * N numbers), and N^2 * 4 columns (N rows * N columns * 4 constraints)

        // iterate over all the possible digits d
        for (int d = 0; d < N; d++) {
            // iterate over all the possible rows r
            for (int r = 0; r < N; r++) {
                // iterator over all the possible columns c
                for (int c = 0; c < N; c++) {
                    mapSparseMatrix(d, r, c, matrix, clues); // map the sparse matrix
                }
            }
        }
        return matrix;
    }

    private void mapSparseMatrix(int d, int r, int c, byte[][] matrix, int[][] clues) {
        if (!isCellFilled(d, r, c, clues)) // if the cell is not already filled
        {
            // this idea for this way of mapping the sparse matrix is taken from the Python implementation: https://code.google.com/p/narorumo/wiki/SudokuDLX
            int rowIndex = c + (N * r) + (N * N * d);
            // there are four 1s in each row, one for each constraint
            int blockIndex = ((c / SIZE) + ((r / SIZE) * SIZE));
            int colIndexRow = 3 * N * d + r;
            int colIndexCol = 3 * N * d + N + c;
            int colIndexBlock = 3 * N * d + 2 * N + blockIndex;
            int colIndexSimple = 3 * N * N + (c + N * r);
            // fill in the 1's
            matrix[rowIndex][colIndexRow] = 1;
            matrix[rowIndex][colIndexCol] = 1;
            matrix[rowIndex][colIndexBlock] = 1;
            matrix[rowIndex][colIndexSimple] = 1;
        }
    }


    // Check if the cell to be filled is already filled with a digit.
    // The idea for this is credited to Alex Rudnick as cited above
    // Meeting pre-existing conditions to check if a cell has already been filled
    private boolean isCellFilled(int digit, int row, int col, int[][] prefill) {
        if (prefill != null) {
            for (int[] ints : prefill) {
                if (matchesExistingConditionsWithoutExactCellMatch(digit, row, col, ints)
                        || matchesExistingConditionsInSameBlockButNotSameCell(digit, row, col, ints)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matchesExistingConditionsWithoutExactCellMatch(int digit, int row, int col, int[] ints) {
        int d = ints[0] - 1;
        int r = ints[1];
        int c = ints[2];
        return (d != digit && row == r && col == c) ||
                ((d == digit) && (row == r || col == c) && !(row == r && col == c));
    }

    private boolean matchesExistingConditionsInSameBlockButNotSameCell(int digit, int row, int col, int[] ints) {
        int d = ints[0] - 1;
        int r = ints[1];
        int c = ints[2];
        int blockStartIndexRow = (r / SIZE) * SIZE;
        int blockEndIndexRow = blockStartIndexRow + SIZE;
        int blockStartIndexCol = (c / SIZE) * SIZE;
        int blockEndIndexCol = blockStartIndexCol + SIZE;
        return d == digit && row > blockStartIndexRow && row < blockEndIndexRow
                && col > blockStartIndexCol && col < blockEndIndexCol && row != r && col != c;
    }


    // the method to convert the sparse matrix Exact Cover problem to a doubly-linked list, which will allow us to later
    // perform our Dancing Links magic.
    // Given that we have 4 constraints for Sudoku, I created a new class ColumnID that is a property of all columns.
    // This ColumnID property contains the information about the constraint and allows us to identify which constraint position
    // we're on, as well as the row and the column and the digit
    // the first constraint is row constraint, the second is col, the third is block, and the fourth is cell.
    // Every constraint contains N^2 columns for every cell
    // The idea for this is taken from Jonathan Chu's explanation (cited above)
    private void createDoubleLinkedLists(byte[][] matrix) {
        root = new ColumnNode(); // the root is used as an entry-way to the linked list i.e. we access the list through the root
        // create the column heads
        createColumnHeads(root, matrix);

        // Once all the ColumnHeads are set, we iterate over the entire matrix
        // Iterate over all the rows
        for (byte[] bytes : matrix) {
            // iterator over all the columns
            var curColumn = (ColumnNode) root.right;
            Node lastCreatedElement = null;
            Node firstElement = null;
            for (byte aByte : bytes) {
                if (aByte == 1)  // i.e. if the sparse matrix element has a 1 i.e. there is a clue here i.e. we were given this value in the Grid
                {
                    // create a new data element and link it
                    Node colElement = curColumn;
                    while (colElement.down != null) {
                        colElement = colElement.down;
                    }
                    colElement.down = new Node();
                    if (firstElement == null) {
                        firstElement = colElement.down;
                    }
                    colElement.down.up = colElement;
                    colElement.down.left = lastCreatedElement;
                    colElement.down.head = curColumn;
                    if (lastCreatedElement != null) {
                        colElement.down.left.right = colElement.down;
                    }
                    lastCreatedElement = colElement.down;
                    curColumn.size++;
                }
                curColumn = (ColumnNode) curColumn.right;
            }
            // link the first and the last element, again making it circular
            if (lastCreatedElement != null) {
                lastCreatedElement.right = firstElement;
                firstElement.left = lastCreatedElement;
            }
        }

        linkTheLastColumnElements(matrix);
    }

    private void linkTheLastColumnElements(byte[][] matrix) {
        var curColumn = (ColumnNode) root.right;
        // link the last column elements with the corresponding columnHeads
        for (int i = 0; i < matrix[0].length; i++) {
            Node colElement = curColumn;
            while (colElement.down != null) {
                colElement = colElement.down;
            }
            colElement.down = curColumn;
            curColumn.up = colElement;
            curColumn = (ColumnNode) curColumn.right;
        }
    }

    private void createColumnHeads(ColumnNode root, byte[][] matrix) {
        var curColumn = root;
        for (int col = 0; col < matrix[0].length; col++) // getting the column heads from the sparse matrix and filling in the information about the
        // constraints. We iterate for all the column heads, thus going through all the items in the first row of the sparse matrix
        {
            // We create the ColumnID that will store the information. We will later map this ID to the current curColumn
            curColumn.right = new ColumnNode();
            curColumn.right.left = curColumn;
            curColumn = (ColumnNode) curColumn.right;
            curColumn.info = createColumnId(col); // the information about the column is set to the new column
            curColumn.head = curColumn;
        }
        curColumn.right = root; // making the list circular i.e. the right-most ColumnHead is linked to the root
        root.left = curColumn;
    }

    private ColumnId createColumnId(int col) {
        ColumnId columnId = new ColumnId();
        if (col < 3 * N * N) {
            // identifying the digit
            int digit = (col / (3 * N)) + 1;
            columnId.number = digit;
            // is it for a row, column or block?
            int index = col - (digit - 1) * 3 * N;
            if (index < N) {
                columnId.constraint = 0; // we're in the row constraint
                columnId.position = index;
            } else if (index < 2 * N) {
                columnId.constraint = 1; // we're in the column constraint
                columnId.position = index - N;
            } else {
                columnId.constraint = 2; // we're in the block constraint
                columnId.position = index - 2 * N;
            }
        } else {
            columnId.constraint = 3; // we're in the cell constraint
            columnId.position = col - 3 * N * N;
        }
        return columnId;
    }

    // the searching algorithm. Pseudo-code from Jonathan Chu's paper (cited above).
    private void search(int k) {
        if (root.right == root) // if we've run out of columns, we've solved the exact cover problem!
        {
            mapSolvedToGrid(); // map the solved linked list to the grid
            return;
        }
        ColumnNode c = choose(); // we choose a column to cover
        cover(c);
        Node r = c.down;
        while (r != c) {
            if (k < solution.size()) {
                solution.remove(k); // if we had to enter this loop again
            }
            solution.add(k, r); // the solution is added

            Node j = r.right;
            while (j != r) {
                cover(j.head);
                j = j.right;
            }
            search(k + 1); //recursively search

            Node r2 = solution.get(k);
            Node j2 = r2.left;
            while (j2 != r2) {
                uncover(j2.head);
                j2 = j2.left;
            }
            r = r.down;
        }
        uncover(c);
    }

    // this allows us to map the solved linked list to the Grid
    private void mapSolvedToGrid() {
        int[] result = new int[N * N];
        for (Node node : solution) {
            // for the first step, we pull all the values of the solved Sudoku board from the linked list to an array result[] in order
            int number = -1; // initialize number and cell number to be a value that can't occur
            int cellNo = -1;
            Node next = node;
            do {
                if (next.head.info.constraint == 0) { // if we're in the row constraint
                    number = next.head.info.number;
                } else if (next.head.info.constraint == 3) { // if we're in the cell constraint
                    cellNo = next.head.info.position;
                }
                next = next.right;
            } while (node != next);
            result[cellNo] = number; // feed values into result[]
        }
        // for the second step, we feed all the values of the array result[] (in order) to the Grid
        int resultCounter = 0;
        for (int r = 0; r < N; r++) // iterates for the rows
        {
            for (int c = 0; c < N; c++) // iterates for the columns
            {
                grid[r][c] = result[resultCounter];
                resultCounter++;
            }
        }
    }


    private ColumnNode choose() {
        // According to Donald Knuth's paper, it is most efficient to choose the column with the smallest possible size.
        // That is what we do.
        ColumnNode rightOfRoot = (ColumnNode) root.right; // we cast the node to the right of the root to be a ColumnNode
        ColumnNode smallest = rightOfRoot;
        while (rightOfRoot.right != root) {
            rightOfRoot = (ColumnNode) rightOfRoot.right;
            if (rightOfRoot.size < smallest.size) // choosing which column has the lowest size
            {
                smallest = rightOfRoot;
            }
        }
        return smallest;
    }

    // covers the column; used as a helper method for the search method. Pseudo code by Jonathan Chu (credited above)
    private void cover(Node column) {
        // we remove the column head by remapping the node to its left to the node to its right; thus, the linked list no longer contains
        // a way to access the column head. Later when we uncover it, we can easily do so by just reversing this process.
        column.right.left = column.left;
        column.left.right = column.right;

        // We also have to do this covering for all the rows in the column
        Node curRow = column.down;
        while (curRow != column) // because it's circular!
        {
            Node curNode = curRow.right;
            while (curNode != curRow) {
                curNode.down.up = curNode.up;
                curNode.up.down = curNode.down;
                curNode.head.size--;
                curNode = curNode.right;
            }
            curRow = curRow.down;
        }
    }

    // uncovers the column i.e. adds back all the nodes of the column to the linked list
    private void uncover(Node column) {
        Node curRow = column.up;
        while (curRow != column) // do this for all the nodes of the column to be uncovered first, and then reinsert the columnHead
        {
            Node curNode = curRow.left;
            while (curNode != curRow) {
                curNode.head.size++;
                curNode.down.up = curNode; // reinserts node into linked list
                curNode.up.down = curNode;
                curNode = curNode.left;
            }
            curRow = curRow.up;
        }
        column.right.left = column; // reinserts column head
        column.left.right = column;
    }

}