import java.util.ArrayList;
import kareltherobot.*;
import java.awt.Color;

public class Order implements Directions {

    // Trains for Line A and Line B
    public ArrayList<Train> lineATrains = new ArrayList<>();
    public ArrayList<Train> lineBTrains = new ArrayList<>();
    public int[][] map = new int[36][21]; 

    public Order() {
        int columnPos    = 15;   // initial avenue
        int rowPos       = 34;   // initial street
        Direction currentDirection = Directions.North;
        int counter      = 0;    

        // Create trains in depot, alternating routes
        while (lineATrains.size() < 22 || lineBTrains.size() < 10) {

            if (counter % 3 == 0) 
            {
                Train trainB = new Train(rowPos, columnPos, currentDirection, 0, Color.GREEN, "routeSJ", this);
                lineBTrains.add(trainB);
            }
            else if (counter % 3 == 1) 
            {
                Train trainA = new Train(rowPos, columnPos, currentDirection, 0, Color.BLUE, "routeAN", this);
                lineATrains.add(trainA);
            }
            else 
            {
                Train trainA = new Train(rowPos, columnPos, currentDirection, 0, Color.BLUE, "routeAE", this);
                lineATrains.add(trainA);
            }

            // mark this cell occupied
            map[rowPos][columnPos] = 1;

            // move starting position in the current direction
            if (currentDirection == Directions.North)      rowPos++;
            else if (currentDirection == Directions.South) rowPos--;
            else if (currentDirection == Directions.East)  columnPos++;
            else if (currentDirection == Directions.West)  columnPos--;

            // turn at corners
            if (columnPos == 15 && rowPos == 35) currentDirection = Directions.West;    
            if (columnPos == 1  && rowPos == 35) currentDirection = Directions.South;  
            if (columnPos == 1  && rowPos == 34) currentDirection = Directions.East;  
            if (columnPos == 14 && rowPos == 34) currentDirection = Directions.South;  
            if (columnPos == 14 && rowPos == 32) currentDirection = Directions.East;  

            counter++;
        }
    }
}