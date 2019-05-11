package box.star.text;

import box.star.Tools;
import box.star.contract.NotNull;
import box.star.text.basic.Scanner;
import box.star.text.basic.ScannerMethod;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;

import static box.star.text.Char.BACKSLASH;

public class MacroShell {

  private final static char
      ENTER_PROCEDURE = '(', EXIT_PROCEDURE = ')',
      ENTER_OBJECT = '{', EXIT_OBJECT = '}',
      ENTER_VARIABLE = '[', EXIT_VARIABLE = ']';

  private static final char[] BREAK_PROCEDURE_MAP =
      new Char.Assembler(Char.MAP_ASCII_ALL_WHITE_SPACE)
          .merge(EXIT_PROCEDURE)
            .toArray();

  public char macroTrigger = '%';

  public static class Command implements Cloneable {
    protected Scanner scanner;
    protected MacroShell main;

    protected String call(String name, String... parameters){
      Stack<String>p = new Stack<>();
      p.addAll(Arrays.asList(parameters));
      return main.doCommand(scanner, name, p);
    }

    protected String call(String name, Stack<String> parameters){
      return main.doCommand(scanner, name, parameters);
    }

    protected String eval(String source){
      return main.start("eval", source);
    }

    protected Stack<String> split(String source){
      ParameterBuilder pb = new ParameterBuilder();
      Scanner scanner = new Scanner("split", source+EXIT_PROCEDURE);
      Stack<String> parameters = new Stack<>();
      scanner.run(pb, main, parameters);
      return parameters;
    }

    protected void enterContext(Scanner scanner, MacroShell main){
      this.scanner = scanner;
      this.main = main;
    }
    /**
     * Your subclass implementation here
     * @param command
     * @param parameters
     * @return
     */
    protected String run(String command, Stack<String> parameters){ return Tools.EMPTY_STRING; }
    
    @Override
    protected Object clone() {
      try { return super.clone(); }
      catch (CloneNotSupportedException e) { throw new RuntimeException(e); }
    }
  }

  public Map<String, String> environment;
  public Map<String, Object> objects;
  public Map<String, MacroShell.Command> commands;

  private CommandBuilder commandBuilder = new CommandBuilder();
  private Main macroRunner = new Main(this);

  public MacroShell(Map<String, String> environment){
    this.environment = new Hashtable<>(environment);
    objects = new Hashtable<>();
    commands = new Hashtable<>();
  }

  public MacroShell addCommand(String name, Command command){
    commands.put(name, command); return this;
  }

  public MacroShell addCommands(Map<String, Command>map){
    commands.putAll(map); return this;
  }

  public MacroShell addObject(String name, Object object){
    objects.put(name, object); return this;
  }

  public MacroShell loadObjects(Map<String, Object>map){
    objects.putAll(map); return this;
  }

  private String nextMacroBody(Scanner scanner, char closure){
    String data = scanner.nextField(closure);
    scanner.nextCharacter(closure);
    return data;
  }

  public String start(Scanner scanner){
    return scanner.run(macroRunner);
  }

  public String start(String file, String text){
    Scanner scanner = new Scanner(file, text);
    return scanner.run(macroRunner);
  }

  public String start(String file, String text, long line, long column, long index){
    Scanner scanner = new Scanner(file, text).at(line, column, index);
    return scanner.run(macroRunner);
  }

  private String doMacro(Scanner scanner){
    MacroShell context = this;
    char next = scanner.next();
    switch (next){
      case ENTER_OBJECT: {
        return(Tools.makeNotNull(
            context.objects.get(nextMacroBody(scanner, EXIT_OBJECT)
            ), "undefined").toString());
      }
      case ENTER_PROCEDURE: {
        String output = scanner.run(commandBuilder, context);
        scanner.nextCharacter(EXIT_PROCEDURE);
        return(output);
      }
      case ENTER_VARIABLE: {
        return(context.environment.get(nextMacroBody(scanner, EXIT_VARIABLE)));
      }
      default: scanner.back();
    }
    throw new IllegalStateException();
//    return Char.toString(macroTrigger); // failed;
  }

  private String doCommand(Scanner scanner, String commandName, Stack<String> parameters) {
    if (commands.containsKey(commandName)){
      Command command = commands.get(commandName);
      command.enterContext(scanner, this);
      return command.run(commandName, parameters);
    }
    throw scanner.syntaxError("unknown command: '"+commandName+"'");
  }

  public static class Main extends ScannerMethod {

    MacroShell context;

    public Main(MacroShell context){ this.context = context; }

    @Override
    protected boolean terminate(@NotNull Scanner scanner, char character) {
      if (character == context.macroTrigger) { swap(context.doMacro(scanner)); return false; }
      return super.terminate(scanner, character);
    }

  }

  private class CommandBuilder extends ScannerMethod {

    MacroShell context;
    private ParameterBuilder parameterBuilder = new ParameterBuilder();

    @Override
    protected void start(@NotNull Scanner scanner, Object[] parameters) {
      this.context = (MacroShell) parameters[0];
    }

    @Override
    protected boolean scan(@NotNull Scanner scanner) {
      if (current() == EXIT_PROCEDURE){ backStep(scanner); }
      else {
        String name = current()+scanner.nextField(BREAK_PROCEDURE_MAP);
        Stack<String> parameters = new Stack<>();
        scanner.run(parameterBuilder, context, parameters);
        swap(context.doCommand(scanner, name, parameters));
      }
      return false;
    }
    
  }

  private static class ParameterBuilder extends  ScannerMethod {

    private static final Char.Assembler assembler =
        new Char.Assembler(BREAK_PROCEDURE_MAP)
            .merge(Char.DOUBLE_QUOTE, Char.SINGLE_QUOTE);

    private char[] PARAMETER_TEXT_MAP;

    MacroShell context;
    Stack<String> parameters;

    @Override
    protected void start(@NotNull Scanner scanner, Object[] parameters) {
      this.context = (MacroShell) parameters[0];
      this.parameters = (Stack<String>)parameters[1];
      scanner.nextMap(Char.MAP_ASCII_ALL_WHITE_SPACE);
      PARAMETER_TEXT_MAP = assembler.merge(context.macroTrigger).toArray();
    }

    @Override
    protected boolean scan(@NotNull Scanner scanner) {
      scanner.nextMap(Char.MAP_ASCII_ALL_WHITE_SPACE);
      return true;
    }

    protected Stack<String> split(String source){
      Scanner scanner = new Scanner("split", source+EXIT_PROCEDURE);
      Stack<String> parameters = new Stack<>();
      scanner.run(this, context, parameters);
      return parameters;
    }

    /**
     * A hack on nextBoundField that allows us to seek-beyond quotes within macro functions.
     *
     * @param scanner
     * @return
     * @throws Scanner.SyntaxError
     */
    private String extractQuote(Scanner scanner) throws Scanner.SyntaxError {

      StringBuilder sb = new StringBuilder();
      int nesting = 0;

      while (true) {

        char c = scanner.next();

        if (c == BACKSLASH && ! scanner.escapeMode()) continue;
        if (c == 0) {
          if (scanner.escapeMode()) {
            throw scanner.syntaxError("expected character escape sequence, found end of stream");
          }
          if (nesting > 0) {
            throw scanner.syntaxError("expected end of macro");
          }
          return sb.toString();
        }

        if (scanner.escapeMode()) {
          String swap = scanner.expand(c);
          sb.append(swap);
          continue;
        }

        if (c == context.macroTrigger) {
          char n = scanner.next();
          if (n == '(') nesting++;
          scanner.back();
        } else if (nesting > 0 && c == ')') nesting--;

        if (nesting == 0 && c == Char.DOUBLE_QUOTE){ scanner.back(); break; }

        sb.append(c);

      }
      return sb.toString();
    }

    private String getParameter(Scanner scanner, char character){
      char c;
      StringBuilder data = new StringBuilder();
      if (character == context.macroTrigger){
        data.append(context.doMacro(scanner));
      } else if (character == Char.DOUBLE_QUOTE) {
        long ln = scanner.getLine(), col = scanner.getColumn(), idx = scanner.getIndex();
        data.append(this.extractQuote(scanner));
        scanner.nextCharacter(character);
        if (data.indexOf(Char.toString(context.macroTrigger)) != -1) {
          data = new StringBuilder(context.start(scanner.getPath(), data.toString(), ln, col, idx));
        }
      } else if (character == Char.SINGLE_QUOTE) {
        data.append(scanner.nextField(character));
        scanner.nextCharacter(character);
      } else {
        data.append(character + scanner.nextBoundField(PARAMETER_TEXT_MAP));
      }
      while (!Char.mapContains(c = scanner.next(), BREAK_PROCEDURE_MAP)){
        data.append(getParameter(scanner, c));
      }
      return data.toString();
    }

    @Override
    protected boolean terminate(@NotNull Scanner scanner, char character) {
      if (character == EXIT_PROCEDURE){ backStep(scanner); return true; }
      else if (character == context.macroTrigger) {
        parameters.addAll(split(getParameter(scanner, character)));
        return false;
      }
      parameters.push(getParameter(scanner, character));
      return false;
    }

    @Override
    protected @NotNull String compile(@NotNull Scanner scanner) { return Tools.EMPTY_STRING; }

  }
}

