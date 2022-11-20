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

            if (!this.stop.getStop()) {
                this.stop.setTrueStop();
                this.lsn.stopAllThread();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
