package box.star.net.tools;

import box.star.Tools;
import box.star.contract.Nullable;
import box.star.net.WebServer;
import box.star.net.http.IHTTPSession;
import box.star.net.http.response.Response;
import box.star.net.http.response.Status;
import box.star.text.Char;
import box.star.text.MacroShell;
import box.star.text.basic.Scanner;
import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.shell.Global;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Stack;

public class RhinoWebDriver extends WebServer.MimeTypeDriver {

  private static final String RHINO_DRIVER_KEY = "javascript/x-nano-starbox-rhino-servlet";
  private static final String RHINO_MACRO_DOCUMENT_DRIVER_KEY = "javascript/x-nano-starbox-rhino-macro-document";

  private static final String initScript = "var Starbox = Packages.box.star, " +
    "Net = Packages.box.star.net, " +
      "System = java.lang.System, " +
        "File = java.io.File;";
  Global global;

  public RhinoWebDriver(WebServer server) {
    global = new Global();
    Context cx = Context.enter();
    global.init(cx);
    cx.evaluateString(global, initScript, "<stdin>", 1, null);
    Context.exit();
    addGlobalObject("global", global);
    addGlobalObject("server", server);
    server.addStaticIndexFile("index.js");
    server.registerMimeTypeDriver(RHINO_DRIVER_KEY, this);
    server.registerMimeTypeDriver(RHINO_MACRO_DOCUMENT_DRIVER_KEY, this);
  }

  public static void createInstance(WebServer ws) {
    new RhinoWebDriver(ws);
  }

  public void addGlobalObject(String name, Object javaObject) {
    Context cx = Context.enter();
    ScriptRuntime.setObjectProp(global, name, Context.javaToJS(javaObject, global), cx);
    Context.exit();
  }

  @Override
  public Response generateServiceResponse(WebServer webServer, String uri, InputStream fileStream, String mimeType, IHTTPSession ihttpSession) {
    Context cx = Context.enter();

    try {
      if (mimeType.equals(RHINO_MACRO_DOCUMENT_DRIVER_KEY)) {
        Scriptable shell = getScriptShell(cx, global);
        ScriptRuntime.setObjectProp(shell, "file", Context.javaToJS(fileStream, shell), cx);
        ScriptRuntime.setObjectProp(shell, "session", Context.javaToJS(ihttpSession, shell), cx);
        Scanner scanner = new Scanner(uri, fileStream);
        MacroShell macroShell = new MacroShell(System.getenv());
        // allow javascript to program the shell
        ScriptRuntime.setObjectProp(shell, "shell", Context.javaToJS(macroShell, shell), cx);
       macroShell.addCommand("js", new MacroShell.Command(){
          @Override
          protected String run(String command, Stack<String> parameters) {
            StringBuilder output = new StringBuilder();
            for (String p: parameters) output.append((String)
                  Context.jsToJava(
                      cx.evaluateString(shell, p,
                          scanner.getPath(), (int) scanner.getLine(),
                          null), String.class)
            );
            return output.toString();
          }
        });
        // chop off the mime-type-magic
        scanner.nextField(Char.LINE_FEED);
        return webServer.htmlResponse(Status.OK, macroShell.start(scanner));
      }
      try (Reader reader = new InputStreamReader(fileStream)) {
        Script script = cx.compileReader(reader, uri, 1, null);
        Scriptable shell = getScriptShell(cx, global);
        script.exec(cx, shell);
        Function f = (Function) ScriptableObject.getProperty(shell, "generateServiceResponse");
        Object[] parameters = new Object[]{
            Context.javaToJS(uri, shell),
            Context.javaToJS(fileStream, shell),
            Context.javaToJS(mimeType, shell),
            Context.javaToJS(ihttpSession, shell)};
        return (Response) Context.jsToJava(f.call(cx, global, shell, parameters), Response.class);
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    finally {
      Context.exit();
    }
  }

  Scriptable getScriptShell(Context cx, @Nullable Scriptable parent) {
    return ScriptRuntime.newObject(cx, Tools.makeNotNull(parent, global), "Object", null);
  }

}
