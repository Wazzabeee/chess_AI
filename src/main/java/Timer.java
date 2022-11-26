
/**
 * Classe implémentée pour respecter la contraine d'1 seconde lors du tournoi
 */
public class Timer extends Thread {

    private final LeftSideNode lsn;
    private final Stop stop;

    public Timer(LeftSideNode lsn, Stop stop) {
        this.lsn = lsn;
        this.stop = stop;
    }

    /**
     * On lance un timer d'1 sec dans un thread séparé qui va se réveiller et arrêter la recherche si celle-ci
     * n'est pas déjà terminée.
     */
    public void run() {
        try {
            Thread.sleep(1000); // Timer à modifier pour augmenter le temps de recherche

            if (!this.stop.getStop()) {
                this.stop.setTrueStop();
                this.lsn.stopAllThread();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
