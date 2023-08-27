package program.typesystem;

import failure.CompileError;
import id.Mdf;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static id.Mdf.*;
import static java.util.List.of;
import static program.typesystem.RunTypeSystem.expectFail;
import static program.typesystem.RunTypeSystem.ok;

public class TestCaptureRules {
  void c1(Mdf lambda, Mdf captured, Mdf method, List<Mdf> capturedAs) {
    capturedAs.forEach(mdf->cInnerOk(codeGen1.formatted(method, mdf, captured, lambda, lambda)));
    Stream.of(Mdf.values()).filter(mdf->!capturedAs.contains(mdf))
      .forEach(mdf->cInnerFail(codeGen1.formatted(method, mdf, captured, lambda, lambda)));
  }
  String codeGen1 = """
    package test
    B:{}
    L:{ %s .absMeth: %s B }
    A:{ recMdf .m(par: %s B) : %s L -> %s L{.absMeth->par} }
    """;

  void c2(Mdf lambda,Mdf captured,Mdf method, List<Mdf> capturedAs) {
    capturedAs.forEach(mdf->{
      cInnerOk(codeGen2a.formatted(method, mdf, captured, lambda, captured, lambda, captured));
      if (!mdf.is(mdf, recMdf)) {
        cInnerOk(codeGen2b.formatted(method, mdf, captured, captured, lambda, lambda));
      }
    });
    Stream.of(Mdf.values()).filter(mdf->!capturedAs.contains(mdf))
      .forEach(mdf->{
        cInnerFail(codeGen2a.formatted(method, mdf, captured, lambda, captured, lambda, captured));
        if (!mdf.is(mdf, recMdf)) {
          cInnerFail(codeGen2b.formatted(method, mdf, captured, captured, lambda, lambda));
        }
      });
  }
  String codeGen2a = """
    package test
    B:{}
    L[X]:{ %s .absMeth: %s X }
    A:{ read .m(par: %s B) : %s L[%s B] -> %s L[%s B]{.absMeth->par} }
    """;
  String codeGen2b = """
    package test
    B:{}
    L[X]:{ %s .absMeth: %s X }
    L:L[%s B]
    A:{ read .m(par: %s B) : %s L -> %s L{.absMeth->par} }
    """;

  @SafeVarargs
  final void c3(Mdf lambda, Mdf captured, Mdf method, List<Mdf>... _capturePairs) {
    c3(lambda, captured, method, false, _capturePairs);
  }
  @SafeVarargs
  final void c4(Mdf lambda, Mdf captured, Mdf method, List<Mdf>... _capturePairs) {
    c3(lambda, captured, method, true, _capturePairs);
  }
  @SafeVarargs
  final void c3(Mdf lambda, Mdf captured, Mdf method, boolean noMutHyg, List<Mdf>... _capturePairs) {
    assert _capturePairs.length>0;
    assert Stream.of(_capturePairs).allMatch(l->l.size()%2==0);
    List<Mdf> capturePairs = Stream.of(_capturePairs).flatMap(l->l.stream()).toList();
    assert capturePairs.size() % 2 == 0;
    record Capture(Mdf capAs, Mdf capAsG){
      @Override public String toString() {
        return capAs+","+capAsG+"  ";
      }
    }
    Set<Capture> caps = new HashSet<>(capturePairs.size() / 2);
    for (int i = 0; i < capturePairs.size(); i += 2) {
      caps.add(new Capture(capturePairs.get(i), capturePairs.get(i+1)));
    }

    var permutations = new ArrayList<Capture>(Mdf.values().length * Mdf.values().length);
    for (Mdf mdf : Mdf.values()) {
      for (Mdf mdfi : Mdf.values()) {
        permutations.add(new Capture(mdf, mdfi));
      }
    }

    var validCaps = new ArrayList<Capture>();
    var exceptions = new ArrayList<Throwable>();
    var templateA = noMutHyg ? codeGen3NoMutHygA : codeGen3a;
    var templateB = noMutHyg ? codeGen3NoMutHygB : codeGen3b;
    permutations.forEach(c->{
      var codeA = templateA.formatted(method, c.capAs, captured, lambda, c.capAsG, lambda, c.capAsG);
//      System.out.println(codeA);
      var codeB = templateB.formatted(method, c.capAs, c.capAsG, captured, lambda, lambda);
      var codeBValid = !c.capAs.is(mdf, recMdf) && !c.capAsG.is(mdf, recMdf) && !captured.is(mdf, recMdf);
//      if (codeBValid) { System.out.println(codeB); }
      var ok = caps.contains(c);
      if (ok) {
        try {
          cInnerOk(codeA);
          if (codeBValid) { cInnerOk(codeB); }
          validCaps.add(c);
        } catch (AssertionError | CompileError e) { exceptions.add(e); }
      }
      else {
        try {
          cInnerFail(codeA);
          if (codeBValid) { cInnerFail(codeB); }
        } catch (AssertionError e) { validCaps.add(c); exceptions.add(e); }
      }
    });
    if(!exceptions.isEmpty()){
      throw new AssertionError("valid pairs:\n"+validCaps+"\n\nbut we got the following errors:"+exceptions);
    }
  }
  String codeGen3a = """
    package test
    B:{}
    L[X]:{ %s .absMeth: %s X }
    A:{ read .m[T](par: %s T) : %s L[%s T] -> %s L[%s T]{.absMeth->par} }
    """;
  String codeGen3b = """
    package test
    B:{}
    L[X]:{ %s .absMeth: %s X }
    L:L[%s B]
    A:{ read .m(par: %s B) : %s L -> %s L{.absMeth->par} }
    """;
  String codeGen3NoMutHygA = """
    package test
    alias base.NoMutHyg as NoMutHyg,
    B:{}
    L[X]:NoMutHyg[mdf X]{ %s .absMeth: %s X }
    A:{ read .m[T](par: %s T) : %s L[%s T] -> %s L[%s T]{.absMeth->par} }
    """;
  //codeGen3NoMutHygB is not run when %s is mdf/recMdf
  String codeGen3NoMutHygB = """
    package test
    alias base.NoMutHyg as NoMutHyg,
    B:{}
    L[X]:NoMutHyg[mdf X]{ %s .absMeth: %s X }
    L:L[%s B]
    A:{ read .m(par: %s B) : %s L -> %s L{.absMeth->par} }
    """;
  String base = """
    package base
    NoMutHyg[X]:{}
    """;

  void cInnerOk(String code){
//    System.out.println(code);
    try{ok(code, base);}
    catch(AssertionError t){ throw new AssertionError("failed on "+code+"\nwith:\n"+t); }
  }
  void cInnerFail(String code){
    try{expectFail(code, base);}
    catch(AssertionError t){ throw new AssertionError("expected failure but succeeded on "+code); }
  }

  //                     lambda, captured, method, ...capturedAs
  @Test void t001(){ c1(imm, imm, imm, of(imm,read)); }
  @Test void t002(){ c1(read, imm, imm, of(imm,read)); }
  @Test void t003(){ c1(lent, imm, imm, of(imm,read)); }
  @Test void t004(){ c1(mut, imm, imm, of(imm,read)); }
  @Test void t005(){ c1(iso, imm, imm, of(imm,read)); }
  @Test void t006(){ c1(mdf, imm, imm, of(/*not well formed lambda*/)); }
  @Test void t007(){ c1(recMdf, imm, imm, of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t011(){ c1(imm, read, imm, of(/*impossible*/)); }
  @Test void t012(){ c1(read, read, imm, of(imm,read)); }
  @Test void t013(){ c1(lent, read, imm, of(imm,read)); }
  @Test void t014(){ c1(mut, read, imm, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t015(){ c1(iso, read, imm, of(/*impossible*/)); }
  @Test void t016(){ c1(mdf, read, imm, of(/*not well formed lambda*/)); }
  @Test void t017(){ c1(recMdf, read, imm, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t021(){ c1(imm, lent, imm, of(/*impossible*/)); }
  @Test void t022(){ c1(read, lent, imm, of(imm,read)); }
  @Test void t023(){ c1(lent, lent, imm, of(imm,read)); }
  @Test void t024(){ c1(mut, lent, imm, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t025(){ c1(iso, lent, imm, of(/*impossible*/)); }
  @Test void t026(){ c1(mdf, lent, imm, of(/*not well formed lambda*/)); }
  @Test void t027(){ c1(recMdf, lent, imm, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t031(){ c1(imm, mut, imm, of(/*impossible*/)); }
  @Test void t032(){ c1(read, mut, imm, of(imm,read)); }
  @Test void t033(){ c1(lent, mut, imm, of(imm,read)); }
  @Test void t034(){ c1(mut, mut, imm, of(imm,read)); }
  @Test void t035(){ c1(iso, mut, imm, of(imm,read)); }
  @Test void t036(){ c1(mdf, mut, imm, of(/*not well formed lambda*/)); }
  @Test void t037(){ c1(recMdf, mut, imm, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t041(){ c1(imm, iso, imm, of(imm,read)); }
  @Test void t042(){ c1(read, iso, imm, of(imm,read)); }
  @Test void t043(){ c1(lent, iso, imm, of(imm,read)); }
  @Test void t044(){ c1(mut, iso, imm, of(imm,read)); }
  @Test void t045(){ c1(iso, iso, imm, of(imm,read)); }
  @Test void t046(){ c1(mdf, iso, imm, of(/*not well formed lambda*/)); }
  @Test void t047(){ c1(recMdf, iso, imm, of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t051(){ c1(imm, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t052(){ c1(read, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t053(){ c1(lent, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t054(){ c1(mut, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t055(){ c1(iso, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t056(){ c1(mdf, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t057(){ c1(recMdf, mdf, imm, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Test void t061(){ c1(imm, recMdf, imm, of(/*impossible*/)); }
  @Test void t062(){ c1(read, recMdf, imm, of(imm,read)); }
  @Test void t063(){ c1(lent, recMdf, imm, of(imm,read)); }
  @Test void t064(){ c1(mut, recMdf, imm, of(/*impossible*/)); }
  @Test void t065(){ c1(iso, recMdf, imm, of(/*impossible*/)); }
  @Test void t066(){ c1(mdf, recMdf, imm, of(/*not well formed lambda*/)); }
  @Test void t067(){ c1(recMdf, recMdf, imm, of(imm,read)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t181(){ c1(imm, imm, read, of(imm,read)); }
  @Test void t182(){ c1(read, imm, read, of(imm,read)); }
  @Test void t183(){ c1(lent, imm, read, of(imm,read)); }
  @Test void t184(){ c1(mut, imm, read, of(imm,read)); }
  @Test void t185(){ c1(iso, imm, read, of(imm,read)); }
  @Test void t186(){ c1(mdf, imm, read, of(/*not well formed lambda*/)); }
  @Test void t187(){ c1(recMdf, imm, read, of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t101(){ c1(imm, read, read, of(/*impossible*/)); }
  @Test void t102(){ c1(read, read, read, of(read)); }
  @Test void t103(){ c1(lent, read, read, of(read)); }
  @Test void t104(){ c1(mut, read, read, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t105(){ c1(iso, read, read, of(/*impossible*/)); }
  @Test void t106(){ c1(mdf, read, read, of(/*not well formed lambda*/)); }
  @Test void t107(){ c1(recMdf, read, read, of(/*impossible*/)); }
  //                    lambda, captured, method, ...capturedAs
  @Test void t111(){ c1(imm, lent, read, of(/*impossible*/)); }
  @Test void t112(){ c1(read, lent, read, of(read)); }
  @Test void t113(){ c1(lent, lent, read, of(read)); }//the lambda is created read, and can not become anything else but imm.
  @Test void t114(){ c1(mut, lent, read, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t115(){ c1(iso, lent, read, of(/*impossible*/)); }
  @Test void t116(){ c1(mdf, lent, read, of(/*not well formed lambda*/)); }
  @Test void t117(){ c1(recMdf, lent, read, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t121(){ c1(imm, mut, read, of(/*impossible*/)); }
  @Test void t122(){ c1(read, mut, read, of(read)); }
  @Test void t123(){ c1(lent, mut, read, of(read)); }
  @Test void t124(){ c1(mut, mut, read, of(read)); }
  @Test void t125(){ c1(iso, mut, read, of(read)); }
  @Test void t126(){ c1(mdf, mut, read, of(/*not well formed lambda*/)); }
  @Test void t127(){ c1(recMdf, mut, read, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t131(){ c1(imm, iso, read, of(imm,read)); }
  @Test void t132(){ c1(read, iso, read, of(imm,read)); }
  @Test void t133(){ c1(lent, iso, read, of(imm,read)); }
  @Test void t134(){ c1(mut, iso, read, of(imm,read)); }
  @Test void t135(){ c1(iso, iso, read, of(imm,read)); }
  @Test void t136(){ c1(mdf, iso, read, of(/*not well formed lambda*/)); }
  @Test void t137(){ c1(recMdf, iso, read, of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t141(){ c1(imm, mdf, read, of(/*not well formed parameter with mdf*/)); }
  @Test void t142(){ c1(read, mdf, read, of(/*not well formed parameter with mdf*/)); }
  @Test void t143(){ c1(lent, mdf, read, of(/*not well formed parameter with mdf*/)); }
  @Test void t144(){ c1(mut, mdf, read, of(/*not well formed parameter with mdf*/)); }
  @Test void t145(){ c1(iso, mdf, read, of(/*not well formed parameter with mdf*/)); }
  @Test void t146(){ c1(mdf, mdf, read, of(/*not well formed parameter with mdf*/)); }
  @Test void t147(){ c1(recMdf, mdf, read, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Test void t151(){ c1(imm, recMdf, read, of(/*impossible*/)); }
  @Test void t152(){ c1(read, recMdf, read, of(read)); }
  @Test void t153(){ c1(lent, recMdf, read, of(read)); }
  @Test void t154(){ c1(mut, recMdf, read, of(/*impossible*/)); }
  @Test void t155(){ c1(iso, recMdf, read, of(/*impossible*/)); }
  @Test void t156(){ c1(mdf, recMdf, read, of(/*not well formed lambda*/)); }
  @Test void t157(){ c1(recMdf, recMdf, read, of(read)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t161(){ c1(imm, imm, read, of(read,imm)); }
  @Test void t162(){ c1(read, imm, read, of(read,imm)); }
  @Test void t163(){ c1(lent, imm, read, of(read,imm)); }
  @Test void t164(){ c1(mut, imm, read, of(read,imm)); }
  @Test void t165(){ c1(iso, imm, read, of(read,imm)); }
  @Test void t166(){ c1(mdf, imm, read, of(/*not well formed lambda*/)); }
  @Test void t167(){ c1(recMdf, imm, read, of(read,imm)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t201(){ c1(imm, imm, lent, of(/*impossible*/)); }
  @Test void t202(){ c1(read, imm, lent, of(/*impossible*/)); }
  @Test void t203(){ c1(lent, imm, lent, of(imm,read)); }
  @Test void t204(){ c1(mut, imm, lent, of(imm,read)); }
  @Test void t205(){ c1(iso, imm, lent, of(imm,read)); }
  @Test void t206(){ c1(mdf, imm, lent, of(/*not well formed lambda*/)); }
  @Test void t207(){ c1(recMdf, imm, lent, of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t211(){ c1(imm, read, lent, of(/*impossible*/)); }
  @Test void t212(){ c1(read, read, lent, of(/*impossible*/)); }
  @Test void t213(){ c1(lent, read, lent, of(read)); }
  @Test void t214(){ c1(mut, read, lent, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t215(){ c1(iso, read, lent, of(/*impossible*/)); }
  @Test void t216(){ c1(mdf, read, lent, of(/*not well formed lambda*/)); }
  @Test void t217(){ c1(recMdf, read, lent, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t221(){ c1(imm, lent, lent, of(/*impossible*/)); }
  @Test void t222(){ c1(read, lent, lent, of(/*impossible*/)); } // this capture is fine because the method cannot ever be called
  @Test void t223(){ c1(lent, lent, lent, of(read,lent)); }
  @Test void t224(){ c1(mut, lent, lent, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t225(){ c1(iso, lent, lent, of(/*impossible*/)); }
  @Test void t226(){ c1(mdf, lent, lent, of(/*not well formed lambda*/)); }
  @Test void t227(){ c1(recMdf, lent, lent, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t231(){ c1(imm, mut, lent, of(/*impossible*/)); }
  @Test void t232(){ c1(read, mut, lent, of(/*impossible*/)); }
  @Test void t233(){ c1(lent, mut, lent, of(read,lent)); }
  @Test void t234(){ c1(mut, mut, lent, of(read,lent)); }
  @Test void t235(){ c1(iso, mut, lent, of(read,lent)); }
  @Test void t236(){ c1(mdf, mut, lent, of(/*not well formed lambda*/)); }
  @Test void t237(){ c1(recMdf, mut, lent, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t241(){ c1(imm, iso, lent, of(/*impossible*/)); } //TODO: Marco up to here
  @Test void t242(){ c1(read, iso, lent, of(/*impossible*/)); }
  @Test void t243(){ c1(lent, iso, lent, of(imm,read)); }
  @Test void t244(){ c1(mut, iso, lent, of(imm,read)); }
  @Test void t245(){ c1(iso, iso, lent, of(imm,read)); }
  @Test void t246(){ c1(mdf, iso, lent, of(/*not well formed lambda*/)); }
  @Test void t247(){ c1(recMdf, iso, lent, of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t251(){ c1(imm, mdf, lent, of(/*not well formed parameter with mdf*/)); }
  @Test void t252(){ c1(read, mdf, lent, of(/*not well formed parameter with mdf*/)); }
  @Test void t253(){ c1(lent, mdf, lent, of(/*not well formed parameter with mdf*/)); }
  @Test void t254(){ c1(mut, mdf, lent, of(/*not well formed parameter with mdf*/)); }
  @Test void t255(){ c1(iso, mdf, lent, of(/*not well formed parameter with mdf*/)); }
  @Test void t256(){ c1(mdf, mdf, lent, of(/*not well formed parameter with mdf*/)); }
  @Test void t257(){ c1(recMdf, mdf, lent, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Test void t261(){ c1(imm, recMdf, lent, of(/*impossible*/)); }
  @Test void t262(){ c1(read, recMdf, lent, of(/*impossible*/)); }
  @Test void t263(){ c1(lent, recMdf, lent, of(read)); }
  @Test void t264(){ c1(mut, recMdf, lent, of(/*impossible*/)); }
  @Test void t265(){ c1(iso, recMdf, lent, of(/*impossible*/)); }
  @Test void t266(){ c1(mdf, recMdf, lent, of(/*not well formed lambda*/)); }
  @Test void t267(){ c1(recMdf, recMdf, lent, of(read)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t271(){ c1(imm, imm, lent, of(/*impossible*/)); }
  @Test void t272(){ c1(read, imm, lent, of(/*impossible*/)); }
  @Test void t273(){ c1(lent, imm, lent, of(read,imm)); }
  @Test void t274(){ c1(mut, imm, lent, of(read,imm)); }
  @Test void t275(){ c1(iso, imm, lent, of(read,imm)); }
  @Test void t276(){ c1(mdf, imm, lent, of(/*not well formed lambda*/)); }
  @Test void t277(){ c1(recMdf, imm, lent, of(read,imm)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t301(){ c1(imm, imm, mut, of(/*impossible*/)); }
  @Test void t302(){ c1(read, imm, mut, of(/*impossible*/)); }
  @Test void t303(){ c1(lent, imm, mut, of(imm,read)); }
  @Test void t304(){ c1(mut, imm, mut, of(imm,read)); }
  @Test void t305(){ c1(iso, imm, mut, of(imm,read)); }
  @Test void t306(){ c1(mdf, imm, mut, of(/*not well formed lambda*/)); }
  @Test void t307(){ c1(recMdf, imm, mut, of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t311(){ c1(imm, read, mut, of(/*impossible*/)); }
  @Test void t312(){ c1(read, read, mut, of(/*impossible*/)); }
  @Test void t313(){ c1(lent, read, mut, of(read)); } // yes because call I can call the mut method through a lent
  @Test void t314(){ c1(mut, read, mut, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t315(){ c1(iso, read, mut, of(/*impossible*/)); }
  @Test void t316(){ c1(mdf, read, mut, of(/*not well formed lambda*/)); }
  @Test void t317(){ c1(recMdf, read, mut, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t321(){ c1(imm, lent, mut, of(/*impossible*/)); }
  @Test void t322(){ c1(read, lent, mut, of(/*impossible*/)); } // this capture is fine because the method cannot ever be called
  @Test void t323(){ c1(lent, lent, mut, of(read,lent)); }
  @Test void t324(){ c1(mut, lent, mut, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t325(){ c1(iso, lent, mut, of(/*impossible*/)); }
  @Test void t326(){ c1(mdf, lent, mut, of(/*not well formed lambda*/)); }
  @Test void t327(){ c1(recMdf, lent, mut, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t331(){ c1(imm, mut, mut, of(/*impossible*/)); }
  @Test void t332(){ c1(read, mut, mut, of(/*impossible*/)); }
  @Test void t333(){ c1(lent, mut, mut, of(read,lent)); }
  @Test void t334(){ c1(mut, mut, mut, of(read,lent,mut)); }
  @Test void t335(){ c1(iso, mut, mut, of(read,lent,mut)); }
  @Test void t336(){ c1(mdf, mut, mut, of(/*not well formed lambda*/)); }
  @Test void t337(){ c1(recMdf, mut, mut, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t341(){ c1(imm, iso, mut, of(/*impossible*/)); }
  @Test void t342(){ c1(read, iso, mut, of(/*impossible*/)); }
  @Test void t343(){ c1(lent, iso, mut, of(imm,read)); }
  @Test void t344(){ c1(mut, iso, mut, of(imm,read)); }
  @Test void t345(){ c1(iso, iso, mut, of(imm,read)); }
  @Test void t346(){ c1(mdf, iso, mut, of(/*not well formed lambda*/)); }
  @Test void t347(){ c1(recMdf, iso, mut, of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t351(){ c1(imm, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t352(){ c1(read, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t353(){ c1(lent, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t354(){ c1(mut, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t355(){ c1(iso, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t356(){ c1(mdf, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t357(){ c1(recMdf, mdf, mut, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Test void t361(){ c1(imm, recMdf, mut, of(/*impossible*/)); }
  @Test void t362(){ c1(read, recMdf, mut, of(/*impossible*/)); }
  @Test void t363(){ c1(lent, recMdf, mut, of(read)); }
  @Test void t364(){ c1(mut, recMdf, mut, of(/*impossible*/)); }
  @Test void t365(){ c1(iso, recMdf, mut, of(/*impossible*/)); }
  @Test void t366(){ c1(mdf, recMdf, mut, of(/*not well formed lambda*/)); }
  @Test void t367(){ c1(recMdf, recMdf, mut, of(read)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t371(){ c1(imm, imm, mut, of(/*impossible*/)); }
  @Test void t372(){ c1(read, imm, mut, of(/*impossible*/)); }
  @Test void t373(){ c1(lent, imm, mut, of(read,imm)); }
  @Test void t374(){ c1(mut, imm, mut, of(read,imm)); }
  @Test void t375(){ c1(iso, imm, mut, of(read,imm)); }
  @Test void t376(){ c1(mdf, imm, mut, of(/*not well formed lambda*/)); }
  @Test void t377(){ c1(recMdf, imm, mut, of(read,imm)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t401(){ c1(imm, imm, iso, of(/*impossible*/)); }
  @Test void t402(){ c1(read, imm, iso, of(/*impossible*/)); }
  @Test void t403(){ c1(lent, imm, iso, of(imm,read)); }
  @Test void t404(){ c1(mut, imm, iso, of(imm,read)); }
  @Test void t405(){ c1(iso, imm, iso, of(imm,read)); }
  @Test void t406(){ c1(mdf, imm, iso, of(/*not well formed lambda*/)); }
  @Test void t407(){ c1(recMdf, imm, iso, of(imm,read)); } // yes, recMdf could be iso
  //                     lambda, captured, method, ...capturedAs
  @Test void t411(){ c1(imm, read, iso, of(/*impossible*/)); }
  @Test void t412(){ c1(read, read, iso, of(/*impossible*/)); }
  @Test void t413(){ c1(lent, read, iso, of(read)); }
  @Test void t414(){ c1(mut, read, iso, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t415(){ c1(iso, read, iso, of(/*impossible*/)); }
  @Test void t416(){ c1(mdf, read, iso, of(/*not well formed lambda*/)); }
  @Test void t417(){ c1(recMdf, read, iso, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t421(){ c1(imm, lent, iso, of(/*impossible*/)); }
  @Test void t422(){ c1(read, lent, iso, of(/*impossible*/)); }
  @Test void t423(){ c1(lent, lent, iso, of(read,lent)); }
  @Test void t424(){ c1(mut, lent, iso, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t425(){ c1(iso, lent, iso, of(/*impossible*/)); }
  @Test void t426(){ c1(mdf, lent, iso, of(/*not well formed lambda*/)); }
  @Test void t427(){ c1(recMdf, lent, iso, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t431(){ c1(imm, mut, iso, of(/*impossible*/)); }
  @Test void t432(){ c1(read, mut, iso, of(/*impossible*/)); }
  @Test void t433(){ c1(lent, mut, iso, of(read,lent,mut)); } // These 3 look odd, but it's correct because iso lambdas are treated like mut
  @Test void t434(){ c1(mut, mut, iso, of(read,lent,mut)); }
  @Test void t435(){ c1(iso, mut, iso, of(read,lent,mut)); }
  @Test void t436(){ c1(mdf, mut, iso, of(/*not well formed lambda*/)); }
  @Test void t437(){ c1(recMdf, mut, iso, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t441(){ c1(imm, iso, iso, of(/*impossible*/)); }
  @Test void t442(){ c1(read, iso, iso, of(/*impossible*/)); }
  @Test void t443(){ c1(lent, iso, iso, of(imm,read)); }
  @Test void t444(){ c1(mut, iso, iso, of(imm,read)); }
  @Test void t445(){ c1(iso, iso, iso, of(imm,read)); } // all iso is captured as imm
  @Test void t446(){ c1(mdf, iso, iso, of(/*not well formed lambda*/)); }
  @Test void t447(){ c1(recMdf, iso, iso, of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t451(){ c1(imm, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t452(){ c1(read, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t453(){ c1(lent, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t454(){ c1(mut, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t455(){ c1(iso, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t456(){ c1(mdf, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t457(){ c1(recMdf, mdf, iso, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Test void t461(){ c1(imm, recMdf, iso, of(/*impossible*/)); }
  @Test void t462(){ c1(read, recMdf, iso, of(/*impossible*/)); }
  @Test void t463(){ c1(lent, recMdf, iso, of(read)); }
  @Test void t464(){ c1(mut, recMdf, iso, of(/*impossible*/)); }
  @Test void t465(){ c1(iso, recMdf, iso, of(/*impossible*/)); }
  @Test void t466(){ c1(mdf, recMdf, iso, of(/*not well formed lambda*/)); }
  @Test void t467(){ c1(recMdf, recMdf, iso, of(read)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t471(){ c1(imm, imm, iso, of(/*impossible*/)); }
  @Test void t472(){ c1(read, imm, iso, of(/*impossible*/)); }
  @Test void t473(){ c1(lent, imm, iso, of(read,imm)); }
  @Test void t474(){ c1(mut, imm, iso, of(read,imm)); }
  @Test void t475(){ c1(iso, imm, iso, of(read,imm)); }
  @Test void t476(){ c1(mdf, imm, iso, of(/*not well formed lambda*/)); }
  @Test void t477(){ c1(recMdf, imm, iso, of(read,imm)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t501(){ c1(imm, imm, mdf, of(/*not well formed method*/)); }
  @Test void t502(){ c1(read, imm, mdf, of(/*not well formed method*/)); }
  @Test void t503(){ c1(lent, imm, mdf, of(/*not well formed method*/)); }
  @Test void t504(){ c1(mut, imm, mdf, of(/*not well formed method*/)); }
  @Test void t505(){ c1(iso, imm, mdf, of(/*not well formed method*/)); }
  @Test void t506(){ c1(mdf, imm, mdf, of(/*not well formed method*/)); }
  @Test void t507(){ c1(recMdf, imm, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t511(){ c1(imm, read, mdf, of(/*not well formed method*/)); }
  @Test void t512(){ c1(read, read, mdf, of(/*not well formed method*/)); }
  @Test void t513(){ c1(lent, read, mdf, of(/*not well formed method*/)); }
  @Test void t514(){ c1(mut, read, mdf, of(/*not well formed method*/)); }
  @Test void t515(){ c1(iso, read, mdf, of(/*not well formed method*/)); }
  @Test void t516(){ c1(mdf, read, mdf, of(/*not well formed method*/)); }
  @Test void t517(){ c1(recMdf, read, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t521(){ c1(imm, lent, mdf, of(/*not well formed method*/)); }
  @Test void t522(){ c1(read, lent, mdf, of(/*not well formed method*/)); }
  @Test void t523(){ c1(lent, lent, mdf, of(/*not well formed method*/)); }
  @Test void t524(){ c1(mut, lent, mdf, of(/*not well formed method*/)); }
  @Test void t525(){ c1(iso, lent, mdf, of(/*not well formed method*/)); }
  @Test void t526(){ c1(mdf, lent, mdf, of(/*not well formed method*/)); }
  @Test void t527(){ c1(recMdf, lent, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t531(){ c1(imm, mut, mdf, of(/*not well formed method*/)); }
  @Test void t532(){ c1(read, mut, mdf, of(/*not well formed method*/)); }
  @Test void t533(){ c1(lent, mut, mdf, of(/*not well formed method*/)); }
  @Test void t534(){ c1(mut, mut, mdf, of(/*not well formed method*/)); }
  @Test void t535(){ c1(iso, mut, mdf, of(/*not well formed method*/)); }
  @Test void t536(){ c1(mdf, mut, mdf, of(/*not well formed method*/)); }
  @Test void t537(){ c1(recMdf, mut, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t541(){ c1(imm, iso, mdf, of(/*not well formed method*/)); }
  @Test void t542(){ c1(read, iso, mdf, of(/*not well formed method*/)); }
  @Test void t543(){ c1(lent, iso, mdf, of(/*not well formed method*/)); }
  @Test void t544(){ c1(mut, iso, mdf, of(/*not well formed method*/)); }
  @Test void t545(){ c1(iso, iso, mdf, of(/*not well formed method*/)); }
  @Test void t546(){ c1(mdf, iso, mdf, of(/*not well formed method*/)); }
  @Test void t547(){ c1(recMdf, iso, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t551(){ c1(imm, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t552(){ c1(read, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t553(){ c1(lent, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t554(){ c1(mut, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t555(){ c1(iso, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t556(){ c1(mdf, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t557(){ c1(recMdf, mdf, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t561(){ c1(imm, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t562(){ c1(read, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t563(){ c1(lent, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t564(){ c1(mut, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t565(){ c1(iso, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t566(){ c1(mdf, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t567(){ c1(recMdf, recMdf, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t571(){ c1(imm, imm, mdf, of(/*not well formed method*/)); }
  @Test void t572(){ c1(read, imm, mdf, of(/*not well formed method*/)); }
  @Test void t573(){ c1(lent, imm, mdf, of(/*not well formed method*/)); }
  @Test void t574(){ c1(mut, imm, mdf, of(/*not well formed method*/)); }
  @Test void t575(){ c1(iso, imm, mdf, of(/*not well formed method*/)); }
  @Test void t576(){ c1(mdf, imm, mdf, of(/*not well formed method*/)); }
  @Test void t577(){ c1(recMdf, imm, mdf, of(/*not well formed method*/)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t601(){ c1(imm, imm, recMdf, of(read,imm)); }
  @Test void t602(){ c1(read, imm, recMdf, of(read,imm)); }
  @Test void t603(){ c1(lent, imm, recMdf, of(read,imm)); }
  @Test void t604(){ c1(mut, imm, recMdf, of(read,imm)); }
  @Test void t605(){ c1(iso, imm, recMdf, of(read,imm)); }
  @Test void t606(){ c1(mdf, imm, recMdf, of(/* not well formed lambda */)); }
  @Test void t607(){ c1(recMdf, imm, recMdf, of(read,imm)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t611(){ c1(imm, read, recMdf, of(/*not well formed method*/)); }
  @Test void t612(){ c1(read, read, recMdf, of(read)); }
  @Test void t613(){ c1(lent, read, recMdf, of(read)); }
  @Test void t614(){ c1(mut, read, recMdf, of()); }
  @Test void t615(){ c1(iso, read, recMdf, of()); }
  @Test void t616(){ c1(mdf, read, recMdf, of()); }
  @Test void t617(){ c1(recMdf, read, recMdf, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t621(){ c1(imm, lent, recMdf, of(/*not well formed method*/)); }
  @Test void t622(){ c1(read, lent, recMdf, of(read, recMdf)); }
  @Test void t623(){ c1(lent, lent, recMdf, of(read, recMdf)); }
  @Test void t624(){ c1(mut, lent, recMdf, of(/*not well formed method*/)); }
  @Test void t625(){ c1(iso, lent, recMdf, of(/*not well formed method*/)); }
  @Test void t626(){ c1(mdf, lent, recMdf, of(/*not well formed method*/)); }
  @Test void t627(){ c1(recMdf, lent, recMdf, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t631(){ c1(imm, mut, recMdf, of(/*not well formed method*/)); }
  @Test void t632(){ c1(read, mut, recMdf, of(read, recMdf)); }
  @Test void t633(){ c1(lent, mut, recMdf, of(read, recMdf)); }
  @Test void t634(){ c1(mut, mut, recMdf, of(read, recMdf)); }
  @Test void t635(){ c1(iso, mut, recMdf, of(read, recMdf)); }
  @Test void t636(){ c1(mdf, mut, recMdf, of(/* not well formed */)); }
  @Test void t637(){ c1(recMdf, mut, recMdf, of(/* impossible */)); } // same as with a read method
  //                     lambda, captured, method, ...capturedAs
  @Test void t641(){ c1(imm, iso, recMdf, of(read, imm)); }
  @Test void t642(){ c1(read, iso, recMdf, of(read, imm)); }
  @Test void t643(){ c1(lent, iso, recMdf, of(read, imm)); }
  @Test void t644(){ c1(mut, iso, recMdf, of(read, imm)); }
  @Test void t645(){ c1(iso, iso, recMdf, of(read, imm)); }
  @Test void t646(){ c1(mdf, iso, recMdf, of(/*not well formed lambda*/)); }
  @Test void t647(){ c1(recMdf, iso, recMdf, of(read, imm)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t651(){ c1(imm, mdf, recMdf, of(/*not well formed value to capture*/)); }
  @Test void t652(){ c1(read, mdf, recMdf, of(/*not well formed value to capture*/)); }
  @Test void t653(){ c1(lent, mdf, recMdf, of(/*not well formed value to capture*/)); }
  @Test void t654(){ c1(mut, mdf, recMdf, of(/*not well formed value to capture*/)); }
  @Test void t655(){ c1(iso, mdf, recMdf, of(/*not well formed value to capture*/)); }
  @Test void t656(){ c1(mdf, mdf, recMdf, of(/*not well formed value to capture*/)); }
  @Test void t657(){ c1(recMdf, mdf, recMdf, of(/*not well formed value to capture*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t661(){ c1(imm, recMdf, recMdf, of()); }
  @Test void t662(){ c1(read, recMdf, recMdf, of(read)); }
  @Test void t663(){ c1(lent, recMdf, recMdf, of(read)); }
  @Test void t664(){ c1(mut, recMdf, recMdf, of(/*not well formed method*/)); }
  @Test void t665(){ c1(iso, recMdf, recMdf, of(/*not well formed method*/)); }
  @Test void t666(){ c1(mdf, recMdf, recMdf, of(/*not well formed method*/)); }
  @Test void t667(){ c1(recMdf, recMdf, recMdf, of(read, recMdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t671(){ c1(imm, imm, recMdf, of(read, imm)); }
  @Test void t672(){ c1(read, imm, recMdf, of(read, imm)); }
  @Test void t673(){ c1(lent, imm, recMdf, of(read, imm)); }
  @Test void t674(){ c1(mut, imm, recMdf, of(read, imm)); }
  @Test void t675(){ c1(iso, imm, recMdf, of(read, imm)); }
  @Test void t676(){ c1(mdf, imm, recMdf, of(/*not well formed method*/)); }
  @Test void t677(){ c1(recMdf, imm, recMdf, of(read, imm)); }

  // ---------------------- c2 ---------------------
  //                     lambda, captured, method, ...capturedAs
  @Test void t2001(){ c2(imm, imm, imm, of(imm,read,mdf)); }
  @Test void t2002(){ c2(read, imm, imm, of(imm,read,mdf)); }
  @Test void t2003(){ c2(lent, imm, imm, of(imm,read,mdf)); }
  @Test void t2004(){ c2(mut, imm, imm, of(imm,read,mdf)); }
  @Test void t2005(){ c2(iso, imm, imm, of(imm,read,mdf)); }
  @Test void t2006(){ c2(mdf, imm, imm, of(/*not well formed lambda*/)); }
  @Test void t2007(){ c2(recMdf,imm,   imm,   of(imm,read,mdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2011(){ c2(imm, read, imm, of(/*impossible*/)); }
  @Test void t2012(){ c2(read, read, imm, of(imm,read,mdf)); }
  @Test void t2013(){ c2(lent, read, imm, of(imm,read,mdf)); }
  @Test void t2014(){ c2(mut, read, imm, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2015(){ c2(iso, read, imm, of(/*impossible*/)); }
  @Test void t2016(){ c2(mdf, read, imm, of(/*not well formed lambda*/)); }
  @Test void t2017(){ c2(recMdf, read, imm,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2021(){ c2(imm, lent, imm, of(/*impossible*/)); }
  @Test void t2022(){ c2(read, lent, imm, of(imm,read)); }
  @Test void t2023(){ c2(lent, lent, imm, of(imm,read)); }
  @Test void t2024(){ c2(mut, lent, imm, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2025(){ c2(iso, lent, imm, of(/*impossible*/)); }
  @Test void t2026(){ c2(mdf, lent, imm, of(/*not well formed lambda*/)); }
  @Test void t2027(){ c2(recMdf,lent,  imm,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2031(){ c2(imm, mut, imm, of(/*impossible*/)); }
  @Test void t2032(){ c2(read, mut, imm, of(imm,read)); }
  @Test void t2033(){ c2(lent, mut, imm, of(imm,read)); }
  @Test void t2034(){ c2(mut, mut, imm, of(imm,read)); }
  @Test void t2035(){ c2(iso, mut, imm, of(imm,read)); }
  @Test void t2036(){ c2(mdf, mut, imm, of(/*not well formed lambda*/)); }
  @Test void t2037(){ c2(recMdf, mut, imm,  of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2041(){ c2(imm, iso, imm, of(/*not well formed generic type*/)); }
  @Test void t2042(){ c2(read, iso, imm, of(/*not well formed generic type*/)); }
  @Test void t2043(){ c2(lent, iso, imm, of(/*not well formed generic type*/)); }
  @Test void t2044(){ c2(mut, iso, imm, of(/*not well formed generic type*/)); }
  @Test void t2045(){ c2(iso, iso, imm, of(/*not well formed generic type*/)); }
  @Test void t2046(){ c2(mdf, iso, imm, of(/*not well formed lambda*/)); }
  @Test void t2047(){ c2(recMdf, iso, imm,   of(/*not well formed generic type*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2051(){ c2(imm, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t2052(){ c2(read, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t2053(){ c2(lent, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t2054(){ c2(mut, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t2055(){ c2(iso, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t2056(){ c2(mdf, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t2057(){ c2(recMdf,mdf,   imm, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Test void t2061(){ c2(imm, recMdf, imm, of(/*impossible*/)); }
  @Test void t2062(){ c2(read, recMdf, imm, of(imm,read)); }
  @Test void t2063(){ c2(lent, recMdf, imm, of(imm,read)); }
  @Test void t2064(){ c2(mut, recMdf, imm, of(/*impossible*/)); }
  @Test void t2065(){ c2(iso, recMdf, imm, of(/*impossible*/)); }
  @Test void t2066(){ c2(mdf, recMdf, imm, of(/*not well formed lambda*/)); }
  @Test void t2067(){ c2(recMdf, recMdf, imm, of(imm,read)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t2181(){ c2(imm, imm, read, of(imm,read,recMdf,mdf)); }
  @Test void t2182(){ c2(read, imm, read, of(imm,read,recMdf,mdf)); }
  @Test void t2183(){ c2(lent, imm, read, of(imm,read,recMdf,mdf)); }//HARD!
  @Test void t2184(){ c2(mut, imm, read, of(imm,read,recMdf,mdf)); }//HARD!  Note how this is different wrt c1
  @Test void t2185(){ c2(iso, imm, read, of(imm,read,recMdf,mdf)); }
  @Test void t2186(){ c2(mdf, imm, read, of(/*not well formed lambda*/)); }
  @Test void t2187(){ c2(recMdf,imm,   read,   of(imm,read,recMdf,mdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2101(){ c2(imm, read, read, of(/*impossible*/)); }
  @Test void t2102(){ c2(read, read, read, of(read,mdf,recMdf)); }
  @Test void t2103(){ c2(lent, read, read, of(read,mdf,recMdf)); }
  @Test void t2104(){ c2(mut, read, read, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2105(){ c2(iso, read, read, of(/*impossible*/)); }
  @Test void t2106(){ c2(mdf, read, read, of(/*not well formed lambda*/)); }
  @Test void t2107(){ c2(recMdf,read,  read,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2111(){ c2(imm, lent, read, of(/*impossible*/)); }
  @Test void t2112(){ c2(read, lent, read, of(read)); }// captures lent as recMdf (adapt)
  @Test void t2113(){ c2(lent, lent, read, of(read)); }//the lambda is created read, and can not become anything else but imm.
  @Test void t2114(){ c2(mut, lent, read, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2115(){ c2(iso, lent, read, of(/*impossible*/)); }
  @Test void t2116(){ c2(mdf, lent, read, of(/*not well formed lambda*/)); }
  @Test void t2117(){ c2(recMdf,lent,  read,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2121(){ c2(imm, mut, read, of(/*impossible*/)); }
  @Test void t2122(){ c2(read, mut, read, of(read)); }
  @Test void t2123(){ c2(lent, mut, read, of(read)); }
  @Test void t2124(){ c2(mut, mut, read, of(read)); }
  @Test void t2125(){ c2(iso, mut, read, of(read)); }
  @Test void t2126(){ c2(mdf, mut, read, of(/*not well formed lambda*/)); }
  @Test void t2127(){ c2(recMdf,mut,   read,  of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2131(){ c2(imm, iso, read, of(/*not well formed type arg*/)); }
  @Test void t2132(){ c2(read, iso, read, of(/*not well formed type arg*/)); }
  @Test void t2133(){ c2(lent, iso, read, of(/*not well formed type arg*/)); }
  @Test void t2134(){ c2(mut, iso, read, of(/*not well formed type arg*/)); }
  @Test void t2135(){ c2(iso, iso, read, of(/*not well formed type arg*/)); }
  @Test void t2136(){ c2(mdf, iso, read, of(/*not well formed lambda*/)); }
  @Test void t2137(){ c2(recMdf,iso,   read,   of(/*not well formed type arg*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2141(){ c2(imm, mdf, read, of(/*not well formed parameter with mdf*/)); }
  @Test void t2142(){ c2(read, mdf, read, of(/*not well formed parameter with mdf*/)); }
  @Test void t2143(){ c2(lent, mdf, read, of(/*not well formed parameter with mdf*/)); }
  @Test void t2144(){ c2(mut, mdf, read, of(/*not well formed parameter with mdf*/)); }
  @Test void t2145(){ c2(iso, mdf, read, of(/*not well formed parameter with mdf*/)); }
  @Test void t2146(){ c2(mdf, mdf, read, of(/*not well formed parameter with mdf*/)); }
  @Test void t2147(){ c2(recMdf,mdf,   read, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Test void t2151(){ c2(imm, recMdf, read, of(/*impossible*/)); }
  @Test void t2152(){ c2(read, recMdf, read, of(read)); }
  @Test void t2153(){ c2(lent, recMdf, read, of(read)); }
  @Test void t2154(){ c2(mut, recMdf, read, of(/*impossible*/)); }
  @Test void t2155(){ c2(iso, recMdf, read, of(/*impossible*/)); }
  @Test void t2156(){ c2(mdf, recMdf, read, of(/*not well formed lambda*/)); }
  @Test void t2157(){ c2(recMdf,recMdf,   read,   of(read,recMdf,mdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2161(){ c2(imm, imm, read, of(read,imm,recMdf,mdf)); }
  @Test void t2162(){ c2(read, imm, read, of(read,imm,recMdf,mdf)); }
  @Test void t2163(){ c2(lent, imm, read, of(read,imm,mdf,recMdf)); } // this is fine because the recMdf is treated as imm
  @Test void t2164(){ c2(mut, imm, read, of(read,imm,mdf,recMdf)); }
  @Test void t2165(){ c2(iso, imm, read, of(read,imm,recMdf,mdf)); }
  @Test void t2166(){ c2(mdf, imm, read, of(/*not well formed lambda*/)); }
  @Test void t2167(){ c2(recMdf,imm,   read,   of(read,imm,recMdf,mdf)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t2201(){ c2(imm, imm, lent, of(/*impossible*/)); }
  @Test void t2202(){ c2(read, imm, lent, of(/*impossible*/)); }
  @Test void t2203(){ c2(lent, imm, lent, of(imm,read,mdf,recMdf)); }
  @Test void t2204(){ c2(mut, imm, lent, of(imm,read,mdf,recMdf)); }
  @Test void t2205(){ c2(iso, imm, lent, of(imm,read,mdf,recMdf)); }
  @Test void t2206(){ c2(mdf, imm, lent, of(/*not well formed lambda*/)); }
  @Test void t2207(){ c2(recMdf,imm,   lent,   of(imm,read,mdf,recMdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2211(){ c2(imm, read, lent, of(/*impossible*/)); }
  @Test void t2212(){ c2(read, read, lent, of(/*impossible*/)); }
  @Test void t2213(){ c2(lent, read, lent, of(read,recMdf,mdf)); }
  @Test void t2214(){ c2(mut, read, lent, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2215(){ c2(iso, read, lent, of(/*impossible*/)); }
  @Test void t2216(){ c2(mdf, read, lent, of(/*not well formed lambda*/)); }
  @Test void t2217(){ c2(recMdf,read,  lent,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2221(){ c2(imm, lent, lent, of(/*impossible*/)); }
  @Test void t2222(){ c2(read, lent, lent, of(/*impossible*/)); } // this capture is fine because the method cannot ever be called
  @Test void t2223(){ c2(lent, lent, lent, of(read,lent,mdf,recMdf)); }
  @Test void t2224(){ c2(mut, lent, lent, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2225(){ c2(iso, lent, lent, of(/*impossible*/)); }
  @Test void t2226(){ c2(mdf, lent, lent, of(/*not well formed lambda*/)); }
  @Test void t2227(){ c2(recMdf,lent,  lent,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2231(){ c2(imm, mut, lent, of(/*impossible*/)); }
  @Test void t2232(){ c2(read, mut, lent, of(/*impossible*/)); }
  @Test void t2233(){ c2(lent, mut, lent, of(read,lent)); }
  @Test void t2234(){ c2(mut, mut, lent, of(read,lent)); }
  @Test void t2235(){ c2(iso, mut, lent, of(read,lent)); }
  @Test void t2236(){ c2(mdf, mut, lent, of(/*not well formed lambda*/)); }
  @Test void t2237(){ c2(recMdf,mut,   lent,  of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2241(){ c2(imm, iso, lent, of(/*impossible*/)); }
  @Test void t2242(){ c2(read, iso, lent, of(/*impossible*/)); }
  @Test void t2243(){ c2(lent, iso, lent, of(/*not well formed type argument*/)); }
  @Test void t2244(){ c2(mut, iso, lent, of(/*not well formed type argument*/)); }
  @Test void t2245(){ c2(iso, iso, lent, of(/*not well formed type argument*/)); }
  @Test void t2246(){ c2(mdf, iso, lent, of(/*not well formed lambda*/)); }
  @Test void t2247(){ c2(recMdf,iso,   lent,   of(/*not well formed type argument*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2251(){ c2(imm, mdf, lent, of(/*not well formed parameter with mdf*/)); }
  @Test void t2252(){ c2(read, mdf, lent, of(/*not well formed parameter with mdf*/)); }
  @Test void t2253(){ c2(lent, mdf, lent, of(/*not well formed parameter with mdf*/)); }
  @Test void t2254(){ c2(mut, mdf, lent, of(/*not well formed parameter with mdf*/)); }
  @Test void t2255(){ c2(iso, mdf, lent, of(/*not well formed parameter with mdf*/)); }
  @Test void t2256(){ c2(mdf, mdf, lent, of(/*not well formed parameter with mdf*/)); }
  @Test void t2257(){ c2(recMdf,mdf,   lent, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Test void t2261(){ c2(imm, recMdf, lent, of(/*impossible*/)); }
  @Test void t2262(){ c2(read, recMdf, lent, of(/*impossible*/)); }
  @Test void t2263(){ c2(lent, recMdf, lent, of(read)); }
  @Test void t2264(){ c2(mut, recMdf, lent, of(/*impossible*/)); }
  @Test void t2265(){ c2(iso, recMdf, lent, of(/*impossible*/)); }
  @Test void t2266(){ c2(mdf, recMdf, lent, of(/*not well formed lambda*/)); }
  @Test void t2267(){ c2(recMdf,recMdf,   lent,   of(read,recMdf,mdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2271(){ c2(imm, imm, lent, of(/*impossible*/)); }
  @Test void t2272(){ c2(read, imm, lent, of(/*impossible*/)); }
  @Test void t2273(){ c2(lent, imm, lent, of(read,imm,mdf,recMdf)); }
  @Test void t2274(){ c2(mut, imm, lent, of(read,imm,mdf,recMdf)); }
  @Test void t2275(){ c2(iso, imm, lent, of(read,imm,mdf,recMdf)); }
  @Test void t2276(){ c2(mdf, imm, lent, of(/*not well formed lambda*/)); }
  @Test void t2277(){ c2(recMdf,imm,   lent,   of(read,imm,mdf,recMdf)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t2301(){ c2(imm, imm, mut, of(/*impossible*/)); }
  @Test void t2302(){ c2(read, imm, mut, of(/*impossible*/)); }
  @Test void t2303(){ c2(lent, imm, mut, of(imm,read,mdf)); }
  @Test void t2304(){ c2(mut, imm, mut, of(imm,read,mdf)); }
  @Test void t2305(){ c2(iso, imm, mut, of(imm,read,mdf)); }
  @Test void t2306(){ c2(mdf, imm, mut, of(/*not well formed lambda*/)); }
  @Test void t2307(){ c2(recMdf,imm,   mut,   of(imm,read,mdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2311(){ c2(imm, read, mut, of(/*impossible*/)); }
  @Test void t2312(){ c2(read, read, mut, of(/*impossible*/)); }
  @Test void t2313(){ c2(lent, read, mut, of(read,mdf)); } // yes because call I can call the mut method through a lent
  @Test void t2314(){ c2(mut, read, mut, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2315(){ c2(iso, read, mut, of(/*impossible*/)); }
  @Test void t2316(){ c2(mdf, read, mut, of(/*not well formed lambda*/)); }
  @Test void t2317(){ c2(recMdf,read,  mut,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2321(){ c2(imm, lent, mut, of(/*impossible*/)); }
  @Test void t2322(){ c2(read, lent, mut, of(/*impossible*/)); } // this capture is fine because the method cannot ever be called
  @Test void t2323(){ c2(lent, lent, mut, of(read,lent,mdf)); }
  @Test void t2324(){ c2(mut, lent, mut, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2325(){ c2(iso, lent, mut, of(/*impossible*/)); }
  @Test void t2326(){ c2(mdf, lent, mut, of(/*not well formed lambda*/)); }
  @Test void t2327(){ c2(recMdf,lent,  mut,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2331(){ c2(imm, mut, mut, of(/*impossible*/)); }
  @Test void t2332(){ c2(read, mut, mut, of(/*impossible*/)); }
  @Test void t2333(){ c2(lent, mut, mut, of(read,lent)); }
  @Test void t2334(){ c2(mut, mut, mut, of(read,lent,mut,mdf)); }
  @Test void t2335(){ c2(iso, mut, mut, of(read,lent,mut,mdf)); }
  @Test void t2336(){ c2(mdf, mut, mut, of(/*not well formed lambda*/)); }
  @Test void t2337(){ c2(recMdf,mut,   mut,  of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2341(){ c2(imm, iso, mut, of(/*impossible*/)); }
  @Test void t2342(){ c2(read, iso, mut, of(/*impossible*/)); }
  @Test void t2343(){ c2(lent, iso, mut, of(/*not well formed type params*/)); }
  @Test void t2344(){ c2(mut, iso, mut, of(/*not well formed type params*/)); }
  @Test void t2345(){ c2(iso, iso, mut, of(/*not well formed type params*/)); }
  @Test void t2346(){ c2(mdf, iso, mut, of(/*not well formed lambda*/)); }
  @Test void t2347(){ c2(recMdf,iso,   mut,   of(/*not well formed type params*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2351(){ c2(imm, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t2352(){ c2(read, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t2353(){ c2(lent, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t2354(){ c2(mut, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t2355(){ c2(iso, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t2356(){ c2(mdf, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t2357(){ c2(recMdf,mdf,   mut, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Test void t2361(){ c2(imm, recMdf, mut, of(/*impossible*/)); }
  @Test void t2362(){ c2(read, recMdf, mut, of(/*impossible*/)); }
  @Test void t2363(){ c2(lent, recMdf, mut, of(read)); }
  @Test void t2364(){ c2(mut, recMdf, mut, of(/*impossible*/)); }
  @Test void t2365(){ c2(iso, recMdf, mut, of(/*impossible*/)); }
  @Test void t2366(){ c2(mdf, recMdf, mut, of(/*not well formed lambda*/)); }
  @Test void t2367(){ c2(recMdf,recMdf,   mut,   of(read)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2371(){ c2(imm, imm, mut, of(/*impossible*/)); }
  @Test void t2372(){ c2(read, imm, mut, of(/*impossible*/)); }
  @Test void t2373(){ c2(lent, imm, mut, of(read,imm,mdf)); }
  @Test void t2374(){ c2(mut, imm, mut, of(read,imm,mdf)); }
  @Test void t2375(){ c2(iso, imm, mut, of(read,imm,mdf)); }
  @Test void t2376(){ c2(mdf, imm, mut, of(/*not well formed lambda*/)); }
  @Test void t2377(){ c2(recMdf,imm,   mut,   of(read,imm,mdf)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t2401(){ c2(imm, imm, iso, of(/*impossible*/)); }
  @Test void t2402(){ c2(read, imm, iso, of(/*impossible*/)); }
  @Test void t2403(){ c2(lent, imm, iso, of(imm,read,mdf)); }
  @Test void t2404(){ c2(mut, imm, iso, of(imm,read,mdf)); }
  @Test void t2405(){ c2(iso, imm, iso, of(imm,read,mdf)); }
  @Test void t2406(){ c2(mdf, imm, iso, of(/*not well formed lambda*/)); }
  @Test void t2407(){ c2(recMdf,imm,   iso,   of(imm,read,mdf)); } // yes, recMdf could be iso
  //                     lambda, captured, method, ...capturedAs
  @Test void t2411(){ c2(imm, read, iso, of(/*impossible*/)); }
  @Test void t2412(){ c2(read, read, iso, of(/*impossible*/)); }
  @Test void t2413(){ c2(lent, read, iso, of(read,mdf)); }
  @Test void t2414(){ c2(mut, read, iso, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2415(){ c2(iso, read, iso, of(/*impossible*/)); }
  @Test void t2416(){ c2(mdf, read, iso, of(/*not well formed lambda*/)); }
  @Test void t2417(){ c2(recMdf,read,  iso,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2421(){ c2(imm, lent, iso, of(/*impossible*/)); }
  @Test void t2422(){ c2(read, lent, iso, of(/*impossible*/)); }
  @Test void t2423(){ c2(lent, lent, iso, of(read,lent,mdf)); }
  @Test void t2424(){ c2(mut, lent, iso, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2425(){ c2(iso, lent, iso, of(/*impossible*/)); }
  @Test void t2426(){ c2(mdf, lent, iso, of(/*not well formed lambda*/)); }
  @Test void t2427(){ c2(recMdf,lent,  iso,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2431(){ c2(imm, mut, iso, of(/*impossible*/)); }
  @Test void t2432(){ c2(read, mut, iso, of(/*impossible*/)); }
  @Test void t2433(){ c2(lent, mut, iso, of(read,lent,mut,mdf)); } // These 3 look odd, but it's correct because iso lambdas are treated like mut
  @Test void t2434(){ c2(mut, mut, iso, of(read,lent,mut,mdf)); }
  @Test void t2435(){ c2(iso, mut, iso, of(read,lent,mut,mdf)); }
  @Test void t2436(){ c2(mdf, mut, iso, of(/*not well formed lambda*/)); }
  @Test void t2437(){ c2(recMdf,mut,   iso,  of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2441(){ c2(imm, iso, iso, of(/*impossible*/)); }
  @Test void t2442(){ c2(read, iso, iso, of(/*impossible*/)); }
  @Test void t2443(){ c2(lent, iso, iso, of(/*not well formed type params*/)); }
  @Test void t2444(){ c2(mut, iso, iso, of(/*not well formed type params*/)); }
  @Test void t2445(){ c2(iso, iso, iso, of(/*not well formed type params*/)); } // all iso is captured as imm
  @Test void t2446(){ c2(mdf, iso, iso, of(/*not well formed lambda*/)); }
  @Test void t2447(){ c2(recMdf,iso,   iso,   of(/*not well formed type params*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2451(){ c2(imm, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t2452(){ c2(read, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t2453(){ c2(lent, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t2454(){ c2(mut, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t2455(){ c2(iso, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t2456(){ c2(mdf, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t2457(){ c2(recMdf,mdf,   iso, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Test void t2461(){ c2(imm, recMdf, iso, of(/*impossible*/)); }
  @Test void t2462(){ c2(read, recMdf, iso, of(/*impossible*/)); }
  @Test void t2463(){ c2(lent, recMdf, iso, of(read)); }
  @Test void t2464(){ c2(mut, recMdf, iso, of(/*impossible*/)); }
  @Test void t2465(){ c2(iso, recMdf, iso, of(/*impossible*/)); }
  @Test void t2466(){ c2(mdf, recMdf, iso, of(/*not well formed lambda*/)); }
  @Test void t2467(){ c2(recMdf,recMdf,   iso,   of(read)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2471(){ c2(imm, imm, iso, of(/*impossible*/)); }
  @Test void t2472(){ c2(read, imm, iso, of(/*impossible*/)); }
  @Test void t2473(){ c2(lent, imm, iso, of(read,imm,mdf)); }
  @Test void t2474(){ c2(mut, imm, iso, of(read,imm,mdf)); }
  @Test void t2475(){ c2(iso, imm, iso, of(read,imm,mdf)); }
  @Test void t2476(){ c2(mdf, imm, iso, of(/*not well formed lambda*/)); }
  @Test void t2477(){ c2(recMdf,imm,   iso,   of(read,imm,mdf)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t2501(){ c2(imm, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2502(){ c2(read, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2503(){ c2(lent, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2504(){ c2(mut, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2505(){ c2(iso, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2506(){ c2(mdf, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2507(){ c2(recMdf,imm,   mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2511(){ c2(imm, read, mdf, of(/*not well formed method*/)); }
  @Test void t2512(){ c2(read, read, mdf, of(/*not well formed method*/)); }
  @Test void t2513(){ c2(lent, read, mdf, of(/*not well formed method*/)); }
  @Test void t2514(){ c2(mut, read, mdf, of(/*not well formed method*/)); }
  @Test void t2515(){ c2(iso, read, mdf, of(/*not well formed method*/)); }
  @Test void t2516(){ c2(mdf, read, mdf, of(/*not well formed method*/)); }
  @Test void t2517(){ c2(recMdf,read,  mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2521(){ c2(imm, lent, mdf, of(/*not well formed method*/)); }
  @Test void t2522(){ c2(read, lent, mdf, of(/*not well formed method*/)); }
  @Test void t2523(){ c2(lent, lent, mdf, of(/*not well formed method*/)); }
  @Test void t2524(){ c2(mut, lent, mdf, of(/*not well formed method*/)); }
  @Test void t2525(){ c2(iso, lent, mdf, of(/*not well formed method*/)); }
  @Test void t2526(){ c2(mdf, lent, mdf, of(/*not well formed method*/)); }
  @Test void t2527(){ c2(recMdf,lent,  mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2531(){ c2(imm, mut, mdf, of(/*not well formed method*/)); }
  @Test void t2532(){ c2(read, mut, mdf, of(/*not well formed method*/)); }
  @Test void t2533(){ c2(lent, mut, mdf, of(/*not well formed method*/)); }
  @Test void t2534(){ c2(mut, mut, mdf, of(/*not well formed method*/)); }
  @Test void t2535(){ c2(iso, mut, mdf, of(/*not well formed method*/)); }
  @Test void t2536(){ c2(mdf, mut, mdf, of(/*not well formed method*/)); }
  @Test void t2537(){ c2(recMdf,mut,   mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2541(){ c2(imm, iso, mdf, of(/*not well formed method*/)); }
  @Test void t2542(){ c2(read, iso, mdf, of(/*not well formed method*/)); }
  @Test void t2543(){ c2(lent, iso, mdf, of(/*not well formed method*/)); }
  @Test void t2544(){ c2(mut, iso, mdf, of(/*not well formed method*/)); }
  @Test void t2545(){ c2(iso, iso, mdf, of(/*not well formed method*/)); }
  @Test void t2546(){ c2(mdf, iso, mdf, of(/*not well formed method*/)); }
  @Test void t2547(){ c2(recMdf,iso,   mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2551(){ c2(imm, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t2552(){ c2(read, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t2553(){ c2(lent, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t2554(){ c2(mut, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t2555(){ c2(iso, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t2556(){ c2(mdf, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t2557(){ c2(recMdf,mdf,   mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2561(){ c2(imm, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t2562(){ c2(read, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t2563(){ c2(lent, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t2564(){ c2(mut, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t2565(){ c2(iso, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t2566(){ c2(mdf, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t2567(){ c2(recMdf,recMdf,   mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2571(){ c2(imm, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2572(){ c2(read, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2573(){ c2(lent, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2574(){ c2(mut, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2575(){ c2(iso, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2576(){ c2(mdf, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2577(){ c2(recMdf,imm,   mdf, of(/*not well formed method*/)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t2601(){ c2(imm, imm, recMdf, of(/*not well formed method*/)); }
  @Test void t2602(){ c2(read, imm, recMdf, of(/*not well formed method*/)); }
  @Test void t2603(){ c2(lent, imm, recMdf, of(/*not well formed method*/)); }
  @Test void t2604(){ c2(mut, imm, recMdf, of(/*not well formed method*/)); }
  @Test void t2605(){ c2(iso, imm, recMdf, of(/*not well formed method*/)); }
  @Test void t2606(){ c2(mdf, imm, recMdf, of(/*not well formed method*/)); }
  @Test void t2607(){ c2(recMdf,imm,   recMdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2611(){ c2(imm, read, recMdf, of(/*not well formed method*/)); }
  @Test void t2612(){ c2(read, read, recMdf, of(/*not well formed method*/)); }
  @Test void t2613(){ c2(lent, read, recMdf, of(/*not well formed method*/)); }
  @Test void t2614(){ c2(mut, read, recMdf, of(/*not well formed method*/)); }
  @Test void t2615(){ c2(iso, read, recMdf, of(/*not well formed method*/)); }
  @Test void t2616(){ c2(mdf, read, recMdf, of(/*not well formed method*/)); }
  @Test void t2617(){ c2(recMdf,read,  recMdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2621(){ c2(imm, lent, recMdf, of(/*not well formed method*/)); }
  @Test void t2622(){ c2(read, lent, recMdf, of(/*not well formed method*/)); }
  @Test void t2623(){ c2(lent, lent, recMdf, of(/*not well formed method*/)); }
  @Test void t2624(){ c2(mut, lent, recMdf, of(/*not well formed method*/)); }
  @Test void t2625(){ c2(iso, lent, recMdf, of(/*not well formed method*/)); }
  @Test void t2626(){ c2(mdf, lent, recMdf, of(/*not well formed method*/)); }
  @Test void t2627(){ c2(recMdf,lent,  recMdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2631(){ c2(imm, mut, recMdf, of(/*not well formed method*/)); }
  @Test void t2632(){ c2(read, mut, recMdf, of(/*not well formed method*/)); }
  @Test void t2633(){ c2(lent, mut, recMdf, of(/*not well formed method*/)); }
  @Test void t2634(){ c2(mut, mut, recMdf, of(/*not well formed method*/)); }
  @Test void t2635(){ c2(iso, mut, recMdf, of(/*not well formed method*/)); }
  @Test void t2636(){ c2(mdf, mut, recMdf, of(/*not well formed method*/)); }
  @Test void t2637(){ c2(recMdf,mut,   recMdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2641(){ c2(imm, iso, recMdf, of(/*not well formed method*/)); }
  @Test void t2642(){ c2(read, iso, recMdf, of(/*not well formed method*/)); }
  @Test void t2643(){ c2(lent, iso, recMdf, of(/*not well formed method*/)); }
  @Test void t2644(){ c2(mut, iso, recMdf, of(/*not well formed method*/)); }
  @Test void t2645(){ c2(iso, iso, recMdf, of(/*not well formed method*/)); }
  @Test void t2646(){ c2(mdf, iso, recMdf, of(/*not well formed method*/)); }
  @Test void t2647(){ c2(recMdf,iso,   recMdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2651(){ c2(imm, mdf, recMdf, of(/*not well formed method*/)); }
  @Test void t2652(){ c2(read, mdf, recMdf, of(/*not well formed method*/)); }
  @Test void t2653(){ c2(lent, mdf, recMdf, of(/*not well formed method*/)); }
  @Test void t2654(){ c2(mut, mdf, recMdf, of(/*not well formed method*/)); }
  @Test void t2655(){ c2(iso, mdf, recMdf, of(/*not well formed method*/)); }
  @Test void t2656(){ c2(mdf, mdf, recMdf, of(/*not well formed method*/)); }
  @Test void t2657(){ c2(recMdf,mdf,   recMdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2661(){ c2(imm, recMdf, recMdf, of(/*not well formed method*/)); }
  @Test void t2662(){ c2(read, recMdf, recMdf, of(/*not well formed method*/)); }
  @Test void t2663(){ c2(lent, recMdf, recMdf, of(/*not well formed method*/)); }
  @Test void t2664(){ c2(mut, recMdf, recMdf, of(/*not well formed method*/)); }
  @Test void t2665(){ c2(iso, recMdf, recMdf, of(/*not well formed method*/)); }
  @Test void t2666(){ c2(mdf, recMdf, recMdf, of(/*not well formed method*/)); }
  @Test void t2667(){ c2(recMdf,recMdf,   recMdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2671(){ c2(imm, imm, recMdf, of(/*not well formed method*/)); }
  @Test void t2672(){ c2(read, imm, recMdf, of(/*not well formed method*/)); }
  @Test void t2673(){ c2(lent, imm, recMdf, of(/*not well formed method*/)); }
  @Test void t2674(){ c2(mut, imm, recMdf, of(/*not well formed method*/)); }
  @Test void t2675(){ c2(iso, imm, recMdf, of(/*not well formed method*/)); }
  @Test void t2676(){ c2(mdf, imm, recMdf, of(/*not well formed method*/)); }
  @Test void t2677(){ c2(recMdf,imm,   recMdf, of(/*not well formed method*/)); }

  // ----------------------------- c3 --------------------------------
  static List<Mdf> readAll = of(read,mut  , read,lent  , read,read  , read,recMdf  , read,mdf  , read,imm);
  static List<Mdf> lentAll = of(lent,mut  , lent,lent  , lent,read  , lent,recMdf  , lent,mdf  , lent,imm);
  static List<Mdf> mutAll = of(mut,mut  , mut,lent  , mut,read  , mut,recMdf  , mut,mdf  , mut,imm);
  static List<Mdf> immAll = of(imm,mut  , imm,lent  , imm,read  , imm,recMdf  , imm,mdf  , imm,imm);
  static List<Mdf> mdfImm = of(mdf,imm  , mdf,read);
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3001(){ c3(imm, imm, imm, readAll,immAll,mdfImm); }
  @Test void t3002(){ c3(read, imm, imm, readAll,immAll,mdfImm); }
  @Test void t3003(){ c3(lent, imm, imm, readAll,immAll,mdfImm); }
  @Test void t3004(){ c3(mut, imm, imm, readAll,immAll,mdfImm); }
  @Test void t3005(){ c3(iso, imm, imm, mdfImm,immAll,readAll); }
  @Test void t3006(){ c3(mdf, imm, imm, of()); }
  @Test void t3007(){ c3(recMdf,imm,   imm,   readAll,immAll,mdfImm); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3011(){ c3(imm, read, imm, of()); }
  @Test void t3012(){ c3(read, read, imm, readAll,immAll,mdfImm); }
  @Test void t3013(){ c3(lent, read, imm, readAll,immAll,mdfImm); }
  @Test void t3014(){ c3(mut, read, imm, of()); }//NOT NoMutHyg
  @Test void t3015(){ c3(iso, read, imm, of()); }
  @Test void t3016(){ c3(mdf, read, imm, of()); }
  @Test void t3017(){ c3(recMdf,read,  imm,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3021(){ c3(imm, lent, imm, of()); }
  @Test void t3022(){ c3(read, lent, imm, readAll,immAll,mdfImm); }
  @Test void t3023(){ c3(lent, lent, imm, readAll,immAll,mdfImm); }
  @Test void t3024(){ c3(mut, lent, imm, of()); }//NOT NoMutHyg
  @Test void t3025(){ c3(iso, lent, imm, of()); }
  @Test void t3026(){ c3(mdf, lent, imm, of()); }
  @Test void t3027(){ c3(recMdf,lent,  imm,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3031(){ c3(imm, mut, imm, of()); }
  @Test void t3032(){ c3(read, mut, imm, readAll,immAll,mdfImm); }
  @Test void t3033(){ c3(lent, mut, imm, readAll,immAll,mdfImm); }
  @Test void t3034(){ c3(mut, mut, imm, readAll,immAll,mdfImm); }
  //two rules: imm,imm implies read,imm
  //           imm,imm on imm methods should imply imm,mut using adapt
  @Test void t3035(){ c3(iso, mut, imm, readAll,immAll,mdfImm); }
  @Test void t3036(){ c3(mdf, mut, imm, of()); }
  @Test void t3037(){ c3(recMdf,mut,   imm,  of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3041(){ c3(imm, iso, imm, readAll,immAll,mdfImm); }
  @Test void t3042(){ c3(read, iso, imm, readAll,immAll,mdfImm); }
  @Test void t3043(){ c3(lent, iso, imm, readAll,immAll,mdfImm); }
  @Test void t3044(){ c3(mut, iso, imm, readAll,immAll,mdfImm); }
  @Test void t3045(){ c3(iso, iso, imm, readAll,immAll,mdfImm); }
  @Test void t3046(){ c3(mdf, iso, imm, of()); }
  @Test void t3047(){ c3(recMdf,iso,   imm,   readAll,immAll,mdfImm); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3051(){ c3(imm, mdf, imm, readAll,immAll,mdfImm); }
  @Test void t3052(){ c3(read, mdf, imm, readAll,immAll,mdfImm); }
  @Test void t3053(){ c3(lent, mdf, imm, readAll,immAll,mdfImm); }
  @Test void t3054(){ c3(mut, mdf, imm, of()); }
  @Test void t3055(){ c3(iso, mdf, imm, of()); }
  @Test void t3056(){ c3(mdf, mdf, imm, of()); }
  @Test void t3057(){ c3(recMdf,mdf,   imm, readAll,immAll,mdfImm); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3061(){ c3(imm, recMdf, imm, of()); }
  @Test void t3062(){ c3(read, recMdf, imm, readAll,immAll,mdfImm); }
  @Test void t3063(){ c3(lent, recMdf, imm, readAll,immAll,mdfImm); }
  @Test void t3064(){ c3(mut, recMdf, imm, of()); }
  @Test void t3065(){ c3(iso, recMdf, imm, of()); }
  @Test void t3066(){ c3(mdf, recMdf, imm, of()); }
  @Test void t3067(){ c3(recMdf,recMdf,   imm,   readAll,immAll,mdfImm); }

  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3181(){ c3(imm,   imm, read, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  @Test void t3182(){ c3(read,  imm, read, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  @Test void t3183(){ c3(lent,  imm, read, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  @Test void t3184(){ c3(mut,   imm, read, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  @Test void t3185(){ c3(iso,   imm, read, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  @Test void t3186(){ c3(mdf, imm, read, of()); }
  @Test void t3187(){ c3(recMdf,imm, read, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3101(){ c3(imm, read, read, of()); }
  @Test void t3102(){ c3(read,  read, read, readAll,of(mdf,read, recMdf,read)); }
  @Test void t3103(){ c3(lent,  read, read, readAll,of(mdf,read, recMdf,read)); }
  @Test void t3104(){ c3(mut, read, read, of()); }//NOT NoMutHyg
  @Test void t3105(){ c3(iso, read, read, of()); }
  @Test void t3106(){ c3(mdf, read, read, of()); }
  @Test void t3107(){ c3(recMdf,read,  read,   of()); }
  //                     lambda, captured, method, ...(capturedAs, capturedAsG)
  @Test void t3111(){ c3(imm, lent, read, of()); }
  @Test void t3112(){ c3(read,  lent,  read,   readAll,of(recMdf,read  , recMdf,recMdf  , recMdf,mdf  , mdf,read  , mdf,recMdf)); }
  @Test void t3113(){ c3(lent,  lent,  read,   readAll,of(recMdf,read  , recMdf,recMdf  , recMdf,mdf  , mdf,read  , mdf,recMdf)); }
  @Test void t3114(){ c3(mut, lent, read, of()); }//NOT NoMutHyg
  @Test void t3115(){ c3(iso, lent, read, of()); }
  @Test void t3116(){ c3(mdf, lent, read, of()); }
  @Test void t3117(){ c3(recMdf,lent,  read,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3121(){ c3(imm, mut, read, of()); }
  @Test void t3122(){ c3(read,  mut,   read,   readAll,of(recMdf,read  , recMdf,mdf  , mdf,read, mdf,recMdf, recMdf,recMdf)); }
  @Test void t3123(){ c3(lent,  mut,   read,   readAll,of(recMdf,read  , recMdf,mdf  , mdf,read, mdf,recMdf, recMdf,recMdf)); }
  @Test void t3124(){ c3(mut,   mut,   read,   readAll,of(recMdf,read  , recMdf,mdf  , mdf,read, mdf,recMdf, recMdf,recMdf)); }
  @Test void t3125(){ c3(iso,   mut,   read,   readAll,of(recMdf,read  , recMdf,mdf  , mdf,read, mdf,recMdf, recMdf,recMdf)); }
  @Test void t3126(){ c3(mdf, mut, read, of()); }
  @Test void t3127(){ c3(recMdf,mut,   read,  of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3131(){ c3(imm,   iso, read, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  @Test void t3132(){ c3(read,  iso, read, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  @Test void t3133(){ c3(lent,  iso, read, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  @Test void t3134(){ c3(mut,   iso, read, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  @Test void t3135(){ c3(iso,   iso, read, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  @Test void t3136(){ c3(mdf, iso, read, of()); }
  @Test void t3137(){ c3(recMdf,iso, read, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3141(){ c3(imm,   mdf, read, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  @Test void t3142(){ c3(read,  mdf,   read, readAll,of(recMdf,read  , recMdf,recMdf  , recMdf,mdf  , mdf,read  , mdf,recMdf)); }
  @Test void t3143(){ c3(lent,  mdf,   read, readAll,of(recMdf,read  , recMdf,recMdf  , recMdf,mdf  , mdf,read  , mdf,recMdf)); }
  @Test void t3144(){ c3(mut, mdf, read, of()); }
  @Test void t3145(){ c3(iso, mdf, read, of()); }
  @Test void t3146(){ c3(mdf, mdf, read, of()); }
  @Test void t3147(){ c3(recMdf,mdf,   read, readAll,of(recMdf,read  , recMdf,recMdf  , recMdf,mdf  , mdf,read  , mdf,recMdf)); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3151(){ c3(imm, recMdf, read, of()); }
  @Test void t3152(){ c3(read,  recMdf, read, readAll,of(mdf,read, recMdf,read)); }
  @Test void t3153(){ c3(lent,  recMdf, read, readAll,of(mdf,read, recMdf,read)); }
  @Test void t3154(){ c3(mut, recMdf, read, of()); }
  @Test void t3155(){ c3(iso, recMdf, read, of()); }
  @Test void t3156(){ c3(mdf, recMdf, read, of()); }
  @Test void t3157(){ c3(recMdf,recMdf,   read,   readAll,of(recMdf,read  , recMdf,recMdf  , recMdf,mdf  , mdf,read  , mdf,recMdf)); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3161(){ c3(imm,   imm, read, readAll,immAll,mdfImm,of(recMdf,read  , recMdf,imm)); }
  @Test void t3162(){ c3(read,  imm, read, readAll,immAll,mdfImm,of(recMdf,read  , recMdf,imm)); }
  @Test void t3163(){ c3(lent,  imm, read, readAll,immAll,mdfImm,of(recMdf,read  , recMdf,imm)); } // this is fine because the recMdf is treated as imm
  @Test void t3164(){ c3(mut,   imm, read, readAll,immAll,mdfImm,of(recMdf,read  , recMdf,imm)); }
  @Test void t3165(){ c3(iso,   imm, read, readAll,immAll,mdfImm,of(recMdf,read  , recMdf,imm)); }
  @Test void t3166(){ c3(mdf, imm, read, of()); }
  @Test void t3167(){ c3(recMdf,imm, read, readAll,immAll,mdfImm,of(recMdf,read  , recMdf,imm)); }

  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3201(){ c3(imm, imm, lent, of()); }
  @Test void t3202(){ c3(read, imm, lent, of()); }
  @Test void t3203(){ c3(lent,  imm, lent, readAll,mdfImm,immAll,of(recMdf,read  , recMdf,imm)); }
  @Test void t3204(){ c3(mut,   imm, lent, readAll,mdfImm,immAll,of(recMdf,read  , recMdf,imm)); }
  @Test void t3205(){ c3(iso,   imm, lent, readAll,mdfImm,immAll,of(recMdf,read  , recMdf,imm)); }
  @Test void t3206(){ c3(mdf, imm, lent, of()); }
  @Test void t3207(){ c3(recMdf,imm, lent, readAll,mdfImm,immAll,of(recMdf,read  , recMdf,imm)); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3211(){ c3(imm, read, lent, of()); }
  @Test void t3212(){ c3(read, read, lent, of()); }
  @Test void t3213(){ c3(lent,  read, lent, readAll,of(recMdf,read, mdf,read)); }
  @Test void t3214(){ c3(mut, read, lent, of()); }//NOT NoMutHyg
  @Test void t3215(){ c3(iso, read, lent, of()); }
  @Test void t3216(){ c3(mdf, read, lent, of()); }
  @Test void t3217(){ c3(recMdf,read,  lent,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3221(){ c3(imm, lent, lent, of()); }
  @Test void t3222(){ c3(read, lent, lent, of()); }
  @Test void t3223(){ c3(lent,  lent,  lent,   readAll,lentAll,of(recMdf,lent, recMdf,read, mdf,read, mdf,lent)); }
  @Test void t3224(){ c3(mut, lent, lent, of()); }//NOT NoMutHyg
  @Test void t3225(){ c3(iso, lent, lent, of()); }
  @Test void t3226(){ c3(mdf, lent, lent, of()); }
  @Test void t3227(){ c3(recMdf,lent,  lent,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3231(){ c3(imm, mut, lent, of()); }
  @Test void t3232(){ c3(read, mut, lent, of()); }
  @Test void t3233(){ c3(lent,  mut,   lent,   readAll,lentAll,of(recMdf,read, mdf,read, mdf,lent, recMdf,lent)); }
  @Test void t3234(){ c3(mut,   mut,   lent,   readAll,lentAll,of(recMdf,read, mdf,read, mdf,lent, recMdf,lent)); }
  @Test void t3235(){ c3(iso,   mut,   lent,   readAll,lentAll,of(recMdf,read, mdf,read, mdf,lent, recMdf,lent)); }
  @Test void t3236(){ c3(mdf, mut, lent, of()); }
  @Test void t3237(){ c3(recMdf,mut,   lent,  of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3241(){ c3(imm, iso, lent, of()); }
  @Test void t3242(){ c3(read, iso, lent, of()); }
  @Test void t3243(){ c3(lent,  iso, lent, readAll,mdfImm,immAll,of(recMdf,read, recMdf,imm)); }
  @Test void t3244(){ c3(mut,   iso, lent, readAll,mdfImm,immAll,of(recMdf,read, recMdf,imm)); }
  @Test void t3245(){ c3(iso,   iso, lent, readAll,mdfImm,immAll,of(recMdf,read, recMdf,imm)); }
  @Test void t3246(){ c3(mdf, iso, lent, of()); }
  @Test void t3247(){ c3(recMdf,iso, lent, readAll,mdfImm,immAll,of(recMdf,read, recMdf,imm)); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3251(){ c3(imm, mdf, lent, of()); }
  @Test void t3252(){ c3(read, mdf, lent, of()); }
  @Test void t3253(){ c3(lent,  mdf,   lent, readAll,of(recMdf,read, mdf,read, mdf,mdf)); }
  @Test void t3254(){ c3(mut, mdf, lent, of()); }
  @Test void t3255(){ c3(iso, mdf, lent, of()); }
  @Test void t3256(){ c3(mdf, mdf, lent, of()); }
  @Test void t3257(){ c3(recMdf,mdf,   lent, readAll,of(recMdf,read, mdf,read, mdf,mdf)); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3261(){ c3(imm, recMdf, lent, of()); }
  @Test void t3262(){ c3(read, recMdf, lent, of()); }
  @Test void t3263(){ c3(lent,  recMdf, lent, readAll,of(recMdf,read, mdf,read)); }
  @Test void t3264(){ c3(mut, recMdf, lent, of()); }
  @Test void t3265(){ c3(iso, recMdf, lent, of()); }
  @Test void t3266(){ c3(mdf, recMdf, lent, of()); }
  @Test void t3267(){ c3(recMdf,recMdf,   lent,   readAll,of(recMdf,read, recMdf,mdf, recMdf,recMdf, mdf,read, mdf,recMdf)); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3271(){ c3(imm, imm, lent, of()); }
  @Test void t3272(){ c3(read, imm, lent, of()); }
  @Test void t3273(){ c3(lent,  imm, lent, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  @Test void t3274(){ c3(mut,   imm, lent, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  @Test void t3275(){ c3(iso,   imm, lent, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  @Test void t3276(){ c3(mdf, imm, lent, of()); }
  @Test void t3277(){ c3(recMdf,imm, lent, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }

  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3301(){ c3(imm, imm, mut, of()); }
  @Test void t3302(){ c3(read, imm, mut, of()); }
  @Test void t3303(){ c3(lent, imm, mut, readAll,immAll,mdfImm); }
  @Test void t3304(){ c3(mut, imm, mut, readAll,immAll,mdfImm); }
  @Test void t3305(){ c3(iso, imm, mut, readAll,immAll,mdfImm); }
  @Test void t3306(){ c3(mdf, imm, mut, of()); }
  @Test void t3307(){ c3(recMdf,imm,   mut,   readAll,immAll,mdfImm); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3311(){ c3(imm, read, mut, of()); }
  @Test void t3312(){ c3(read, read, mut, of()); }
  @Test void t3313(){ c3(lent, read, mut, readAll,of(mdf,read)); }
  @Test void t3314(){ c3(mut, read, mut, of()); }//NOT NoMutHyg
  @Test void t3315(){ c3(iso, read, mut, of()); }
  @Test void t3316(){ c3(mdf, read, mut, of()); }
  @Test void t3317(){ c3(recMdf,read,  mut,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3321(){ c3(imm, lent, mut, of()); }
  @Test void t3322(){ c3(read, lent, mut, of()); } // this capture is fine because the method cannot ever be called
  @Test void t3323(){ c3(lent,  lent, mut, readAll,lentAll,of(mdf,read, mdf,lent)); }
  @Test void t3324(){ c3(mut, lent, mut, of()); }//NOT NoMutHyg
  @Test void t3325(){ c3(iso, lent, mut, of()); }
  @Test void t3326(){ c3(mdf, lent, mut, of()); }
  @Test void t3327(){ c3(recMdf,lent,  mut,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3331(){ c3(imm, mut, mut, of()); }
  @Test void t3332(){ c3(read, mut, mut, of()); }
  @Test void t3333(){ c3(lent,  mut, mut, readAll,lentAll,of(mdf,read, mdf,lent)); }
  @Test void t3334(){ c3(mut,   mut,   mut,   readAll,lentAll,of(mdf,read, mdf,lent, mdf,mut, mut,mut  , mut,lent  , mut,read  , mut,mdf  , mut,imm, mut,recMdf)); }
  @Test void t3335(){ c3(iso,   mut,   mut,   readAll,lentAll,of(mdf,read, mdf,lent, mdf,mut, mut,mut  , mut,lent  , mut,read  , mut,mdf  , mut,imm, mut,recMdf)); }
  @Test void t3336(){ c3(mdf, mut, mut, of()); }
  @Test void t3337(){ c3(recMdf,mut,   mut,  of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3341(){ c3(imm, iso, mut, of()); }
  @Test void t3342(){ c3(read, iso, mut, of()); }
  @Test void t3343(){ c3(lent, iso, mut, readAll,immAll,mdfImm); }
  @Test void t3344(){ c3(mut, iso, mut, readAll,immAll,mdfImm); }
  @Test void t3345(){ c3(iso, iso, mut, readAll,immAll,mdfImm); }
  @Test void t3346(){ c3(mdf, iso, mut, of()); }
  @Test void t3347(){ c3(recMdf,iso,   mut,   readAll,immAll,mdfImm); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3351(){ c3(imm, mdf, mut, of()); }
  @Test void t3352(){ c3(read, mdf, mut, of()); }
  @Test void t3353(){ c3(lent,  mdf, mut, readAll,of(mdf,read, mdf,mdf)); }
  @Test void t3354(){ c3(mut, mdf, mut, of()); }
  @Test void t3355(){ c3(iso, mdf, mut, of()); }
  @Test void t3356(){ c3(mdf, mdf, mut, of()); }
  @Test void t3357(){ c3(recMdf,mdf, mut, readAll,of(mdf,read, mdf,mdf)); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3361(){ c3(imm, recMdf, mut, of()); }
  @Test void t3362(){ c3(read, recMdf, mut, of()); }
  @Test void t3363(){ c3(lent, recMdf, mut, readAll,of(mdf,read)); }
  @Test void t3364(){ c3(mut, recMdf, mut, of()); }
  @Test void t3365(){ c3(iso, recMdf, mut, of()); }
  @Test void t3366(){ c3(mdf, recMdf, mut, of()); }
  @Test void t3367(){ c3(recMdf,recMdf,   mut,   readAll,of(mdf,read)); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3371(){ c3(imm, imm, mut, of()); }
  @Test void t3372(){ c3(read, imm, mut, of()); }
  @Test void t3373(){ c3(lent, imm, mut, readAll,immAll,mdfImm,of()); }
  @Test void t3374(){ c3(mut, imm, mut, readAll,immAll,mdfImm,of()); }
  @Test void t3375(){ c3(iso, imm, mut, readAll,immAll,mdfImm,of()); }
  @Test void t3376(){ c3(mdf, imm, mut, of()); }
  @Test void t3377(){ c3(recMdf,imm,   mut,   readAll,immAll,mdfImm,of()); }

  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3401(){ c3(imm, imm, iso, of()); }
  @Test void t3402(){ c3(read, imm, iso, of()); }
  @Test void t3403(){ c3(lent, imm, iso, readAll,immAll,mdfImm,of()); }
  @Test void t3404(){ c3(mut, imm, iso, readAll,immAll,mdfImm,of()); }
  @Test void t3405(){ c3(iso, imm, iso, readAll,immAll,mdfImm,of()); }
  @Test void t3406(){ c3(mdf, imm, iso, of()); }
  @Test void t3407(){ c3(recMdf,imm,   iso,   readAll,immAll,mdfImm,of()); } // yes, recMdf could be iso
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3411(){ c3(imm, read, iso, of()); }
  @Test void t3412(){ c3(read, read, iso, of()); }
  @Test void t3413(){ c3(lent, read, iso, readAll,of(mdf,read)); }
  @Test void t3414(){ c3(mut, read, iso, of()); }//NOT NoMutHyg
  @Test void t3415(){ c3(iso, read, iso, of()); }
  @Test void t3416(){ c3(mdf, read, iso, of()); }
  @Test void t3417(){ c3(recMdf,read,  iso,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3421(){ c3(imm, lent, iso, of()); }
  @Test void t3422(){ c3(read, lent, iso, of()); }
  @Test void t3423(){ c3(lent,  lent, iso, readAll,lentAll,of(mdf,read, mdf,lent)); }
  @Test void t3424(){ c3(mut, lent, iso, of()); }//NOT NoMutHyg
  @Test void t3425(){ c3(iso, lent, iso, of()); }
  @Test void t3426(){ c3(mdf, lent, iso, of()); }
  @Test void t3427(){ c3(recMdf,lent,  iso,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3431(){ c3(imm, mut, iso, of()); }
  @Test void t3432(){ c3(read, mut, iso, of()); }
  @Test void t3433(){ c3(lent,  mut,   iso, readAll,mutAll,lentAll,of(mdf,read, mdf,lent, mdf,mut)); } // These 3 look odd, but it's correct because iso lambdas are treated like mut
  @Test void t3434(){ c3(mut,   mut,   iso, readAll,mutAll,lentAll,of(mdf,read, mdf,lent, mdf,mut)); }
  @Test void t3435(){ c3(iso,   mut,   iso, readAll,mutAll,lentAll,of(mdf,read, mdf,lent, mdf,mut)); }
  @Test void t3436(){ c3(mdf, mut, iso, of()); }
  @Test void t3437(){ c3(recMdf,mut,   iso,  of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3441(){ c3(imm, iso, iso, of()); }
  @Test void t3442(){ c3(read, iso, iso, of()); }
  @Test void t3443(){ c3(lent,  iso, iso, readAll,immAll,of(mdf,read, mdf,imm)); }
  @Test void t3444(){ c3(mut,   iso, iso, readAll,immAll,of(mdf,read, mdf,imm)); }
  @Test void t3445(){ c3(iso,   iso, iso, readAll,immAll,of(mdf,read, mdf,imm)); } // all iso is captured as imm
  @Test void t3446(){ c3(mdf, iso, iso, of()); }
  @Test void t3447(){ c3(recMdf,iso, iso, readAll,immAll,of(mdf,read, mdf,imm)); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3451(){ c3(imm, mdf, iso, of()); }
  @Test void t3452(){ c3(read, mdf, iso, of()); }
  @Test void t3453(){ c3(lent,  mdf, iso, readAll,of(mdf,read, mdf,mdf)); }
  @Test void t3454(){ c3(mut, mdf, iso, of()); }
  @Test void t3455(){ c3(iso, mdf, iso, of()); }
  @Test void t3456(){ c3(mdf, mdf, iso, of()); }
  @Test void t3457(){ c3(recMdf,mdf,   iso, readAll,immAll,of(mdf,read, mdf,mdf, mdf,imm, mdf,read)); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3461(){ c3(imm, recMdf, iso, of()); }
  @Test void t3462(){ c3(read, recMdf, iso, of()); }
  @Test void t3463(){ c3(lent, recMdf, iso, readAll,of(mdf,read)); }
  @Test void t3464(){ c3(mut, recMdf, iso, of()); }
  @Test void t3465(){ c3(iso, recMdf, iso, of()); }
  @Test void t3466(){ c3(mdf, recMdf, iso, of()); }
  @Test void t3467(){ c3(recMdf,recMdf,   iso,   readAll,of(mdf,read)); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3471(){ c3(imm, imm, iso, of()); }
  @Test void t3472(){ c3(read, imm, iso, of()); }
  @Test void t3473(){ c3(lent, imm, iso, readAll,immAll,mdfImm); }
  @Test void t3474(){ c3(mut, imm, iso, readAll,immAll,mdfImm); }
  @Test void t3475(){ c3(iso, imm, iso, readAll,immAll,mdfImm); }
  @Test void t3476(){ c3(mdf, imm, iso, of()); }
  @Test void t3477(){ c3(recMdf,imm,   iso,   readAll,immAll,mdfImm); }

  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3501(){ c3(imm, imm, mdf, of()); }
  @Test void t3502(){ c3(read, imm, mdf, of()); }
  @Test void t3503(){ c3(lent, imm, mdf, of()); }
  @Test void t3504(){ c3(mut, imm, mdf, of()); }
  @Test void t3505(){ c3(iso, imm, mdf, of()); }
  @Test void t3506(){ c3(mdf, imm, mdf, of()); }
  @Test void t3507(){ c3(recMdf,imm,   mdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3511(){ c3(imm, read, mdf, of()); }
  @Test void t3512(){ c3(read, read, mdf, of()); }
  @Test void t3513(){ c3(lent, read, mdf, of()); }
  @Test void t3514(){ c3(mut, read, mdf, of()); }
  @Test void t3515(){ c3(iso, read, mdf, of()); }
  @Test void t3516(){ c3(mdf, read, mdf, of()); }
  @Test void t3517(){ c3(recMdf,read,  mdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3521(){ c3(imm, lent, mdf, of()); }
  @Test void t3522(){ c3(read, lent, mdf, of()); }
  @Test void t3523(){ c3(lent, lent, mdf, of()); }
  @Test void t3524(){ c3(mut, lent, mdf, of()); }
  @Test void t3525(){ c3(iso, lent, mdf, of()); }
  @Test void t3526(){ c3(mdf, lent, mdf, of()); }
  @Test void t3527(){ c3(recMdf,lent,  mdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3531(){ c3(imm, mut, mdf, of()); }
  @Test void t3532(){ c3(read, mut, mdf, of()); }
  @Test void t3533(){ c3(lent, mut, mdf, of()); }
  @Test void t3534(){ c3(mut, mut, mdf, of()); }
  @Test void t3535(){ c3(iso, mut, mdf, of()); }
  @Test void t3536(){ c3(mdf, mut, mdf, of()); }
  @Test void t3537(){ c3(recMdf,mut,   mdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3541(){ c3(imm, iso, mdf, of()); }
  @Test void t3542(){ c3(read, iso, mdf, of()); }
  @Test void t3543(){ c3(lent, iso, mdf, of()); }
  @Test void t3544(){ c3(mut, iso, mdf, of()); }
  @Test void t3545(){ c3(iso, iso, mdf, of()); }
  @Test void t3546(){ c3(mdf, iso, mdf, of()); }
  @Test void t3547(){ c3(recMdf,iso,   mdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3551(){ c3(imm, mdf, mdf, of()); }
  @Test void t3552(){ c3(read, mdf, mdf, of()); }
  @Test void t3553(){ c3(lent, mdf, mdf, of()); }
  @Test void t3554(){ c3(mut, mdf, mdf, of()); }
  @Test void t3555(){ c3(iso, mdf, mdf, of()); }
  @Test void t3556(){ c3(mdf, mdf, mdf, of()); }
  @Test void t3557(){ c3(recMdf,mdf,   mdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3561(){ c3(imm, recMdf, mdf, of()); }
  @Test void t3562(){ c3(read, recMdf, mdf, of()); }
  @Test void t3563(){ c3(lent, recMdf, mdf, of()); }
  @Test void t3564(){ c3(mut, recMdf, mdf, of()); }
  @Test void t3565(){ c3(iso, recMdf, mdf, of()); }
  @Test void t3566(){ c3(mdf, recMdf, mdf, of()); }
  @Test void t3567(){ c3(recMdf,recMdf,   mdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3571(){ c3(imm, imm, mdf, of()); }
  @Test void t3572(){ c3(read, imm, mdf, of()); }
  @Test void t3573(){ c3(lent, imm, mdf, of()); }
  @Test void t3574(){ c3(mut, imm, mdf, of()); }
  @Test void t3575(){ c3(iso, imm, mdf, of()); }
  @Test void t3576(){ c3(mdf, imm, mdf, of()); }
  @Test void t3577(){ c3(recMdf,imm,   mdf, of()); }

  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3601(){ c3(imm, imm, recMdf, of()); }
  @Test void t3602(){ c3(read, imm, recMdf, of()); }
  @Test void t3603(){ c3(lent, imm, recMdf, of()); }
  @Test void t3604(){ c3(mut, imm, recMdf, of()); }
  @Test void t3605(){ c3(iso, imm, recMdf, of()); }
  @Test void t3606(){ c3(mdf, imm, recMdf, of()); }
  @Test void t3607(){ c3(recMdf,imm,   recMdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3611(){ c3(imm, read, recMdf, of()); }
  @Test void t3612(){ c3(read, read, recMdf, of()); }
  @Test void t3613(){ c3(lent, read, recMdf, of()); }
  @Test void t3614(){ c3(mut, read, recMdf, of()); }
  @Test void t3615(){ c3(iso, read, recMdf, of()); }
  @Test void t3616(){ c3(mdf, read, recMdf, of()); }
  @Test void t3617(){ c3(recMdf,read,  recMdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3621(){ c3(imm, lent, recMdf, of()); }
  @Test void t3622(){ c3(read, lent, recMdf, of()); }
  @Test void t3623(){ c3(lent, lent, recMdf, of()); }
  @Test void t3624(){ c3(mut, lent, recMdf, of()); }
  @Test void t3625(){ c3(iso, lent, recMdf, of()); }
  @Test void t3626(){ c3(mdf, lent, recMdf, of()); }
  @Test void t3627(){ c3(recMdf,lent,  recMdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3631(){ c3(imm, mut, recMdf, of()); }
  @Test void t3632(){ c3(read, mut, recMdf, of()); }
  @Test void t3633(){ c3(lent, mut, recMdf, of()); }
  @Test void t3634(){ c3(mut, mut, recMdf, of()); }
  @Test void t3635(){ c3(iso, mut, recMdf, of()); }
  @Test void t3636(){ c3(mdf, mut, recMdf, of()); }
  @Test void t3637(){ c3(recMdf,mut,   recMdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3641(){ c3(imm, iso, recMdf, of()); }
  @Test void t3642(){ c3(read, iso, recMdf, of()); }
  @Test void t3643(){ c3(lent, iso, recMdf, of()); }
  @Test void t3644(){ c3(mut, iso, recMdf, of()); }
  @Test void t3645(){ c3(iso, iso, recMdf, of()); }
  @Test void t3646(){ c3(mdf, iso, recMdf, of()); }
  @Test void t3647(){ c3(recMdf,iso,   recMdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3651(){ c3(imm, mdf, recMdf, of()); }
  @Test void t3652(){ c3(read, mdf, recMdf, of()); }
  @Test void t3653(){ c3(lent, mdf, recMdf, of()); }
  @Test void t3654(){ c3(mut, mdf, recMdf, of()); }
  @Test void t3655(){ c3(iso, mdf, recMdf, of()); }
  @Test void t3656(){ c3(mdf, mdf, recMdf, of()); }
  @Test void t3657(){ c3(recMdf,mdf,   recMdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3661(){ c3(imm, recMdf, recMdf, of()); }
  @Test void t3662(){ c3(read, recMdf, recMdf, of()); }
  @Test void t3663(){ c3(lent, recMdf, recMdf, of()); }
  @Test void t3664(){ c3(mut, recMdf, recMdf, of()); }
  @Test void t3665(){ c3(iso, recMdf, recMdf, of()); }
  @Test void t3666(){ c3(mdf, recMdf, recMdf, of()); }
  @Test void t3667(){ c3(recMdf,recMdf,   recMdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3671(){ c3(imm, imm, recMdf, of()); }
  @Test void t3672(){ c3(read, imm, recMdf, of()); }
  @Test void t3673(){ c3(lent, imm, recMdf, of()); }
  @Test void t3674(){ c3(mut, imm, recMdf, of()); }
  @Test void t3675(){ c3(iso, imm, recMdf, of()); }
  @Test void t3676(){ c3(mdf, imm, recMdf, of()); }
  @Test void t3677(){ c3(recMdf,imm,   recMdf, of()); }

  //-------------------c4------------------
  @Test void t4001(){ c4(imm, imm, imm, readAll,immAll,mdfImm); }
  @Test void t4002(){ c4(read, imm, imm, readAll,immAll,mdfImm); }
  @Test void t4003(){ c4(lent, imm, imm, readAll,immAll,mdfImm); }
  @Test void t4004(){ c4(mut, imm, imm, readAll,immAll,mdfImm); }
  @Test void t4005(){ c4(iso, imm, imm, mdfImm,immAll,readAll); }
  @Test void t4006(){ c4(mdf, imm, imm, of()); }
  @Test void t4007(){ c4(recMdf,imm,   imm,   readAll,immAll,mdfImm); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4011(){ c4(imm, read, imm, of()); }
  @Test void t4012(){ c4(read, read, imm, readAll,immAll,mdfImm); }
  @Test void t4013(){ c4(lent, read, imm, readAll,immAll,mdfImm); }
  // [read,lent  , read,read  , mdf,read  , imm,lent  , imm,read  ]
  /*
B:{}
L[X]:NoMutHyg[mdf X]{ imm .absMeth: read X }
L:L[lent B]
A:{ read .m(par: read B) : mut L[lent B] -> mut L{.absMeth->par} }
   */
  @Test void t4014(){ c4(mut,   read,  imm,   of(read,lent  , read,read  , mdf,read  , imm,lent  , imm,read)); }
  @Test void t4015(){ c4(iso, read, imm, of()); }
  @Test void t4016(){ c4(mdf, read, imm, of()); }
  @Test void t4017(){ c4(recMdf,read,  imm,   of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4021(){ c4(imm, lent, imm, of()); }
  @Test void t4022(){ c4(read, lent, imm, readAll,immAll,mdfImm); }
  @Test void t4023(){ c4(lent, lent, imm, readAll,immAll,mdfImm); }
  @Test void t4024(){ c4(mut,   lent,  imm,   of(read,lent  , read,read  , mdf,read  , imm,lent  , imm,read)); } // ok because to call the imm method everything mus have been promotable
  @Test void t4025(){ c4(iso, lent, imm, of()); }
  @Test void t4026(){ c4(mdf, lent, imm, of()); }
  @Test void t4027(){ c4(recMdf,lent,  imm,   of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4031(){ c4(imm, mut, imm, of()); }
  @Test void t4032(){ c4(read, mut, imm, readAll,immAll,mdfImm); }
  @Test void t4033(){ c4(lent, mut, imm, readAll,immAll,mdfImm); }
  @Test void t4034(){ c4(mut, mut, imm, readAll,immAll,mdfImm); }
  //two rules: imm,imm implies read,imm
  //           imm,imm on imm methods should imply imm,mut using adapt
  @Test void t4035(){ c4(iso, mut, imm, readAll,immAll,mdfImm); }
  @Test void t4036(){ c4(mdf, mut, imm, of()); }
  @Test void t4037(){ c4(recMdf,mut,   imm,  of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4041(){ c4(imm, iso, imm, readAll,immAll,mdfImm); }
  @Test void t4042(){ c4(read, iso, imm, readAll,immAll,mdfImm); }
  @Test void t4043(){ c4(lent, iso, imm, readAll,immAll,mdfImm); }
  @Test void t4044(){ c4(mut, iso, imm, readAll,immAll,mdfImm); }
  @Test void t4045(){ c4(iso, iso, imm, readAll,immAll,mdfImm); }
  @Test void t4046(){ c4(mdf, iso, imm, of()); }
  @Test void t4047(){ c4(recMdf,iso,   imm,   readAll,immAll,mdfImm); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4051(){ c4(imm, mdf, imm, readAll,immAll,mdfImm); }
  @Test void t4052(){ c4(read, mdf, imm, readAll,immAll,mdfImm); }
  @Test void t4053(){ c4(lent, mdf, imm, readAll,immAll,mdfImm); }
  @Test void t4054(){ c4(mut,   mdf,   imm, of(read,lent  , read,read  , read,mdf  , mdf,read  , imm,lent  , imm,read  , imm,mdf)); }
  @Test void t4055(){ c4(iso,   mdf, imm, of(read,mdf, imm,mdf)); }
  @Test void t4056(){ c4(mdf, mdf, imm, of()); }
  @Test void t4057(){ c4(recMdf,mdf,   imm, readAll,immAll,mdfImm); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4061(){ c4(imm, recMdf, imm, of()); }
  @Test void t4062(){ c4(read, recMdf, imm, readAll,immAll,mdfImm); }
  @Test void t4063(){ c4(lent, recMdf, imm, readAll,immAll,mdfImm); }
  @Test void t4064(){ c4(mut,   recMdf,   imm,  of(read,lent  , read,read  , read,recMdf  , mdf,read  , imm,lent  , imm,read  , imm,recMdf)); }
  @Test void t4065(){ c4(iso,   recMdf, imm, of(read,recMdf  , imm,recMdf)); }
  @Test void t4066(){ c4(mdf, recMdf, imm, of()); }
  @Test void t4067(){ c4(recMdf,recMdf,   imm,   readAll,immAll,mdfImm); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t4181(){ c4(imm,   imm, read, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  @Test void t4182(){ c4(read,  imm, read, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  @Test void t4183(){ c4(lent,  imm, read, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  @Test void t4184(){ c4(mut,   imm, read, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  @Test void t4185(){ c4(iso,   imm, read, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  @Test void t4186(){ c4(mdf, imm, read, of()); }
  @Test void t4187(){ c4(recMdf,imm, read, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4101(){ c4(imm, read, read, of()); }
  @Test void t4102(){ c4(read,  read, read, readAll,of(mdf,read, recMdf,read)); }
  @Test void t4103(){ c4(lent,  read, read, readAll,of(mdf,read, recMdf,read)); }
  @Test void t4104(){ c4(mut,   read,  read,   of(read,lent  , read,read  , recMdf,read  , mdf,read)); }
  @Test void t4105(){ c4(iso, read, read, of()); }
  @Test void t4106(){ c4(mdf, read, read, of()); }
  @Test void t4107(){ c4(recMdf,read,  read,   of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4111(){ c4(imm, lent, read, of()); }
  @Test void t4112(){ c4(read,  lent,  read,   readAll,of(recMdf,read  , recMdf,recMdf  , recMdf,mdf  , mdf,read  , mdf,recMdf)); }
  @Test void t4113(){ c4(lent,  lent,  read,   readAll,of(recMdf,read  , recMdf,recMdf  , recMdf,mdf  , mdf,read  , mdf,recMdf)); }
  @Test void t4114(){ c4(mut,   lent,  read,   of(read,lent  , read,read  , recMdf,read  , mdf,read)); }
  @Test void t4115(){ c4(iso, lent, read, of()); }
  @Test void t4116(){ c4(mdf, lent, read, of()); }
  @Test void t4117(){ c4(recMdf,lent,  read,   of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4121(){ c4(imm, mut, read, of()); }
  @Test void t4122(){ c4(read,  mut,   read,   readAll,of(recMdf,read  , recMdf,recMdf  , recMdf,mdf  , mdf,read  , mdf,recMdf)); }
  @Test void t4123(){ c4(lent,  mut,   read,   readAll,of(recMdf,read  , recMdf,recMdf  , recMdf,mdf  , mdf,read  , mdf,recMdf)); }
  @Test void t4124(){ c4(mut,   mut,   read,   readAll,of(recMdf,read  , recMdf,recMdf  , recMdf,mdf  , mdf,read  , mdf,recMdf)); }
  @Test void t4125(){ c4(iso,   mut,   read,   readAll,of(recMdf,read  , recMdf,recMdf  , recMdf,mdf  , mdf,read  , mdf,recMdf)); }
  @Test void t4126(){ c4(mdf, mut, read, of()); }
  @Test void t4127(){ c4(recMdf,mut,   read,  of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4131(){ c4(imm,   iso, read, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  @Test void t4132(){ c4(read,  iso, read, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  @Test void t4133(){ c4(lent,  iso, read, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  @Test void t4134(){ c4(mut,   iso, read, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  @Test void t4135(){ c4(iso,   iso, read, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  @Test void t4136(){ c4(mdf, iso, read, of()); }
  @Test void t4137(){ c4(recMdf,iso, read, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4141(){ c4(imm,   mdf, read, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  @Test void t4142(){ c4(read,  mdf,   read, readAll,of(recMdf,read  , recMdf,recMdf  , recMdf,mdf  , mdf,read  , mdf,recMdf)); }
  @Test void t4143(){ c4(lent,  mdf,   read, readAll,of(recMdf,read  , recMdf,recMdf  , recMdf,mdf  , mdf,read  , mdf,recMdf)); }
  @Test void t4144(){ c4(mut,   mdf,   read, of(read,lent  , read,read  , read,mdf  , recMdf,read  , recMdf,mdf  , mdf,read)); }
  @Test void t4145(){ c4(iso,   mdf, read, of(read,mdf  , recMdf,mdf)); }
  @Test void t4146(){ c4(mdf, mdf, read, of()); }
  @Test void t4147(){ c4(recMdf,mdf,   read, readAll,of(recMdf,read  , recMdf,recMdf  , recMdf,mdf  , mdf,read  , mdf,recMdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4151(){ c4(imm, recMdf, read, of()); }
  @Test void t4152(){ c4(read,  recMdf, read, readAll,of(mdf,read, recMdf,read)); }
  @Test void t4153(){ c4(lent,  recMdf, read, readAll,of(mdf,read, recMdf,read)); }
  @Test void t4154(){ c4(mut,   recMdf,   read,  of(read,lent  , read,read  , read,recMdf  , recMdf,read  , mdf,read)); }
  @Test void t4155(){ c4(iso, recMdf, read, of(read)); }
  @Test void t4156(){ c4(mdf, recMdf, read, of()); }
  @Test void t4157(){ c4(recMdf,recMdf,   read,   readAll,of(recMdf,read  , recMdf,recMdf  , recMdf,mdf  , mdf,read  , mdf,recMdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4161(){ c4(imm,   imm, read, readAll,immAll,mdfImm,of(recMdf,read  , recMdf,imm)); }
  @Test void t4162(){ c4(read,  imm, read, readAll,immAll,mdfImm,of(recMdf,read  , recMdf,imm)); }
  @Test void t4163(){ c4(lent,  imm, read, readAll,immAll,mdfImm,of(recMdf,read  , recMdf,imm)); } // this is fine because the recMdf is treated as imm
  @Test void t4164(){ c4(mut,   imm, read, readAll,immAll,mdfImm,of(recMdf,read  , recMdf,imm)); }
  @Test void t4165(){ c4(iso,   imm, read, readAll,immAll,mdfImm,of(recMdf,read  , recMdf,imm)); }
  @Test void t4166(){ c4(mdf, imm, read, of()); }
  @Test void t4167(){ c4(recMdf,imm, read, readAll,immAll,mdfImm,of(recMdf,read  , recMdf,imm)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t4201(){ c4(imm, imm, lent, of()); }
  @Test void t4202(){ c4(read, imm, lent, of()); }
  @Test void t4203(){ c4(lent,  imm, lent, readAll,mdfImm,immAll,of(recMdf,read  , recMdf,imm)); }
  @Test void t4204(){ c4(mut,   imm, lent, readAll,mdfImm,immAll,of(recMdf,read  , recMdf,imm)); }
  @Test void t4205(){ c4(iso,   imm, lent, readAll,mdfImm,immAll,of(recMdf,read  , recMdf,imm)); }
  @Test void t4206(){ c4(mdf, imm, lent, of()); }
  @Test void t4207(){ c4(recMdf,imm, lent, readAll,mdfImm,immAll,of(recMdf,read  , recMdf,imm)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4211(){ c4(imm, read, lent, of()); }
  @Test void t4212(){ c4(read, read, lent, of()); }
  @Test void t4213(){ c4(lent,  read, lent, readAll,of(recMdf,read, mdf,read)); }
  @Test void t4214(){ c4(mut,   read,  lent,   of(read,lent  , read,read  , recMdf,read  , mdf,read)); }
  @Test void t4215(){ c4(iso, read, lent, of()); }
  @Test void t4216(){ c4(mdf, read, lent, of()); }
  @Test void t4217(){ c4(recMdf,read,  lent,   of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4221(){ c4(imm, lent, lent, of()); }
  @Test void t4222(){ c4(read, lent, lent, of()); }
  @Test void t4223(){ c4(lent,  lent,  lent,   readAll,lentAll,of(recMdf,lent  , recMdf,read  , mdf,lent  , mdf,read)); }
  @Test void t4224(){ c4(mut,   lent,  lent,   of(lent,lent  , lent,read  , read,lent  , read,read  , recMdf,lent  , recMdf,read  , mdf,lent  , mdf,read)); }
  @Test void t4225(){ c4(iso, lent, lent, of()); }
  @Test void t4226(){ c4(mdf, lent, lent, of()); }
  @Test void t4227(){ c4(recMdf,lent,  lent,   of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4231(){ c4(imm, mut, lent, of()); }
  @Test void t4232(){ c4(read, mut, lent, of()); }
  @Test void t4233(){ c4(lent,  mut,   lent,   readAll,lentAll,of(recMdf,lent  , recMdf,read  , mdf,lent  , mdf,read)); }
  @Test void t4234(){ c4(mut,   mut,   lent,   readAll,lentAll,of(recMdf,lent  , recMdf,read  , mdf,lent  , mdf,read)); }
  @Test void t4235(){ c4(iso,   mut,   lent,   readAll,lentAll,of(recMdf,lent  , recMdf,read  , mdf,lent  , mdf,read)); }
  @Test void t4236(){ c4(mdf, mut, lent, of()); }
  @Test void t4237(){ c4(recMdf,mut,   lent,  of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4241(){ c4(imm, iso, lent, of()); }
  @Test void t4242(){ c4(read, iso, lent, of()); }
  @Test void t4243(){ c4(lent,  iso, lent, readAll,mdfImm,immAll,of(recMdf,read, recMdf,imm)); }
  @Test void t4244(){ c4(mut,   iso, lent, readAll,mdfImm,immAll,of(recMdf,read, recMdf,imm)); }
  @Test void t4245(){ c4(iso,   iso, lent, readAll,mdfImm,immAll,of(recMdf,read, recMdf,imm)); }
  @Test void t4246(){ c4(mdf, iso, lent, of()); }
  @Test void t4247(){ c4(recMdf,iso, lent, readAll,mdfImm,immAll,of(recMdf,read, recMdf,imm)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4251(){ c4(imm, mdf, lent, of()); }
  @Test void t4252(){ c4(read, mdf, lent, of()); }
  @Test void t4253(){ c4(lent,  mdf,   lent, readAll,of(recMdf,read, mdf,read, mdf,mdf)); }
  @Test void t4254(){ c4(mut,   mdf,   lent, of(read,lent  , read,read  , read,mdf  , recMdf,read  , mdf,read  , mdf,mdf)); }
  @Test void t4255(){ c4(iso,   mdf, lent, of(read,mdf  , mdf,mdf)); }
  @Test void t4256(){ c4(mdf, mdf, lent, of()); }
  @Test void t4257(){ c4(recMdf,mdf,   lent, readAll,of(recMdf,read  , mdf,read  , mdf,mdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4261(){ c4(imm, recMdf, lent, of()); }
  @Test void t4262(){ c4(read, recMdf, lent, of()); }
  @Test void t4263(){ c4(lent,  recMdf, lent, readAll,of(recMdf,read, mdf,read)); }
  @Test void t4264(){ c4(mut,   recMdf,   lent,  of(read,lent  , read,read  , read,recMdf  , recMdf,read  , mdf,read)); }
  @Test void t4265(){ c4(iso, recMdf, lent, of(read)); }
  @Test void t4266(){ c4(mdf, recMdf, lent, of()); }
  @Test void t4267(){ c4(recMdf,recMdf,   lent,   readAll,of(recMdf,read  , recMdf,recMdf  , recMdf,mdf  , mdf,read  , mdf,recMdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4271(){ c4(imm, imm, lent, of()); }
  @Test void t4272(){ c4(read, imm, lent, of()); }
  @Test void t4273(){ c4(lent,  imm, lent, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  @Test void t4274(){ c4(mut,   imm, lent, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  @Test void t4275(){ c4(iso,   imm, lent, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }
  @Test void t4276(){ c4(mdf, imm, lent, of()); }
  @Test void t4277(){ c4(recMdf,imm, lent, readAll,immAll,mdfImm,of(recMdf,read, recMdf,imm)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t4301(){ c4(imm, imm, mut, of()); }
  @Test void t4302(){ c4(read, imm, mut, of()); }
  @Test void t4303(){ c4(lent, imm, mut, readAll,immAll,mdfImm); }
  @Test void t4304(){ c4(mut, imm, mut, readAll,immAll,mdfImm); }
  @Test void t4305(){ c4(iso, imm, mut, readAll,immAll,mdfImm); }
  @Test void t4306(){ c4(mdf, imm, mut, of()); }
  @Test void t4307(){ c4(recMdf,imm,   mut,   readAll,immAll,mdfImm); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4311(){ c4(imm, read, mut, of()); }
  @Test void t4312(){ c4(read, read, mut, of()); }
  @Test void t4313(){ c4(lent, read, mut, readAll,of(mdf,read)); }
  @Test void t4314(){ c4(mut,   read,  mut, of(read,lent  , read,read  , mdf,read)); }
  @Test void t4315(){ c4(iso, read, mut, of()); }
  @Test void t4316(){ c4(mdf, read, mut, of()); }
  @Test void t4317(){ c4(recMdf,read,  mut,   of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4321(){ c4(imm, lent, mut, of()); }
  @Test void t4322(){ c4(read, lent, mut, of()); } // this capture is fine because the method cannot ever be called
  @Test void t4323(){ c4(lent,  lent, mut, readAll,lentAll,of(mdf,lent  , mdf,read)); }
  @Test void t4324(){ c4(mut,   lent,  mut,   of(lent,lent  , lent,read  , read,lent  , read,read  , mdf,lent  , mdf,read)); }
  @Test void t4325(){ c4(iso, lent, mut, of()); }
  @Test void t4326(){ c4(mdf, lent, mut, of()); }
  @Test void t4327(){ c4(recMdf,lent,  mut,   of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4331(){ c4(imm, mut, mut, of()); }
  @Test void t4332(){ c4(read, mut, mut, of()); }
  @Test void t4333(){ c4(lent,  mut, mut, readAll,lentAll,of(mdf,read, mdf,lent)); }
  @Test void t4334(){ c4(mut,   mut,   mut,   readAll,lentAll,of(mut,mut  , mut,recMdf  , mut,mdf  , mut,imm , mdf,mut  , mdf,lent  , mdf,read)); }
  @Test void t4335(){ c4(iso,   mut,   mut,   readAll,lentAll,of(mut,mut  , mut,lent  , mut,read  , mut,recMdf  , mut,mdf  , mut,imm , mdf,mut  , mdf,lent  , mdf,read)); }
  @Test void t4336(){ c4(mdf, mut, mut, of()); }
  @Test void t4337(){ c4(recMdf,mut,   mut,  of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4341(){ c4(imm, iso, mut, of()); }
  @Test void t4342(){ c4(read, iso, mut, of()); }
  @Test void t4343(){ c4(lent, iso, mut, readAll,immAll,mdfImm); }
  @Test void t4344(){ c4(mut, iso, mut, readAll,immAll,mdfImm); }
  @Test void t4345(){ c4(iso, iso, mut, readAll,immAll,mdfImm); }
  @Test void t4346(){ c4(mdf, iso, mut, of()); }
  @Test void t4347(){ c4(recMdf,iso,   mut,   readAll,immAll,mdfImm); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4351(){ c4(imm, mdf, mut, of()); }
  @Test void t4352(){ c4(read, mdf, mut, of()); }
  @Test void t4353(){ c4(lent,  mdf, mut, readAll,of(mdf,read, mdf,mdf)); }
  @Test void t4354(){ c4(mut,   mdf,   mut, of(read,lent  , read,read  , read,mdf  , mdf,read  , mdf,mdf)); }
  @Test void t4355(){ c4(iso,   mdf,   mut, of(read,mdf  , mdf,mdf , imm,mdf)); }
  @Test void t4356(){ c4(mdf, mdf, mut, of()); }
  @Test void t4357(){ c4(recMdf,mdf, mut, readAll,of(mdf,read  , mdf,mdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4361(){ c4(imm, recMdf, mut, of()); }
  @Test void t4362(){ c4(read, recMdf, mut, of()); }
  @Test void t4363(){ c4(lent, recMdf, mut, readAll,of(mdf,read)); }
  @Test void t4364(){ c4(mut,   recMdf,   mut,  of(read,lent  , read,read  , read,recMdf  , mdf,read)); } // TODO: Should I be able to use recMdf in the type sig here???
  @Test void t4365(){ c4(iso, recMdf, mut, of(read)); }
  @Test void t4366(){ c4(mdf, recMdf, mut, of()); }
  @Test void t4367(){ c4(recMdf,recMdf,   mut,   readAll,of(mdf,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4371(){ c4(imm, imm, mut, of()); }
  @Test void t4372(){ c4(read, imm, mut, of()); }
  @Test void t4373(){ c4(lent, imm, mut, readAll,immAll,mdfImm,of()); }
  @Test void t4374(){ c4(mut, imm, mut, readAll,immAll,mdfImm,of()); }
  @Test void t4375(){ c4(iso, imm, mut, readAll,immAll,mdfImm,of()); }
  @Test void t4376(){ c4(mdf, imm, mut, of()); }
  @Test void t4377(){ c4(recMdf,imm,   mut,   readAll,immAll,mdfImm,of()); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t4401(){ c4(imm, imm, iso, of()); }
  @Test void t4402(){ c4(read, imm, iso, of()); }
  @Test void t4403(){ c4(lent, imm, iso, readAll,immAll,mdfImm,of()); }
  @Test void t4404(){ c4(mut, imm, iso, readAll,immAll,mdfImm,of()); }
  @Test void t4405(){ c4(iso, imm, iso, readAll,immAll,mdfImm,of()); }
  @Test void t4406(){ c4(mdf, imm, iso, of()); }
  @Test void t4407(){ c4(recMdf,imm,   iso,   readAll,immAll,mdfImm,of()); } // yes, recMdf could be iso
  //                     lambda, captured, method, ...capturedAs
  @Test void t4411(){ c4(imm, read, iso, of()); }
  @Test void t4412(){ c4(read, read, iso, of()); }
  @Test void t4413(){ c4(lent, read, iso, readAll,of(mdf,read)); }
  @Test void t4414(){ c4(mut,   read,  iso, of(read,lent  , read,read  , mdf,read)); }
  @Test void t4415(){ c4(iso, read, iso, of()); }
  @Test void t4416(){ c4(mdf, read, iso, of()); }
  @Test void t4417(){ c4(recMdf,read,  iso,   of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4421(){ c4(imm, lent, iso, of()); }
  @Test void t4422(){ c4(read, lent, iso, of()); }
  @Test void t4423(){ c4(lent,  lent, iso, readAll,lentAll,of(mdf,read, mdf,lent)); }
  @Test void t4424(){ c4(mut,   lent,  iso,   of(lent,lent  , lent,read  , read,lent  , read,read  , mdf,lent  , mdf,read)); } // ok because promotion before call
  @Test void t4425(){ c4(iso, lent, iso, of()); }
  @Test void t4426(){ c4(mdf, lent, iso, of()); }
  @Test void t4427(){ c4(recMdf,lent,  iso,   of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4431(){ c4(imm, mut, iso, of()); }
  @Test void t4432(){ c4(read, mut, iso, of()); }
  @Test void t4433(){ c4(lent,  mut,   iso, readAll,mutAll,lentAll,of(mdf,read, mdf,lent, mdf,mut)); } // These 3 look odd, but it's correct because iso lambdas are treated like mut
  @Test void t4434(){ c4(mut,   mut,   iso, readAll,mutAll,lentAll,of(mdf,read, mdf,lent, mdf,mut)); }
  @Test void t4435(){ c4(iso,   mut,   iso, readAll,mutAll,lentAll,of(mdf,read, mdf,lent, mdf,mut)); }
  @Test void t4436(){ c4(mdf, mut, iso, of()); }
  @Test void t4437(){ c4(recMdf,mut,   iso,  of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4441(){ c4(imm, iso, iso, of()); }
  @Test void t4442(){ c4(read, iso, iso, of()); }
  @Test void t4443(){ c4(lent,  iso, iso, readAll,immAll,of(mdf,read, mdf,imm)); }
  @Test void t4444(){ c4(mut,   iso, iso, readAll,immAll,of(mdf,read, mdf,imm)); }
  @Test void t4445(){ c4(iso,   iso, iso, readAll,immAll,of(mdf,read, mdf,imm)); } // all iso is captured as imm
  @Test void t4446(){ c4(mdf, iso, iso, of()); }
  @Test void t4447(){ c4(recMdf,iso, iso, readAll,immAll,of(mdf,read, mdf,imm)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4451(){ c4(imm, mdf, iso, of()); }
  @Test void t4452(){ c4(read, mdf, iso, of()); }
  @Test void t4453(){ c4(lent,  mdf, iso, readAll,of(mdf,read  , mdf,mdf)); }
  @Test void t4454(){ c4(mut,   mdf,   iso, of(read,lent  , read,read  , read,mdf  , mdf,read  , mdf,mdf)); }
  @Test void t4455(){ c4(iso,   mdf,   iso, of(read,mdf  , mdf,mdf , imm,mdf)); }
  @Test void t4456(){ c4(mdf, mdf, iso, of()); }
  @Test void t4457(){ c4(recMdf,mdf,   iso, readAll,immAll,of(mdf,read  , mdf,mdf  , mdf,imm)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4461(){ c4(imm, recMdf, iso, of()); }
  @Test void t4462(){ c4(read, recMdf, iso, of()); }
  @Test void t4463(){ c4(lent, recMdf, iso, readAll,of(mdf,read)); }
  @Test void t4464(){ c4(mut,   recMdf,   iso,  of(read,lent  , read,read  , read,recMdf  , mdf,read)); }
  @Test void t4465(){ c4(iso, recMdf, iso, of(read)); }
  @Test void t4466(){ c4(mdf, recMdf, iso, of()); }
  @Test void t4467(){ c4(recMdf,recMdf,   iso,   readAll,of(mdf,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4471(){ c4(imm, imm, iso, of()); }
  @Test void t4472(){ c4(read, imm, iso, of()); }
  @Test void t4473(){ c4(lent, imm, iso, readAll,immAll,mdfImm); }
  @Test void t4474(){ c4(mut, imm, iso, readAll,immAll,mdfImm); }
  @Test void t4475(){ c4(iso, imm, iso, readAll,immAll,mdfImm); }
  @Test void t4476(){ c4(mdf, imm, iso, of()); }
  @Test void t4477(){ c4(recMdf,imm,   iso,   readAll,immAll,mdfImm); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t4501(){ c4(imm, imm, mdf, of()); }
  @Test void t4502(){ c4(read, imm, mdf, of()); }
  @Test void t4503(){ c4(lent, imm, mdf, of()); }
  @Test void t4504(){ c4(mut, imm, mdf, of()); }
  @Test void t4505(){ c4(iso, imm, mdf, of()); }
  @Test void t4506(){ c4(mdf, imm, mdf, of()); }
  @Test void t4507(){ c4(recMdf,imm,   mdf, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4511(){ c4(imm, read, mdf, of()); }
  @Test void t4512(){ c4(read, read, mdf, of()); }
  @Test void t4513(){ c4(lent, read, mdf, of()); }
  @Test void t4514(){ c4(mut, read, mdf, of()); }
  @Test void t4515(){ c4(iso, read, mdf, of()); }
  @Test void t4516(){ c4(mdf, read, mdf, of()); }
  @Test void t4517(){ c4(recMdf,read,  mdf, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4521(){ c4(imm, lent, mdf, of()); }
  @Test void t4522(){ c4(read, lent, mdf, of()); }
  @Test void t4523(){ c4(lent, lent, mdf, of()); }
  @Test void t4524(){ c4(mut, lent, mdf, of()); }
  @Test void t4525(){ c4(iso, lent, mdf, of()); }
  @Test void t4526(){ c4(mdf, lent, mdf, of()); }
  @Test void t4527(){ c4(recMdf,lent,  mdf, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4531(){ c4(imm, mut, mdf, of()); }
  @Test void t4532(){ c4(read, mut, mdf, of()); }
  @Test void t4533(){ c4(lent, mut, mdf, of()); }
  @Test void t4534(){ c4(mut, mut, mdf, of()); }
  @Test void t4535(){ c4(iso, mut, mdf, of()); }
  @Test void t4536(){ c4(mdf, mut, mdf, of()); }
  @Test void t4537(){ c4(recMdf,mut,   mdf, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4541(){ c4(imm, iso, mdf, of()); }
  @Test void t4542(){ c4(read, iso, mdf, of()); }
  @Test void t4543(){ c4(lent, iso, mdf, of()); }
  @Test void t4544(){ c4(mut, iso, mdf, of()); }
  @Test void t4545(){ c4(iso, iso, mdf, of()); }
  @Test void t4546(){ c4(mdf, iso, mdf, of()); }
  @Test void t4547(){ c4(recMdf,iso,   mdf, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4551(){ c4(imm, mdf, mdf, of()); }
  @Test void t4552(){ c4(read, mdf, mdf, of()); }
  @Test void t4553(){ c4(lent, mdf, mdf, of()); }
  @Test void t4554(){ c4(mut, mdf, mdf, of()); }
  @Test void t4555(){ c4(iso, mdf, mdf, of()); }
  @Test void t4556(){ c4(mdf, mdf, mdf, of()); }
  @Test void t4557(){ c4(recMdf,mdf,   mdf, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4561(){ c4(imm, recMdf, mdf, of()); }
  @Test void t4562(){ c4(read, recMdf, mdf, of()); }
  @Test void t4563(){ c4(lent, recMdf, mdf, of()); }
  @Test void t4564(){ c4(mut, recMdf, mdf, of()); }
  @Test void t4565(){ c4(iso, recMdf, mdf, of()); }
  @Test void t4566(){ c4(mdf, recMdf, mdf, of()); }
  @Test void t4567(){ c4(recMdf,recMdf,   mdf, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4571(){ c4(imm, imm, mdf, of()); }
  @Test void t4572(){ c4(read, imm, mdf, of()); }
  @Test void t4573(){ c4(lent, imm, mdf, of()); }
  @Test void t4574(){ c4(mut, imm, mdf, of()); }
  @Test void t4575(){ c4(iso, imm, mdf, of()); }
  @Test void t4576(){ c4(mdf, imm, mdf, of()); }
  @Test void t4577(){ c4(recMdf,imm,   mdf, of()); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t4601(){ c4(imm, imm, recMdf, of()); }
  @Test void t4602(){ c4(read, imm, recMdf, of()); }
  @Test void t4603(){ c4(lent, imm, recMdf, of()); }
  @Test void t4604(){ c4(mut, imm, recMdf, of()); }
  @Test void t4605(){ c4(iso, imm, recMdf, of()); }
  @Test void t4606(){ c4(mdf, imm, recMdf, of()); }
  @Test void t4607(){ c4(recMdf,imm,   recMdf, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4611(){ c4(imm, read, recMdf, of()); }
  @Test void t4612(){ c4(read, read, recMdf, of()); }
  @Test void t4613(){ c4(lent, read, recMdf, of()); }
  @Test void t4614(){ c4(mut, read, recMdf, of()); }
  @Test void t4615(){ c4(iso, read, recMdf, of()); }
  @Test void t4616(){ c4(mdf, read, recMdf, of()); }
  @Test void t4617(){ c4(recMdf,read,  recMdf, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4621(){ c4(imm, lent, recMdf, of()); }
  @Test void t4622(){ c4(read, lent, recMdf, of()); }
  @Test void t4623(){ c4(lent, lent, recMdf, of()); }
  @Test void t4624(){ c4(mut, lent, recMdf, of()); }
  @Test void t4625(){ c4(iso, lent, recMdf, of()); }
  @Test void t4626(){ c4(mdf, lent, recMdf, of()); }
  @Test void t4627(){ c4(recMdf,lent,  recMdf, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4631(){ c4(imm, mut, recMdf, of()); }
  @Test void t4632(){ c4(read, mut, recMdf, of()); }
  @Test void t4633(){ c4(lent, mut, recMdf, of()); }
  @Test void t4634(){ c4(mut, mut, recMdf, of()); }
  @Test void t4635(){ c4(iso, mut, recMdf, of()); }
  @Test void t4636(){ c4(mdf, mut, recMdf, of()); }
  @Test void t4637(){ c4(recMdf,mut,   recMdf, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4641(){ c4(imm, iso, recMdf, of()); }
  @Test void t4642(){ c4(read, iso, recMdf, of()); }
  @Test void t4643(){ c4(lent, iso, recMdf, of()); }
  @Test void t4644(){ c4(mut, iso, recMdf, of()); }
  @Test void t4645(){ c4(iso, iso, recMdf, of()); }
  @Test void t4646(){ c4(mdf, iso, recMdf, of()); }
  @Test void t4647(){ c4(recMdf,iso,   recMdf, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4651(){ c4(imm, mdf, recMdf, of()); }
  @Test void t4652(){ c4(read, mdf, recMdf, of()); }
  @Test void t4653(){ c4(lent, mdf, recMdf, of()); }
  @Test void t4654(){ c4(mut, mdf, recMdf, of()); }
  @Test void t4655(){ c4(iso, mdf, recMdf, of()); }
  @Test void t4656(){ c4(mdf, mdf, recMdf, of()); }
  @Test void t4657(){ c4(recMdf,mdf,   recMdf, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4661(){ c4(imm, recMdf, recMdf, of()); }
  @Test void t4662(){ c4(read, recMdf, recMdf, of()); }
  @Test void t4663(){ c4(lent, recMdf, recMdf, of()); }
  @Test void t4664(){ c4(mut, recMdf, recMdf, of()); }
  @Test void t4665(){ c4(iso, recMdf, recMdf, of()); }
  @Test void t4666(){ c4(mdf, recMdf, recMdf, of()); }
  @Test void t4667(){ c4(recMdf,recMdf,   recMdf, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t4671(){ c4(imm, imm, recMdf, of()); }
  @Test void t4672(){ c4(read, imm, recMdf, of()); }
  @Test void t4673(){ c4(lent, imm, recMdf, of()); }
  @Test void t4674(){ c4(mut, imm, recMdf, of()); }
  @Test void t4675(){ c4(iso, imm, recMdf, of()); }
  @Test void t4676(){ c4(mdf, imm, recMdf, of()); }
  @Test void t4677(){ c4(recMdf,imm,   recMdf, of()); }
}
//a mut lambda could capture a mut as iso inside an iso method?

//write counterexample

/*
//NO-a mut lambda could capture a lent as iso inside an iso method?
A:{
  read .foo(par: lent Break): mut BreakBox -> { par }
  }
BreakBox:{
  iso .release: iso Break // would this be allowed?
  }
//No since A.foo(myLent) can be promoted to iso
//NO-a mut lambda could capture a read as imm inside an iso/imm method
similar, the A.foo(myRead) can be promoted to iso
 */


/*
For the Generix X capture version:
@Test void t051(){ c(imm,   mdf,   imm); }
@Test void t052(){ c(read,  mdf,   imm,   of(imm,read)); }
@Test void t053(){ c(lent,  mdf,   imm,   of(imm,read)); }
@Test void t054(){ c(mut,   mdf,   imm); }//NOT NoMutHyg
@Test void t055(){ c(iso,   mdf,   imm); }//NOT NoMutHyg
@Test void t056(){ c(mdf,   mdf,   imm); }
@Test void t057(){ c(recMdf,mdf,   imm); }
 */
