package program.typesystem;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.EnumSet.of;
import static program.Program.isSubType;
import static id.Mdf.*;
import id.Mdf;
import utils.OneOr;

//LUB (Least Upper Bound):
//The smallest element uu such that x≤ux≤u for all elements xx in the set.
//GLB (Greatest Lower Bound):
//The largest element ll such that l≤xl≤x for all elements xx in the set.
public class MdfLubGlb {
  private static final EnumSet<Mdf> allMdf = EnumSet.copyOf(
    EnumSet.allOf(Mdf.class).stream()
      .filter(Mdf::isSyntaxMdf)
      .collect(Collectors.toSet()));
  private static final Map<Set<Mdf>, Mdf> lubMap = new HashMap<>();
  private static final Map<Set<Mdf>, Mdf> glbMap = new HashMap<>();
  static void init(EnumSet<Mdf> options,Mdf lub,Mdf glb){
    var novel1= lubMap.put(options,lub);
    var novel2= glbMap.put(options, glb);
    assert novel1==null && novel2==null;//both new entries
    assert options.stream()
      .allMatch(x -> isSubType(lub,x)) : "LUB violated";
    assert options.stream()
      .allMatch(x -> isSubType(x,glb)) : "GLB violated";
   
    assert allMdf.stream()
      .filter(lubC -> options.stream().allMatch(x -> isSubType( lubC,x)))//all possible 'lub candidates'
      .filter(lubC->lubC!=lub)//all 'other possible' lub candidates
      .allMatch(lubC -> isSubType(lubC, lub))
      : "LUB is not the least";

    assert allMdf.stream()
    .filter(glbC -> options.stream().allMatch(x -> isSubType( glbC,x)))//all possible 'glb candidates'
    .filter(glbC->glbC!=glb)//all 'other possible' lub candidates
    .allMatch(glbC -> isSubType(glbC, glb))
    : "GLP is not the greates";
  }
  static {
    init(of(iso),      iso,     iso);
    init(of(imm),      imm,     imm);
    init(of(mut),      mut,     mut);
    init(of(lent),     lent,    lent);
    init(of(read),     read,    read);
    init(of(readOnly), readOnly,readOnly);

    init(of(iso,imm),        iso, imm);
    init(of(iso,mut),        iso, mut);
    init(of(iso,lent),       iso, lent);
    init(of(iso, read),      iso, read);
    init(of(iso, readOnly),  iso, readOnly);
    init(of(imm, mut),       iso, read);//is it read?
    init(of(imm, lent),      iso, read);
    init(of(imm, read),      imm, read);
    init(of(imm, readOnly),  imm, readOnly);
    init(of(mut, lent),      mut, lent);
    init(of(mut, read),      mut, read);
    init(of(mut, readOnly),  mut, readOnly);
    init(of(lent, read),     mut, readOnly);
    init(of(lent, readOnly), mut, readOnly);
    init(of(read, readOnly), read,readOnly);

    init(of(iso, imm, mut),      iso,  read);
    init(of(iso, imm, lent),     iso,  readOnly);
    init(of(iso, imm, read),     iso,  read);
    init(of(iso, imm, readOnly), iso);
    init(of(iso, mut, lent), iso);
    init(of(iso, mut, read), iso);
    init(of(iso, mut, readOnly), iso);
    init(of(iso, lent, read), iso);
    init(of(iso, lent, readOnly), iso);
    init(of(iso, read, readOnly), iso);
    init(of(imm, mut, lent), iso);
    init(of(imm, mut, read), iso);
    init(of(imm, mut, readOnly), iso);
    init(of(imm, lent, read), iso);
    init(of(imm, lent, readOnly), iso);
    init(of(imm, read, readOnly), imm);
    init(of(mut, lent, read), iso);
    init(of(mut, lent, readOnly), mut);
    init(of(mut, read, readOnly), iso);
    init(of(lent, read, readOnly), iso);

    init(of(iso, imm, mut, lent), iso);
    init(of(iso, imm, mut, read), iso);
    init(of(iso, imm, mut, readOnly), iso);
    init(of(iso, imm, lent, read), iso);
    init(of(iso, imm, lent, readOnly), iso);
    init(of(iso, imm, read, readOnly), iso);
    init(of(iso, mut, lent, read), iso);
    init(of(iso, mut, lent, readOnly), iso);
    init(of(iso, mut, read, readOnly), iso);
    init(of(iso, lent, read, readOnly), iso);
    init(of(imm, mut, lent, read), iso);
    init(of(imm, mut, lent, readOnly), iso);
    init(of(imm, mut, read, readOnly), iso);
    init(of(imm, lent, read, readOnly), iso);
    init(of(mut, lent, read, readOnly), iso);

    init(of(iso, imm, mut, lent, read), iso);
    init(of(iso, imm, mut, lent, readOnly), iso);
    init(of(iso, imm, mut, read, readOnly), iso);
    init(of(iso, imm, lent, read, readOnly), iso);
    init(of(iso, mut, lent, read, readOnly), iso);
    init(of(imm, mut, lent, read, readOnly), iso);

    init(of(iso, imm, mut, lent, read, readOnly), iso);
    assert lubMap.size()==63;
    assert glbMap.size()==63;
  }
}
