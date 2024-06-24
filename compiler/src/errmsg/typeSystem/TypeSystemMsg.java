package errmsg.typeSystem;

import failure.CompileError;
import id.Id;
import id.Mdf;
import program.CM;
import program.Program;
import program.typesystem.XBs;
import utils.Bug;

import java.util.*;

public class TypeSystemMsg {

  public static String undefinedMeth(CompileError rawError, Program p) {
    ast.T recvT = switch (rawError.attributes.get("recvT")) {
      case astFull.T t -> t.toAstT();
      case ast.T t -> t;
      default -> throw Bug.unreachable();
    };
    var name = (Id.MethName) rawError.attributes.get("name");
    var ms = p.meths(XBs.empty(), Mdf.recMdf, recvT.itOrThrow(), 0);

    List<CM> sortedMs = sortSimilarity(name, ms);

    StringBuilder msg = new StringBuilder(STR."""
    Method \{name.name()} does not exist in \{recvT}
    Did you mean \{sortedMs.getFirst().name().name()}?

    Other candidates:
    """);
    for(var meth : sortedMs) {
      msg.append(STR."""
                     \{meth.c()}\{meth.name().name()}
                     """);
    }
    assert rawError.pos().isPresent();
    String header = STR."""
    In position \{rawError.pos().get().toString()}
    [E\{rawError.code()} \{rawError.name()}]
    """;
    return header + msg;
  }

  private static List<CM> sortSimilarity(Id.MethName name, List<CM> ms) {
    HashMap<CM, Integer> similarityRankings = new HashMap<>();
    ArrayList<CM> newList = new ArrayList<>(ms);
    for(CM meth : ms) {
      int similarity = levenshteinDist(name.name(), meth.name().name());
      similarityRankings.put(meth, similarity);
    }
    newList.sort(Comparator.comparingInt(similarityRankings::get));
    return newList.reversed();
  }

  private static int levenshteinDist(String str1, String str2) {
    int[][] dp = new int[str1.length() + 1][str2.length() + 1];
    for(int i=0; i<=str1.length(); i++) {
      for(int j=0; j<str2.length(); j++) {
        if(i == 0) {
          dp[i][j] = j;
        } else if (j == 0) {
          dp[i][j] = i;
        } else {
          dp[i][j] = Math.min(dp[i-1][j-1] + subCost(str1.charAt(i-1), str2.charAt(j-1)),
            Math.min(dp[i-1][j] + 1, dp[i][j-1] + 1));
        }
      }
    }
    return dp[str1.length()][str2.length()];
  }

  private static int subCost(char a, char b) {
    return a == b ? 0 : 1;
  }
}
