package httpfs;

import RequestAndResponse.Response;
import ca.concordia.Packet;
import ca.concordia.PacketType;
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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;

import static java.nio.charset.StandardCharsets.UTF_8;

@Command(name = "httpfs",
        commandListHeading = "%nThe commands are:%n",
        subcommands = {HelpCommand.class},
        description = "httpfs is a simple file server",
        version = "httpfs CLI version 1.0.0")
public class Httpfs implements Runnable {

    Response response = new Response();
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

    public Httpfs(int port) { this.port = port; }
    public Httpfs() {}
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
        RequestWorker requestWorker = new RequestWorker(dirPath);

        DatagramSocket socketServer = new DatagramSocket(8007);
        logger.info("httpfs is listening at {}", socketServer.getLocalSocketAddress());

        while(true) {
            ByteBuffer buf = ByteBuffer
                    .allocate(Packet.MAX_LEN)
                    .order(ByteOrder.BIG_ENDIAN);
            buf.clear();

            byte[] buffer = new byte[Packet.MAX_LEN];

            DatagramPacket dp = new DatagramPacket(buffer, buffer.length);

            socketServer.receive(dp);

            Packet packet = Packet.fromBytes(dp.getData());

            String payload = new String(packet.getPayload(), UTF_8);
            logger.info("Packet: {}", packet);
            logger.info("Packet peer address: {}, and peer port: {}", packet.getPeerAddress().toString(), packet.getPeerPort());
            logger.info("Payload: {}", payload);
        }
    }

    private void selectiveRepeat() {

    }

    private void makeHandShake(Packet packet, SocketAddress routerAddr, DatagramChannel channel) throws IOException {
        logger.info("Received SYN from servers");
        this.currentSeqNum = this.currentSeqNum + 1;
        Packet pSYN_ACK = packet.toBuilder()
                .setSequenceNumber(getCurrentSeqNum())
                .setType(PacketType.SYN_ACK.getIntValue())
                .create();

        logger.info("Sending SYN_ACK");
        channel.send(pSYN_ACK.toBuffer(), routerAddr);

        ByteBuffer buf = ByteBuffer
                .allocate(Packet.MAX_LEN)
                .order(ByteOrder.BIG_ENDIAN);
        SocketAddress router = channel.receive(buf);
        buf.flip();
        Packet resp = Packet.fromBuffer(buf);
        if(resp.getType() == PacketType.ACK.getIntValue()) {
            logger.info("Received ACK from client. selection repeat can start");
            this.setHandShake(true);
        }
    }

    public Packet[] segmentMessage(byte[] b, int maximumSizePayload) {
        return null;
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
