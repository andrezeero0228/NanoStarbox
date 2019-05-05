package box.star.text;

/**
 * This interface hosts the text scanner method core operating functions.
 */
public interface TextScannerMethod {

  /**
   * Called by the TextScanner to signal that a new method call is beginning.
   *
   * @param parameters the parameters given by the caller.
   */
  void startMethod(Object... parameters);

  /**
   * Signals whether or not the process should continue reading input.
   *
   * @param input The string buffer.
   * @return true if the TextScanner should read more input.
   */
  boolean continueScanning(StringBuilder input);

  /**
   * Extended Operations Option
   *
   * This method is called when it is again safe to call seek/scan/next on the
   * TextScanner.
   *
   * You can use this feature to create a virtual-pipe-chain.
   *
   * You can also (ideally) pre-process output, if having an exact copy of input
   * data is not relevant for your purpose.
   *
   * @param scanner the TextScanner.
   * @param scanned the input buffer.
   * @return the scanned data as a string.
   */
  String computeMethodCall(TextScanner scanner, StringBuilder scanned);

  /**
   * Return true to break processing on this character.
   *
   * @param character
   * @return false to continue processing.
   */
  boolean exitMethod(char character);

}
