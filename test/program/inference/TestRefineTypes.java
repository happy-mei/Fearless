package program.inference;

import astFull.T;
import main.Main;
import org.junit.jupiter.api.Disabled;
import parser.Parser;
import utils.Bug;
import utils.Err;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class TestRefineTypes {
  astFull.T addInfers(astFull.T t){
    return t.match(
      gx->gx.name().equals("Infer") ? T.infer:t,
      it->new astFull.T(t.mdf(),it.withTs(it.ts().stream().map(this::addInfers).toList()))
    );
  }
  void ok(String expected, String expr, String t1, String t2, String program){
    Main.resetAll();
    var e = new Parser(Parser.dummy,expr).parseFullE(Bug::err, s->Optional.empty());
    var pT1 = addInfers(new Parser(Parser.dummy, t1).parseFullT());
    var pT2 = addInfers(new Parser(Parser.dummy, t2).parseFullT());
    var p = Parser.parseAll(List.of(new Parser(Path.of("Dummy.fear"), program)));
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    var inferredSigs = p.inferSignatures();

    var e1 = new RefineTypes(inferredSigs).fixType(e, pT1);
    var e2 = new RefineTypes(inferredSigs).fixType(e1, pT2);
    Err.strCmpFormat(expected, e2.toString());
  }

  @Disabled
  @Test
  void something() {ok("""
    """, "", "", "", """
    package a
    """);}

  @Test
  void ab() {ok("""
    varName:imm a.B[]
    """, "varName", "a.A[]", "a.B[]", """
    package a
    A:{}
    B:A{}
    """);}

  @Test
  void firstInfer() {ok("""
    varName:imm a.B[]
    """, "varName", "Infer", "a.B[]", """
    package a
    A:{}
    B:A{}
    """);}

  @Test
  void secondInfer() {ok("""
    varName:imm a.A[]
    """, "varName", "a.A[]", "Infer", """
    package a
    A:{}
    B:A{}
    """);}

  @Test
  void inferInfer() {ok("""
    varName:infer
    """, "varName", "Infer", "Infer", """
    package a
    A:{}
    B:A{}
    """);}
  @Test
  void same() {ok("""
    varName:imm a.A[]
    """, "varName", "a.A[]", "a.A[]", """
    package a
    A:{}
    """);}
  @Test
  void aGen() {ok("""
    varName:imm a.A[imm a.B[],imm a.B[]]
    """, "varName", "a.A[X,a.B[]]", "a.A[a.B[],Y]", """
    package a
    A[X,Y]:{}
    B:{}
    """);}

  @Test//varName:imm a.A[mut a.B[],read a.B[]]
  void aGenMdf1() {ok("""
    varName:imma.A[lent a.C[],read a.B[]]
    """, "varName", "a.A[mut X,imm a.B[]]", "a.A[lent a.C[],read Y]", """
    package a
    A[X,Y]:{}
    B:{}
    C:{}
    """);}
  //Yes, it should be a.A[lent a.C[],_] instead of a.A[mut a.C[],_]:
  //We get to lent a.C[] = mut a.C[] and we take the 'best type: as specified by t2 (the best type)
  @Test
  void aGenMdf2() {ok("""
    varName:imm a.A[mdf a.B[],read a.B[]]
    """, "varName", "a.A[mut X,mdf a.B[]]", "a.A[mdf a.B[],read Y]", """
    package a
    A[X,Y]:{}
    B:{}
    """);}
  @Test
  void aGenDeep() {ok("""
    varName:imm a.A[imma.B[],imma.A[imma.B[],imma.B[]]]
    varName:imma.A[imma.B[],imma.A[immX,immX]]
    """, "varName", "a.A[X,a.A[X,X]]", "a.A[a.B[],Y]", """
    package a
    A[X,Y]:{}
    B:{}
    """);}
  // TODO: Test gen A and gen B, which one is kept
  @Test
  void aGenInfinite() {ok("""
    varName:imma.A[imma.B[],imma.A[imm X,imm X]]
    """, "varName", "a.A[X,a.A[X,X]]", "a.A[a.B[],X]", """
    package a
    A[X,Y]:{}
    B:{}
    """);}

  @Test
  void lhsGxRhsIT() {ok("""
    varName:imm a.A[]
    """, "varName", "X", "a.A[]", """
    package a
    A:{}
    """);}
  @Test
  void lhsITRhsGx() {ok("""
    varName:imm a.A[]
    """, "varName", "a.A[]", "X", """
    package a
    A:{}
    """);}

  @Test
  void refineGens() {ok("""
    varName:imm a.A[imm a.B[]]
    """, "varName", "a.A[Infer]", "a.A[a.B[]]", """
    package a
    A[X]:{}
    B:{}
    """);}

  @Test
  void refineGensNested1() {ok("""
    varName:imm a.A[imm a.A[imm a.B[]]]
    """, "varName", "a.A[Infer]", "a.A[a.A[a.B[]]]", """
    package a
    A[X]:{}
    B:{}
    """);}
  @Test
  void refineGensNested2() {ok("""
    varName:imm a.A[imm a.A[imm a.B[]]]
    """, "varName", "a.A[imm a.A[Infer]]", "a.A[a.A[a.B[]]]", """
    package a
    A[X]:{}
    B:{}
    """);}
}
