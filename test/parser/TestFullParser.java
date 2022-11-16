package parser;

import main.CompileError;
import main.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import utils.Bug;
import utils.Err;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertNull;

class TestFullParser {
  void ok(String expected, String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
        .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
        .toList();
    String res = Parser.parseAll(ps).toString();
    Err.strCmpFormat(expected,res);
  }
  void fail(String expectedErr, String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
        .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
        .toList();
    try {
      var res = Parser.parseAll(ps);
      Assertions.fail("Parsing did not fail. Got: "+res);
    } catch (Bug | CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }
  @Test void testEmptyPackage(){ ok("""
    {}
    """,
    """
    package pkg1
    """); }
  @Test void testMultiFile(){ ok("""
    {}
    """,
      """
      package pkg1
      """,
      """
      package pkg1
      """); }
  @Test void testAliasConflictsPackageLocal1(){ ok("""
    {}
    """,
      """
      package pkg1
      alias base.True as True,
      """,
      """
      package pkg2
      alias base.True as True,
      """); }
  @Test void failConflictingAliases1(){ fail("""
    In position null
    conflictingAlias:1
    This alias is in conflict with other aliases in the same package: True
    conflicts:
    ([###]Dummy0.fear:2:0) alias base.True[] as True
    ([###]Dummy1.fear:2:0) alias base.True[] as True
    """,
      """
      package pkg1
      alias base.True as True,
      """,
      """
      package pkg1
      alias base.True as True,
      """); }
  @Test void testMultiPackage(){ ok("""
    {}
    """,
      """
      package pkg1
      """,
      """
      package pkg2
      """,
      """
      package pkg1
      """); }
  @Test void testOneDecl(){ ok("""
    {pkg1.MyTrue/0=
      Dec[
      name=pkg1.MyTrue,
      xs=[],
      lambda=Lambda[mdf=mdf,its=[base.True[]],
      selfName=null,
      meths=[],
      t=infer]]}
    """,
    """
    package pkg1
    MyTrue:base.True
    """); }
  @Test void testManyDecls(){ ok("""
    {pkg1.My12/0=Dec[
      name=pkg1.My12,xs=[],lambda=Lambda[mdf=mdf,its=[12[]],selfName=null,meths=[],t=infer]
      ],
      pkg1.MyFalse/0=Dec[
        name=pkg1.MyFalse,xs=[],lambda=Lambda[mdf=mdf,its=[base.False[]],selfName=null,meths=[],t=infer]
      ],
      pkg2.MyTrue/0=Dec[
        name=pkg2.MyTrue,xs=[],lambda=Lambda[mdf=mdf,its=[base.True[]],selfName=null,meths=[],t=infer]
      ],
      pkg1.MyTrue/0=Dec[
        name=pkg1.MyTrue,xs=[],lambda=Lambda[mdf=mdf,its=[base.True[]],selfName=null,meths=[],t=infer]
      ]}
    """,
      """
      package pkg1
      MyTrue:base.True
      MyFalse:base.False
      """,
      """
      package pkg1
      alias 12 as Twelve,
      My12:Twelve
      """,
      """
      package pkg2
      MyTrue:base.True
      """); }
  @Test void failConflictingDecls1(){ fail("""
      In position null
      conflictingDecl:2
      This trait declaration is in conflict with other trait declarations in the same package: MyTrue/0
      conflicts:
      ([###]/Dummy0.fear:2:0) MyTrue/0
      ([###]/Dummy1.fear:3:0) MyTrue/0
          """,
    """
    package pkg1
    MyTrue:base.True
    """,
    """
    package pkg1
    MyFalse:base.False
    MyTrue:base.True
    """); }

  @Test void baseVoid(){ ok("""
    {base.Void/0=Dec[
      name=base.Void,
      xs=[],
      lambda=Lambda[mdf=mdf,its=[],selfName=null,meths=[],t=infer]
      ]}
    """, """
    package base
    Void:{}
    """
    );}
  @Test void baseLoopSingleMeth(){ ok("""
    {pkg1.Loop/0=Dec[
      name=pkg1.Loop,
      xs=[],
      lambda=Lambda[mdf=mdf,its=[base.AbstractLoop[]],
      selfName=null,
      meths=[[-]([]):[-]->MCall[receiver=this:infer,name=!,ts=Optional.empty,es=[],t=infer]],
      t=infer]]}
    """, """
    package pkg1
    alias base.AbstractLoop as AbsLoop,
    Loop:AbsLoop{this!}
    """
  );}
  @Test void baseLoop(){ ok("""
    {base.Loop/0=Dec[
      name=base.Loop,
      xs=[],
      lambda=Lambda[
        mdf=mdf,
        its=[],
        selfName=null,
        meths=[
          !([]):Sig[mdf=imm,gens=[],ret=imm base.Void[]]->
            MCall[receiver=this:infer,name=!,ts=Optional.empty,es=[],t=infer]
        ],
        t=infer
      ]]}
    """, """
    package base
    alias base.Void as Void,
    Loop:{!:Void->this!}
    """
  );}
  @Test void baseLoopExplicit(){ ok("""
    {base.Loop/0=Dec[
      name=base.Loop,
      xs=[],
      lambda=Lambda[
        mdf=mdf,
        its=[],
        selfName=null,
        meths=[
          !([]):Sig[mdf=imm,gens=[],ret=imm base.Void[]]->
            MCall[receiver=this:infer,name=!,ts=Optional.empty,es=[],t=infer]
        ],
        t=infer
      ]]}
    """, """
    package base
    alias base.Void as Void,
    Loop:{imm !():imm Void->this!}
    """
  );}
  @Test void baseLoopMoreExplicit(){ ok("""
    {base.Loop/0=Dec[
      name=base.Loop,
      xs=[],
      lambda=Lambda[
        mdf=mdf,
        its=[],
        selfName=null,
        meths=[
          !([]):Sig[mdf=imm,gens=[],ret=imm base.Void[]]->
            MCall[receiver=this:infer,name=!,ts=Optional.empty,es=[],t=infer]
        ],
        t=infer
      ]]}
    """, """
    package base
    alias base.Void as Void,
    Loop[]:{imm ![]():imm Void[]->this!}
    """
  );}
  @Test void baseLoopAbs(){ ok("""
    {base.AbsLoop/0=Dec[
      name=base.AbsLoop,
      xs=[],
      lambda=Lambda[
        mdf=mdf,
        its=[],
        selfName=null,
        meths=[
          !([]):Sig[mdf=imm,gens=[],ret=imm base.Void[]]->[-]
        ],
        t=infer
      ]]}
    """, """
    package base
    AbsLoop:{!:base.Void}
    """
  );}
  @Test void methWithArgs(){ ok("""
    {base.A/0=Dec[
    name=base.A,
    xs=[],
    lambda=Lambda[
      mdf=mdf,
      its=[],
      selfName=null,
      meths=[
        .foo([a:imm base.A[]]):Sig[mdf=imm,gens=[],ret=imm base.A[]]->[-]],t=infer]]}
    """, """
    package base
    A:{.foo(a: A): A,}
    """
  );}
  @Test void methWith2Args(){ ok("""
    {base.A/0=Dec[
      name=base.A,
      xs=[],
      lambda=Lambda[
        mdf=mdf,
        its=[],
        selfName=null,
        meths=[
          .foo([a:imm base.A[],b:imm base.A[]]):Sig[mdf=imm,gens=[],ret=imm base.A[]]->[-]],t=infer]
        ]}
    """, """
    package base
    A:{.foo(a: A, b: A): A,}
    """
  );}
  @Test void methWith2ArgsAndMdf(){ ok("""
    {base.A/0=Dec[
      name=base.A,
      xs=[],
      lambda=Lambda[
        mdf=mdf,
        its=[],
        selfName=null,
        meths=[
          .foo([a:imm base.A[], b:read base.A[]]):Sig[mdf=imm,gens=[],ret=imm base.A[]]->[-]],t=infer]
        ]}
    """, """
    package base
    A:{.foo(a: A, b: read A): A,}
    """
    );}
    @Test void methWithGens1(){ ok("""
    {base.A/0=Dec[
      name=base.A,
      xs=[],
      lambda=Lambda[
        mdf=mdf,
        its=[],
        selfName=null,
        meths=[
          .foo([a:imm base.A[],b:read GX[name=B]]):Sig[mdf=imm,gens=[GX[name=B]],ret=imm base.A[]]->[-]],t=infer]
        ]}
    """, """
    package base
    A:{.foo[B](a: A, b: read B): A,}
    """
    );}
  @Test void methWithGens2(){ ok("""
    {base.A/0=Dec[
      name=base.A,
      xs=[],
      lambda=Lambda[
        mdf=mdf,
        its=[],
        selfName=null,
        meths=[
          .foo([a:imm base.A[],b:read GX[name=B]]):Sig[mdf=imm,gens=[GX[name=B]],ret=read GX[name=B]]->[-]],t=infer]
        ]}
    """, """
    package base
    A:{.foo[B](a: A, b: read B): read B,}
    """
  );}
  @Test void failConcreteInGens(){ fail("""
    In position [###]/Dummy0.fear:2:7
    concreteTypeInFormalParams:3
    Trait and method declarations may only have type parameters. This concrete type was provided instead:
    imm base.A[]
    """, """
    package base
    A:{.foo[A](a: A, b: A): A}
    """
  );}
  @Test void extendsNewDec(){ ok("""
    {base.HasName/0=Dec[
      name=base.HasName,
      xs=[],
      lambda=Lambda[mdf=mdf,its=[],selfName=null,meths=[.name([]):Sig[mdf=imm,gens=[],ret=imm base.String[]]->[-]],t=infer]],
    base.Dog/0=Dec[
      name=base.Dog,
      xs=[],
      lambda=Lambda[mdf=mdf,its=[base.HasName[]],selfName=null,meths=[],t=infer]]}
    """, """
    package base
    alias base.String as String,
    HasName:{ .name: String, }
    Dog:HasName
    """
  );}
  @Test void multipleExtends(){ ok("""
    {base.HasHunger/0=Dec[
      name=base.HasHunger,
      xs=[],
      lambda=Lambda[mdf=mdf,its=[],selfName=null,meths=[.hunger([]):Sig[mdf=imm,gens=[],ret=immbase.UNum[]]->[-]],t=infer]],
    base.HasName/0=Dec[
      name=base.HasName,
      xs=[],
      lambda=Lambda[mdf=mdf,its=[],selfName=null,meths=[.name([]):Sig[mdf=imm,gens=[],ret=immbase.String[]]->[-]],t=infer]],
    base.Dog/0=Dec[
      name=base.Dog,
      xs=[],
      lambda=Lambda[mdf=mdf,its=[base.HasHunger[],base.HasName[]],selfName=null,meths=[],t=infer]]}
    """, """
    package base
    alias base.UNum as UNum, alias base.String as String,
    HasHunger:{ .hunger: UNum, }
    HasName:{ .name: String, }
    Dog:HasHunger,HasName{}
    """
  );}
  @Test void equalsSugar1(){ ok("""
    {base.B/0=Dec[
      name=base.B,
      xs=[],
      lambda=Lambda[
        mdf=mdf,
        its=[],
        selfName=null,
        meths=[#([]):Sig[mdf=imm,gens=[],ret=imm 5[]]->MCall[
          receiver=Lambda[
            mdf=imm,
            its=[base.A[]],
            selfName=null,
            meths=[],
            t=infer],
          name=.foo,
          ts=Optional.empty,
          es=[
            Lambda[mdf=imm,its=[5[]],selfName=null,meths=[],t=infer],
            Lambda[mdf=mdf,its=[],selfName=null,meths=[[-]([lol:infer,fearIntrinsic0:infer]):[-]->fearIntrinsic0:infer],t=infer]
          ],
          t=infer
        ]],
      t=infer]],
    base.Cont/2=Dec[
      name=base.Cont,
      xs=[GX[name=X],GX[name=R]],
      lambda=Lambda[
        mdf=mdf,
        its=[],
        selfName=null,
        meths=[#([x:mdfGX[name=X],self:immbase.A[]]):Sig[mdf=mut,gens=[],ret=mdfGX[name=R]]->[-]],
        t=infer
      ]],
    base.A/0=Dec[
      name=base.A,
      xs=[],
      lambda=Lambda[
        mdf=mdf,
        its=[],
        selfName=null,
        meths=[
          .foo([x:mdf GX[name=T],cont:mut base.Cont[mdf GX[name=T],mdf GX[name=T]]]):Sig[mdf=imm,gens=[GX[name=T]],ret=mdf GX[name=T]]->
            MCall[receiver=cont:infer,name=#,ts=Optional.empty,es=[x:infer,cont:infer],t=infer]
        ],
      t=infer]]}
    """, """
    package base
    Cont[X,R]:{ mut #(x: mdf X, self: A): mdf R }
    A:{ .foo[T](x: mdf T, cont: mut Cont[mdf T, mdf T]): mdf T -> cont#(x, cont) }
    B:{ #: 5 -> A
      .foo (lol=5)
      }
    """
  );}
}
