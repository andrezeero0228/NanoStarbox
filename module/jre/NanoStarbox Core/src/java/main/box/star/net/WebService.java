package box.star.net;

import box.star.content.MimeTypeMap;
import box.star.net.http.HTTPServer;
import box.star.net.http.IHTTPSession;
import box.star.net.http.response.Response;
import box.star.net.tools.*;

import java.util.*;

public class WebService extends HTTPServer {

  public final List<ContentProvider> contentProviders = new ArrayList<ContentProvider>();
  public final Map<String, MimeTypeDriver<WebService>> mimeTypeDrivers = new Hashtable<>();
  public final MimeTypeMap mimeTypeMap = new MimeTypeMap();

  public WebService() { super(); }

  public WebService mountContentProvider(ContentProvider contentProvider){
    contentProviders.add(contentProvider);
    return this;
  }

  public WebService(String host, int port){
    super();
    configuration.set(CONFIG_HOST, host);
    configuration.set(CONFIG_PORT, port);
  }

  final public void addMimeTypeDriver(String mimeType, MimeTypeDriver<WebService> driver) {
    mimeTypeDrivers.put(mimeType, driver);
  }

  public ServerContent getContent(IHTTPSession session){
    String uri = session.getUri();
    // first: uri-equality
    for (ContentProvider provider:contentProviders){
      String path = provider.getBaseUri();
      if (path.equals(uri)) return provider.getContent(session);
    }
    // second: parent-uri-equality
    while (! uri.equals("/") ) {
      uri = uri.substring(0, Math.max(0, uri.lastIndexOf('/')));
      if (uri.equals("")) uri = "/";
      for (ContentProvider provider:contentProviders){
        String path = provider.getBaseUri();
        if (path.equals(uri)){
          ServerContent content = provider.getContent(session);
          if (content != null) return content;
        }
      }
    }
    // third: fail-silently
    return null;
  }

  protected ServerResult getResult(ServerContent content) {
    if (content == null) return null;
    if (content.isOkay()){
      MimeTypeDriver<WebService> driver = mimeTypeDrivers.get(content.mimeType);
      if (driver != null) return driver.createMimeTypeResult(this, content);
    }
    return new ServerResult(content);
  }

  private boolean quiet = true;

  @Override
  protected Response serviceRequest(IHTTPSession session) {

    if (!this.quiet) {
      Map<String, String> header = session.getHeaders();
      Map<String, String> parms = session.getParms();
      String uri = session.getUri();
      System.out.println(session.getMethod() + " '" + uri + "' ");

      Iterator<String> e = header.keySet().iterator();
      while (e.hasNext()) {
        String value = e.next();
        System.out.println("  HDR: '" + value + "' = '" + header.get(value) + "'");
      }
      e = parms.keySet().iterator();
      while (e.hasNext()) {
        String value = e.next();
        System.out.println("  PRM: '" + value + "' = '" + parms.get(value) + "'");
      }
    }

    try {
      ServerResult serverResult = getResult(getContent(session));
      if (serverResult == null) return Response.notFoundResponse();
      return serverResult.getResponse();
    } catch (Exception e){
      return serverExceptionResponse(e);
    }
  }

}