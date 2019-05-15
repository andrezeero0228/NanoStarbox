package box.star.net.tools;

import box.star.content.MimeTypeMap;
import box.star.net.http.IHTTPSession;
import box.star.net.http.response.Response;
import box.star.net.http.response.Status;

import java.util.HashMap;
import java.util.Map;

public class ContentProvider {

  private static final Map<String, String> mimeTypePaths = new HashMap<>();
  private final MimeTypeMap mimeTypeMap;
  private final String baseUri;

  // <user-methods>

  final public void setUriMimeType(String uri, String mimeType){
    mimeTypePaths.put(uri, mimeType);
  }

  final protected String getMimeType(String uri){
    if (mimeTypePaths.containsKey(uri)) return mimeTypePaths.get(uri);
    return mimeTypeMap.get(mimeTypeMap.scanFileExtension(uri));
  }

  final public String getBaseUri() { return baseUri; }

  final protected ServerContent redirect(String location){
    Response redirection = Response.newFixedLengthResponse(Status.REDIRECT, "text/html", "<html><body>Redirected: <a href=\"" + location + "\">" + location + "</a></body></html>");
    redirection.addHeader("Location", location);
    return new ServerContent(redirection);
  }

  // </user-methods>

  public ContentProvider(MimeTypeMap mimeTypeMap, String baseUri){
    this.baseUri = baseUri;
    this.mimeTypeMap = mimeTypeMap;
  }

  public ServerContent getContent(IHTTPSession session) {
    return null;
  }

}
