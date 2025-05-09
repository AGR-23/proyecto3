import java.util.ArrayList;
import kareltherobot.*;
import java.awt.Color;

public class Order implements Directions {

    public ArrayList<Train> lineATrains = new ArrayList<>();
    public ArrayList<Train> lineBTrains = new ArrayList<>();
    public int[][] map = new int[36][21];

    public Order() {
        int columnPos = 15;
        int rowPos    = 34;
        Direction currentDirection = Directions.North;

        // 1) Crear 24 trenes de la línea A
        for (int i = 0; i < 24; i++) {
            String routeA = (i % 2 == 0) ? "routeAN" : "routeAE";
            Train trainA = new Train(rowPos, columnPos, currentDirection, 0, Color.BLUE, routeA, this);
            lineATrains.add(trainA);
            map[rowPos][columnPos] = 1;

            Position next = advanceAndTurn(rowPos, columnPos, currentDirection);
            rowPos = next.row;
            columnPos = next.col;
            currentDirection = next.dir;
        }

        // 2) Crear 8 trenes de la línea B
        for (int j = 0; j < 8; j++) {
            Train trainB = new Train(rowPos, columnPos, currentDirection, 0, Color.GREEN, "routeSJ", this);
            lineBTrains.add(trainB);
            map[rowPos][columnPos] = 1;

            Position next = advanceAndTurn(rowPos, columnPos, currentDirection);
            rowPos = next.row;
            columnPos = next.col;
            currentDirection = next.dir;
        }
    }

    private static class Position {
        int row, col;
        Direction dir;
        Position(int row, int col, Direction dir) {
            this.row = row; this.col = col; this.dir = dir;
        }
    }

    private Position advanceAndTurn(int row, int col, Direction dir) {
        // mover según la dirección
        if (dir == Directions.North)      row++;
        else if (dir == Directions.South) row--;
        else if (dir == Directions.East)  col++;
        else if (dir == Directions.West)  col--;

        // giros en las esquinas
        if (col == 15 && row == 35)        dir = Directions.West;
        else if (col == 1  && row == 35)   dir = Directions.South;
        else if (col == 1  && row == 34)   dir = Directions.East;
        else if (col == 14 && row == 34)   dir = Directions.South;
        else if (col == 14 && row == 32)   dir = Directions.East;

        return new Position(row, col, dir);
    }
}