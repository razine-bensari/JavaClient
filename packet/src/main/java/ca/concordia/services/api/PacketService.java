package ca.concordia.services.api;

import ca.concordia.domain.Packet;

public interface PacketService {
    Packet[] segmentMessage(String responseToSegment);
}
