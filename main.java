import kareltherobot.*;
import java.util.List;

public class main {
    public static void main(String[] args) throws InterruptedException {
        World.readWorld("MetroMed.kwld");
        World.setVisible(true);
        World.setDelay(7);   // ralentiza para ver el movimiento

        Order order = new Order();
        List<Train> lineA = order.lineATrains;
        List<Train> lineB = order.lineBTrains;

        // índices que apuntan al último elemento de cada lista
        int idxA = lineA.size() - 1;
        int idxB = lineB.size() - 1;

        // mientras queden trenes en A o en B
        while (idxA >= 0 || idxB >= 0) {
            // Batch: 2 trenes de A
            for (int i = 0; i < 2 && idxA >= 0; i++, idxA--) {
                Train t = lineA.get(idxA);
                System.out.println("Arrancando A row="+t.row);
                new Thread(t).start();
            }
            // Batch: 1 tren de B
            if (idxB >= 0) {
                Train t = lineB.get(idxB);
                System.out.println("Arrancando B row="+t.row);
                new Thread(t).start();
                idxB--;
            }
            // Espera 15 s antes del siguiente batch (si aún quedan trenes)
            if (idxA >= 0 || idxB >= 0) {
                Thread.sleep(15_00);
            }
        }
    }
}