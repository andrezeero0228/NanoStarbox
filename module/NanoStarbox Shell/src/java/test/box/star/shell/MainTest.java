package box.star.shell;

import box.star.shell.runtime.parts.TextCommand;
import box.star.shell.runtime.parts.TextEnvironment;
import box.star.shell.runtime.parts.TextRedirection;
import box.star.text.basic.Scanner;
import org.junit.jupiter.api.Test;

class MainTest {

  @Test void main(){
    Main shell = new Main("hi");
    System.out.println(shell.getShellBaseDirectory());
    Scanner scanner = new Scanner("test", "shit hit the fan 1 > 'colloqial ism'");
    //TextRedirection r = TextRedirection.parseRedirect(scanner);
    //scanner = new Scanner("test2", "A21=44 b72=33 cout shit | grep fu");
    TextCommand e = TextCommand.parseCommandLine(scanner);
    System.out.println(String.join(", ", TextRedirection.redirectionOperators));
  }


}