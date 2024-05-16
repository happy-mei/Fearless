package program.typesystem;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import ast.T;
import files.Pos;
import ast.E.Sig;
import id.Id.GX;
import id.Id.IT;
import id.Id.MethName;
import id.Id.Ty;
import id.Id;
import id.Mdf;
import program.CM;
import utils.Bug;

//MethName is expressions always have empty mdf.
//Any other MethName will have the mdf.

class TestMultiSigBuilder {

  CM cmFactory(Mdf mdf, Sig s){
    return new CM(){
      public Mdf mdf(){ return mdf; }
      public MethName name() {return Bug.err();}
      public List<String> xs() {return Bug.err();}
      public boolean isAbs() {return Bug.err();}
      public CM withSig(Sig sig) {return Bug.err();}
      public Pos pos() {return Bug.err();}
      public IT<T> c() {return Bug.err();}
      public Sig sig() {return s;}
      public <TT extends Ty> Map<GX<TT>, Set<Mdf>> bounds() {return Bug.err();}
    };
  }
  T dts(Mdf mdf, String name, List<T> ts) {
    return new T(mdf,new IT<T>(name,ts));
  }
  T x(String name) {
    return new T(Mdf.mdf,new GX<T>(name));
  }
  
  MultiSig base(Mdf mdf0, List<T> expectedT,Mdf formalMdf){
    LinkedHashMap<Id.GX<T>, Set<Mdf>> bounds= new LinkedHashMap<>();
    List<T> ts= List.of();
    T ret= dts(Mdf.mut,"a.Foo",List.of());
    XBs xbs= XBs.empty();
    return allData(mdf0,expectedT,formalMdf,bounds,ts,ret,xbs);    
  }
  MultiSig xGen(Mdf mdf0, List<T> expectedT,Mdf formalMdf){
    LinkedHashMap<Id.GX<T>, Set<Mdf>> bounds= new LinkedHashMap<>();
    List<T> ts= List.of();//TODO: what is the purpose of bounds then?
    T ret= x("X");        //bounds vs XBs?
    XBs xbs= XBs.empty().add("X",Set.of(Mdf.mut,Mdf.imm,Mdf.read));
    return allData(mdf0,expectedT,formalMdf,bounds,ts,ret,xbs);    
  }
  MultiSig allData(
    Mdf mdf0, List<T> expectedT,Mdf formalMdf,
    LinkedHashMap<Id.GX<T>, Set<Mdf>> bounds,
    List<T> ts,T ret,XBs xbs){
    var gens=bounds.keySet().stream().toList();
    Sig sig= new Sig(gens,bounds,ts,ret,Optional.empty());
    CM cm= cmFactory(formalMdf,sig);
    return MultiSigBuilder.multiMethod(xbs,cm,mdf0,expectedT);  
  }
  @Test void justMut(){ assertEquals("""
     Attempted signatures:
     ():mut a.Foo[]
     ():lent a.Foo[]
     """,base(Mdf.mut,List.of(),Mdf.mut).toString()); }
  @Test void justImm(){ assertEquals("""
    Attempted signatures:
    ():iso a.Foo[]
    ():iso a.Foo[]
    ():mut a.Foo[]
    ():lent a.Foo[]
    """,base(Mdf.imm,List.of(),Mdf.imm).toString()); }
  @Test void justIso(){ assertEquals("""
    Attempted signatures:
    ():iso a.Foo[]
    ():iso a.Foo[]
    ():mut a.Foo[]
    ():lent a.Foo[]
    """,base(Mdf.iso,List.of(),Mdf.iso).toString()); }
  @Test void justRead(){ assertEquals("""
    Attempted signatures:
    ():mut a.Foo[]
    ():lent a.Foo[]
    """,base(Mdf.read,List.of(),Mdf.read).toString()); }
//--------
  @Test void xGenImm(){ assertEquals("""
    Attempted signatures:
    ():imm X
    ():imm X
    ():imm X
    ():imm X//does not look right! where is base?
    """,xGen(Mdf.imm,List.of(),Mdf.imm).toString()); }


}
