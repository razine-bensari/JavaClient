package httpc.impl;

import RequestAndResponse.Request;
import RequestAndResponse.Response;
import ca.concordia.Packet;
import ca.concordia.PacketType;
import ca.concordia.UDPClient;
import httpc.api.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.api.Converter;
import utils.api.Parser;
import utils.impl.HttpParser;
import utils.impl.HttpResponseConverter;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static java.nio.channels.SelectionKey.OP_READ;

public class HttpClient implements Client {
    public Response response = new Response();
    boolean timedOutInHandshake = false;
    public boolean handShake = false;
    public long currentSeqNum = 1;
    private static final Logger logger = LoggerFactory.getLogger(UDPClient.class);
    private Parser parser = new HttpParser();
    private Converter<Response, String> converter = new HttpResponseConverter();

    public Response get(Request request) {
        return getResponse(request);
    }

    public Response post(Request request) {
        return getResponse(request);
    }

    private Response getResponse(Request request) {
        Response response = null;
        SocketAddress routerAddress = new InetSocketAddress("localhost", 3000);
        InetSocketAddress serverAddress = new InetSocketAddress("localhost", 8007);
        response = runClient(routerAddress, serverAddress, request);
        return response;
    }

    public HttpClient(){

    }

    private Response runClient(SocketAddress routerAddr, InetSocketAddress serverAddr, Request request) {
        try {
                Packet pSYN = new Packet.Builder()
                .setType(PacketType.SYN.getIntValue())
                .setSequenceNumber(getCurrentSeqNum())
                .setPortNumber(serverAddr.getPort())
                .setPeerAddress(serverAddr.getAddress())
                .setPayload("in syn message".getBytes())
                .create();

            DatagramSocket socket = new DatagramSocket();
            DatagramPacket dp = new DatagramPacket(pSYN.toBytes(), pSYN.toBytes().length, routerAddr);

            socket.connect(routerAddr);
            socket.send(dp);


        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    private Packet[] segmentMessage(byte[] bytes, int maxSizePayload) {
        return null;
    }

    private int getWindowSize(int length) {
        if (length == 1) {
            return 1;
        } else {
            return length/2;
        }
    }

    private void closeConnection() {
        this.setHandShake(false);
        this.setCurrentSeqNum(1L);
    }

    private Response selectiveRepeat(Packet[] packets, int windowSize, DatagramChannel channel) {
        return null;
    }

    private void selectiveRepeat() {

    }

    private void makeHandShake(SocketAddress routerAddr, InetSocketAddress serverAddr) {
        Packet pSYN = new Packet.Builder()
                .setType(PacketType.SYN.getIntValue())
                .setSequenceNumber(getCurrentSeqNum())
                .setPortNumber(serverAddr.getPort())
                .setPeerAddress(serverAddr.getAddress())
                .setPayload("in syn message".getBytes())
                .create();

        try(DatagramChannel channel = DatagramChannel.open()){
            channel.send(pSYN.toBuffer(), routerAddr);

            logger.info("Sending SYN for handshake");

//            // Try to receive a packet within timeout.
            channel.configureBlocking(false);
            Selector selector = Selector.open();
            channel.register(selector, OP_READ);
            logger.info("Waiting for the response");
            selector.select(5000);

            Set<SelectionKey> keys = selector.selectedKeys();
            if(keys.isEmpty()){
                logger.error("No response after timeout");
                this.setTimedOutInHandshake(true);
            } else {
                this.setTimedOutInHandshake(false);
            }

            // We want the SYN_ACK from server
            ByteBuffer buf = ByteBuffer
                    .allocate(Packet.MAX_LEN)
                    .order(ByteOrder.BIG_ENDIAN);
            SocketAddress router = channel.receive(buf);
            buf.flip();
            Packet resp = Packet.fromBuffer(buf);
            logger.info("Packet: {}", resp);
            //logger.info("Router: {}", router);
            String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);
            logger.info("Payload: {}",  payload);

            // Send ACK from SYN_ACK
            if(resp.getType() == PacketType.SYN_ACK.getIntValue()) {
                this.currentSeqNum = this.currentSeqNum + 1;
                Packet pACK = resp.toBuilder()
                        .setType(PacketType.ACK.getIntValue())
                        .setSequenceNumber(getCurrentSeqNum())
                        .create();
                logger.info("Sending ACK from SYN_ACK");
                channel.send(pACK.toBuffer(), routerAddr);
                setHandShake(true);
            }
            keys.clear();
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
    }

    private void setTimeoutHandShake(DatagramChannel channel) throws IOException {

    }

    public boolean isHandShaken() {
        return handShake;
    }

    public Parser getParser() {
        return parser;
    }

    public void setParser(Parser parser) {
        this.parser = parser;
    }

    public Converter<Response, String> getConverter() {
        return converter;
    }

    public void setConverter(Converter<Response, String> converter) {
        this.converter = converter;
    }

    public void setHandShake(boolean handShake) {
        this.handShake = handShake;
    }

    public long getCurrentSeqNum() {
        return currentSeqNum;
    }

    public void setCurrentSeqNum(long currentSqeNum) {
        this.currentSeqNum = currentSqeNum;
    }

    public static Logger getLogger() {
        return logger;
    }

    public boolean isTimedOutInHandshake() {
        return timedOutInHandshake;
    }

    public void setTimedOutInHandshake(boolean timedOutInHandshake) {
        this.timedOutInHandshake = timedOutInHandshake;
    }
}
