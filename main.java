import kareltherobot.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class main {
    public static void main(String[] args) {
        World.readWorld("MetroMed.kwld");
        World.setVisible(true);
        World.setDelay(5);

        Order order = new Order();
        World.setDelay(5);

        // Iniciar todos los trenes
       order.startAllTrainsInBatches(3);
        // order.startAllTrains();
        // order.startAllTrainsInBatches();
    }

}