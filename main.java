import kareltherobot.*;

public class main {
    public static void main(String[] args) {
        World.readWorld("MetroMed.kwld");
        World.setVisible(true);
        World.setDelay(0);

        Order order = new Order();
        // Lanza los trenes en paquetes de 3
        order.startAllTrainsInBatches(3);
    }
}