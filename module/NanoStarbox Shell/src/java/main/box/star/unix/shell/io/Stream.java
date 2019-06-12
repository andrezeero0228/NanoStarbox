package box.star.unix.shell.io;

public interface Stream {
  boolean isWritable();
  boolean isReadable();
  boolean isTerminal();
  boolean isPipe();
  boolean isFile();
  boolean isBuffered();
  boolean isRandom();
  boolean hasURI();
  String getURI();
  <STREAM> STREAM getController();
}