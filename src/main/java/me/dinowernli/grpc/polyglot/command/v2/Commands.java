package me.dinowernli.grpc.polyglot.command.v2;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameters;

public class Commands {
  public static class CommandRoot {
    private JCommander jCommander;
    private CommandCall commandCall;
    private CommandList commandList;
    private SharedFlags sharedFlags;

    public static CommandRoot create() {
      JCommander.Builder jCommander = JCommander.newBuilder();

      CommandCall commandCall = new CommandCall();
      commandCall.attach(jCommander);

      CommandList commandList = new CommandList();
      commandCall.attach(jCommander);

      SharedFlags sharedFlags = new SharedFlags();
      commandCall.attach(jCommander);

      return new CommandRoot(jCommander.build(), commandCall, commandList);
    }

    private CommandRoot(JCommander jCommander, CommandCall commandCall, CommandList commandList) {
      this.jCommander = jCommander;
      this.commandCall = commandCall;
      this.commandList = commandList;
    }

    public CommandRoot parse(String[] args) {
      jCommander.parse(args);
      return this;
    }

    public void run() {
      String parsedCommand = jCommander.getParsedCommand();
      if (parsedCommand.equals("call")) {
        commandCall.run();
      } else if (parsedCommand.equals("list")) {
        commandList.run();
      } else {
        throw new IllegalArgumentException("Unknown command: " + parsedCommand);
      }
    }
  }

  @Parameters(separators = "=", commandDescription = "Call a grpc method on a remote server")
  public static class CommandCall {
    private void attach(JCommander.Builder commandBuilder) {
      commandBuilder.addCommand("call", this);
    }

    private void run() {

    }
  }

  @Parameters(separators = "=", commandDescription = "List all grpc methods on a remote server")
  public static class CommandList {
    private void attach(JCommander.Builder commandBuilder) {
      commandBuilder.addCommand("list", this);
    }

    private void run() {

    }
  }

  @Parameters(separators = "=")
  public static class SharedFlags {
    private void attach(JCommander.Builder commandBuilder) {
      commandBuilder.addObject(this);
    }
  }
}
