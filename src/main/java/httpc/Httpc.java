/*
  Author: Razine Ahmed Bensari
  COMP445 –Winter 2020
  Data Communications & Computer Networks
  Lab Assignment # 1
  Due Date: Sunday, Feb9, 2020 by 11:59PM
  */
package httpc;

import httpc.subcommands.Get;
import httpc.subcommands.Post;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;


@Command(name = "httpc",
        description = "httpc is a curl-like application but supports HTTP protocol only.",
        commandListHeading = "%nThe commands are:%n",
        version = "httpc CLI version 1.0.0",
        subcommands = {
        CommandLine.HelpCommand.class, Get.class, Post.class
        })
public class Httpc implements Callable<Integer> {

    public static void main(String... args) {
        CommandLine cli = new CommandLine(new Httpc());
        System.exit(cli.execute(args));
    }

    public Integer call() {
        return 0; //My error code
    }
}