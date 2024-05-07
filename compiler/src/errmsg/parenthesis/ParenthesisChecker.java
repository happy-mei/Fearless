package errmsg.parenthesis;

import java.util.Iterator;
import java.util.Stack;
import java.util.stream.IntStream;

public class ParenthesisChecker {
  private Stack<Parenthesis> stack = new Stack<>();
  private final String input;
  private int line = 1;
  private int pos = 0;

  public ParenthesisChecker(String input) {this.input = input;}

  public String compute() {
    IntStream codePointsStream = input.codePoints();
    ParenthesisCheckerState state = ParenthesisCheckerState.DEFAULT;
    for(int codePoint: (Iterable<Integer>)codePointsStream::iterator) {
      state = state.process(this, Character.toString(codePoint));
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
