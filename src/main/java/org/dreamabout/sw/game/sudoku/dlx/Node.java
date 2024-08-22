package org.dreamabout.sw.game.sudoku.dlx;
public class Node  // we define a node that knows about four other nodes, as well as its column head
{
    Node left;
    Node right;
    Node up;
    Node down;
    ColumnNode head;
}