package httpc.impl;

import RequestAndResponse.Request;
import RequestAndResponse.Response;
import ca.concordia.UDPClient;
import ca.concordia.domain.Packet;
import ca.concordia.domain.PacketType;
import ca.concordia.services.PacketServiceImpl;
import ca.concordia.services.api.PacketService;
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
import java.util.Hashtable;

public class HttpClient implements Client {

    public static SocketAddress routerAddress = new InetSocketAddress("localhost", 3000);
    public static InetSocketAddress serverAddress = new InetSocketAddress("localhost", 8007);

    public static DatagramSocket socketClient;


    public static Response response = new Response();
    boolean timedOutInHandshake = false;
    public boolean handShake = false;
    public boolean sentAndReceivedRequest = false;
    public long currentSeqNum = 0;
    private static final Logger logger = LoggerFactory.getLogger(UDPClient.class);
    private Parser parser = new HttpParser();
    private Converter<Response, String> converter = new HttpResponseConverter();

    public PacketService pService = new PacketServiceImpl();



    public Response get(Request request) {
        return this.makeHandShake(request);
    }

    private Response selectiveRepeatGET(Request request, Packet pACKServer) throws IOException {

        socketClient.setSoTimeout(1000 * 300); // 30 seconds before time out

        /* This hashtable will hold the payload of all ACK packets*/
        Hashtable<Integer, String> payloads = new Hashtable<>();


        while(isHandShaken()) {

            Packet pResponse;

            if(pACKServer.getSequenceNumber() == 2 && !this.sentAndReceivedRequest) {
                pResponse = pACKServer;
                this.sentAndReceivedRequest = true;
            } else {
                pResponse = obtainPacket(); //get packet from server
            }
            logger.info("pResponse size: {}", pResponse.getPayload().length);


            // Final ack from client has been received by server
            if(pResponse.getType() == PacketType.END.getIntValue()) {
                response =  converter.convert(constructPayloadFromPayloads(payloads));
                this.handShake = false;
            } else {
                // Continue to store payloads from packets
                payloads.put((int)pResponse.getSequenceNumber(), new String(pResponse.getPayload(), StandardCharsets.UTF_8));
            }
            // Send ACK from received packet

            Packet pACK = pResponse.toBuilder()
                    .setType(PacketType.ACK.getIntValue())
                    .create();

            DatagramPacket dpACK = new DatagramPacket(pACK.toBytes(), pACK.toBytes().length, routerAddress);


            socketClient.send(dpACK);

            if(!isHandShaken()) {
                socketClient.close();
            }
        }
        return response;
    }

    private String constructPayloadFromPayloads(Hashtable<Integer, String> payloads) {
        StringBuilder httpPayload = new StringBuilder();

        /* Builds payload using sequences number in a sorted fashion */
        for(int i = 2; i < payloads.size() + 2; i++) {
            httpPayload.append(payloads.get(i));
        }
        return httpPayload.toString();
    }

    private Packet obtainPacket() throws IOException {
        byte[] buffer = new byte[Packet.MAX_LEN];
        DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
        socketClient.receive(dp);
        return Packet.fromBytes(dp.getData());
    }

    public Response post(Request request) {
        return getResponse(request);
    }

    private Response getResponse(Request request) {
        return response;
    }

    public HttpClient(){
        try {
            socketClient = new DatagramSocket();
            //socketClient.setSoTimeout(1000);
            socketClient.connect(routerAddress);
        } catch (SocketException e) {
            logger.info("Got a timeout due to constructor: {}", e.getMessage());
        }
    }

    private void closeConnection() {
        this.setHandShake(false);
        this.setCurrentSeqNum(1L);
        socketClient.close();
    }

    private Response makeHandShake(Request request) {
        Packet pACK = null;
        try {
            socketClient.setSoTimeout(3000); // Every one seconds, reattempt connection
            while(true) {
                DatagramPacket dpSYN = pService.makeNewDatagramPacket(PacketType.SYN.getIntValue(), getCurrentSeqNum(), "in syn payload", serverAddress, routerAddress);
                socketClient.send(dpSYN);

                byte[] buffer = new byte[Packet.MAX_LEN];
                DatagramPacket dpResponse = new DatagramPacket(buffer, buffer.length);

                socketClient.receive(dpResponse);

                Packet pSYN_ACK = Packet.fromBytes(dpResponse.getData());
                if(pSYN_ACK.getType() == PacketType.SYN_ACK.getIntValue()) {

                    String payload = new String(pSYN_ACK.getPayload(), StandardCharsets.UTF_8);


                    // Make Get Packet
                    currentSeqNum++;
                    DatagramPacket getDP = pService.makeNewDatagramPacket(PacketType.ACK.getIntValue(), currentSeqNum, parser.parseRequest(request), serverAddress, routerAddress);
                    // Send get request as single packet
                    socketClient.send(getDP);

                    //Wait for first ACK from server
                    byte[] buffer2 = new byte[Packet.MAX_LEN];
                    DatagramPacket dpResponse2 = new DatagramPacket(buffer2, buffer.length);

                    /* Ready to send data/ACK */
                    //socketClient.setSoTimeout(1000); // 4 second to get response from server

                    // receive ack from get
                    socketClient.receive(dpResponse2);

                    pACK = Packet.fromBytes(dpResponse2.getData());
                    this.handShake = true;
                    logger.info("Handshake Completed");


                    return selectiveRepeatGET(request, pACK);


                } else {
                    logger.info("Retrying handshake");
                }
            }
        } catch (SocketTimeoutException e) {
            logger.info("Failed to perform handshake. Time out received. Retrying");
            currentSeqNum = 0;
            response = this.makeHandShake(request);
        } catch (IOException e) {
            e.getMessage();
        }
        return response;
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
