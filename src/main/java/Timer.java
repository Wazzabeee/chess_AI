public class Timer extends Thread {

    private LeftSideNode lsn;
    private Stop stop;

    public Timer(LeftSideNode lsn, Stop stop) {
        this.lsn = lsn;
        this.stop = stop;
    }

    public void run() {
        try {
            Thread.sleep(1000);

            System.out.println("Sleep finish");

            if (!this.stop.getStop()) {
                System.out.println("bestmove " + this.lsn.getBestMove().toString());
                this.stop.setTrueStop();
                this.lsn.stopAllThread();
            }
        } catch (InterruptedException e) {
        }
    }
}