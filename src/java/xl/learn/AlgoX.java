package xl.learn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xuelin on 4/12/17.
 */
public class AlgoX {
    public static class Node {
        public Node left;
        public Node right;
        public Node up;
        public Node down;
        public ColumnNode columnNode;
        public int row;
        public Node(ColumnNode cn, int row) {
            this.left = this.right = this.up = this.down = this;
            this.columnNode = cn;
            this.row = row;
        }
        public void unlinkLR() {
//            System.out.println("UnlinkLR " + this);
            this.left.right = this.right;
            this.right.left = this.left;
        }
        public void relinkLR() {
//            System.out.println("RelinkLR " + this);
            this.left.right = this;
            this.right.left = this;
        }
        public void unlinkUD() {
//            System.out.println("UnlinkUD " + this);
            this.up.down = this.down;
            this.down.up = this.up;
        }
        public void relinkUD() {
//            System.out.println("RelinkUD " + this);
            this.up.down = this;
            this.down.up = this;
        }

        public Node(int row) {
            this(null, row);
        }
        public String toString() {
            return "Row: " + row;
        }
    }

    public static class ColumnNode extends Node {
        public int size = 0;
        public boolean primary = true;
        public final String name;

        public String toString() {
            return "column: " + name;
        }
        public ColumnNode(String name) {
            this(name, true);
        }

        public ColumnNode(String name, boolean primary) {
            super(-1);
            this.name = name;
            this.columnNode = this;
            this.primary = primary;
        }

        public void cover() {
//            System.out.println("Covering " + this.toString());
            this.unlinkLR();

            for (Node colDataObj = this.down; colDataObj != this; colDataObj = colDataObj.down) {
                for (Node rowDataObj = colDataObj.right; rowDataObj != colDataObj; rowDataObj = rowDataObj.right) {
                    rowDataObj.unlinkUD();
                    rowDataObj.columnNode.size--;
                }
            }
        }

        public void uncover() {
//            System.out.println("Uncovering " + this.toString());
            for (Node colDataObj = this.up;
                 colDataObj != this;
                    colDataObj = colDataObj.up)
            {
                for (Node rowDataObj = colDataObj.left;
                     rowDataObj != colDataObj;
                     rowDataObj = rowDataObj.left)
                {
                     rowDataObj.columnNode.size++;
                    rowDataObj.relinkUD();
                }
            }
            this.relinkLR();
        }
    }

    public static ColumnNode fromMatrix(int[][] grid, Collection<Integer> removedRows, Collection<Integer> primaryCols)
    {
        ColumnNode header = new ColumnNode("header");

        int rowCount = grid.length;
        int columnCount = grid[0].length;

        ArrayList<ColumnNode> colNodes = new ArrayList<>();
        for (int j = 0; j < columnCount; j++) {
            boolean isPrimary = true;
            if (primaryCols != null && !primaryCols.contains(j))
                isPrimary= false;
            ColumnNode colNode = new ColumnNode("" + j, isPrimary);
            colNodes.add(colNode);
        }

        Map<Integer, Node> rowToRowNode = new HashMap<>();
        List<Node> rowNodes = new ArrayList<>();
        for (int i = 0; i < rowCount; i++) {
            rowNodes.clear();
            for (int j = 0; j < columnCount; j++) {
                Node colNode = colNodes.get(j);
                if (grid[i][j] != 0) {
                    Node node = new Node(i);
                    rowNodes.add(node);

                    node.columnNode = colNode.columnNode;

                    node.up = colNode;
                    node.down = colNode.down;
                    node.up.down = node;
                    node.down.up = node;
                    colNode.columnNode.size++;
                }
            }
            if (rowNodes.size() > 0) {
                rowToRowNode.put(i, rowNodes.get(0));
                for (int k = 0; k < rowNodes.size(); k++) {
                    int last = (rowNodes.size() - 1);
                    rowNodes.get(k).left = rowNodes.get(k > 0 ? k - 1 : last);
                    rowNodes.get(k).right = rowNodes.get(k < last ? k + 1 : 0);
                }
            }
        }

        for (int i = colNodes.size() - 1; i >= 0; i--) {
            ColumnNode tmpNode = colNodes.get(i);
            if (tmpNode.size == 0) {
                colNodes.remove(i);
            }
        }
        colNodes.add(header);
        for (int i = colNodes.size() - 1; i >= 0; i--) {
            ColumnNode tmpNode = colNodes.get(i);
            tmpNode.left = i == 0 ? colNodes.get(colNodes.size() - 1) : colNodes.get(i - 1);
            tmpNode.right = i == colNodes.size() - 1 ? colNodes.get(0) : colNodes.get(i + 1);
        }

        if (removedRows != null && removedRows.size() > 0) {
            for (int removedRow: removedRows) {
                Node rowNode = rowToRowNode.get(removedRow);
                if (rowNode != null) {
                    for (Node node = rowNode.left; node != rowNode; node = node.left) {
                        node.columnNode.cover();
                    }
                    rowNode.columnNode.cover();
                }
            }
        }

        return header;
    }


    public ArrayList<ArrayList<Node>> answers = new ArrayList<>();
    public ArrayList<Node> currAnswer = new ArrayList<>();
    public ColumnNode header;
    boolean useHeuristic;
    boolean findFirstSolution;

    public AlgoX(ColumnNode h, boolean heuristic, boolean findFirstSolution) {
        this.header = h;
        this.useHeuristic = heuristic;
        this.findFirstSolution = findFirstSolution;
    }

    public void search()
    {
        boolean hasPrimaryCols = false;
        for (ColumnNode node = (ColumnNode)header.right; node != header; node = (ColumnNode)node.right) {
            if (node.primary) {
                hasPrimaryCols = true;
                break;
            }
        }
        if (!hasPrimaryCols) {
            answers.add((ArrayList)currAnswer.clone());
            return;
        }

        ColumnNode selectedCol = (ColumnNode)header.right;
        if (useHeuristic) {
            int curr = Integer.MAX_VALUE;
            ColumnNode col = (ColumnNode)header.right;
            while (col != header && col.primary) {
                if (col.size < curr) {
                    curr = col.size;
                    selectedCol = col;
                }
                col = (ColumnNode)col.right;
            }
        }

        selectedCol.cover();

        ColumnNode lastCovered = selectedCol;
        for (Node colDataObj = selectedCol.down; colDataObj != selectedCol; colDataObj = colDataObj.down) {
            currAnswer.add(colDataObj);

            for (Node rowDataObj = colDataObj.right; rowDataObj != colDataObj; rowDataObj = rowDataObj.right) {
                rowDataObj.columnNode.cover();
            }

            search();
            if (findFirstSolution && answers.size() > 0) {
                return;
            }

            Node lastNode = currAnswer.remove(currAnswer.size() - 1);
            lastCovered = lastNode.columnNode;

            for (Node rowDataObj = lastNode.left; rowDataObj != lastNode; rowDataObj = rowDataObj.left) {
                rowDataObj.columnNode.uncover();
            }
        }

        lastCovered.uncover();
    }

    public static List<List<Integer>> solveGrid(Collection<Collection<Integer>> grid, Collection<Integer> primaryCols,
                                                List<Integer> removedRows, boolean useHeuristic, boolean findFirst)
    {
        int rowCount = grid.size();
        int[][] gridArr = new int[rowCount][];
        int colCount = grid.iterator().next().size();
        int rowIndex = 0;
        for (Collection<Integer> row: grid) {
            gridArr[rowIndex] = new int[colCount];

            int colIndex = 0;
            for (Integer col: row) {
                gridArr[rowIndex][colIndex] =  col;
                colIndex++;
            }

            rowIndex++;
        }

        ColumnNode header = fromMatrix(gridArr, removedRows, primaryCols);
        AlgoX algoX = new AlgoX(header, useHeuristic, findFirst);
        algoX.search();
        List<ArrayList<Node>> answers = algoX.answers;
        List<List<Integer>> retval = new ArrayList<>();
        for (ArrayList<Node> answer: answers) {
            List<Integer> rows = new ArrayList<>();
            retval.add(rows);
            for (Node node: answer) {
                rows.add(node.row);
            }
        }
        return retval;
    }

    public static void main(String[] args) {
        boolean useHeuristic;
        if (args.length > 0) {
            useHeuristic = "true".equalsIgnoreCase(args[0]);
        }
        else {
            useHeuristic = true;
        }

        boolean findFirstSolution;
        if (args.length > 1) {
            findFirstSolution = "true".equalsIgnoreCase(args[1]);
        }
        else {
            findFirstSolution = false;
        }

        int[][] grid = {
                {0, 1, 1},
                {1, 1, 0},
                {0, 0, 1},
                {1, 0, 0}
        };
        ColumnNode header = fromMatrix(grid, null, null);
        AlgoX algoX = new AlgoX(header, useHeuristic, findFirstSolution);
        algoX.search();

        int index = 0;
        for (List<Node> answer: algoX.answers) {
            System.out.println("answer " + index++ + ":");
            for (Node node: answer) {
                System.out.println("row: " + node.row);
            }
        }
    }
}
