package errmsg.parenthesis;

public enum ParenthesisCheckerState {
  DEFAULT {
    @Override public ParenthesisCheckerState process(ParenthesisChecker checker, String str) {
      ParenthesisType pt = ParenthesisType.getBySymbol(str);
      // Parenthesis character
      if(pt != null) {
        return processParenthesis(checker, pt);
      }
      // Not a parenthesis character
      switch (str) {
        case "\n" -> {
          checker.incrementLine();
          return this;
        }
        case "/" -> {
          return SLASH;
        }
        case "\"" -> {
          return Q1;
        }
        default -> {return this;}
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

    @Override public String getErrorMessage() {
      return "MISSING CLOSE";
    }
  },
  ERR_EXTRA {
    @Override public String getErrorMessage() {
      return "EXTRA CLOSE";
    }
  },
  ERR_WRONG{
    @Override public String getErrorMessage() {
      return "WRONG CLOSE";
    }
  },

  // STATES FOR PROCESSING COMMENTS AND STRINGS
  SLASH {
    @Override public ParenthesisCheckerState process(ParenthesisChecker checker, String str) {
      if(str.equals("\n")) {
        checker.incrementLine();
        return DEFAULT;
      }
      return switch(str) {
        case "/" -> COMM;
        case "*" -> MULTI_COMM;
        default -> DEFAULT;
      };
    }
  },
  COMM {
    @Override public ParenthesisCheckerState process(ParenthesisChecker checker, String str) {
      if(str.equals("\n")) {
        checker.incrementLine();
        return DEFAULT;
      }
      return this;
    }
  },
  MULTI_COMM {
    @Override public ParenthesisCheckerState process(ParenthesisChecker checker, String str) {
      if(str.equals("*")) {return STAR;}
      if(str.equals("\n")) {checker.incrementLine();}
      return this;
    }
  },
  STAR {
    @Override public ParenthesisCheckerState process(ParenthesisChecker checker, String str) {
      if(str.equals("/")) {return DEFAULT;}
      if(str.equals("\n")) {checker.incrementLine();}
      return MULTI_COMM;
    }
  },
  Q1 {
    @Override public ParenthesisCheckerState process(ParenthesisChecker checker, String str) {
      if(str.equals("\"")) {return Q2;}
      if(str.equals("\n")) {checker.incrementLine();}
      return STRING;
    }
  },
  Q2 {
    @Override public ParenthesisCheckerState process(ParenthesisChecker checker, String str) {
      if(str.equals("\"")) {return MULTI_STRING;}
      if(str.equals("\n")) {checker.incrementLine();}
      return DEFAULT;
    }
  },
  MULTI_STRING {
    @Override public ParenthesisCheckerState process(ParenthesisChecker checker, String str) {
      switch(str) {
        case "\\" -> {return ESCAPE_MULTI;}
        case "\"" -> {return E1;}
        case "\n" -> {checker.incrementLine(); return this;}
        default -> {return this;}
      }
    }
  },
  STRING {
    @Override public ParenthesisCheckerState process(ParenthesisChecker checker, String str) {
      switch(str) {
        case "\"" -> {return DEFAULT;}
        case "\\" -> {return ESCAPE;}
        case "\n" -> {checker.incrementLine(); return this;}
        default -> {return this;}
      }
    }
  },
  E1 {
    @Override public ParenthesisCheckerState process(ParenthesisChecker checker, String str) {
      switch(str) {
        case "\"" -> {return E2;}
        case "\\" -> {return ESCAPE_MULTI;}
        case "\n" -> {checker.incrementLine(); return MULTI_STRING;}
        default -> {return MULTI_STRING;}
      }
    }
  },
  E2 {
    @Override public ParenthesisCheckerState process(ParenthesisChecker checker, String str) {
      switch(str) {
        case "\"" -> {return DEFAULT;}
        case "\\" -> {return ESCAPE_MULTI;}
        case "\n" -> {checker.incrementLine(); return MULTI_STRING;}
        default -> {return MULTI_STRING;}
      }
    }
  },
  ESCAPE {
    @Override public ParenthesisCheckerState process(ParenthesisChecker checker, String str) {
      return STRING;
    }
  },
  ESCAPE_MULTI {
    @Override public ParenthesisCheckerState process(ParenthesisChecker checker, String str) {
      return MULTI_STRING;
    }
  };

  // Enum Methods
  public ParenthesisCheckerState process(ParenthesisChecker checker, String str){return this;}
  public String getErrorMessage() {
    throw new IllegalStateException("No parenthesis error was found");
  }
}
