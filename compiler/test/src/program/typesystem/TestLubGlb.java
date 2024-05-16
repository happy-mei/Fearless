package program.typesystem;

import static id.Mdf.mut;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
//import static program.typesystem.RunTypeSystem.ok;
//import static program.typesystem.RunTypeSystem.fail;

import id.Mdf;

class TestLubGlb {

  @Test void test() {
    MdfLubGlb.lub(EnumSet.of(mut));
    //MdfLubGlb is selftesting.
  }
  @Test void testHandMadeLub(){
    for(var mdfs: MdfLubGlb.domain()){
      assertEquals(MdfLubGlb.lub(mdfs),mostSpecific(mdfs),
        "For mdfs= "+mdfs);
    }
  }
  @Test void testHandMadeGlb(){
    for(var mdfs: MdfLubGlb.domain()){
      assertEquals(MdfLubGlb.glb(mdfs),mostGeneral(mdfs),
        "For mdfs= "+mdfs);
    }
  }
  Mdf mostSpecific(Set<Mdf> options){
    return mostSpecGen(options,this::mostSpecWin);
  }
  Mdf mostGeneral(Set<Mdf> options){
    return mostSpecGen(options,this::mostGenWin);
  }
  boolean mostSpecWin(Mdf mi, Mdf mj){ return program.Program.isSubType(mi, mj); }
  boolean mostGenWin(Mdf mi, Mdf mj){ return program.Program.isSubType(mj, mi); }
  Mdf mostSpecGen(Set<Mdf> options, BiPredicate<Mdf,Mdf> test){
    assert !options.isEmpty();
    List<Mdf> res= new ArrayList<>(Stream.of(Mdf.values())
      .filter(Mdf::isSyntaxMdf)
      .filter(mi->options.stream()
        .allMatch(mj->test.test(mi,mj)))
      .toList());
    assert !res.isEmpty();
    return res.stream().max(Comparator.comparingInt(
      EMethTypeSystem.recvPriority::indexOf
      )).get();
  }
}
