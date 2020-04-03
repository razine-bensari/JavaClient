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
import java.util.Arrays;
import java.util.Hashtable;
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
            makeHandShake();
            return selectiveRepeatGET(request);
        } catch (SocketTimeoutException e) {
            logger.info(e.getMessage() + " during handshake. Trying again.");
            this.get(request);
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
        return response;
    }

    private Response selectiveRepeatGET(Request request) {
        /* Ready to send data */
        try{
            socketClient.setSoTimeout(10000); // 10 seconds

            // Make Get Packet
            currentSeqNum++;
            DatagramPacket getDP = makePacket(PacketType.DATA.getIntValue(), currentSeqNum, parser.parseRequest(request));

            // Send get request as single packet
            socketClient.send(getDP);

            /* This hashtable will hold the payload of all ACK packets*/
            Hashtable<Integer, String> payloads = new Hashtable<>();

            while(isHandShaken()) {

                Packet pResponse = obtainPacket(); //get packet from server

                //If it is a the last DATA packet (FIN)
                if(pResponse.getType() == PacketType.END.getIntValue()) {
                    response =  converter.convert(constructPayloadFromPayloads(payloads));
                } else {
                    // Continue to store payloads from packets
                    payloads.put((int)pResponse.getSequenceNumber(), Arrays.toString(pResponse.getPayload()));
                }
                // Send ACK from received packet
                pResponse.toBuilder()
                        .setType(PacketType.ACK.getIntValue())
                        .create();
                DatagramPacket pACK = new DatagramPacket(pResponse.toBytes(), pResponse.toBytes().length, routerAddress);

                socketClient.send(pACK);
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        return response;
    }

    private String constructPayloadFromPayloads(Hashtable<Integer, String> payloads) {
        StringBuilder httpPayload = new StringBuilder();

        /* Builds payload using sequences number in a sorted fashion */
        for(int i = 2; i < payloads.size(); i++) {
            httpPayload.append(payloads.get(i));
        }
        return httpPayload.toString();
    }

    private Packet obtainPacket() throws IOException {
        byte[] buffer = new byte[Packet.MAX_LEN];
        DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
        try {
            socketClient.receive(dp);
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("Could not receive packet");
        }
        return Packet.fromBytes(dp.getData());
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
            socketClient.setSoTimeout(2000);
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

    private void makeHandShake() throws IOException {
        DatagramPacket dpSYN = makePacket(PacketType.SYN.getIntValue(), getCurrentSeqNum(), "in syn payload");
        socketClient.send(dpSYN);

        byte[] buffer = new byte[Packet.MAX_LEN];
        DatagramPacket dpResponse = new DatagramPacket(buffer, buffer.length);
        socketClient.receive(dpResponse);

        Packet pSYN_ACK = Packet.fromBytes(dpResponse.getData());
        if(pSYN_ACK.getType() == PacketType.SYN_ACK.getIntValue()) {
            String payload = new String(pSYN_ACK.getPayload(), StandardCharsets.UTF_8);
            logger.info("Handshake ACK from server, sending data");
        }
        this.handShake = true;
    }

    private DatagramPacket makePacket(int type, long seq, String payload){
        Packet packet =  new Packet.Builder()
                .setType(type)
                .setSequenceNumber(seq)
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
