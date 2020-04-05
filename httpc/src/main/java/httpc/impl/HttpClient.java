package httpc.impl;

import RequestAndResponse.Request;
import RequestAndResponse.Response;
import ca.concordia.RTT;
import ca.concordia.TimeOut;
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
import java.util.Arrays;
import java.util.Hashtable;

public class HttpClient implements Client {

    public static SocketAddress routerAddress = new InetSocketAddress("localhost", 3000);
    public static InetSocketAddress serverAddress = new InetSocketAddress("localhost", 8007);

    public static DatagramSocket socketClient;


    public static Response response = new Response();
    boolean timedOutInHandshake = false;
    public boolean handShake = false;
    public boolean lastPacketSent = false;
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

        socketClient.setSoTimeout(1000 * 300); // 300 seconds before time out

        /* This hashtable will hold the payload of all ACK packets*/
        Hashtable<Integer, String> payloads = new Hashtable<>();

        int lastSeq = -1;
        int windowSize = 1;

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
            if((pResponse.getType() == PacketType.END.getIntValue()) || receivedAll(payloads, lastSeq)) {
                if((pResponse.getType() == PacketType.END.getIntValue())) {
                    lastSeq = (int) pResponse.getSequenceNumber();
                    windowSize = pResponse.getPayload()[0];
                }
                if(receivedAll(payloads, lastSeq)) {
                    response =  converter.convert(constructPayloadFromPayloads(payloads));
                    this.handShake = false;
                }
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

            if(receivedAll(payloads, lastSeq)) {
                response =  converter.convert(constructPayloadFromPayloads(payloads));
                this.handShake = false;
            }
            if(!isHandShaken()) {
                socketClient.close();
                this.sentAndReceivedRequest = false;
            }
        }
        return response;
    }

    private boolean receivedAll(Hashtable<Integer, String> payloads, int lastSeq) {
        if(lastSeq == -1) {
            return false;
        } else {
            for(int i = 2; i <= (lastSeq - 1); i++) {
                if(payloads.get(i) == null) {
                    return false;
                }
            }
            return true;
        }
    }

    private boolean receivedAllPacketInWindow(Hashtable<Integer, String> payloads, int windowSize) {
        for(int i = (payloads.size() + 1); i >= ((payloads.size() + 1) - windowSize); i--) {
            if(payloads.get(i) == null) {
                return false;
            }
        }
        return true;
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
            socketClient.setSoTimeout(300); // Every one seconds, reattempt connection
            while(true) {
                DatagramPacket dpSYN = pService.makeNewDatagramPacket(PacketType.SYN.getIntValue(), getCurrentSeqNum(), "in syn payload", serverAddress, routerAddress);
                socketClient.send(dpSYN);

                byte[] buffer = new byte[Packet.MAX_LEN];
                DatagramPacket dpResponse = new DatagramPacket(buffer, buffer.length);

                socketClient.receive(dpResponse);

                Packet pSYN_ACK = Packet.fromBytes(dpResponse.getData());
                if(pSYN_ACK.getType() == PacketType.SYN_ACK.getIntValue()) {
                    if(request.getHttpMethod().toString().equals("GET")) {

                        // Make Get Packet
                        currentSeqNum++;
                        DatagramPacket getDP = pService.makeNewDatagramPacket(PacketType.ACK.getIntValue(), currentSeqNum, parser.parseRequest(request), serverAddress, routerAddress);
                        // Send get request as single packet
                        socketClient.send(getDP);

                        //Wait for first ACK from server
                        byte[] buffer2 = new byte[Packet.MAX_LEN];
                        DatagramPacket dpResponse2 = new DatagramPacket(buffer2, buffer.length);

                        // receive ack from get
                        socketClient.receive(dpResponse2);

                        pACK = Packet.fromBytes(dpResponse2.getData());
                        this.handShake = true;
                        logger.info("Handshake Completed");

                        return selectiveRepeatGET(request, pACK);
                    }
                    if(request.getHttpMethod().toString().equals("GET")) {

                        this.handShake = true;
                        logger.info("Handshake Completed");

                        return selectiveRepeatPOST(request, pSYN_ACK);
                    }
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

    private Response selectiveRepeatPOST(Request request, Packet pSYN_ACK) throws IOException {
            try{

            Packet pLastACK = null;

            String requestToSegment = parser.parseRequest(request);

            // Array of packets to send to client
            Packet[] packets = pService.segmentMessage(requestToSegment, pSYN_ACK);

            // Get window size (half of packets array)
            int windowSize = packets.length/2;
            logger.info("Window Size is: {}", windowSize);
            logger.info("Packets[].length is: {}", packets.length);


            // Array of each ACK sent packet
            boolean[] pStatuses = new boolean[packets.length];
            Arrays.fill(pStatuses, false); // False by default

            //Initializing timeouts
            TimeOut timeOut = new TimeOut();
            RTT rtt = new RTT(windowSize);

            /* Initial socket timeout */
            socketClient.setSoTimeout(2000); // 2 seconds

            while(!areAllSent(pStatuses)) {

                // Set timeout for buffer
                socketClient.setSoTimeout(timeOut.getEstimatedTimeOut());

                // Return the first index of NAK packet
                int indexNAK = getFirstNAK(pStatuses);

                // Returns current window size. Size = half of packet length by default and smaller when reaching
                // end of packets array
                int newWindowRange = getUpdatedWindowSize((indexNAK + windowSize), packets.length);

                // Send all packets not ACK yet in window
                sendPacketsInWindow(indexNAK, newWindowRange, windowSize,packets, pStatuses, rtt);

                // Receive ACK packet from client
                pLastACK = receivePacketsACK(indexNAK, newWindowRange, windowSize,pStatuses, timeOut, rtt);

            }
            // Received ACK from END packet
            if(pLastACK != null) {
                if(pLastACK.getType() == PacketType.ACK.getIntValue() && pLastACK.getSequenceNumber() == packets[packets.length - 1].getSequenceNumber()){
                    logger.info("END packet sent, ACK received from server");
                    socketClient.close();
                    response = converter.convert(Arrays.toString(pLastACK.getPayload()));
                    return converter.convert(Arrays.toString(pLastACK.getPayload()));
                }
            }
        } catch(Exception e) {
            logger.info(e.getMessage());
        }
            return response;
    }

    private int getUpdatedWindowSize(int i, int length) {
        return Math.min(i, length);
    }

    private boolean areAllSent(boolean[] pStatuses) {
        for (boolean pStatus : pStatuses) {
            if (!pStatus) {
                return false;
            }
        }
        return true;
    }

    public void sendPacketsInWindow(int indexNAK, int windowRange, int windowSize, Packet[] packets, boolean[] pStatuses, RTT rtt) throws IOException {

        // Loop through window and send packet if not ACK
        for(int i = indexNAK; i < windowRange; i++) {
            Packet pCurrent = packets[i];

            // If packet has already been ACK, do not send it, skip current iteration
            if(pStatuses[((int) (pCurrent.getSequenceNumber() - 2))]) {
                continue;
            }

            /* Else, send packets */

            // Create packet to send
            Packet pACK = pService.makePacketFromPacket(pCurrent, pCurrent.getType(), pCurrent.getSequenceNumber(), new String(pCurrent.getPayload(), StandardCharsets.UTF_8));
            DatagramPacket dpACK = new DatagramPacket(pACK.toBytes(), pACK.toBytes().length, routerAddress);

            //Add timeout
            rtt.initializeStartTime(System.currentTimeMillis(), indexNAK, windowSize);

            // Send YAY!
            logger.info("Datagram Packet Size: {}", dpACK.getData().length);
            socketClient.send(dpACK);

            // Make boolean true if last packet has been sent.
            // This tells the server that there is no need to wait for ACK of END after socket timeout
            if(pACK.getType() == PacketType.END.getIntValue()) {
                this.lastPacketSent = true;
            }
        }
    }

    private Packet receivePacketsACK(int indexNAK, int newWindowRange, int windowSize, boolean[] pStatuses, TimeOut timeOut, RTT rtt) throws IOException {

        // Method will return last ack from client
        Packet pLastACK = null;

        // Loop through window and receive ACK packet
        for(int i = indexNAK; i < newWindowRange; i++) {
            byte[] buffer = new byte[Packet.MAX_LEN];
            DatagramPacket dpACK = new DatagramPacket(buffer, buffer.length);
            try {
                socketClient.receive(dpACK);

                // Update timeout
                timeOut.calculateTimeOut(rtt.getRTTime(System.currentTimeMillis(), indexNAK, windowSize));

            } catch (SocketTimeoutException e) {
                logger.info("Socket timed out when receiving from clients");
                e.printStackTrace();
                // Packet timed out! Skip rest of iteration
                continue;
            }

            // Make received ACk as true in array
            Packet pACK = Packet.fromBytes(dpACK.getData());
            pLastACK = pACK;
            pStatuses[((int) (pACK.getSequenceNumber() - 2))] = true;

        }
        return pLastACK;
    }

    private int getFirstNAK(boolean[] pStatuses) {
        for(int i = 0; i < pStatuses.length; i++) {
            if(!pStatuses[i]) {
                return i;
            }
        }
        return 1;
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
