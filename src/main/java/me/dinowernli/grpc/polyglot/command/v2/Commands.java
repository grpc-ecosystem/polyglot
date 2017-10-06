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


      return new CommandRoot(jCommander.build(), commandCall, commandList);
    }

    private CommandRoot(JCommander jCommander, CommandCall commandCall, CommandList commandList) {
      this.jCommander = jCommander;
      this.commandCall = commandCall;
      this.commandList = commandList;
    }

    public CommandCall callCommand() {
      return commandCall;
    }

    public CommandList listCommand() {
      return commandList;
    }
  }

  @Parameters(separators = "=", commandDescription = "Call a grpc method on a remote server")
  public static class CommandCall {
    private void attach(JCommander.Builder commandBuilder) {
      commandBuilder.addCommand("call", this);
    }
  }

  @Parameters(separators = "=", commandDescription = "List all grpc methods on a remote server")
  public static class CommandList {
    private void attach(JCommander.Builder commandBuilder) {
      commandBuilder.addCommand("list", this);
    }
  }

  @Parameters(separators = "=")
  public static class SharedFlags {

  }

  private interface Attachable {
    void attach(JComm)
  }
}
