import kareltherobot.*;

public class main {
    public static void main(String[] args) {
        World.readWorld("MetroMed.kwld");
        World.setVisible(true);
        World.setDelay(7);
        
        // Deshabilitar mensajes y controles
        World.showSpeedControl(false);
        World.setTrace(false);
        World.setDelay(7); // Mínimo delay posible

        Order orderManager = new Order();
        
        orderManager.startSystem();
    }
}