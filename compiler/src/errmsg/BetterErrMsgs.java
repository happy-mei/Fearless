package errmsg;

import errmsg.parenthesis.ParenthesisChecker;
import org.antlr.v4.runtime.Token;

public class BetterErrMsgs {
  private final String input;
  Object offendingSymbol;
  int line;
  int pos;

  public BetterErrMsgs(String input, Object offendingSymbol, int line, int pos) {
    this.input = input;
    this.offendingSymbol = offendingSymbol;
    this.line = line;
    this.pos = pos;
  }

  public String syntaxError() {
    if (offendingSymbol instanceof Token token) {
      switch(token.getText()) {
        case "(", ")", "[", "]", "{", "}" -> {
          return new ParenthesisChecker(input).compute();
        }
      }
    }
    return "";
  }
}
