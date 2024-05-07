package errmsg.parenthesis;

import java.util.Stack;

public class ParenthesisChecker {
  private Stack<Parenthesis> stack = new Stack<>();
  private final String input;
  private int line = 1;
  private int pos = 0;

  public ParenthesisChecker(String input) {this.input = input;}

  public String compute() {
    int length = input.codePointCount(0, input.length());
    ParenthesisCheckerState state = ParenthesisCheckerState.DEFAULT;
    for(int i=0; i<length; i++) {
      int chr = input.codePointAt(input.offsetByCodePoints(0, i));
      state = state.process(this, Character.toString(chr));
    }
    return state.getErrorMessage(this, input);
  }

  public void incrementLine() {
    this.line++;
    this.pos = 0;
  }

  public void incrementPos() {
    this.pos++;
  }

  public void addToStack(ParenthesisType parenthesisType) {
    this.stack.push(new Parenthesis(parenthesisType, this.line, this.pos));
  }

  public Stack<Parenthesis> getStack() {
    return this.stack;
  }
}
