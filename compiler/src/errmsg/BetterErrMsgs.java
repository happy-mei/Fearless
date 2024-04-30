package errmsg;

import errmsg.parenthesis.ParenthesisChecker;
import org.antlr.v4.runtime.Token;

public class BetterErrMsgs {
  private final String input;
  Object offendingSymbol;
  private final String msg;

  public BetterErrMsgs(String input, Object offendingSymbol, String msg) {
    this.input = input;
    this.offendingSymbol = offendingSymbol;
    this.msg = msg;
  }

  public String syntaxError() {
    if (offendingSymbol instanceof Token token) {
      switch(token.getText()) {
        case "(", ")", "[", "]", "{", "}" -> {
          return new ParenthesisChecker(input).compute();
        }
      }
    }
    return this.msg;
  }
}
