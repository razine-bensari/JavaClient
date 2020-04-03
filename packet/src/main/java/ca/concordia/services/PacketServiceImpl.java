package ca.concordia.services;

import ca.concordia.domain.Packet;
import ca.concordia.services.api.PacketService;
import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;

public class PacketServiceImpl implements PacketService {

    @Override
    public Packet[] segmentMessage(String responseToSegment) {
        byte[] resByte = responseToSegment.getBytes();
        int numOfPackets = getNumPackets(resByte);

        Packet[] packets = new Packet[numOfPackets];

        for(int i=0; i < numOfPackets; i++) {

        }
    }

    public int getNumPackets(byte[] array) {

        int dividend = array.length / 1013;

        if(array.length % 1013 == 0) {
            return dividend;
        } else {
            return (dividend + 1);
        }
    }
}
