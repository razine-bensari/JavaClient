package ca.concordia.domain;

public enum PacketType {
    DATA(1), SYN(2), ACK(3), SYN_ACK(4), NAK(5), FIN(6), END(0);

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
