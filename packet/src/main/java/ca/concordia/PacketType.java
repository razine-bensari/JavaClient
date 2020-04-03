package ca.concordia;

public enum PacketType {
    DATA(0), SYN(1), ACK(2), SYN_ACK(3), NAK(4), FIN(5), END(-1);

    private int intValue;

    PacketType(int intValue) {
        this.intValue = intValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

}
