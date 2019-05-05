package box.star.text;

import org.junit.jupiter.api.Test;

import java.io.File;

import static box.star.text.TextScanner.ASCII.META_DOCUMENT_TAG_END;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TextScannerTest {

  TextScanner x = new TextScanner(new File("src/java/resource/mixed-content-page.html"));

  @Test
  void start(){
    String result;
    result = x.scan(new TextScanner.Method("doctype"){
      char[] controlTerms = new char[]{META_DOCUMENT_TAG_END};
      {
        this.bufferLimit = 0;
      }
      @Override
      public boolean matchBoundary(TextScannerMethodContext context, char character) {
        if (context.haveEscapeWarrant()) return false;
        return TextScanner.charMapContains(character, controlTerms);
      }
    });
    System.err.println(result + x.scanExact(META_DOCUMENT_TAG_END));
  }


}