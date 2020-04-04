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

@Command(name = "httpfs",
        commandListHeading = "%nThe commands are:%n",
        subcommands = {HelpCommand.class},
        description = "httpfs is a simple file server",
        version = "httpfs CLI version 1.0.0")
public class Httpfs implements Runnable {

    public static SocketAddress routerAddress = new InetSocketAddress("localhost", 3000);
    public static InetSocketAddress serverAddress = new InetSocketAddress("localhost", 8007);

    public PacketService pService = new PacketServiceImpl();

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

            socketServer.receive(dp);

            Packet packet = Packet.fromBytes(dp.getData());

            logger.info("Packet: {}", packet);

            /* Client wants to establish connection */
            if(packet.getType() == PacketType.SYN.getIntValue()){
                makeHandShake(socketServer, packet);
            }
            /* Client started sending data, first packet contains request method*/
            else if(packet.getType() == PacketType.ACK.getIntValue() && packet.getSequenceNumber() == 1) {
                Request request = reqConverter.convert(new String(packet.getPayload(), StandardCharsets.UTF_8));
                Method method = request.getHttpMethod();
                logger.info("Received DATA packet");
                logger.info("That is the payload: {}", new String(packet.getPayload(), StandardCharsets.UTF_8));
                if(Method.GET == method) {
                    selectiveRepeatForGET(requestWorker.processRequest(request), packet);
                }
                else if(Method.POST == method) {
                    selectiveRepeatForPOST(requestWorker.processRequest(request), packet);
                } else {
                    send405(packet);
                }

            }
        }
    }

    private void selectiveRepeatForPOST(Response response, Packet packet) {
    }

    private void selectiveRepeatForGET(Response response, Packet getPacket) {
        try{

            Packet pLastACK = null;

            String responseToSegment = parser.parseResponse(response);

            // Array of packets to send to client
            Packet[] packets = pService.segmentMessage(responseToSegment, getPacket);


            // Array of each ACK sent packet
            boolean[] pStatuses = new boolean[packets.length];
            Arrays.fill(pStatuses, false); // False by default

            //Initializing timeouts
            TimeOut timeOut = new TimeOut();
            RTT rtt = new RTT();

            /* Initial socket timeout */
            socketServer.setSoTimeout(2000); // 2 seconds

            while(!areAllSent(pStatuses)) {

                // Set timeout for buffer
                socketServer.setSoTimeout(timeOut.getEstimatedTimeOut());

                // Return the first index of NAK packet
                int indexNAK = getFirstNAK(pStatuses);

                // Returns current window size. Size = 4 by default and smaller when reaching
                // end of packets array
                int windowSize = getUpdatedWindowSize((indexNAK + 4), packets.length);

                // Send all packets not ACK yet in window
                sendPacketsInWindow(indexNAK, windowSize, packets, pStatuses, rtt);

                // Receive ACK packet from client
                pLastACK = receivePacketsACK(indexNAK, windowSize, pStatuses, timeOut, rtt);

                // Received ACK from END packet
                assert pLastACK != null;
                if(pLastACK.getType() == PacketType.ACK.getIntValue() && pLastACK.getSequenceNumber() == packets[packets.length - 1].getSequenceNumber()){
                    logger.info("END packet sent, ACK received from client");
                    socketServer.setSoTimeout(0);
                    break;
                }

            }
        } catch(Exception e) {
            logger.info(e.getMessage());
        }
    }

    private Packet receivePacketsACK(int indexNAK, int windowSize, boolean[] pStatuses, TimeOut timeOut, RTT rtt) throws IOException {

        // Method will return last ack from client
        Packet pLastACK = null;

        // Loop through window and receive ACK packet
        for(int i = indexNAK; i < windowSize; i++) {
            byte[] buffer = new byte[Packet.MAX_LEN];
            DatagramPacket dpACK = new DatagramPacket(buffer, buffer.length);
            try {
                socketServer.receive(dpACK);

                // Update timeout
                timeOut.calculateTimeOut(rtt.getRTTime(System.currentTimeMillis(), indexNAK));

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

    public void sendPacketsInWindow(int indexNAK, int windowSize, Packet[] packets, boolean[] pStatuses, RTT rtt) throws IOException {

        // Loop through window and send packet if not ACK
        for(int i = indexNAK; i < windowSize; i++) {
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
            rtt.initializeStartTime(System.currentTimeMillis(), indexNAK);

            // Send YAY!
            logger.info("Datagram Packet Size: {}", dpACK.getData().length);
            socketServer.send(dpACK);
        }
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
        return 0;
    }

    private boolean areAllSent(boolean[] pStatuses) {
        for (boolean pStatus : pStatuses) {
            if (!pStatus) {
                return false;
            }
        }
        return true;
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
