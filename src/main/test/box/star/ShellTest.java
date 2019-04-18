package box.star;

import org.junit.jupiter.api.Test;

import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;

class ShellTest {

    Shell.IExecutiveFactory shellFactory = new Shell.IExecutiveFactory() {

        @Override
        public Shell.IExecutive getMainController() {
            return new Shell.IExecutive() {
                @Override
                public void main(String[] parameters) {
                    try {
                        PrintWriter error = sh.getPrintWriter(2);
                        error.println("hello world");
                        error.flush();
                        Thread.sleep(Integer.valueOf(parameters[0]));
                    } catch (Exception e) {e.printStackTrace();}
                }

                @Override
                public int exitStatus() {
                    return 12;
                }
            };
        }

        @Override
        public Shell.IExecutive getSubController(Shell.IExecutive mainShell) {
            return getMainController();
        }

    };

    Shell sh = new Shell(shellFactory);

    @Test void run(){
        sh.exec("650");
        assertEquals(12, sh.getExitStatus());
    }

}