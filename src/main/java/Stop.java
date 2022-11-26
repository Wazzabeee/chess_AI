/**
 * Classe permettant d'arrêter la recherche si le temps défini est écoulé
 */
public class Stop {
    private boolean stop;

    public Stop() {
        this.stop = false;
    }

    public void setTrueStop() {
        this.stop = true;
    }

    public boolean getStop() {
        return this.stop;
    }
}
