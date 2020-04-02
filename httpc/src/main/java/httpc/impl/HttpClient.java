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
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.Timer;

public class HttpClient implements Client {

    public static SocketAddress routerAddress = new InetSocketAddress("localhost", 3000);
    public static InetSocketAddress serverAddress = new InetSocketAddress("localhost", 8007);

    public static DatagramSocket socketClient;

    static DatagramPacket[] packetBuffer;
    static Timer timeout;
    static int[] window;
    static int startWindow;


    public static Response response = new Response();
    boolean timedOutInHandshake = false;
    public boolean handShake = false;
    public long currentSeqNum = 0;
    private static final Logger logger = LoggerFactory.getLogger(UDPClient.class);
    private Parser parser = new HttpParser();
    private Converter<Response, String> converter = new HttpResponseConverter();



    public Response get(Request request) {
        try {
            makeHandShake(socketClient);
            selectiveRepeatGET();
        } catch (SocketTimeoutException e) {
            logger.info(e.getMessage() + " during handshake. Trying again.");
            this.get(request);
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
        return response;
    }

    private void selectiveRepeatGET() {

    }

    public Response post(Request request) {
        return getResponse(request);
    }

    private Response getResponse(Request request) {
//        try {
//            DatagramSocket socketClient = new DatagramSocket();
//            socketClient.connect(routerAddress);
//            makeHandShake(socketClient);
//        } catch (Exception e) {
//            logger.info("Error socket: {}", e.getMessage());
//        }
        return response;
    }

    public HttpClient(){
        try {
            socketClient = new DatagramSocket();
            socketClient.setSoTimeout(5000);
            socketClient.connect(routerAddress);
        } catch (SocketException e) {
            logger.info(e.getMessage());
        }
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

//    private Response selectiveRepeat(Packet[] packets, int windowSize, Request request, SocketAddress routerAddr, InetSocketAddress serverAddr) {
//       // packetBuffer = getPacketBufferFromRequest(request, serverAddr); /* Contains the packets for handshake */
//    }

//    private DatagramPacket[] getPacketBufferFromRequest(Request request, InetSocketAddress serverAddr) {
//        String requestString = parser.parseRequest(request);
//
//    }




    private void makeHandShake(DatagramSocket socketClient) throws IOException {
        DatagramPacket dpSYN = makePacket(PacketType.SYN.getIntValue(), getCurrentSeqNum(), "in syn payload");
        socketClient.send(dpSYN);

        byte[] buffer = new byte[Packet.MAX_LEN];
        DatagramPacket dpResponse = new DatagramPacket(buffer, buffer.length);
        socketClient.receive(dpResponse);

        Packet pACK = Packet.fromBytes(dpResponse.getData());
        if(pACK.getType() == PacketType.SYN_ACK.getIntValue() && pACK.getSequenceNumber() == 1) {
            String payload = new String(pACK.getPayload(), StandardCharsets.UTF_8);
            logger.info("Handshake ACK from server, sending data");
            logger.info("Packet: {}", pACK);
            logger.info("Payload: {}",  payload);
        }

    }

    private DatagramPacket makePacket(int type, long seq, String payload){
        Packet packet =  new Packet.Builder()
                .setType(PacketType.SYN.getIntValue())
                .setSequenceNumber(getCurrentSeqNum())
                .setPortNumber(serverAddress.getPort())
                .setPeerAddress(serverAddress.getAddress())
                .setPayload(payload.getBytes())
                .create();
        return new DatagramPacket(packet.toBytes(), packet.toBytes().length, routerAddress);
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
