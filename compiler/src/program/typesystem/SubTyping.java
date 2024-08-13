package program.typesystem;

import program.Program;

import java.util.HashMap;

public interface SubTyping {
  HashMap<Program.SubTypeQuery, Program.SubTypeResult> subTypeCache();
}
