package ca.concordia;

/*
* The following is taken from the book
*
* */


public class TimeOut {

    public int estimatedRTT;
    public int devRTT;

    public TimeOut() {
        this.estimatedRTT = 500;
        this.devRTT = 0; //Per book suggestion
    }

    public int getEstimatedTimeOut() {
        return this.estimatedRTT + (4 * this.devRTT);
    }

    public void calculateTimeOut(int sampleRTT) {
        this.estimatedRTT = (int)((0.875 * this.estimatedRTT) + (0.125 * sampleRTT));
        this.devRTT = (int)((0.75 * this.devRTT) + (0.25 * (Math.abs(this.estimatedRTT - sampleRTT))));
    }
}
