package errmsg.parenthesis;

import java.util.List;
import java.util.Stack;

public enum ParenthesisCheckerState {
  DEFAULT {
    @Override public ParenthesisCheckerState process(ParenthesisChecker checker, int i) {
      String str = checker.getStringValue(i, 1);
      ParenthesisType pt = ParenthesisType.getBySymbol(str);
      // Parenthesis character
      if(pt != null) {
        ParenthesisCheckerState newState = processParenthesis(checker, pt);
        checker.incrementPos();
        return newState;
      }
      // Not a parenthesis character
      switch (str) {
        case "\n" -> {
          checker.incrementLine();
          return this;
        }
        case "/" -> {
          checker.incrementPos();
          String lookAhead = checker.getStringValue(i, 2);
          if(lookAhead.equals("//")) {return COMM;}
          if(lookAhead.equals("/*")) {return MULTI_COMM;}
          return this;
        }
        case "\"" -> {
          checker.incrementPos();
          String lookahead = checker.getStringValue(i, 3);
          if(lookahead.equals("\"\"\"")) {return MULTI_STRING;}
          return STRING;
        }
        default -> {
          checker.incrementPos();
          return this;
        }
      }
    }

    private ParenthesisCheckerState processParenthesis(ParenthesisChecker checker, ParenthesisType pt) {
      if(!pt.isOpen) {
        // Check if close without open
        if(checker.getStack().isEmpty()) {
          checker.addToStack(pt);
          return ERR_EXTRA;
        }
        // Check if wrong close
        String check = checker.getStack().peek().type().symbol;
        if(!check.equals(pt.pair)) {
          checker.addToStack(pt);
          return ERR_WRONG;
        }
        // Pop because valid close
        checker.getStack().pop();
      } else {
        // Add opening parenthesis to stack
        checker.addToStack(pt);
      }
      return this;
    }

    @Override public String getErrorMessage(ParenthesisChecker checker, String input) {
      Stack<Parenthesis> stack = checker.getStack();
      assert !stack.isEmpty();
      return stack.size() == 1 ? getSingleUnclosed(stack.pop(), input) : getMultiUnclosed(stack, input);
    }

    private String getSingleUnclosed(Parenthesis open, String input) {
      String prefix = open.line() + ": ";
      String message = String.format("Error: unclosed opening parenthesis '%s' at %d:%d\n", open.type().symbol, open.line(), open.pos());
      message += prefix + input.lines().toList().get(open.line()-1) + "\n";
      message += " ".repeat(open.pos() + prefix.length()) + "^ unclosed parenthesis\n";
      return message;
    }

    private String getMultiUnclosed(Stack<Parenthesis> stack, String input) {
      String message = "Error: multiple unclosed opening parenthesis\n";
      // TODO: Point out multiple opening parenthesis if possible?
      // This is a placeholder
      return getSingleUnclosed(stack.pop(), input);
    }
  },
  ERR_EXTRA {
    @Override public String getErrorMessage(ParenthesisChecker checker, String input) {
      Parenthesis close = checker.getStack().pop();
      String prefix = close.line() + ": ";
      String message = String.format("Error: unexpected closing parenthesis '%s' at %d:%d\n", close.type().symbol, close.line(), close.pos());
      message += prefix + input.lines().toList().get(close.line()-1) + "\n";
      message += " ".repeat(close.pos() + prefix.length()) + "^ unexpected close\n";
      return message;
    }
  },
  ERR_WRONG{
    @Override public String getErrorMessage(ParenthesisChecker checker, String input) {
      Stack<Parenthesis> stack = checker.getStack();
      Parenthesis close = stack.pop();
      Parenthesis open = stack.pop();
      return close.line() != open.line() ? getMultiLine(open, close, input) : getSingleLine(open, close, input);
    }

    private String getSingleLine(Parenthesis open, Parenthesis close, String input) {
      String line = input.lines().toList().get(open.line()-1);
      String prefix = open.line() + ": ";
      String whitespace = " ".repeat(open.pos() + prefix.length());
      String suggestion = "is it meant to be '" + open.type().pair + "'?";
      String indicator = whitespace + "^" + "-".repeat(close.pos()-open.pos()-1) + "^ mismatched close, " + suggestion;
//      String message = String.format("Error: mismatched closing parenthesis '%s' at %d:%d\n", close.type().symbol, close.line(), close.pos());
//      message += prefix + line + "\n" + indicator + "\n" + whitespace + "|\n" + whitespace + "unclosed open\n";
      return """
        Error: mismatched closing parenthesis '%s' at %d:%d
        %s%s
        %s
        %s|
        %sunclosed open
        
        """.formatted(close.type().symbol, close.line(), close.pos(), prefix, line, indicator, whitespace, whitespace);
    }

    private String getMultiLine(Parenthesis open, Parenthesis close, String input) {
      List<String> lines = input.lines().toList();
      StringBuilder message = new StringBuilder(
        String.format("Error: mismatched closing parenthesis '%s' at %d:%d\n", close.type().symbol, close.line(), close.pos()));
      boolean flag = false;
      for(int i=open.line(); i<=close.line(); i++) {
        if(i > open.line()+1 && i < close.line()-1) {
          if(!flag) {
            String range = (open.line()+2) + "-" + (close.line()-2) + ": ";
            message.append(range);
            message.append("... ... ...\n");
            flag = true;}
          continue;
        }
        String prefix = i + "  : ";
        message.append(prefix).append(lines.get(i-1)).append("\n");
        if(i == open.line()) {
          message.append(" ".repeat(open.pos() + prefix.length())).append("^ unclosed open\n");
        }
        if(i == close.line()) {
          String suggestion = "is it meant to be '" + open.type().pair + "'?";
          message.append(" ".repeat(close.pos() + prefix.length())).append("^ mismatched close, ").append(suggestion).append("\n");
        }
      }
      return message.toString();
    }
  },
  COMM {
    @Override public ParenthesisCheckerState process(ParenthesisChecker checker, int i) {
      String str = checker.getStringValue(i, 1);
      if(str.equals("\n")) {
        checker.incrementLine();
        return DEFAULT;
      }
      checker.incrementPos();
      return this;
    }
  },
  MULTI_COMM {
    @Override public ParenthesisCheckerState process(ParenthesisChecker checker, int i) {
      String str = checker.getStringValue(i, 1);
      if(str.equals("\n")) {checker.incrementLine();}
      else {checker.incrementPos();}
      if(str.equals("*")) {
        String lookAhead = checker.getStringValue(i-1, 3);
        if(lookAhead.equals("/*/")) {return this;}
        if(lookAhead.endsWith("*/")) {return DEFAULT;}
      }
      return this;
    }
  },
  MULTI_STRING {
    @Override public ParenthesisCheckerState process(ParenthesisChecker checker, int i) {
      String str = checker.getStringValue(i, 1);
      switch(str) {
        case "\\" -> {checker.incrementPos();return ESCAPE_MULTI;}
        case "\"" -> {
          checker.incrementPos();
          String lookAhead = checker.getStringValue(i, 3);
          if(lookAhead.equals("\"\"\"")) {return DEFAULT;}
          return this;
        }
        case "\n" -> {checker.incrementLine(); return this;}
        default -> {checker.incrementPos();return this;}
      }
    }
  },
  STRING {
    @Override public ParenthesisCheckerState process(ParenthesisChecker checker, int i) {
      String str = checker.getStringValue(i, 1);
      switch(str) {
        case "\"" -> {checker.incrementPos();return DEFAULT;}
        case "\\" -> {checker.incrementPos();return ESCAPE;}
        case "\n" -> {checker.incrementLine(); return this;}
        default -> {checker.incrementPos();return this;}
      }
    }
  },
  ESCAPE {
    @Override public ParenthesisCheckerState process(ParenthesisChecker checker, int i) {
      checker.incrementPos();
      return STRING;
    }
  },
  ESCAPE_MULTI {
    @Override public ParenthesisCheckerState process(ParenthesisChecker checker, int i) {
      checker.incrementPos();
      return MULTI_STRING;
    }
  };

  // Enum Methods
  public ParenthesisCheckerState process(ParenthesisChecker checker, int i){checker.incrementPos();return this;}
  public String getErrorMessage(ParenthesisChecker checker, String input) {
    throw new IllegalStateException("No parenthesis error was found");
  }
}
