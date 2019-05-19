package box.star.io;

import box.star.Tools;
import box.star.content.ParsedContentType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Attila Szegedi
 * @version $Id: SourceReader.java,v 1.2 2010/02/15 19:31:17 szegedia%freemail.hu Exp $
 */
public class SourceConnector {
  public static URL toUrl(String path) {
    // Assume path is URL if it contains a colon and there are at least
    // 2 characters in the protocol part. The later allows under Windows
    // to interpret paths with driver letter as file, not URL.
    if (path.indexOf(':') >= 2) {
      try {
        return new URL(path);
      }
      catch (MalformedURLException ex) {
        // not a URL
      }
    }
    return null;
  }

  public static Object readFileOrUrl(String path, boolean convertToString,
                                     String defaultEncoding) throws IOException {
    URL url = toUrl(path);
    InputStream is = null;
    int capacityHint = 0;
    String encoding;
    final String contentType;
    byte[] data;
    try {
      if (url == null) {
        File file = new File(path);
        contentType = encoding = null;
        capacityHint = (int) file.length();
        is = new FileInputStream(file);
      } else {
        URLConnection uc = url.openConnection();
        is = uc.getInputStream();
        if (convertToString) {
          ParsedContentType pct = new ParsedContentType(uc.getContentType());
          contentType = pct.getContentType();
          encoding = pct.getEncoding();
        } else {
          contentType = encoding = null;
        }
        capacityHint = uc.getContentLength();
        // Ignore insane values for Content-Length
        if (capacityHint > (1 << 20)) {
          capacityHint = -1;
        }
      }
      if (capacityHint <= 0) {
        capacityHint = 4096;
      }

      data = Tools.readStream(is, capacityHint);
    }
    finally {
      if (is != null) {
        is.close();
      }
    }

    Object result;
    if (!convertToString) {
      result = data;
    } else {
      if (encoding == null) {
        // None explicitly specified in Content-type header. Use RFC-4329
        // 4.2.2 section to autodetect
        if (data.length > 3 && data[0] == -1 && data[1] == -2 && data[2] == 0 && data[3] == 0) {
          encoding = "UTF-32LE";
        } else if (data.length > 3 && data[0] == 0 && data[1] == 0 && data[2] == -2 && data[3] == -1) {
          encoding = "UTF-32BE";
        } else if (data.length > 2 && data[0] == -17 && data[1] == -69 && data[2] == -65) {
          encoding = "UTF-8";
        } else if (data.length > 1 && data[0] == -1 && data[1] == -2) {
          encoding = "UTF-16LE";
        } else if (data.length > 1 && data[0] == -2 && data[1] == -1) {
          encoding = "UTF-16BE";
        } else {
          // No autodetect. See if we have explicit value on command line
          encoding = defaultEncoding;
          if (encoding == null) {
            // No explicit encoding specification
            if (url == null) {
              // Local files default to system encoding
              encoding = System.getProperty("file.encoding");
            } else if (contentType != null && contentType.startsWith("application/")) {
              // application/* types default to UTF-8
              encoding = "UTF-8";
            } else {
              // text/* MIME types default to US-ASCII
              encoding = "US-ASCII";
            }
          }
        }
      }
      String strResult = new String(data, encoding);
      // Skip BOM
      if (strResult.length() > 0 && strResult.charAt(0) == '\uFEFF') {
        strResult = strResult.substring(1);
      }
      result = strResult;
    }
    return result;
  }

  public static InputStream getFileOrUrlInputStream(String path) throws IOException {
    URL url = toUrl(path);
    try {
      if (url == null) {
        File file = new File(path);
        return new FileInputStream(file);
      } else {
        URLConnection uc = url.openConnection();
        return uc.getInputStream();
      }
    }
    catch (Exception e) {throw new RuntimeException(e);}
  }

  public static InputStream getRuntimeFileOrUrlInputStream(String path) {
    try /* open-stream throwing runtime exceptions with closure */ {
      return getFileOrUrlInputStream(path);
    }
    catch (Exception e) {throw new RuntimeException(e);}
  }
}