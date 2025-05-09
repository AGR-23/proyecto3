import kareltherobot.*;
import java.awt.Color;
import java.util.ArrayList;

public class main { 
    public static void main(String[] args) {
        World.readWorld("MetroMed.kwld");
        World.setVisible(true); 
        World.setDelay(0);                         

        Order order = new Order(); 
        World.setDelay(50); 

        new Thread(order.lineATrains.getLast()).start();
        new Thread(order.lineATrains.get(order.lineATrains.size() - 2)).start();
        new Thread(order.lineBTrains.getLast()).start();
    }
}