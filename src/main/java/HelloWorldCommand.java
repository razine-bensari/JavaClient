package main.java;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "hello",
        description = "Says hello"
)
public class HelloWorldCommand implements Runnable {
    public static void main(String[] args) {
        CommandLine.run(new HelloWorldCommand(), args);
    }

    public void run() {
        System.out.println("Hello World!");
    }
}