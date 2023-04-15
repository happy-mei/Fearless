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
  @Example void t407(){ c1(recMdf,imm,   iso,   of(imm,read)); }
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
  @Example void t433(){ c1(lent,  mut,   iso,   of(read,lent)); } // TODO: double check this
  @Example void t434(){ c1(mut,   mut,   iso,   of(read,lent,mut)); }
  @Example void t435(){ c1(iso,   mut,   iso,   of(read,lent,mut)); }
  @Example void t436(){ c1(mdf,   mut,   iso,   of(/*not well formed lambda*/)); }
  @Example void t437(){ c1(recMdf,mut,   iso,  of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t441(){ c1(imm,   iso,   iso,    of(/*impossible*/)); }
  @Example void t442(){ c1(read,  iso,   iso,    of(/*impossible*/)); }
  @Example void t443(){ c1(lent,  iso,   iso,   of(imm,read)); }
  @Example void t444(){ c1(mut,   iso,   iso,   of(imm,read)); }
  @Example void t445(){ c1(iso,   iso,   iso,   of(imm,read)); }
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
  @Example void t471(){ c1(imm,   imm,   iso,    of(/*impossible*/)); } // todo: up to
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
