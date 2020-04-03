package ca.concordia;

public class RTT {

    long[] startTime;

    public RTT() {
        this.startTime = new long[]{0, 0, 0, 0}; //For our size window of 4. since we will have 4 packet with each RTT value
    }

    public void initializeStartTime(long currentTime, int index) {
        this.startTime[index % 4] = currentTime;
    }

    public int getRTTime(long stopTime, int index) {
        long rtt = stopTime - this.startTime[ index % 4];
        return (int) rtt;
    }

}
