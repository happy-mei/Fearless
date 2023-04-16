package program.typesystem;

import id.Mdf;
import net.jqwik.api.Example;
import utils.Bug;

import java.util.List;
import java.util.stream.Stream;

import static id.Mdf.*;
import static id.Mdf.mdf;
import static java.util.List.of;
import static program.typesystem.RunTypeSystem.expectFail;
import static program.typesystem.RunTypeSystem.ok;

public class TestCaptureRules {
  void c1(Mdf lambda, Mdf captured, Mdf method, List<Mdf> capturedAs) {
    capturedAs.forEach(mdf->cInnerOk(codeGen1.formatted(method, mdf, captured, lambda, lambda)));
    Stream.of(Mdf.values()).filter(mdf->!capturedAs.contains(mdf))
      .forEach(mdf->cInnerFail(codeGen1.formatted(method, mdf, captured, lambda, lambda)));
  }
  void c2(Mdf lambda,Mdf captured,Mdf method, List<Mdf> capturedAs) {
    capturedAs.forEach(mdf->cInnerOk(codeGen2.formatted(method, mdf, captured, lambda, captured, lambda, captured)));
    Stream.of(Mdf.values()).filter(mdf->!capturedAs.contains(mdf))
      .forEach(mdf->cInnerFail(codeGen2.formatted(method, mdf, captured, lambda, captured, lambda, captured)));
  }
  void c3(Mdf lambda,Mdf captured,Mdf method,List<Mdf> capturedAs, List<Mdf> capturedAsG) {
    throw Bug.todo();
  }
  String codeGen1 = """
    package test
    B:{}
    L:{ %s .absMeth: %s B }
    A:{ read .m(par: %s B) : %s L -> %s L{.absMeth->par} }
    """;

  String codeGen2 = """
    package test
    B:{}
    L[X]:{ %s .absMeth: %s X }
    A:{ read .m(par: %s B) : %s L[%s B] -> %s L[%s B]{.absMeth->par} }
    """;

  String codeGen3 = """
    package test
    B:{}
    L[X]:{ %s .absMeth: %s X }
    A:{ read .m[T](par: %s T) : %s L[%s T] -> %s L[%s T]{.absMeth->par} }
    """;

  void cInnerOk(String code){
    System.out.println(code);
    try{ok(code);}
    catch(AssertionError t){ throw new AssertionError("failed on "+code+"\nwith:\n"+t); }
  }
  void cInnerFail(String code){
    try{expectFail(code);}
    catch(AssertionError t){ throw new AssertionError("expected failure but succeeded on "+code); }
  }

  //                     lambda, captured, method, ...capturedAs
  @Example void t001(){ c1(imm,   imm,   imm,   of(imm,read)); }
  @Example void t002(){ c1(read,  imm,   imm,   of(imm,read)); }
  @Example void t003(){ c1(lent,  imm,   imm,   of(imm,read)); }
  @Example void t004(){ c1(mut,   imm,   imm,   of(imm,read)); }
  @Example void t005(){ c1(iso,   imm,   imm,   of(imm,read)); }
  @Example void t006(){ c1(mdf,   imm,   imm,   of(/*not well formed lambda*/)); }
  @Example void t007(){ c1(recMdf,imm,   imm,   of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t011(){ c1(imm,   read,  imm,   of(/*impossible*/)); }
  @Example void t012(){ c1(read,  read,  imm,   of(imm,read)); }
  @Example void t013(){ c1(lent,  read,  imm,   of(imm,read)); }
  @Example void t014(){ c1(mut,   read,  imm,   of(/*impossible*/)); }//NOT NoMutHyg
  @Example void t015(){ c1(iso,   read,  imm,   of(/*impossible*/)); }
  @Example void t016(){ c1(mdf,   read,  imm,   of(/*not well formed lambda*/)); }
  @Example void t017(){ c1(recMdf,read,  imm,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t021(){ c1(imm,   lent,  imm,   of(/*impossible*/)); }
  @Example void t022(){ c1(read,  lent,  imm,   of(imm,read)); }
  @Example void t023(){ c1(lent,  lent,  imm,   of(imm,read)); }
  @Example void t024(){ c1(mut,   lent,  imm,   of(/*impossible*/)); }//NOT NoMutHyg
  @Example void t025(){ c1(iso,   lent,  imm,   of(/*impossible*/)); }
  @Example void t026(){ c1(mdf,   lent,  imm,   of(/*not well formed lambda*/)); }
  @Example void t027(){ c1(recMdf,lent,  imm,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t031(){ c1(imm,   mut,   imm,  of(/*impossible*/)); }
  @Example void t032(){ c1(read,  mut,   imm,   of(imm,read)); }
  @Example void t033(){ c1(lent,  mut,   imm,   of(imm,read)); }
  @Example void t034(){ c1(mut,   mut,   imm,   of(imm,read)); }
  @Example void t035(){ c1(iso,   mut,   imm,   of(imm,read)); }
  @Example void t036(){ c1(mdf,   mut,   imm,   of(/*not well formed lambda*/)); }
  @Example void t037(){ c1(recMdf,mut,   imm,  of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t041(){ c1(imm,   iso,   imm,   of(imm,read)); }
  @Example void t042(){ c1(read,  iso,   imm,   of(imm,read)); }
  @Example void t043(){ c1(lent,  iso,   imm,   of(imm,read)); }
  @Example void t044(){ c1(mut,   iso,   imm,   of(imm,read)); }
  @Example void t045(){ c1(iso,   iso,   imm,   of(imm,read)); }
  @Example void t046(){ c1(mdf,   iso,   imm,   of(/*not well formed lambda*/)); }
  @Example void t047(){ c1(recMdf,iso,   imm,   of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t051(){ c1(imm,   mdf,   imm, of(/*not well formed parameter with mdf*/)); }
  @Example void t052(){ c1(read,  mdf,   imm, of(/*not well formed parameter with mdf*/)); }
  @Example void t053(){ c1(lent,  mdf,   imm, of(/*not well formed parameter with mdf*/)); }
  @Example void t054(){ c1(mut,   mdf,   imm, of(/*not well formed parameter with mdf*/)); }
  @Example void t055(){ c1(iso,   mdf,   imm, of(/*not well formed parameter with mdf*/)); }
  @Example void t056(){ c1(mdf,   mdf,   imm, of(/*not well formed parameter with mdf*/)); }
  @Example void t057(){ c1(recMdf,mdf,   imm, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Example void t061(){ c1(imm,   recMdf,   imm,  of(/*impossible*/)); }
  @Example void t062(){ c1(read,  recMdf,   imm,   of(imm,read)); }
  @Example void t063(){ c1(lent,  recMdf,   imm,   of(imm,read)); }
  @Example void t064(){ c1(mut,   recMdf,   imm,  of(/*impossible*/)); }
  @Example void t065(){ c1(iso,   recMdf,   imm,  of(/*impossible*/)); }
  @Example void t066(){ c1(mdf,   recMdf,   imm,   of(/*not well formed lambda*/)); }
  @Example void t067(){ c1(recMdf,recMdf,   imm,   of(imm,read)); }

  //                     lambda, captured, method, ...capturedAs
  @Example void t181(){ c1(imm,   imm,   read,   of(imm,read)); }
  @Example void t182(){ c1(read,  imm,   read,   of(imm,read)); }
  @Example void t183(){ c1(lent,  imm,   read,   of(imm,read)); }
  @Example void t184(){ c1(mut,   imm,   read,   of(imm,read)); }
  @Example void t185(){ c1(iso,   imm,   read,   of(imm,read)); }
  @Example void t186(){ c1(mdf,   imm,   read,   of(/*not well formed lambda*/)); }
  @Example void t187(){ c1(recMdf,imm,   read,   of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t101(){ c1(imm,   read,  read,   of(/*impossible*/)); }
  @Example void t102(){ c1(read,  read,  read,   of(read)); }
  @Example void t103(){ c1(lent,  read,  read,   of(read)); }
  @Example void t104(){ c1(mut,   read,  read,   of(/*impossible*/)); }//NOT NoMutHyg
  @Example void t105(){ c1(iso,   read,  read,   of(/*impossible*/)); }
  @Example void t106(){ c1(mdf,   read,  read,   of(/*not well formed lambda*/)); }
  @Example void t107(){ c1(recMdf,read,  read,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t111(){ c1(imm,   lent,  read,   of(/*impossible*/)); }
  @Example void t112(){ c1(read,  lent,  read,   of(read,recMdf)); }//recMdf is ok, at least can not find counter example, the lent lambda can become mut only in controlled way
  @Example void t113(){ c1(lent,  lent,  read,   of(read,recMdf)); }//the lambda is created read, and can not become anything else but imm.
  @Example void t114(){ c1(mut,   lent,  read,   of(/*impossible*/)); }//NOT NoMutHyg
  @Example void t115(){ c1(iso,   lent,  read,   of(/*impossible*/)); }
  @Example void t116(){ c1(mdf,   lent,  read,   of(/*not well formed lambda*/)); }
  @Example void t117(){ c1(recMdf,lent,  read,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t121(){ c1(imm,   mut,   read,  of(/*impossible*/)); }
  @Example void t122(){ c1(read,  mut,   read,   of(read,recMdf)); }
  @Example void t123(){ c1(lent,  mut,   read,   of(read,recMdf)); }
  @Example void t124(){ c1(mut,   mut,   read,   of(read,recMdf)); }
  @Example void t125(){ c1(iso,   mut,   read,   of(read,recMdf)); }
  @Example void t126(){ c1(mdf,   mut,   read,   of(/*not well formed lambda*/)); }
  @Example void t127(){ c1(recMdf,mut,   read,  of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t131(){ c1(imm,   iso,   read,   of(imm,read)); }
  @Example void t132(){ c1(read,  iso,   read,   of(imm,read)); }
  @Example void t133(){ c1(lent,  iso,   read,   of(imm,read)); }
  @Example void t134(){ c1(mut,   iso,   read,   of(imm,read)); }
  @Example void t135(){ c1(iso,   iso,   read,   of(imm,read)); }
  @Example void t136(){ c1(mdf,   iso,   read,   of(/*not well formed lambda*/)); }
  @Example void t137(){ c1(recMdf,iso,   read,   of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t141(){ c1(imm,   mdf,   read, of(/*not well formed parameter with mdf*/)); }
  @Example void t142(){ c1(read,  mdf,   read, of(/*not well formed parameter with mdf*/)); }
  @Example void t143(){ c1(lent,  mdf,   read, of(/*not well formed parameter with mdf*/)); }
  @Example void t144(){ c1(mut,   mdf,   read, of(/*not well formed parameter with mdf*/)); }
  @Example void t145(){ c1(iso,   mdf,   read, of(/*not well formed parameter with mdf*/)); }
  @Example void t146(){ c1(mdf,   mdf,   read, of(/*not well formed parameter with mdf*/)); }
  @Example void t147(){ c1(recMdf,mdf,   read, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Example void t151(){ c1(imm,   recMdf,   read,  of(/*impossible*/)); }
  @Example void t152(){ c1(read,  recMdf,   read,   of(read)); }
  @Example void t153(){ c1(lent,  recMdf,   read,   of(read)); }
  @Example void t154(){ c1(mut,   recMdf,   read,  of(/*impossible*/)); }
  @Example void t155(){ c1(iso,   recMdf,   read,  of(/*impossible*/)); }
  @Example void t156(){ c1(mdf,   recMdf,   read,   of(/*not well formed lambda*/)); }
  @Example void t157(){ c1(recMdf,recMdf,   read,   of(read,recMdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t161(){ c1(imm,   imm,   read,   of(read,imm)); }
  @Example void t162(){ c1(read,  imm,   read,   of(read,imm)); }
  @Example void t163(){ c1(lent,  imm,   read,   of(read,imm)); }
  @Example void t164(){ c1(mut,   imm,   read,   of(read,imm)); }
  @Example void t165(){ c1(iso,   imm,   read,   of(read,imm)); }
  @Example void t166(){ c1(mdf,   imm,   read,   of(/*not well formed lambda*/)); }
  @Example void t167(){ c1(recMdf,imm,   read,   of(read,imm)); }

  //                     lambda, captured, method, ...capturedAs
  @Example void t201(){ c1(imm,   imm,   lent,    of(/*impossible*/)); }
  @Example void t202(){ c1(read,  imm,   lent,    of(/*impossible*/)); }
  @Example void t203(){ c1(lent,  imm,   lent,   of(imm,read)); }
  @Example void t204(){ c1(mut,   imm,   lent,   of(imm,read)); }
  @Example void t205(){ c1(iso,   imm,   lent,   of(imm,read)); }
  @Example void t206(){ c1(mdf,   imm,   lent,   of(/*not well formed lambda*/)); }
  @Example void t207(){ c1(recMdf,imm,   lent,   of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t211(){ c1(imm,   read,  lent,   of(/*impossible*/)); }
  @Example void t212(){ c1(read,  read,  lent,   of(/*impossible*/)); }
  @Example void t213(){ c1(lent,  read,  lent,   of(read)); }
  @Example void t214(){ c1(mut,   read,  lent,   of(/*impossible*/)); }//NOT NoMutHyg
  @Example void t215(){ c1(iso,   read,  lent,   of(/*impossible*/)); }
  @Example void t216(){ c1(mdf,   read,  lent,   of(/*not well formed lambda*/)); }
  @Example void t217(){ c1(recMdf,read,  lent,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t221(){ c1(imm,   lent,  lent,   of(/*impossible*/)); }
  @Example void t222(){ c1(read,  lent,  lent,   of(/*impossible*/)); } // this capture is fine because the method cannot ever be called
  @Example void t223(){ c1(lent,  lent,  lent,   of(read,lent)); }
  @Example void t224(){ c1(mut,   lent,  lent,   of(/*impossible*/)); }//NOT NoMutHyg
  @Example void t225(){ c1(iso,   lent,  lent,   of(/*impossible*/)); }
  @Example void t226(){ c1(mdf,   lent,  lent,   of(/*not well formed lambda*/)); }
  @Example void t227(){ c1(recMdf,lent,  lent,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t231(){ c1(imm,   mut,   lent,  of(/*impossible*/)); }
  @Example void t232(){ c1(read,  mut,   lent,  of(/*impossible*/)); }
  @Example void t233(){ c1(lent,  mut,   lent,   of(read,lent)); }
  @Example void t234(){ c1(mut,   mut,   lent,   of(read,lent)); }
  @Example void t235(){ c1(iso,   mut,   lent,   of(read,lent)); }
  @Example void t236(){ c1(mdf,   mut,   lent,   of(/*not well formed lambda*/)); }
  @Example void t237(){ c1(recMdf,mut,   lent,  of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t241(){ c1(imm,   iso,   lent,    of(/*impossible*/)); } //TODO: Marco up to here
  @Example void t242(){ c1(read,  iso,   lent,    of(/*impossible*/)); }
  @Example void t243(){ c1(lent,  iso,   lent,   of(imm,read)); }
  @Example void t244(){ c1(mut,   iso,   lent,   of(imm,read)); }
  @Example void t245(){ c1(iso,   iso,   lent,   of(imm,read)); }
  @Example void t246(){ c1(mdf,   iso,   lent,   of(/*not well formed lambda*/)); }
  @Example void t247(){ c1(recMdf,iso,   lent,   of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t251(){ c1(imm,   mdf,   lent, of(/*not well formed parameter with mdf*/)); }
  @Example void t252(){ c1(read,  mdf,   lent, of(/*not well formed parameter with mdf*/)); }
  @Example void t253(){ c1(lent,  mdf,   lent, of(/*not well formed parameter with mdf*/)); }
  @Example void t254(){ c1(mut,   mdf,   lent, of(/*not well formed parameter with mdf*/)); }
  @Example void t255(){ c1(iso,   mdf,   lent, of(/*not well formed parameter with mdf*/)); }
  @Example void t256(){ c1(mdf,   mdf,   lent, of(/*not well formed parameter with mdf*/)); }
  @Example void t257(){ c1(recMdf,mdf,   lent, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Example void t261(){ c1(imm,   recMdf,   lent,  of(/*impossible*/)); }
  @Example void t262(){ c1(read,  recMdf,   lent,  of(/*impossible*/)); }
  @Example void t263(){ c1(lent,  recMdf,   lent,   of(read)); }
  @Example void t264(){ c1(mut,   recMdf,   lent,  of(/*impossible*/)); }
  @Example void t265(){ c1(iso,   recMdf,   lent,  of(/*impossible*/)); }
  @Example void t266(){ c1(mdf,   recMdf,   lent,   of(/*not well formed lambda*/)); }
  @Example void t267(){ c1(recMdf,recMdf,   lent,   of(read)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t271(){ c1(imm,   imm,   lent,    of(/*impossible*/)); }
  @Example void t272(){ c1(read,  imm,   lent,    of(/*impossible*/)); }
  @Example void t273(){ c1(lent,  imm,   lent,   of(read,imm)); }
  @Example void t274(){ c1(mut,   imm,   lent,   of(read,imm)); }
  @Example void t275(){ c1(iso,   imm,   lent,   of(read,imm)); }
  @Example void t276(){ c1(mdf,   imm,   lent,   of(/*not well formed lambda*/)); }
  @Example void t277(){ c1(recMdf,imm,   lent,   of(read,imm)); }

  //                     lambda, captured, method, ...capturedAs
  @Example void t301(){ c1(imm,   imm,   mut,    of(/*impossible*/)); }
  @Example void t302(){ c1(read,  imm,   mut,    of(/*impossible*/)); }
  @Example void t303(){ c1(lent,  imm,   mut,   of(imm,read)); }
  @Example void t304(){ c1(mut,   imm,   mut,   of(imm,read)); }
  @Example void t305(){ c1(iso,   imm,   mut,   of(imm,read)); }
  @Example void t306(){ c1(mdf,   imm,   mut,   of(/*not well formed lambda*/)); }
  @Example void t307(){ c1(recMdf,imm,   mut,   of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t311(){ c1(imm,   read,  mut,   of(/*impossible*/)); }
  @Example void t312(){ c1(read,  read,  mut,   of(/*impossible*/)); }
  @Example void t313(){ c1(lent,  read,  mut,   of(read)); } // yes because call I can call the mut method through a lent
  @Example void t314(){ c1(mut,   read,  mut,   of(/*impossible*/)); }//NOT NoMutHyg
  @Example void t315(){ c1(iso,   read,  mut,   of(/*impossible*/)); }
  @Example void t316(){ c1(mdf,   read,  mut,   of(/*not well formed lambda*/)); }
  @Example void t317(){ c1(recMdf,read,  mut,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t321(){ c1(imm,   lent,  mut,   of(/*impossible*/)); }
  @Example void t322(){ c1(read,  lent,  mut,   of(/*impossible*/)); } // this capture is fine because the method cannot ever be called
  @Example void t323(){ c1(lent,  lent,  mut,   of(read,lent)); }
  @Example void t324(){ c1(mut,   lent,  mut,   of(/*impossible*/)); }//NOT NoMutHyg
  @Example void t325(){ c1(iso,   lent,  mut,   of(/*impossible*/)); }
  @Example void t326(){ c1(mdf,   lent,  mut,   of(/*not well formed lambda*/)); }
  @Example void t327(){ c1(recMdf,lent,  mut,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t331(){ c1(imm,   mut,   mut,  of(/*impossible*/)); }
  @Example void t332(){ c1(read,  mut,   mut,  of(/*impossible*/)); }
  @Example void t333(){ c1(lent,  mut,   mut,   of(read,lent)); } // TODO: double check this
  @Example void t334(){ c1(mut,   mut,   mut,   of(read,lent,mut)); }
  @Example void t335(){ c1(iso,   mut,   mut,   of(read,lent,mut)); }
  @Example void t336(){ c1(mdf,   mut,   mut,   of(/*not well formed lambda*/)); }
  @Example void t337(){ c1(recMdf,mut,   mut,  of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t341(){ c1(imm,   iso,   mut,    of(/*impossible*/)); }
  @Example void t342(){ c1(read,  iso,   mut,    of(/*impossible*/)); }
  @Example void t343(){ c1(lent,  iso,   mut,   of(imm,read)); }
  @Example void t344(){ c1(mut,   iso,   mut,   of(imm,read)); }
  @Example void t345(){ c1(iso,   iso,   mut,   of(imm,read)); }
  @Example void t346(){ c1(mdf,   iso,   mut,   of(/*not well formed lambda*/)); }
  @Example void t347(){ c1(recMdf,iso,   mut,   of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t351(){ c1(imm,   mdf,   mut, of(/*not well formed parameter with mdf*/)); }
  @Example void t352(){ c1(read,  mdf,   mut, of(/*not well formed parameter with mdf*/)); }
  @Example void t353(){ c1(lent,  mdf,   mut, of(/*not well formed parameter with mdf*/)); }
  @Example void t354(){ c1(mut,   mdf,   mut, of(/*not well formed parameter with mdf*/)); }
  @Example void t355(){ c1(iso,   mdf,   mut, of(/*not well formed parameter with mdf*/)); }
  @Example void t356(){ c1(mdf,   mdf,   mut, of(/*not well formed parameter with mdf*/)); }
  @Example void t357(){ c1(recMdf,mdf,   mut, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Example void t361(){ c1(imm,   recMdf,   mut,  of(/*impossible*/)); }
  @Example void t362(){ c1(read,  recMdf,   mut,  of(/*impossible*/)); }
  @Example void t363(){ c1(lent,  recMdf,   mut,   of(read)); }
  @Example void t364(){ c1(mut,   recMdf,   mut,  of(/*impossible*/)); }
  @Example void t365(){ c1(iso,   recMdf,   mut,  of(/*impossible*/)); }
  @Example void t366(){ c1(mdf,   recMdf,   mut,   of(/*not well formed lambda*/)); }
  @Example void t367(){ c1(recMdf,recMdf,   mut,   of(read)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t371(){ c1(imm,   imm,   mut,    of(/*impossible*/)); }
  @Example void t372(){ c1(read,  imm,   mut,    of(/*impossible*/)); }
  @Example void t373(){ c1(lent,  imm,   mut,   of(read,imm)); }
  @Example void t374(){ c1(mut,   imm,   mut,   of(read,imm)); }
  @Example void t375(){ c1(iso,   imm,   mut,   of(read,imm)); }
  @Example void t376(){ c1(mdf,   imm,   mut,   of(/*not well formed lambda*/)); }
  @Example void t377(){ c1(recMdf,imm,   mut,   of(read,imm)); }

  //                     lambda, captured, method, ...capturedAs
  @Example void t401(){ c1(imm,   imm,   iso,    of(/*impossible*/)); }
  @Example void t402(){ c1(read,  imm,   iso,    of(/*impossible*/)); }
  @Example void t403(){ c1(lent,  imm,   iso,   of(imm,read)); }
  @Example void t404(){ c1(mut,   imm,   iso,   of(imm,read)); }
  @Example void t405(){ c1(iso,   imm,   iso,   of(imm,read)); }
  @Example void t406(){ c1(mdf,   imm,   iso,   of(/*not well formed lambda*/)); }
  @Example void t407(){ c1(recMdf,imm,   iso,   of(imm,read)); } // yes, recMdf could be iso
  //                     lambda, captured, method, ...capturedAs
  @Example void t411(){ c1(imm,   read,  iso,   of(/*impossible*/)); }
  @Example void t412(){ c1(read,  read,  iso,   of(/*impossible*/)); }
  @Example void t413(){ c1(lent,  read,  iso,   of(read)); }
  @Example void t414(){ c1(mut,   read,  iso,   of(/*impossible*/)); }//NOT NoMutHyg
  @Example void t415(){ c1(iso,   read,  iso,   of(/*impossible*/)); }
  @Example void t416(){ c1(mdf,   read,  iso,   of(/*not well formed lambda*/)); }
  @Example void t417(){ c1(recMdf,read,  iso,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t421(){ c1(imm,   lent,  iso,   of(/*impossible*/)); }
  @Example void t422(){ c1(read,  lent,  iso,   of(/*impossible*/)); }
  @Example void t423(){ c1(lent,  lent,  iso,   of(read,lent)); }
  @Example void t424(){ c1(mut,   lent,  iso,   of(/*impossible*/)); }//NOT NoMutHyg
  @Example void t425(){ c1(iso,   lent,  iso,   of(/*impossible*/)); }
  @Example void t426(){ c1(mdf,   lent,  iso,   of(/*not well formed lambda*/)); }
  @Example void t427(){ c1(recMdf,lent,  iso,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t431(){ c1(imm,   mut,   iso,  of(/*impossible*/)); }
  @Example void t432(){ c1(read,  mut,   iso,  of(/*impossible*/)); }
  @Example void t433(){ c1(lent,  mut,   iso,   of(read,lent,mut)); } // These 3 look odd, but it's correct because iso lambdas are treated like mut
  @Example void t434(){ c1(mut,   mut,   iso,   of(read,lent,mut)); }
  @Example void t435(){ c1(iso,   mut,   iso,   of(read,lent,mut)); }
  @Example void t436(){ c1(mdf,   mut,   iso,   of(/*not well formed lambda*/)); }
  @Example void t437(){ c1(recMdf,mut,   iso,  of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t441(){ c1(imm,   iso,   iso,    of(/*impossible*/)); }
  @Example void t442(){ c1(read,  iso,   iso,    of(/*impossible*/)); }
  @Example void t443(){ c1(lent,  iso,   iso,   of(imm,read)); }
  @Example void t444(){ c1(mut,   iso,   iso,   of(imm,read)); }
  @Example void t445(){ c1(iso,   iso,   iso,   of(imm,read)); } // all iso is captured as imm
  @Example void t446(){ c1(mdf,   iso,   iso,   of(/*not well formed lambda*/)); }
  @Example void t447(){ c1(recMdf,iso,   iso,   of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t451(){ c1(imm,   mdf,   iso, of(/*not well formed parameter with mdf*/)); }
  @Example void t452(){ c1(read,  mdf,   iso, of(/*not well formed parameter with mdf*/)); }
  @Example void t453(){ c1(lent,  mdf,   iso, of(/*not well formed parameter with mdf*/)); }
  @Example void t454(){ c1(mut,   mdf,   iso, of(/*not well formed parameter with mdf*/)); }
  @Example void t455(){ c1(iso,   mdf,   iso, of(/*not well formed parameter with mdf*/)); }
  @Example void t456(){ c1(mdf,   mdf,   iso, of(/*not well formed parameter with mdf*/)); }
  @Example void t457(){ c1(recMdf,mdf,   iso, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Example void t461(){ c1(imm,   recMdf,   iso,  of(/*impossible*/)); }
  @Example void t462(){ c1(read,  recMdf,   iso,  of(/*impossible*/)); }
  @Example void t463(){ c1(lent,  recMdf,   iso,   of(read)); }
  @Example void t464(){ c1(mut,   recMdf,   iso,  of(/*impossible*/)); }
  @Example void t465(){ c1(iso,   recMdf,   iso,  of(/*impossible*/)); }
  @Example void t466(){ c1(mdf,   recMdf,   iso,   of(/*not well formed lambda*/)); }
  @Example void t467(){ c1(recMdf,recMdf,   iso,   of(read)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t471(){ c1(imm,   imm,   iso,    of(/*impossible*/)); }
  @Example void t472(){ c1(read,  imm,   iso,    of(/*impossible*/)); }
  @Example void t473(){ c1(lent,  imm,   iso,   of(read,imm)); }
  @Example void t474(){ c1(mut,   imm,   iso,   of(read,imm)); }
  @Example void t475(){ c1(iso,   imm,   iso,   of(read,imm)); }
  @Example void t476(){ c1(mdf,   imm,   iso,   of(/*not well formed lambda*/)); }
  @Example void t477(){ c1(recMdf,imm,   iso,   of(read,imm)); }

  //                     lambda, captured, method, ...capturedAs
  @Example void t501(){ c1(imm,   imm,   mdf, of(/*not well formed method*/)); }
  @Example void t502(){ c1(read,  imm,   mdf, of(/*not well formed method*/)); }
  @Example void t503(){ c1(lent,  imm,   mdf, of(/*not well formed method*/)); }
  @Example void t504(){ c1(mut,   imm,   mdf, of(/*not well formed method*/)); }
  @Example void t505(){ c1(iso,   imm,   mdf, of(/*not well formed method*/)); }
  @Example void t506(){ c1(mdf,   imm,   mdf, of(/*not well formed method*/)); }
  @Example void t507(){ c1(recMdf,imm,   mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t511(){ c1(imm,   read,  mdf, of(/*not well formed method*/)); }
  @Example void t512(){ c1(read,  read,  mdf, of(/*not well formed method*/)); }
  @Example void t513(){ c1(lent,  read,  mdf, of(/*not well formed method*/)); }
  @Example void t514(){ c1(mut,   read,  mdf, of(/*not well formed method*/)); }
  @Example void t515(){ c1(iso,   read,  mdf, of(/*not well formed method*/)); }
  @Example void t516(){ c1(mdf,   read,  mdf, of(/*not well formed method*/)); }
  @Example void t517(){ c1(recMdf,read,  mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t521(){ c1(imm,   lent,  mdf, of(/*not well formed method*/)); }
  @Example void t522(){ c1(read,  lent,  mdf, of(/*not well formed method*/)); }
  @Example void t523(){ c1(lent,  lent,  mdf, of(/*not well formed method*/)); }
  @Example void t524(){ c1(mut,   lent,  mdf, of(/*not well formed method*/)); }
  @Example void t525(){ c1(iso,   lent,  mdf, of(/*not well formed method*/)); }
  @Example void t526(){ c1(mdf,   lent,  mdf, of(/*not well formed method*/)); }
  @Example void t527(){ c1(recMdf,lent,  mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t531(){ c1(imm,   mut,   mdf, of(/*not well formed method*/)); }
  @Example void t532(){ c1(read,  mut,   mdf, of(/*not well formed method*/)); }
  @Example void t533(){ c1(lent,  mut,   mdf, of(/*not well formed method*/)); }
  @Example void t534(){ c1(mut,   mut,   mdf, of(/*not well formed method*/)); }
  @Example void t535(){ c1(iso,   mut,   mdf, of(/*not well formed method*/)); }
  @Example void t536(){ c1(mdf,   mut,   mdf, of(/*not well formed method*/)); }
  @Example void t537(){ c1(recMdf,mut,   mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t541(){ c1(imm,   iso,   mdf, of(/*not well formed method*/)); }
  @Example void t542(){ c1(read,  iso,   mdf, of(/*not well formed method*/)); }
  @Example void t543(){ c1(lent,  iso,   mdf, of(/*not well formed method*/)); }
  @Example void t544(){ c1(mut,   iso,   mdf, of(/*not well formed method*/)); }
  @Example void t545(){ c1(iso,   iso,   mdf, of(/*not well formed method*/)); }
  @Example void t546(){ c1(mdf,   iso,   mdf, of(/*not well formed method*/)); }
  @Example void t547(){ c1(recMdf,iso,   mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t551(){ c1(imm,   mdf,   mdf, of(/*not well formed method*/)); }
  @Example void t552(){ c1(read,  mdf,   mdf, of(/*not well formed method*/)); }
  @Example void t553(){ c1(lent,  mdf,   mdf, of(/*not well formed method*/)); }
  @Example void t554(){ c1(mut,   mdf,   mdf, of(/*not well formed method*/)); }
  @Example void t555(){ c1(iso,   mdf,   mdf, of(/*not well formed method*/)); }
  @Example void t556(){ c1(mdf,   mdf,   mdf, of(/*not well formed method*/)); }
  @Example void t557(){ c1(recMdf,mdf,   mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t561(){ c1(imm,   recMdf,   mdf, of(/*not well formed method*/)); }
  @Example void t562(){ c1(read,  recMdf,   mdf, of(/*not well formed method*/)); }
  @Example void t563(){ c1(lent,  recMdf,   mdf, of(/*not well formed method*/)); }
  @Example void t564(){ c1(mut,   recMdf,   mdf, of(/*not well formed method*/)); }
  @Example void t565(){ c1(iso,   recMdf,   mdf, of(/*not well formed method*/)); }
  @Example void t566(){ c1(mdf,   recMdf,   mdf, of(/*not well formed method*/)); }
  @Example void t567(){ c1(recMdf,recMdf,   mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t571(){ c1(imm,   imm,   mdf, of(/*not well formed method*/)); }
  @Example void t572(){ c1(read,  imm,   mdf, of(/*not well formed method*/)); }
  @Example void t573(){ c1(lent,  imm,   mdf, of(/*not well formed method*/)); }
  @Example void t574(){ c1(mut,   imm,   mdf, of(/*not well formed method*/)); }
  @Example void t575(){ c1(iso,   imm,   mdf, of(/*not well formed method*/)); }
  @Example void t576(){ c1(mdf,   imm,   mdf, of(/*not well formed method*/)); }
  @Example void t577(){ c1(recMdf,imm,   mdf, of(/*not well formed method*/)); }

  //                     lambda, captured, method, ...capturedAs
  @Example void t601(){ c1(imm,   imm,   recMdf, of(/*not well formed method*/)); }
  @Example void t602(){ c1(read,  imm,   recMdf, of(/*not well formed method*/)); }
  @Example void t603(){ c1(lent,  imm,   recMdf, of(/*not well formed method*/)); }
  @Example void t604(){ c1(mut,   imm,   recMdf, of(/*not well formed method*/)); }
  @Example void t605(){ c1(iso,   imm,   recMdf, of(/*not well formed method*/)); }
  @Example void t606(){ c1(mdf,   imm,   recMdf, of(/*not well formed method*/)); }
  @Example void t607(){ c1(recMdf,imm,   recMdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t611(){ c1(imm,   read,  recMdf, of(/*not well formed method*/)); }
  @Example void t612(){ c1(read,  read,  recMdf, of(/*not well formed method*/)); }
  @Example void t613(){ c1(lent,  read,  recMdf, of(/*not well formed method*/)); }
  @Example void t614(){ c1(mut,   read,  recMdf, of(/*not well formed method*/)); }
  @Example void t615(){ c1(iso,   read,  recMdf, of(/*not well formed method*/)); }
  @Example void t616(){ c1(mdf,   read,  recMdf, of(/*not well formed method*/)); }
  @Example void t617(){ c1(recMdf,read,  recMdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t621(){ c1(imm,   lent,  recMdf, of(/*not well formed method*/)); }
  @Example void t622(){ c1(read,  lent,  recMdf, of(/*not well formed method*/)); }
  @Example void t623(){ c1(lent,  lent,  recMdf, of(/*not well formed method*/)); }
  @Example void t624(){ c1(mut,   lent,  recMdf, of(/*not well formed method*/)); }
  @Example void t625(){ c1(iso,   lent,  recMdf, of(/*not well formed method*/)); }
  @Example void t626(){ c1(mdf,   lent,  recMdf, of(/*not well formed method*/)); }
  @Example void t627(){ c1(recMdf,lent,  recMdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t631(){ c1(imm,   mut,   recMdf, of(/*not well formed method*/)); }
  @Example void t632(){ c1(read,  mut,   recMdf, of(/*not well formed method*/)); }
  @Example void t633(){ c1(lent,  mut,   recMdf, of(/*not well formed method*/)); }
  @Example void t634(){ c1(mut,   mut,   recMdf, of(/*not well formed method*/)); }
  @Example void t635(){ c1(iso,   mut,   recMdf, of(/*not well formed method*/)); }
  @Example void t636(){ c1(mdf,   mut,   recMdf, of(/*not well formed method*/)); }
  @Example void t637(){ c1(recMdf,mut,   recMdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t641(){ c1(imm,   iso,   recMdf, of(/*not well formed method*/)); }
  @Example void t642(){ c1(read,  iso,   recMdf, of(/*not well formed method*/)); }
  @Example void t643(){ c1(lent,  iso,   recMdf, of(/*not well formed method*/)); }
  @Example void t644(){ c1(mut,   iso,   recMdf, of(/*not well formed method*/)); }
  @Example void t645(){ c1(iso,   iso,   recMdf, of(/*not well formed method*/)); }
  @Example void t646(){ c1(mdf,   iso,   recMdf, of(/*not well formed method*/)); }
  @Example void t647(){ c1(recMdf,iso,   recMdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t651(){ c1(imm,   mdf,   recMdf, of(/*not well formed method*/)); }
  @Example void t652(){ c1(read,  mdf,   recMdf, of(/*not well formed method*/)); }
  @Example void t653(){ c1(lent,  mdf,   recMdf, of(/*not well formed method*/)); }
  @Example void t654(){ c1(mut,   mdf,   recMdf, of(/*not well formed method*/)); }
  @Example void t655(){ c1(iso,   mdf,   recMdf, of(/*not well formed method*/)); }
  @Example void t656(){ c1(mdf,   mdf,   recMdf, of(/*not well formed method*/)); }
  @Example void t657(){ c1(recMdf,mdf,   recMdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t661(){ c1(imm,   recMdf,   recMdf, of(/*not well formed method*/)); }
  @Example void t662(){ c1(read,  recMdf,   recMdf, of(/*not well formed method*/)); }
  @Example void t663(){ c1(lent,  recMdf,   recMdf, of(/*not well formed method*/)); }
  @Example void t664(){ c1(mut,   recMdf,   recMdf, of(/*not well formed method*/)); }
  @Example void t665(){ c1(iso,   recMdf,   recMdf, of(/*not well formed method*/)); }
  @Example void t666(){ c1(mdf,   recMdf,   recMdf, of(/*not well formed method*/)); }
  @Example void t667(){ c1(recMdf,recMdf,   recMdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t671(){ c1(imm,   imm,   recMdf, of(/*not well formed method*/)); }
  @Example void t672(){ c1(read,  imm,   recMdf, of(/*not well formed method*/)); }
  @Example void t673(){ c1(lent,  imm,   recMdf, of(/*not well formed method*/)); }
  @Example void t674(){ c1(mut,   imm,   recMdf, of(/*not well formed method*/)); }
  @Example void t675(){ c1(iso,   imm,   recMdf, of(/*not well formed method*/)); }
  @Example void t676(){ c1(mdf,   imm,   recMdf, of(/*not well formed method*/)); }
  @Example void t677(){ c1(recMdf,imm,   recMdf, of(/*not well formed method*/)); }

  // ---------------------- c2 ---------------------
  //                     lambda, captured, method, ...capturedAs
  @Example void t2001(){ c2(imm,   imm,   imm,   of(imm,read,mdf)); }
  @Example void t2002(){ c2(read,  imm,   imm,   of(imm,read,mdf)); }
  @Example void t2003(){ c2(lent,  imm,   imm,   of(imm,read,mdf)); }
  @Example void t2004(){ c2(mut,   imm,   imm,   of(imm,read,mdf)); }
  @Example void t2005(){ c2(iso,   imm,   imm,   of(imm,read,mdf)); }
  @Example void t2006(){ c2(mdf,   imm,   imm,   of(/*not well formed lambda*/)); }
  @Example void t2007(){ c2(recMdf,imm,   imm,   of(imm,read,mdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2011(){ c2(imm,   read,  imm,   of(/*impossible*/)); }
  @Example void t2012(){ c2(read,  read,  imm,   of(imm,read,mdf)); }
  @Example void t2013(){ c2(lent,  read,  imm,   of(imm,read,mdf)); }
  @Example void t2014(){ c2(mut,   read,  imm,   of(/*impossible*/)); }//NOT NoMutHyg
  @Example void t2015(){ c2(iso,   read,  imm,   of(/*impossible*/)); }
  @Example void t2016(){ c2(mdf,   read,  imm,   of(/*not well formed lambda*/)); }
  @Example void t2017(){ c2(recMdf,read,  imm,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2021(){ c2(imm,   lent,  imm,   of(/*impossible*/)); }
  @Example void t2022(){ c2(read,  lent,  imm,   of(imm,read,mdf)); } // TODO: Nick here
  @Example void t2023(){ c2(lent,  lent,  imm,   of(imm,read)); }
  @Example void t2024(){ c2(mut,   lent,  imm,   of(/*impossible*/)); }//NOT NoMutHyg
  @Example void t2025(){ c2(iso,   lent,  imm,   of(/*impossible*/)); }
  @Example void t2026(){ c2(mdf,   lent,  imm,   of(/*not well formed lambda*/)); }
  @Example void t2027(){ c2(recMdf,lent,  imm,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2031(){ c2(imm,   mut,   imm,  of(/*impossible*/)); }
  @Example void t2032(){ c2(read,  mut,   imm,   of(imm,read)); }
  @Example void t2033(){ c2(lent,  mut,   imm,   of(imm,read)); }
  @Example void t2034(){ c2(mut,   mut,   imm,   of(imm,read)); }
  @Example void t2035(){ c2(iso,   mut,   imm,   of(imm,read)); }
  @Example void t2036(){ c2(mdf,   mut,   imm,   of(/*not well formed lambda*/)); }
  @Example void t2037(){ c2(recMdf,mut,   imm,  of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2041(){ c2(imm,   iso,   imm,   of(imm,read)); }
  @Example void t2042(){ c2(read,  iso,   imm,   of(imm,read)); }
  @Example void t2043(){ c2(lent,  iso,   imm,   of(imm,read)); }
  @Example void t2044(){ c2(mut,   iso,   imm,   of(imm,read)); }
  @Example void t2045(){ c2(iso,   iso,   imm,   of(imm,read)); }
  @Example void t2046(){ c2(mdf,   iso,   imm,   of(/*not well formed lambda*/)); }
  @Example void t2047(){ c2(recMdf,iso,   imm,   of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2051(){ c2(imm,   mdf,   imm, of(/*not well formed parameter with mdf*/)); }
  @Example void t2052(){ c2(read,  mdf,   imm, of(/*not well formed parameter with mdf*/)); }
  @Example void t2053(){ c2(lent,  mdf,   imm, of(/*not well formed parameter with mdf*/)); }
  @Example void t2054(){ c2(mut,   mdf,   imm, of(/*not well formed parameter with mdf*/)); }
  @Example void t2055(){ c2(iso,   mdf,   imm, of(/*not well formed parameter with mdf*/)); }
  @Example void t2056(){ c2(mdf,   mdf,   imm, of(/*not well formed parameter with mdf*/)); }
  @Example void t2057(){ c2(recMdf,mdf,   imm, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Example void t2061(){ c2(imm,   recMdf,   imm,  of(/*impossible*/)); }
  @Example void t2062(){ c2(read,  recMdf,   imm,   of(imm,read)); }
  @Example void t2063(){ c2(lent,  recMdf,   imm,   of(imm,read)); }
  @Example void t2064(){ c2(mut,   recMdf,   imm,  of(/*impossible*/)); }
  @Example void t2065(){ c2(iso,   recMdf,   imm,  of(/*impossible*/)); }
  @Example void t2066(){ c2(mdf,   recMdf,   imm,   of(/*not well formed lambda*/)); }
  @Example void t2067(){ c2(recMdf,recMdf,   imm,   of(imm,read)); }

  //                     lambda, captured, method, ...capturedAs
  @Example void t2181(){ c2(imm,   imm,   read,   of(imm,read)); }
  @Example void t2182(){ c2(read,  imm,   read,   of(imm,read)); }
  @Example void t2183(){ c2(lent,  imm,   read,   of(imm,read)); }
  @Example void t2184(){ c2(mut,   imm,   read,   of(imm,read)); }
  @Example void t2185(){ c2(iso,   imm,   read,   of(imm,read)); }
  @Example void t2186(){ c2(mdf,   imm,   read,   of(/*not well formed lambda*/)); }
  @Example void t2187(){ c2(recMdf,imm,   read,   of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2101(){ c2(imm,   read,  read,   of(/*impossible*/)); }
  @Example void t2102(){ c2(read,  read,  read,   of(read)); }
  @Example void t2103(){ c2(lent,  read,  read,   of(read)); }
  @Example void t2104(){ c2(mut,   read,  read,   of(/*impossible*/)); }//NOT NoMutHyg
  @Example void t2105(){ c2(iso,   read,  read,   of(/*impossible*/)); }
  @Example void t2106(){ c2(mdf,   read,  read,   of(/*not well formed lambda*/)); }
  @Example void t2107(){ c2(recMdf,read,  read,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2111(){ c2(imm,   lent,  read,   of(/*impossible*/)); }
  @Example void t2112(){ c2(read,  lent,  read,   of(read,recMdf)); }//recMdf is ok, at least can not find counter example, the lent lambda can become mut only in controlled way
  @Example void t2113(){ c2(lent,  lent,  read,   of(read,recMdf)); }//the lambda is created read, and can not become anything else but imm.
  @Example void t2114(){ c2(mut,   lent,  read,   of(/*impossible*/)); }//NOT NoMutHyg
  @Example void t2115(){ c2(iso,   lent,  read,   of(/*impossible*/)); }
  @Example void t2116(){ c2(mdf,   lent,  read,   of(/*not well formed lambda*/)); }
  @Example void t2117(){ c2(recMdf,lent,  read,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2121(){ c2(imm,   mut,   read,  of(/*impossible*/)); }
  @Example void t2122(){ c2(read,  mut,   read,   of(read,recMdf)); }
  @Example void t2123(){ c2(lent,  mut,   read,   of(read,recMdf)); }
  @Example void t2124(){ c2(mut,   mut,   read,   of(read,recMdf)); }
  @Example void t2125(){ c2(iso,   mut,   read,   of(read,recMdf)); }
  @Example void t2126(){ c2(mdf,   mut,   read,   of(/*not well formed lambda*/)); }
  @Example void t2127(){ c2(recMdf,mut,   read,  of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2131(){ c2(imm,   iso,   read,   of(imm,read)); }
  @Example void t2132(){ c2(read,  iso,   read,   of(imm,read)); }
  @Example void t2133(){ c2(lent,  iso,   read,   of(imm,read)); }
  @Example void t2134(){ c2(mut,   iso,   read,   of(imm,read)); }
  @Example void t2135(){ c2(iso,   iso,   read,   of(imm,read)); }
  @Example void t2136(){ c2(mdf,   iso,   read,   of(/*not well formed lambda*/)); }
  @Example void t2137(){ c2(recMdf,iso,   read,   of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2141(){ c2(imm,   mdf,   read, of(/*not well formed parameter with mdf*/)); }
  @Example void t2142(){ c2(read,  mdf,   read, of(/*not well formed parameter with mdf*/)); }
  @Example void t2143(){ c2(lent,  mdf,   read, of(/*not well formed parameter with mdf*/)); }
  @Example void t2144(){ c2(mut,   mdf,   read, of(/*not well formed parameter with mdf*/)); }
  @Example void t2145(){ c2(iso,   mdf,   read, of(/*not well formed parameter with mdf*/)); }
  @Example void t2146(){ c2(mdf,   mdf,   read, of(/*not well formed parameter with mdf*/)); }
  @Example void t2147(){ c2(recMdf,mdf,   read, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Example void t2151(){ c2(imm,   recMdf,   read,  of(/*impossible*/)); }
  @Example void t2152(){ c2(read,  recMdf,   read,   of(read)); }
  @Example void t2153(){ c2(lent,  recMdf,   read,   of(read)); }
  @Example void t2154(){ c2(mut,   recMdf,   read,  of(/*impossible*/)); }
  @Example void t2155(){ c2(iso,   recMdf,   read,  of(/*impossible*/)); }
  @Example void t2156(){ c2(mdf,   recMdf,   read,   of(/*not well formed lambda*/)); }
  @Example void t2157(){ c2(recMdf,recMdf,   read,   of(read,recMdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2161(){ c2(imm,   imm,   read,   of(read,imm)); }
  @Example void t2162(){ c2(read,  imm,   read,   of(read,imm)); }
  @Example void t2163(){ c2(lent,  imm,   read,   of(read,imm)); }
  @Example void t2164(){ c2(mut,   imm,   read,   of(read,imm)); }
  @Example void t2165(){ c2(iso,   imm,   read,   of(read,imm)); }
  @Example void t2166(){ c2(mdf,   imm,   read,   of(/*not well formed lambda*/)); }
  @Example void t2167(){ c2(recMdf,imm,   read,   of(read,imm)); }

  //                     lambda, captured, method, ...capturedAs
  @Example void t2201(){ c2(imm,   imm,   lent,    of(/*impossible*/)); }
  @Example void t2202(){ c2(read,  imm,   lent,    of(/*impossible*/)); }
  @Example void t2203(){ c2(lent,  imm,   lent,   of(imm,read)); }
  @Example void t2204(){ c2(mut,   imm,   lent,   of(imm,read)); }
  @Example void t2205(){ c2(iso,   imm,   lent,   of(imm,read)); }
  @Example void t2206(){ c2(mdf,   imm,   lent,   of(/*not well formed lambda*/)); }
  @Example void t2207(){ c2(recMdf,imm,   lent,   of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2211(){ c2(imm,   read,  lent,   of(/*impossible*/)); }
  @Example void t2212(){ c2(read,  read,  lent,   of(/*impossible*/)); }
  @Example void t2213(){ c2(lent,  read,  lent,   of(read)); }
  @Example void t2214(){ c2(mut,   read,  lent,   of(/*impossible*/)); }//NOT NoMutHyg
  @Example void t2215(){ c2(iso,   read,  lent,   of(/*impossible*/)); }
  @Example void t2216(){ c2(mdf,   read,  lent,   of(/*not well formed lambda*/)); }
  @Example void t2217(){ c2(recMdf,read,  lent,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2221(){ c2(imm,   lent,  lent,   of(/*impossible*/)); }
  @Example void t2222(){ c2(read,  lent,  lent,   of(/*impossible*/)); } // this capture is fine because the method cannot ever be called
  @Example void t2223(){ c2(lent,  lent,  lent,   of(read,lent)); }
  @Example void t2224(){ c2(mut,   lent,  lent,   of(/*impossible*/)); }//NOT NoMutHyg
  @Example void t2225(){ c2(iso,   lent,  lent,   of(/*impossible*/)); }
  @Example void t2226(){ c2(mdf,   lent,  lent,   of(/*not well formed lambda*/)); }
  @Example void t2227(){ c2(recMdf,lent,  lent,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2231(){ c2(imm,   mut,   lent,  of(/*impossible*/)); }
  @Example void t2232(){ c2(read,  mut,   lent,  of(/*impossible*/)); }
  @Example void t2233(){ c2(lent,  mut,   lent,   of(read,lent)); }
  @Example void t2234(){ c2(mut,   mut,   lent,   of(read,lent)); }
  @Example void t2235(){ c2(iso,   mut,   lent,   of(read,lent)); }
  @Example void t2236(){ c2(mdf,   mut,   lent,   of(/*not well formed lambda*/)); }
  @Example void t2237(){ c2(recMdf,mut,   lent,  of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2241(){ c2(imm,   iso,   lent,    of(/*impossible*/)); } //TODO: Marco up to here
  @Example void t2242(){ c2(read,  iso,   lent,    of(/*impossible*/)); }
  @Example void t2243(){ c2(lent,  iso,   lent,   of(imm,read)); }
  @Example void t2244(){ c2(mut,   iso,   lent,   of(imm,read)); }
  @Example void t2245(){ c2(iso,   iso,   lent,   of(imm,read)); }
  @Example void t2246(){ c2(mdf,   iso,   lent,   of(/*not well formed lambda*/)); }
  @Example void t2247(){ c2(recMdf,iso,   lent,   of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2251(){ c2(imm,   mdf,   lent, of(/*not well formed parameter with mdf*/)); }
  @Example void t2252(){ c2(read,  mdf,   lent, of(/*not well formed parameter with mdf*/)); }
  @Example void t2253(){ c2(lent,  mdf,   lent, of(/*not well formed parameter with mdf*/)); }
  @Example void t2254(){ c2(mut,   mdf,   lent, of(/*not well formed parameter with mdf*/)); }
  @Example void t2255(){ c2(iso,   mdf,   lent, of(/*not well formed parameter with mdf*/)); }
  @Example void t2256(){ c2(mdf,   mdf,   lent, of(/*not well formed parameter with mdf*/)); }
  @Example void t2257(){ c2(recMdf,mdf,   lent, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Example void t2261(){ c2(imm,   recMdf,   lent,  of(/*impossible*/)); }
  @Example void t2262(){ c2(read,  recMdf,   lent,  of(/*impossible*/)); }
  @Example void t2263(){ c2(lent,  recMdf,   lent,   of(read)); }
  @Example void t2264(){ c2(mut,   recMdf,   lent,  of(/*impossible*/)); }
  @Example void t2265(){ c2(iso,   recMdf,   lent,  of(/*impossible*/)); }
  @Example void t2266(){ c2(mdf,   recMdf,   lent,   of(/*not well formed lambda*/)); }
  @Example void t2267(){ c2(recMdf,recMdf,   lent,   of(read)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2271(){ c2(imm,   imm,   lent,    of(/*impossible*/)); }
  @Example void t2272(){ c2(read,  imm,   lent,    of(/*impossible*/)); }
  @Example void t2273(){ c2(lent,  imm,   lent,   of(read,imm)); }
  @Example void t2274(){ c2(mut,   imm,   lent,   of(read,imm)); }
  @Example void t2275(){ c2(iso,   imm,   lent,   of(read,imm)); }
  @Example void t2276(){ c2(mdf,   imm,   lent,   of(/*not well formed lambda*/)); }
  @Example void t2277(){ c2(recMdf,imm,   lent,   of(read,imm)); }

  //                     lambda, captured, method, ...capturedAs
  @Example void t2301(){ c2(imm,   imm,   mut,    of(/*impossible*/)); }
  @Example void t2302(){ c2(read,  imm,   mut,    of(/*impossible*/)); }
  @Example void t2303(){ c2(lent,  imm,   mut,   of(imm,read)); }
  @Example void t2304(){ c2(mut,   imm,   mut,   of(imm,read)); }
  @Example void t2305(){ c2(iso,   imm,   mut,   of(imm,read)); }
  @Example void t2306(){ c2(mdf,   imm,   mut,   of(/*not well formed lambda*/)); }
  @Example void t2307(){ c2(recMdf,imm,   mut,   of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2311(){ c2(imm,   read,  mut,   of(/*impossible*/)); }
  @Example void t2312(){ c2(read,  read,  mut,   of(/*impossible*/)); }
  @Example void t2313(){ c2(lent,  read,  mut,   of(read)); } // yes because call I can call the mut method through a lent
  @Example void t2314(){ c2(mut,   read,  mut,   of(/*impossible*/)); }//NOT NoMutHyg
  @Example void t2315(){ c2(iso,   read,  mut,   of(/*impossible*/)); }
  @Example void t2316(){ c2(mdf,   read,  mut,   of(/*not well formed lambda*/)); }
  @Example void t2317(){ c2(recMdf,read,  mut,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2321(){ c2(imm,   lent,  mut,   of(/*impossible*/)); }
  @Example void t2322(){ c2(read,  lent,  mut,   of(/*impossible*/)); } // this capture is fine because the method cannot ever be called
  @Example void t2323(){ c2(lent,  lent,  mut,   of(read,lent)); }
  @Example void t2324(){ c2(mut,   lent,  mut,   of(/*impossible*/)); }//NOT NoMutHyg
  @Example void t2325(){ c2(iso,   lent,  mut,   of(/*impossible*/)); }
  @Example void t2326(){ c2(mdf,   lent,  mut,   of(/*not well formed lambda*/)); }
  @Example void t2327(){ c2(recMdf,lent,  mut,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2331(){ c2(imm,   mut,   mut,  of(/*impossible*/)); }
  @Example void t2332(){ c2(read,  mut,   mut,  of(/*impossible*/)); }
  @Example void t2333(){ c2(lent,  mut,   mut,   of(read,lent)); } // TODO: double check this
  @Example void t2334(){ c2(mut,   mut,   mut,   of(read,lent,mut)); }
  @Example void t2335(){ c2(iso,   mut,   mut,   of(read,lent,mut)); }
  @Example void t2336(){ c2(mdf,   mut,   mut,   of(/*not well formed lambda*/)); }
  @Example void t2337(){ c2(recMdf,mut,   mut,  of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2341(){ c2(imm,   iso,   mut,    of(/*impossible*/)); }
  @Example void t2342(){ c2(read,  iso,   mut,    of(/*impossible*/)); }
  @Example void t2343(){ c2(lent,  iso,   mut,   of(imm,read)); }
  @Example void t2344(){ c2(mut,   iso,   mut,   of(imm,read)); }
  @Example void t2345(){ c2(iso,   iso,   mut,   of(imm,read)); }
  @Example void t2346(){ c2(mdf,   iso,   mut,   of(/*not well formed lambda*/)); }
  @Example void t2347(){ c2(recMdf,iso,   mut,   of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2351(){ c2(imm,   mdf,   mut, of(/*not well formed parameter with mdf*/)); }
  @Example void t2352(){ c2(read,  mdf,   mut, of(/*not well formed parameter with mdf*/)); }
  @Example void t2353(){ c2(lent,  mdf,   mut, of(/*not well formed parameter with mdf*/)); }
  @Example void t2354(){ c2(mut,   mdf,   mut, of(/*not well formed parameter with mdf*/)); }
  @Example void t2355(){ c2(iso,   mdf,   mut, of(/*not well formed parameter with mdf*/)); }
  @Example void t2356(){ c2(mdf,   mdf,   mut, of(/*not well formed parameter with mdf*/)); }
  @Example void t2357(){ c2(recMdf,mdf,   mut, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Example void t2361(){ c2(imm,   recMdf,   mut,  of(/*impossible*/)); }
  @Example void t2362(){ c2(read,  recMdf,   mut,  of(/*impossible*/)); }
  @Example void t2363(){ c2(lent,  recMdf,   mut,   of(read)); }
  @Example void t2364(){ c2(mut,   recMdf,   mut,  of(/*impossible*/)); }
  @Example void t2365(){ c2(iso,   recMdf,   mut,  of(/*impossible*/)); }
  @Example void t2366(){ c2(mdf,   recMdf,   mut,   of(/*not well formed lambda*/)); }
  @Example void t2367(){ c2(recMdf,recMdf,   mut,   of(read)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2371(){ c2(imm,   imm,   mut,    of(/*impossible*/)); }
  @Example void t2372(){ c2(read,  imm,   mut,    of(/*impossible*/)); }
  @Example void t2373(){ c2(lent,  imm,   mut,   of(read,imm)); }
  @Example void t2374(){ c2(mut,   imm,   mut,   of(read,imm)); }
  @Example void t2375(){ c2(iso,   imm,   mut,   of(read,imm)); }
  @Example void t2376(){ c2(mdf,   imm,   mut,   of(/*not well formed lambda*/)); }
  @Example void t2377(){ c2(recMdf,imm,   mut,   of(read,imm)); }

  //                     lambda, captured, method, ...capturedAs
  @Example void t2401(){ c2(imm,   imm,   iso,    of(/*impossible*/)); }
  @Example void t2402(){ c2(read,  imm,   iso,    of(/*impossible*/)); }
  @Example void t2403(){ c2(lent,  imm,   iso,   of(imm,read)); }
  @Example void t2404(){ c2(mut,   imm,   iso,   of(imm,read)); }
  @Example void t2405(){ c2(iso,   imm,   iso,   of(imm,read)); }
  @Example void t2406(){ c2(mdf,   imm,   iso,   of(/*not well formed lambda*/)); }
  @Example void t2407(){ c2(recMdf,imm,   iso,   of(imm,read)); } // yes, recMdf could be iso
  //                     lambda, captured, method, ...capturedAs
  @Example void t2411(){ c2(imm,   read,  iso,   of(/*impossible*/)); }
  @Example void t2412(){ c2(read,  read,  iso,   of(/*impossible*/)); }
  @Example void t2413(){ c2(lent,  read,  iso,   of(read)); }
  @Example void t2414(){ c2(mut,   read,  iso,   of(/*impossible*/)); }//NOT NoMutHyg
  @Example void t2415(){ c2(iso,   read,  iso,   of(/*impossible*/)); }
  @Example void t2416(){ c2(mdf,   read,  iso,   of(/*not well formed lambda*/)); }
  @Example void t2417(){ c2(recMdf,read,  iso,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2421(){ c2(imm,   lent,  iso,   of(/*impossible*/)); }
  @Example void t2422(){ c2(read,  lent,  iso,   of(/*impossible*/)); }
  @Example void t2423(){ c2(lent,  lent,  iso,   of(read,lent)); }
  @Example void t2424(){ c2(mut,   lent,  iso,   of(/*impossible*/)); }//NOT NoMutHyg
  @Example void t2425(){ c2(iso,   lent,  iso,   of(/*impossible*/)); }
  @Example void t2426(){ c2(mdf,   lent,  iso,   of(/*not well formed lambda*/)); }
  @Example void t2427(){ c2(recMdf,lent,  iso,   of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2431(){ c2(imm,   mut,   iso,  of(/*impossible*/)); }
  @Example void t2432(){ c2(read,  mut,   iso,  of(/*impossible*/)); }
  @Example void t2433(){ c2(lent,  mut,   iso,   of(read,lent,mut)); } // These 3 look odd, but it's correct because iso lambdas are treated like mut
  @Example void t2434(){ c2(mut,   mut,   iso,   of(read,lent,mut)); }
  @Example void t2435(){ c2(iso,   mut,   iso,   of(read,lent,mut)); }
  @Example void t2436(){ c2(mdf,   mut,   iso,   of(/*not well formed lambda*/)); }
  @Example void t2437(){ c2(recMdf,mut,   iso,  of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2441(){ c2(imm,   iso,   iso,    of(/*impossible*/)); }
  @Example void t2442(){ c2(read,  iso,   iso,    of(/*impossible*/)); }
  @Example void t2443(){ c2(lent,  iso,   iso,   of(imm,read)); }
  @Example void t2444(){ c2(mut,   iso,   iso,   of(imm,read)); }
  @Example void t2445(){ c2(iso,   iso,   iso,   of(imm,read)); } // all iso is captured as imm
  @Example void t2446(){ c2(mdf,   iso,   iso,   of(/*not well formed lambda*/)); }
  @Example void t2447(){ c2(recMdf,iso,   iso,   of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2451(){ c2(imm,   mdf,   iso, of(/*not well formed parameter with mdf*/)); }
  @Example void t2452(){ c2(read,  mdf,   iso, of(/*not well formed parameter with mdf*/)); }
  @Example void t2453(){ c2(lent,  mdf,   iso, of(/*not well formed parameter with mdf*/)); }
  @Example void t2454(){ c2(mut,   mdf,   iso, of(/*not well formed parameter with mdf*/)); }
  @Example void t2455(){ c2(iso,   mdf,   iso, of(/*not well formed parameter with mdf*/)); }
  @Example void t2456(){ c2(mdf,   mdf,   iso, of(/*not well formed parameter with mdf*/)); }
  @Example void t2457(){ c2(recMdf,mdf,   iso, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Example void t2461(){ c2(imm,   recMdf,   iso,  of(/*impossible*/)); }
  @Example void t2462(){ c2(read,  recMdf,   iso,  of(/*impossible*/)); }
  @Example void t2463(){ c2(lent,  recMdf,   iso,   of(read)); }
  @Example void t2464(){ c2(mut,   recMdf,   iso,  of(/*impossible*/)); }
  @Example void t2465(){ c2(iso,   recMdf,   iso,  of(/*impossible*/)); }
  @Example void t2466(){ c2(mdf,   recMdf,   iso,   of(/*not well formed lambda*/)); }
  @Example void t2467(){ c2(recMdf,recMdf,   iso,   of(read)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2471(){ c2(imm,   imm,   iso,    of(/*impossible*/)); }
  @Example void t2472(){ c2(read,  imm,   iso,    of(/*impossible*/)); }
  @Example void t2473(){ c2(lent,  imm,   iso,   of(read,imm)); }
  @Example void t2474(){ c2(mut,   imm,   iso,   of(read,imm)); }
  @Example void t2475(){ c2(iso,   imm,   iso,   of(read,imm)); }
  @Example void t2476(){ c2(mdf,   imm,   iso,   of(/*not well formed lambda*/)); }
  @Example void t2477(){ c2(recMdf,imm,   iso,   of(read,imm)); }

  //                     lambda, captured, method, ...capturedAs
  @Example void t2501(){ c2(imm,   imm,   mdf, of(/*not well formed method*/)); }
  @Example void t2502(){ c2(read,  imm,   mdf, of(/*not well formed method*/)); }
  @Example void t2503(){ c2(lent,  imm,   mdf, of(/*not well formed method*/)); }
  @Example void t2504(){ c2(mut,   imm,   mdf, of(/*not well formed method*/)); }
  @Example void t2505(){ c2(iso,   imm,   mdf, of(/*not well formed method*/)); }
  @Example void t2506(){ c2(mdf,   imm,   mdf, of(/*not well formed method*/)); }
  @Example void t2507(){ c2(recMdf,imm,   mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2511(){ c2(imm,   read,  mdf, of(/*not well formed method*/)); }
  @Example void t2512(){ c2(read,  read,  mdf, of(/*not well formed method*/)); }
  @Example void t2513(){ c2(lent,  read,  mdf, of(/*not well formed method*/)); }
  @Example void t2514(){ c2(mut,   read,  mdf, of(/*not well formed method*/)); }
  @Example void t2515(){ c2(iso,   read,  mdf, of(/*not well formed method*/)); }
  @Example void t2516(){ c2(mdf,   read,  mdf, of(/*not well formed method*/)); }
  @Example void t2517(){ c2(recMdf,read,  mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2521(){ c2(imm,   lent,  mdf, of(/*not well formed method*/)); }
  @Example void t2522(){ c2(read,  lent,  mdf, of(/*not well formed method*/)); }
  @Example void t2523(){ c2(lent,  lent,  mdf, of(/*not well formed method*/)); }
  @Example void t2524(){ c2(mut,   lent,  mdf, of(/*not well formed method*/)); }
  @Example void t2525(){ c2(iso,   lent,  mdf, of(/*not well formed method*/)); }
  @Example void t2526(){ c2(mdf,   lent,  mdf, of(/*not well formed method*/)); }
  @Example void t2527(){ c2(recMdf,lent,  mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2531(){ c2(imm,   mut,   mdf, of(/*not well formed method*/)); }
  @Example void t2532(){ c2(read,  mut,   mdf, of(/*not well formed method*/)); }
  @Example void t2533(){ c2(lent,  mut,   mdf, of(/*not well formed method*/)); }
  @Example void t2534(){ c2(mut,   mut,   mdf, of(/*not well formed method*/)); }
  @Example void t2535(){ c2(iso,   mut,   mdf, of(/*not well formed method*/)); }
  @Example void t2536(){ c2(mdf,   mut,   mdf, of(/*not well formed method*/)); }
  @Example void t2537(){ c2(recMdf,mut,   mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2541(){ c2(imm,   iso,   mdf, of(/*not well formed method*/)); }
  @Example void t2542(){ c2(read,  iso,   mdf, of(/*not well formed method*/)); }
  @Example void t2543(){ c2(lent,  iso,   mdf, of(/*not well formed method*/)); }
  @Example void t2544(){ c2(mut,   iso,   mdf, of(/*not well formed method*/)); }
  @Example void t2545(){ c2(iso,   iso,   mdf, of(/*not well formed method*/)); }
  @Example void t2546(){ c2(mdf,   iso,   mdf, of(/*not well formed method*/)); }
  @Example void t2547(){ c2(recMdf,iso,   mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2551(){ c2(imm,   mdf,   mdf, of(/*not well formed method*/)); }
  @Example void t2552(){ c2(read,  mdf,   mdf, of(/*not well formed method*/)); }
  @Example void t2553(){ c2(lent,  mdf,   mdf, of(/*not well formed method*/)); }
  @Example void t2554(){ c2(mut,   mdf,   mdf, of(/*not well formed method*/)); }
  @Example void t2555(){ c2(iso,   mdf,   mdf, of(/*not well formed method*/)); }
  @Example void t2556(){ c2(mdf,   mdf,   mdf, of(/*not well formed method*/)); }
  @Example void t2557(){ c2(recMdf,mdf,   mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2561(){ c2(imm,   recMdf,   mdf, of(/*not well formed method*/)); }
  @Example void t2562(){ c2(read,  recMdf,   mdf, of(/*not well formed method*/)); }
  @Example void t2563(){ c2(lent,  recMdf,   mdf, of(/*not well formed method*/)); }
  @Example void t2564(){ c2(mut,   recMdf,   mdf, of(/*not well formed method*/)); }
  @Example void t2565(){ c2(iso,   recMdf,   mdf, of(/*not well formed method*/)); }
  @Example void t2566(){ c2(mdf,   recMdf,   mdf, of(/*not well formed method*/)); }
  @Example void t2567(){ c2(recMdf,recMdf,   mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2571(){ c2(imm,   imm,   mdf, of(/*not well formed method*/)); }
  @Example void t2572(){ c2(read,  imm,   mdf, of(/*not well formed method*/)); }
  @Example void t2573(){ c2(lent,  imm,   mdf, of(/*not well formed method*/)); }
  @Example void t2574(){ c2(mut,   imm,   mdf, of(/*not well formed method*/)); }
  @Example void t2575(){ c2(iso,   imm,   mdf, of(/*not well formed method*/)); }
  @Example void t2576(){ c2(mdf,   imm,   mdf, of(/*not well formed method*/)); }
  @Example void t2577(){ c2(recMdf,imm,   mdf, of(/*not well formed method*/)); }

  //                     lambda, captured, method, ...capturedAs
  @Example void t2601(){ c2(imm,   imm,   recMdf, of(/*not well formed method*/)); }
  @Example void t2602(){ c2(read,  imm,   recMdf, of(/*not well formed method*/)); }
  @Example void t2603(){ c2(lent,  imm,   recMdf, of(/*not well formed method*/)); }
  @Example void t2604(){ c2(mut,   imm,   recMdf, of(/*not well formed method*/)); }
  @Example void t2605(){ c2(iso,   imm,   recMdf, of(/*not well formed method*/)); }
  @Example void t2606(){ c2(mdf,   imm,   recMdf, of(/*not well formed method*/)); }
  @Example void t2607(){ c2(recMdf,imm,   recMdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2611(){ c2(imm,   read,  recMdf, of(/*not well formed method*/)); }
  @Example void t2612(){ c2(read,  read,  recMdf, of(/*not well formed method*/)); }
  @Example void t2613(){ c2(lent,  read,  recMdf, of(/*not well formed method*/)); }
  @Example void t2614(){ c2(mut,   read,  recMdf, of(/*not well formed method*/)); }
  @Example void t2615(){ c2(iso,   read,  recMdf, of(/*not well formed method*/)); }
  @Example void t2616(){ c2(mdf,   read,  recMdf, of(/*not well formed method*/)); }
  @Example void t2617(){ c2(recMdf,read,  recMdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2621(){ c2(imm,   lent,  recMdf, of(/*not well formed method*/)); }
  @Example void t2622(){ c2(read,  lent,  recMdf, of(/*not well formed method*/)); }
  @Example void t2623(){ c2(lent,  lent,  recMdf, of(/*not well formed method*/)); }
  @Example void t2624(){ c2(mut,   lent,  recMdf, of(/*not well formed method*/)); }
  @Example void t2625(){ c2(iso,   lent,  recMdf, of(/*not well formed method*/)); }
  @Example void t2626(){ c2(mdf,   lent,  recMdf, of(/*not well formed method*/)); }
  @Example void t2627(){ c2(recMdf,lent,  recMdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2631(){ c2(imm,   mut,   recMdf, of(/*not well formed method*/)); }
  @Example void t2632(){ c2(read,  mut,   recMdf, of(/*not well formed method*/)); }
  @Example void t2633(){ c2(lent,  mut,   recMdf, of(/*not well formed method*/)); }
  @Example void t2634(){ c2(mut,   mut,   recMdf, of(/*not well formed method*/)); }
  @Example void t2635(){ c2(iso,   mut,   recMdf, of(/*not well formed method*/)); }
  @Example void t2636(){ c2(mdf,   mut,   recMdf, of(/*not well formed method*/)); }
  @Example void t2637(){ c2(recMdf,mut,   recMdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2641(){ c2(imm,   iso,   recMdf, of(/*not well formed method*/)); }
  @Example void t2642(){ c2(read,  iso,   recMdf, of(/*not well formed method*/)); }
  @Example void t2643(){ c2(lent,  iso,   recMdf, of(/*not well formed method*/)); }
  @Example void t2644(){ c2(mut,   iso,   recMdf, of(/*not well formed method*/)); }
  @Example void t2645(){ c2(iso,   iso,   recMdf, of(/*not well formed method*/)); }
  @Example void t2646(){ c2(mdf,   iso,   recMdf, of(/*not well formed method*/)); }
  @Example void t2647(){ c2(recMdf,iso,   recMdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2651(){ c2(imm,   mdf,   recMdf, of(/*not well formed method*/)); }
  @Example void t2652(){ c2(read,  mdf,   recMdf, of(/*not well formed method*/)); }
  @Example void t2653(){ c2(lent,  mdf,   recMdf, of(/*not well formed method*/)); }
  @Example void t2654(){ c2(mut,   mdf,   recMdf, of(/*not well formed method*/)); }
  @Example void t2655(){ c2(iso,   mdf,   recMdf, of(/*not well formed method*/)); }
  @Example void t2656(){ c2(mdf,   mdf,   recMdf, of(/*not well formed method*/)); }
  @Example void t2657(){ c2(recMdf,mdf,   recMdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2661(){ c2(imm,   recMdf,   recMdf, of(/*not well formed method*/)); }
  @Example void t2662(){ c2(read,  recMdf,   recMdf, of(/*not well formed method*/)); }
  @Example void t2663(){ c2(lent,  recMdf,   recMdf, of(/*not well formed method*/)); }
  @Example void t2664(){ c2(mut,   recMdf,   recMdf, of(/*not well formed method*/)); }
  @Example void t2665(){ c2(iso,   recMdf,   recMdf, of(/*not well formed method*/)); }
  @Example void t2666(){ c2(mdf,   recMdf,   recMdf, of(/*not well formed method*/)); }
  @Example void t2667(){ c2(recMdf,recMdf,   recMdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t2671(){ c2(imm,   imm,   recMdf, of(/*not well formed method*/)); }
  @Example void t2672(){ c2(read,  imm,   recMdf, of(/*not well formed method*/)); }
  @Example void t2673(){ c2(lent,  imm,   recMdf, of(/*not well formed method*/)); }
  @Example void t2674(){ c2(mut,   imm,   recMdf, of(/*not well formed method*/)); }
  @Example void t2675(){ c2(iso,   imm,   recMdf, of(/*not well formed method*/)); }
  @Example void t2676(){ c2(mdf,   imm,   recMdf, of(/*not well formed method*/)); }
  @Example void t2677(){ c2(recMdf,imm,   recMdf, of(/*not well formed method*/)); }
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
@Example void t051(){ c(imm,   mdf,   imm); }
@Example void t052(){ c(read,  mdf,   imm,   of(imm,read)); }
@Example void t053(){ c(lent,  mdf,   imm,   of(imm,read)); }
@Example void t054(){ c(mut,   mdf,   imm); }//NOT NoMutHyg
@Example void t055(){ c(iso,   mdf,   imm); }//NOT NoMutHyg
@Example void t056(){ c(mdf,   mdf,   imm); }
@Example void t057(){ c(recMdf,mdf,   imm); }
 */
