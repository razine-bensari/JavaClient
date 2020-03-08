package httpfs;

import httpc.api.Executor;
import httpc.api.Validator;
import httpc.impl.HttpExecutor;
import httpc.impl.HttpValidator;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Option;
import utils.impl.HttpParser;
import utils.impl.HttpRequestConverter;
import utils.impl.HttpResponseConverter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Command(name = "httpfs",
        commandListHeading = "%nThe commands are:%n",
        subcommands = {HelpCommand.class},
        description = "httpfs is a simple file server",
        version = "httpfs CLI version 1.0.0")
public class Httpfs implements Runnable {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private Executor executor = new HttpExecutor();
    private HttpParser parser = new HttpParser();
    private HttpRequestConverter reqConverter = new HttpRequestConverter();
    private HttpResponseConverter resConverter = new HttpResponseConverter();
    private Validator validator = new HttpValidator();
    private boolean upAndRunning = true;
    protected Thread runningThread = null;
    private ServerSocket serverSocket;

    @Option(names = {"-v", "--verbose"}, description = "Shows verbose output & prints debugging messages") boolean verbose;
    @Option(names = {"-p", "--port"}, description = "Specifies the port number that the server will listen and serve at.Default is 8080.") int port;
    @Option(names = {"-d", "--dir"}, description = "Specifies the directory that the server will use to read/write requested files.Default is the current directory when launching the application.") String dirPath;

    public Httpfs(int port){
        this.port = port;
    }
     public Httpfs() {

     }
    public static void main(String... args) {
        new CommandLine(new Httpfs()).execute(args);
    }

    @Override
    public void run() {
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        System.out.println("httpfs has been called");
        if(port == 0){
            port = 8080;
        }
        try {
            ServerSocket server = new ServerSocket(port);
            while(isUpAndRunning()){
                Socket clientSocket;
                clientSocket = server.accept();
                if(!isUpAndRunning()){
                    System.out.println("Server Shutting Down . . .");
                    return;
                }
                clientSocket.setSoTimeout(1000); // Six seconds timeout
                new Thread(new RequestWorker(clientSocket)).start();
            }
            System.out.println("Server Shutting Down . . .") ;
        } catch (IOException e){
            e.printStackTrace();
            e.getMessage();
        }
    }

    @Command(name = "stop", description = "Stops the httpfs server.")
    public synchronized void shutDown(){
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
        this.upAndRunning = false;
    }
    private synchronized boolean isUpAndRunning() {
        return this.upAndRunning;
    }
}
