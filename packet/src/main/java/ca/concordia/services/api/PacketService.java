package ca.concordia.services.api;

import ca.concordia.domain.Packet;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public interface PacketService {

    Packet[] segmentMessage(String responseToSegment, Packet packet);

    DatagramPacket makeNewDatagramPacket(int type, long seq, String payload, InetSocketAddress serverAddr, SocketAddress routerAddress);

    Packet makePacketFromPacket(Packet packet, int type, long seq, String payload);
}
