package box.star.bin.sh;

import box.star.bin.sh.promise.FunctionMain;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class Function extends Process implements Runnable, Cloneable, FunctionMain {

  protected Shell shell;
  protected SharedMap<String, String> local;
  protected BufferedInputStream stdin;
  protected BufferedOutputStream stdout, stderr;
  private Thread thread;
  private String[] parameters;
  private int status;
  private Pipe p_stdin, p_stdout, p_stderr;
  private boolean destroying, running;

  @Override
  protected Function clone() {
    hostAcessOnly("clone");
    try {
      return (Function) super.clone();
    }
    catch (Exception ignored) {}
    return null;
  }

  final Function createInstance(Shell shell, SharedMap<String, String> superLocal) {
    hostAcessOnly("createInstance(shell, superLocal)");
    Function instance = clone();
    instance.shell = shell;
    if (superLocal == null) instance.local = new SharedMap<>();
    else instance.local = superLocal;
    return instance;
  }

  final Function exec(String... parameters) {
    hostAcessOnly("exec(String... parameters)");
    this.parameters = parameters;
    p_stdin = new Pipe();
    p_stdout = new Pipe();
    p_stderr = new Pipe();
    stdin = new BufferedInputStream(p_stdin.input);
    stdout = new BufferedOutputStream(p_stdout.output);
    stderr = new BufferedOutputStream(p_stderr.output);
    thread = new Thread(this);
    thread.start();
    return this;
  }

  @Override
  public int main(String[] parameters) {return 0;}

  @Override
  final public void run() {
    if (running) hostAcessOnly("run");
    running = true;
    try {
      status = main(parameters);
    }
    finally {
      running = false;
      try {
        stdin.close();
        stdout.close();
        stderr.close();
      }
      catch (IOException ignored) {}
    }
  }

  @Override
  final public int exitValue() {
    hostAcessOnly("exitValue");
    try { thread.join(); }
    catch (InterruptedException ie) {}
    catch (Exception e) {if (!destroying) throw new RuntimeException(e);}
    return status;
  }

  @Override
  final public boolean isAlive() {
    hostAcessOnly("isAlive() == true");
    return thread.isAlive();
  }

  private void hostAcessOnly(String context){
    if (Thread.currentThread().equals(thread)){
      throw new RuntimeException(context, new IllegalThreadStateException("this method cannot be accessed from within its own thread."));
    }
  }

  @Override
  final public OutputStream getOutputStream() {
    hostAcessOnly("getOutputStream");
    return new BufferedOutputStream(p_stdin.output);
  }

  @Override
  final public InputStream getInputStream() {
    hostAcessOnly("getInputStream");
    return new BufferedInputStream(p_stdout.input);
  }
  @Override
  final public InputStream getErrorStream() {
    hostAcessOnly("getErrorStream");
    return new BufferedInputStream(p_stderr.input);
  }

  @Override
  final public int waitFor() throws InterruptedException {
    hostAcessOnly("waitFor");
    thread.join();
    return status;
  }

  @Override
  final public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
    hostAcessOnly("waitFor(timeout, unit)");
    if (!thread.isAlive()) return true;
    if (timeout <= 0) return false;

    long remainingNanos = unit.toNanos(timeout);
    long deadline = System.nanoTime() + remainingNanos;

    do {
      // Round up to next millisecond
      long msTimeout = TimeUnit.NANOSECONDS.toMillis(remainingNanos + 999_999L);
      thread.wait(msTimeout);
      if (Thread.interrupted())
        throw new InterruptedException();
      if (!thread.isAlive()) {
        return true;
      }
      remainingNanos = deadline - System.nanoTime();
    } while (remainingNanos > 0);
    return (!thread.isAlive());
  }

  @Override
  final public void destroy() {
    hostAcessOnly("destroy");
    try {
      destroying = true;
      stdin.close();
      stdout.close();
      stderr.close();
      if (thread.isAlive()) {
        thread.interrupt();
      }
    }
    catch (IOException e) { e.printStackTrace(); }
  }

  @Override
  final public Process destroyForcibly() {
    hostAcessOnly("destroyForcibly");
    destroy();
    return this;
  }

  private class Pipe {

    PipedOutputStream output = new PipedOutputStream();
    PipedInputStream input = new PipedInputStream();

    Pipe() {
      try {
        output.connect(input);
      }
      catch (IOException e) { throw new RuntimeException(e); }
    }

    void close() {
      hostAcessOnly("close");
      try {
        input.close();
        output.close();
      }
      catch (Exception e) {}
    }
  }

}