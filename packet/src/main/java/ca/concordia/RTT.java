package ca.concordia;

import java.util.Arrays;

public class RTT {

    long[] startTime;

    public RTT(int windowSize) {
        this.startTime = new long[windowSize]; //For our size window of 4. since we will have 4 packet with each RTT value
        Arrays.fill(startTime, 0);
    }

    public void initializeStartTime(long currentTime, int index, int windowSize) {
        this.startTime[index % windowSize] = currentTime;
    }

    public int getRTTime(long stopTime, int index, int windowSize) {
        long rtt = stopTime - this.startTime[ index % windowSize];
        return (int) rtt;
    }

}
