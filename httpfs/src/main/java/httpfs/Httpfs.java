package httpfs;

import RequestAndResponse.Method;
import RequestAndResponse.Request;
import RequestAndResponse.Response;
import ca.concordia.domain.Packet;
import ca.concordia.domain.PacketType;
import ca.concordia.UDPServer;
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
import java.util.Arrays;

@Command(name = "httpfs",
        commandListHeading = "%nThe commands are:%n",
        subcommands = {HelpCommand.class},
        description = "httpfs is a simple file server",
        version = "httpfs CLI version 1.0.0")
public class Httpfs implements Runnable {

    public static SocketAddress routerAddress = new InetSocketAddress("localhost", 3000);
    public static InetSocketAddress serverAddress = new InetSocketAddress("localhost", 8007);

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
            else if(packet.getType() == PacketType.DATA.getIntValue() && packet.getSequenceNumber() == 1) {
                Request request = reqConverter.convert(Arrays.toString(packet.getPayload()));
                Method method = request.getHttpMethod();
                switch (method) {
                    case GET:
                        selectiveRepeatForGET(requestWorker.processRequest(request));
                    case POST:
                        selectiveRepeatForPOST(requestWorker.processRequest(request));
                    default:
                        send405();
                }
            }
        }
    }

    private void selectiveRepeatForPOST(Response response) {
    }

    private void selectiveRepeatForGET(Response response) {
        try{
            String responseToSegment = parser.parseResponse(response);

            Packet[] packets = segmentMessage(responseToSegment);


        } catch(Exception e) {
            logger.info(e.getMessage());
        }
    }

    private void send405() throws IOException {
        DatagramPacket respDP = makePacket(PacketType.DATA.getIntValue(), currentSeqNum, parser.parseResponse(this.response));
        socketServer.send(respDP);
    }

    private void selectiveRepeatGET() {

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

    public Packet[] segmentMessage(String responseToSegment) {
        int numPackets =
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

    private int getWindowSize(int length) {
        if (length == 1) {
            return 1;
        } else {
            return length/2;
        }
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
