package program.inference;

import astFull.T;
import id.Id;
import main.Main;
import org.junit.jupiter.api.Test;
import parser.Parser;
import utils.Err;
import wellFormedness.WellFormednessFullShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TestRefineSigGens {
  T addInfers(T t){
    return t.match(
      gx->gx.name().equals("Infer") ? T.infer:t,
      it->new T(t.mdf(),it.withTs(it.ts().stream().map(this::addInfers).toList()))
    );
  }
  void ok(String expected, String program, String... rps){
    ok(expected, program, Set.of(), rps);
  }
  void ok(String expected, String program, Set<String> fresh, String... rps){
    Main.resetAll();
    List<RefineTypes.RP> parsed = Arrays.stream(rps)
      .map(rp->rp.split("=", 2))
      .map(ts->new RefineTypes.RP(
        addInfers(new Parser(Parser.dummy, ts[0]).parseFullT()),
        addInfers(new Parser(Parser.dummy, ts[1]).parseFullT())
        ))
      .toList();
    var p = Parser.parseAll(List.of(new Parser(Path.of("Dummy.fear"), program)));
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    var inferredSigs = p.inferSignaturesToCore();
    var freshParsed = fresh.stream().map(name->new Id.GX<ast.T>(name)).collect(Collectors.toUnmodifiableSet());

    var refined = new RefineTypes(inferredSigs).refineSigGens(parsed, freshParsed);
    Err.strCmpFormat(expected, refined.toString());
  }

  @Test void replaceGens() { ok("""
    [RP[t1=imm a.Foo[mdf X],t2=imm a.Foo[mdf X]]]
    """, """
    package a
    Foo[A]:{}
    """, "a.Foo[mdf Y] = a.Foo[mdf X]"); }

  @Test void replaceGens2() { ok("""
    [RP[t1=mut a.Foo[mdf X],t2=mut a.Foo[mdf X]]]
    """, """
    package a
    Foo[A]:{}
    """, "mut a.Foo[mdf Y] = mut a.Foo[mdf X]"); }

  @Test void replaceGensKeepMdf() { ok("""
    [RP[t1=imm a.Foo[mdfX],t2=imm a.Foo[mdfX]]]
    """, """
    package a
    Foo[A]:{}
    """, "imm a.Foo[mdf Y] = imm a.Foo[mdf X]"); }

  @Test void aGen() { ok("""
    [RP[t1=imma.A[imma.B[],imma.B[]],t2=imma.A[imma.B[],imma.B[]]]]
    """, """
    package a
    A[X,Y]:{}
    B:{}
    """, "a.A[X,a.B[]] = a.A[a.B[],Y]"); }

  @Test void aGen2() { ok("""
    [RP[t1=imma.A[imma.B[],imma.B[]],t2=imma.A[imma.B[],imma.B[]]],RP[t1=immY,t2=imma.B[]]]
    """, """
    package a
    A[X,Y]:{}
    B:{}
    """, "a.A[Y,a.B[]] = a.A[a.B[],Y]", "X = Y"); }
}
