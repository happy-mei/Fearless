package program.typesystem;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import ast.T;
import id.Id.GX;
import id.Id.IT;
import id.Mdf;
import static id.Mdf.*;

//MethName is expressions always have empty mdf.
//Any other MethName will have the mdf.

class TestMultiSigBuilder {

  T dts(Mdf mdf, String name, List<T> ts) {
    return new T(mdf,new IT<T>(name,ts));
  }
  T x(String name) { return mdfX(mdf,name); }
  T mdfX(Mdf mdf,String name) { return new T(mdf,new GX<T>(name));
  }
  
  MultiSig base(Mdf mdf0, List<T> expectedT,Mdf formalMdf){
    List<T> ts= List.of();
    T ret= dts(mut,"a.Foo",List.of());
    XBs xbs= XBs.empty();
    return MultiSigBuilder
      .multiMethod(xbs,formalMdf,ts,ret,mdf0,expectedT)
      .get();
  }
  MultiSig xGen(Mdf mdf0, List<T> expectedT,Mdf formalMdf){
    List<T> ts= List.of();
    T ret= x("X");
    XBs xbs= XBs.empty().add("X",Set.of(mut,imm,read));
    return MultiSigBuilder
      .multiMethod(xbs,formalMdf,ts,ret,mdf0,expectedT)
      .get();
  }
  @Test void justMut(){ assertEquals("""
    Attempted signatures:
    ():mut a.Foo[] kind: Base
    ():mutH a.Foo[] kind: MutHPromRec
    """,base(mut,List.of(),mut).toString()); }
  @Test void justImm(){ assertEquals("""
    Attempted signatures:
    ():iso a.Foo[] kind: IsoHProm
    ():iso a.Foo[] kind: IsoProm
    ():mut a.Foo[] kind: Base
    ():mutH a.Foo[] kind: ReadHProm
    """,base(imm,List.of(),imm).toString()); }
  @Test void justIso(){ assertEquals("""
    Attempted signatures:
    ():iso a.Foo[] kind: IsoHProm
    ():iso a.Foo[] kind: IsoProm
    ():mut a.Foo[] kind: Base
    ():mutH a.Foo[] kind: ReadHProm
    """,base(iso,List.of(),iso).toString()); }
  @Test void justRead(){ assertEquals("""
    Attempted signatures:
    ():mut a.Foo[] kind: Base
    ():mutH a.Foo[] kind: ReadHProm
    """,base(read,List.of(),read).toString()); }
//--------
  @Test void xGenImm(){ assertEquals("""
    Attempted signatures:
    ():imm X kind: IsoHProm
    ():imm X kind: IsoProm
    ():X kind: Base
    ():readH X kind: ReadHProm
    """,xGen(imm,List.of(),imm).toString()); }

//--------
  @Test void singleBoundImmSeenAsImm(){
    List<T> ts= List.of();
    T ret= x("X");
    XBs xbs= XBs.empty().add("X",Set.of(imm));
    var mm= MultiSigBuilder.
      multiMethod(xbs,imm,ts,ret,imm,List.of(mdfX(imm,"X"))).get();
    assertEquals("""
    Attempted signatures:
    ():X kind: IsoHProm
    ():X kind: IsoProm
    ():X kind: Base
    ():X kind: ReadHProm
    """,mm.toString()); }

}
