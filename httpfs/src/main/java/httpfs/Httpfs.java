package httpfs;

import RequestAndResponse.Method;
import RequestAndResponse.Request;
import RequestAndResponse.Response;
import ca.concordia.RTT;
import ca.concordia.TimeOut;
import ca.concordia.UDPServer;
import ca.concordia.domain.Packet;
import ca.concordia.domain.PacketType;
import ca.concordia.services.PacketServiceImpl;
import ca.concordia.services.api.PacketService;
import httpc.api.Executor;
import httpc.api.Validator;
import httpc.impl.HttpExecutor;
import httpc.impl.HttpValidator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Option;
import utils.impl.HttpParser;
import utils.impl.HttpRequestConverter;
import utils.impl.HttpResponseConverter;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Hashtable;

@Command(name = "httpfs",
        commandListHeading = "%nThe commands are:%n",
        subcommands = {HelpCommand.class},
        description = "httpfs is a simple file server",
        version = "httpfs CLI version 1.0.0")
public class Httpfs implements Runnable {

    public static SocketAddress routerAddress = new InetSocketAddress("localhost", 3000);
    public static InetSocketAddress serverAddress = new InetSocketAddress("localhost", 8007);

    public PacketService pService = new PacketServiceImpl();

    Request request;
    Response response;
    DatagramSocket socketServer;
    RequestWorker requestWorker;
    private static final Logger logger = LoggerFactory.getLogger(UDPServer.class);
    public long currentSeqNum = 2;
    boolean timedOutInHandshake = false;
    private Executor executor = new HttpExecutor();
    private HttpParser parser = new HttpParser();
    private HttpRequestConverter reqConverter = new HttpRequestConverter();
    private HttpResponseConverter resConverter = new HttpResponseConverter();
    private Validator validator = new HttpValidator();
    protected Thread runningThread = null;
    public boolean handShake = true;
    public boolean connectionOpen = true;
    public boolean lastPacketSent = false;
    public boolean sentAndReceivedRequest = false;

    @Option(names = {"-v", "--verbose"}, description = "Shows verbose output & prints debugging messages") boolean verbose;
    @Option(names = {"-p", "--port"}, description = "Specifies the port number that the server will listen and serve at.Default is 8080.") int port;
    @Option(names = {"-d", "--dir"}, description = "Specifies the directory that the server will use to read/write requested files.Default is the current directory when launching the application.") String dirPath;

    public Httpfs() {
        try {
            this.socketServer = new DatagramSocket(8007);
            socketServer.connect(routerAddress);

            this.requestWorker = new RequestWorker(this.dirPath);

            this.response = new Response();
            response.setVersion("HTTP/1.0");
            response.setStatusCode("405");
            response.setPhrase("Method Not Allowed");

        } catch (SocketException e) {
            logger.info(e.getMessage());
        }
    }

    public static void main(String... args) {
        new CommandLine(new Httpfs()).execute(args);
    }

    @Override
    public void run() {
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        try {
            listenAndServe();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenAndServe() throws IOException {
        if(!StringUtils.isEmpty(dirPath)){
            createPathToDirectory(dirPath);
        }
        logger.info("httpfs is listening at {}", socketServer.getLocalSocketAddress());

        socketServer.setSoTimeout(0); //Infinite time out

        while(isOpenConnection()) {

            byte[] buffer = new byte[Packet.MAX_LEN];
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length);

            socketServer.setSoTimeout(0); //Infinite time out

            socketServer.receive(dp);

            Packet packet = Packet.fromBytes(dp.getData());

            logger.info("Packet: {}", packet);

            /* Client wants to establish connection */
            if(packet.getType() == PacketType.SYN.getIntValue()){
                makeHandShake(socketServer, packet);
            }
            /* Client started sending data, first packet contains request method*/
            else if(packet.getType() == PacketType.DATA.getIntValue() && packet.getSequenceNumber() == 2) {
                Request request = reqConverter.convert(new String(packet.getPayload(), StandardCharsets.UTF_8));
                Method method = request.getHttpMethod();
                logger.info("Received DATA packet");
                logger.info("That is the payload: {}", new String(packet.getPayload(), StandardCharsets.UTF_8));
                if(Method.GET == method) {
                    selectiveRepeatForGET(requestWorker.processRequest(request), packet);
                }
                else if(Method.POST == method) {
                    selectiveRepeatForPOST(requestWorker, packet);
                } else {
                    send405(packet);
                }

            }
        }
    }

    private void selectiveRepeatForPOST(RequestWorker requestWorker, Packet pACKServer) throws IOException {

        socketServer.setSoTimeout(1000 * 300); // 300 seconds before time out

        /* This hashtable will hold the payload of all ACK packets*/
        Hashtable<Integer, String> payloads = new Hashtable<>();

        int lastSeq = -1;


        while(isHandShaken()) {

            Packet pResponse;

            if(pACKServer.getSequenceNumber() == 2 && !this.sentAndReceivedRequest) {
                pResponse = pACKServer;
                payloads.put((int)pResponse.getSequenceNumber(), new String(pResponse.getPayload(), StandardCharsets.UTF_8));
                this.sentAndReceivedRequest = true;
            } else {
                pResponse = obtainPacket(); //get packet from server
            }
            logger.info("pResponse size: {}", pResponse.getPayload().length);


            // Final ack from client has been received by server
            if((pResponse.getType() == PacketType.END.getIntValue()) || receivedAll(payloads, lastSeq)) {
                lastSeq = (int) pResponse.getSequenceNumber();
                int windowSize = pResponse.getPayload()[0];
                if(receivedAllPacketInWindow(payloads, windowSize)) {
                    request =  reqConverter.convert(constructPayloadFromPayloads(payloads));
                    this.handShake = false;
                }
            } else {
                // Continue to store payloads from packets
                payloads.put((int)pResponse.getSequenceNumber(), new String(pResponse.getPayload(), StandardCharsets.UTF_8));
            }

            // Send ACK from received packet
            if(this.request == null) {
                Packet pACK = pResponse.toBuilder()
                        .setType(PacketType.ACK.getIntValue())
                        .create();
                DatagramPacket dpACK = new DatagramPacket(pACK.toBytes(), pACK.toBytes().length, routerAddress);
                socketServer.send(dpACK);
             // Send response
            } else {
                Packet pRES_ACK = pResponse.toBuilder()
                        .setType(PacketType.ACK.getIntValue())
                        .setPayload(parser.parseResponse(requestWorker.processRequest(this.request)).getBytes())
                        .create();
                DatagramPacket dpRES_ACK = new DatagramPacket(pRES_ACK.toBytes(), pRES_ACK.toBytes().length, routerAddress);
                socketServer.send(dpRES_ACK);
            }

            if(!isHandShaken()) {
                socketServer.close();
                this.sentAndReceivedRequest = false;
            }
        }
    }

    private void selectiveRepeatForGET(Response response, Packet getPacket) {
        try{

            Packet pLastACK = null;

            String responseToSegment = parser.parseResponse(response);

            // Array of packets to send to client
            Packet[] packets = pService.segmentMessage(responseToSegment, getPacket);

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
            socketServer.setSoTimeout(2000); // 2 seconds

            while(!areAllSent(pStatuses)) {

                // Set timeout for buffer
                socketServer.setSoTimeout(timeOut.getEstimatedTimeOut());

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
                    logger.info("END packet sent, ACK received from client");
                    socketServer.setSoTimeout(0);
                }
            }
        } catch(Exception e) {
            logger.info(e.getMessage());
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
                socketServer.receive(dpACK);

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
            socketServer.send(dpACK);

            // Make boolean true if last packet has been sent.
            // This tells the server that there is no need to wait for ACK of END after socket timeout
            if(pACK.getType() == PacketType.END.getIntValue()) {
                this.lastPacketSent = true;
            }
        }
    }

    private boolean receivedAll(Hashtable<Integer, String> payloads, int lastSeq) {
        if(lastSeq == -1) {
            return false;
        } else {
            for(int i = 2; i <= lastSeq; i++) {
                if(payloads.get(i) == null) {
                    return false;
                }
            }
            return true;
        }
    }

    private String constructPayloadFromPayloads(Hashtable<Integer, String> payloads) {
        StringBuilder httpPayload = new StringBuilder();

        /* Builds payload using sequences number in a sorted fashion */
        for(int i = 2; i < payloads.size() + 2; i++) {
            httpPayload.append(payloads.get(i));
        }
        return httpPayload.toString();
    }

    private boolean receivedAllPacketInWindow(Hashtable<Integer, String> payloads, int windowSize) {
        boolean received = true;
        for(int i = (payloads.size() + 1); i >= ((payloads.size() + 2) - windowSize); i--) {
            if(payloads.get(i) == null) {
                received = false;
            }
        }
        return received;
    }

    private int getUpdatedWindowSize(int i, int length) {
        return Math.min(i, length);
    }

    private int getFirstNAK(boolean[] pStatuses) {
        for(int i = 0; i < pStatuses.length; i++) {
            if(!pStatuses[i]) {
                return i;
            }
        }
        return 1;
    }

    private boolean areAllSent(boolean[] pStatuses) {
        for (boolean pStatus : pStatuses) {
            if (!pStatus) {
                return false;
            }
        }
        return true;
    }

    private Packet obtainPacket() throws IOException {
        byte[] buffer = new byte[Packet.MAX_LEN];
        DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
        socketServer.receive(dp);
        return Packet.fromBytes(dp.getData());
    }


    private void send405(Packet packet) throws IOException {
        Packet p405 = packet.toBuilder()
                .setType(PacketType.DATA.getIntValue())
                .setPayload(parser.parseResponse(this.response).getBytes())
                .create();
        DatagramPacket respDP = new DatagramPacket(p405.toBytes(), p405.toBytes().length, routerAddress);
        socketServer.send(respDP);
    }

    private void makeHandShake(DatagramSocket socketServer, Packet packet) throws IOException {
        Packet pSYN_ACK = packet.toBuilder()
                .setType(PacketType.SYN_ACK.getIntValue())
                .setPayload("in syn_ack".getBytes())
                .create();

        DatagramPacket dpSYN_ACK = new DatagramPacket(pSYN_ACK.toBytes(), pSYN_ACK.toBytes().length, routerAddress);
        socketServer.send(dpSYN_ACK);
        this.handShake = true;
    }

    public void createPathToDirectory(String dirpath) {
        String absolutePath = "/Users/razine/workspace/JavaClientServerHTTP/fs";
        String sfPath = absolutePath + "/" + dirpath;
        File dir = new File(sfPath);
        dir.mkdirs();
    }

    private void closeConnection() {
        this.setHandShake(false);
        this.setCurrentSeqNum(0);
    }

    public boolean isHandShaken() {
        return handShake;
    }

    public boolean isOpenConnection() {
        return connectionOpen;
    }

    public boolean isHandShake() {
        return handShake;
    }

    public void setHandShake(boolean handShake) {
        this.handShake = handShake;
    }

    public long getCurrentSeqNum() {
        return currentSeqNum;
    }

    public void setCurrentSeqNum(long currentSeqNum) {
        this.currentSeqNum = currentSeqNum;
    }
    public boolean isTimedOutInHandshake() {
        return timedOutInHandshake;
    }

    public void setTimedOutInHandshake(boolean timedOutInHandshake) {
        this.timedOutInHandshake = timedOutInHandshake;
    }

    @Command(name = "stop", description = "Stops the httpfs server.")
    public synchronized void shutDown(){
        this.connectionOpen = false;
    }
}
