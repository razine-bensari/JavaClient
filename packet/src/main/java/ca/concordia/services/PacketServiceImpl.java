package ca.concordia.services;

import ca.concordia.domain.Packet;
import ca.concordia.domain.PacketType;
import ca.concordia.services.api.PacketService;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PacketServiceImpl implements PacketService {

    public static SocketAddress routerAddress = new InetSocketAddress("localhost", 3000);
    public static InetSocketAddress serverAddress = new InetSocketAddress("localhost", 8007);

    @Override
    public Packet[] segmentMessage(String responseToSegment, Packet packet) {
        byte[] resByte = responseToSegment.getBytes();
        int numOfPackets = getNumPackets(resByte);

        List<Packet> packets = new ArrayList<>(numOfPackets);
        //Packet[] packets = new Packet[numOfPackets];

        /* First packet sent starts at sequence number 2 */
        for(int i = 0; i < numOfPackets; i++) {
            /* If only one packet is enough */
            if((i + 1) == numOfPackets) {
                //String payload = new String(resByte, i, resByte.length);
                byte[] strArr = Arrays.copyOfRange(resByte, (i * 1013), ((i + 1) * 1013));
                packets.add(makePacketFromPacket(packet, PacketType.DATA.getIntValue(), (i + 2), new String(strArr)));
            } else {
                /* More than 2 packets are necessary */
                //String payload = new String(resByte, (i * 1013), ((i + 1) * 1013));
                byte[] strArr = Arrays.copyOfRange(resByte, (i * 1013), ((i + 1) * 1013));
                packets.add(makePacketFromPacket(packet, PacketType.DATA.getIntValue(), (i + 2), new String(strArr)));
            }
        }
        // Append END packet to packet list
        int seq = numOfPackets + 2;
        packets.add(this.makePacketFromPacket(packets.get(0), PacketType.END.getIntValue(), seq,  ""));
        return packets.toArray(new Packet[numOfPackets + 1]);
    }

    @Override
    public DatagramPacket makeNewDatagramPacket(int type, long seq, String payload, InetSocketAddress serverAddr, SocketAddress routerAddress) {
        Packet packet =  new Packet.Builder()
                .setType(type)
                .setSequenceNumber(seq)
                .setPortNumber(serverAddr.getPort())
                .setPeerAddress(serverAddr.getAddress())
                .setPayload(payload.getBytes())
                .create();
        return new DatagramPacket(packet.toBytes(), packet.toBytes().length, routerAddress);
    }

    @Override
    public Packet makePacketFromPacket(Packet packet, int type, long seq, String payload) {
        return packet.toBuilder()
                .setType(type)
                .setSequenceNumber(seq)
                .setPayload(payload.getBytes())
                .create();
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
