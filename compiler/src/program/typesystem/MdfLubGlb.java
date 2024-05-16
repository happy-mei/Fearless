package program.typesystem;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.EnumSet.of;
import static program.Program.isSubType;
import static id.Mdf.*;
import id.Mdf;

//LUB (Least Upper Bound):
//The smallest element e such that x≤e for all elements x in the set.
//GLB (Greatest Lower Bound):
//The largest element e such that e≤x for all elements x in the set.
public class MdfLubGlb {
  private static final EnumSet<Mdf> allMdf = EnumSet.copyOf(
    EnumSet.allOf(Mdf.class).stream()
      .filter(Mdf::isSyntaxMdf)
      .collect(Collectors.toSet()));
  private static final Map<Set<Mdf>, Mdf> lubMap = new HashMap<>();
  private static final Map<Set<Mdf>, Mdf> glbMap = new HashMap<>();
  public static final Set<Set<Mdf>> domain(){
    return Collections.unmodifiableSet(lubMap.keySet());
  }
  public static Mdf lub(Set<Mdf> options) { return lubMap.get(options); }
  public static Mdf glb(Set<Mdf> options) { return glbMap.get(options); }
  static boolean isUb(EnumSet<Mdf> options,Mdf ub){
    return options.stream().allMatch(x -> isSubType(ub,x));
  }
  static boolean isLb(EnumSet<Mdf> options,Mdf lb){
    return options.stream().allMatch(x -> isSubType(x,lb));
  }
  static boolean isLub(EnumSet<Mdf> options,Mdf lub){
    var isUb= isUb(options,lub);
    var isLowest= allMdf.stream()
      .filter(mdf -> isUb(options,mdf))
      .allMatch(ub -> isSubType(ub, lub));    
    return isUb && isLowest;
  }
  static boolean isGlb(EnumSet<Mdf> options,Mdf glb){
    var isLb= isLb(options,glb);
    var isGreatest= allMdf.stream()
      .filter(mdf -> isLb(options,mdf))
      .allMatch(lb -> isSubType(glb,lb));    
    return isLb && isGreatest;
  }
  static void init(EnumSet<Mdf> options,Mdf lub,Mdf glb){
    var novel1= lubMap.put(options,lub);
    var novel2= glbMap.put(options, glb);
    assert novel1==null && novel2==null;//both new entries
    assert isLub(options,lub)
      :"not lub: "+lub+" "+options;
    assert isGlb(options,glb)
      :"not glb: "+glb+" "+options;
    var otherLub= allMdf.stream()
      .filter(mdf->mdf!=lub).filter(mdf->isLub(options,mdf)).toList();
    assert otherLub.isEmpty()
      :"not unique Lub: "+otherLub;    
    var otherGlb= allMdf.stream()
      .filter(mdf->mdf!=glb).filter(mdf->isGlb(options,mdf)).toList();
    assert otherGlb.isEmpty()
      :"not unique glb: "+otherGlb;
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
    init(of(imm, mut),       iso, read);//is it read?//test confirms
    init(of(imm, lent),      iso, readOnly); //accidentally wrote read
    init(of(imm, read),      imm, read);
    init(of(imm, readOnly),  imm, readOnly);
    init(of(mut, lent),      mut, lent);
    init(of(mut, read),      mut, read);
    init(of(mut, readOnly),  mut, readOnly);
    init(of(lent, read),     mut, readOnly);
    init(of(lent, readOnly), lent,readOnly);//accidentally wrote mut
    init(of(read, readOnly), read,readOnly);

    init(of(iso, imm, mut),        iso,  read);
    init(of(iso, imm, lent),       iso,  readOnly);
    init(of(iso, imm, read),       iso,  read);
    init(of(iso, imm, readOnly),   iso,  readOnly);
    init(of(iso, mut, lent),       iso,  lent);
    init(of(iso, mut, read),       iso,  read);
    init(of(iso, mut, readOnly),   iso,  readOnly);
    init(of(iso, lent, read),      iso,  readOnly);//accidentally wrote read
    init(of(iso, lent, readOnly),  iso,  readOnly);
    init(of(iso, read, readOnly),  iso,  readOnly);
    init(of(imm, mut, lent),       iso,  readOnly);//accidentally wrote read
    init(of(imm, mut, read),       iso,  read);
    init(of(imm, mut, readOnly),   iso,  readOnly);
    init(of(imm, lent, read),      iso,  readOnly);
    init(of(imm, lent, readOnly),  iso,  readOnly);
    init(of(imm, read, readOnly),  imm,  readOnly);
    init(of(mut, lent, read),      mut,  readOnly);//accidentally wrote read
    init(of(mut, lent, readOnly),  mut,  readOnly);
    init(of(mut, read, readOnly),  mut,  readOnly);
    init(of(lent, read, readOnly), mut, readOnly);//accidentally wrote lent instead of mut

    init(of(iso, imm, mut, lent),       iso, readOnly);
    init(of(iso, imm, mut, read),       iso, read);
    init(of(iso, imm, mut, readOnly),   iso, readOnly);
    init(of(iso, imm, lent, read),      iso, readOnly);
    init(of(iso, imm, lent, readOnly),  iso, readOnly);
    init(of(iso, imm, read, readOnly),  iso, readOnly);
    init(of(iso, mut, lent, read),      iso, readOnly);
    init(of(iso, mut, lent, readOnly),  iso, readOnly);
    init(of(iso, mut, read, readOnly),  iso, readOnly);
    init(of(iso, lent, read, readOnly), iso, readOnly);
    init(of(imm, mut, lent, read),      iso, readOnly);
    init(of(imm, mut, lent, readOnly),  iso, readOnly);
    init(of(imm, mut, read, readOnly),  iso, readOnly);
    init(of(imm, lent, read, readOnly), iso, readOnly);
    init(of(mut, lent, read, readOnly), mut, readOnly);

    init(of(iso, imm, mut, lent, read),      iso, readOnly);
    init(of(iso, imm, mut, lent, readOnly),  iso, readOnly);
    init(of(iso, imm, mut, read, readOnly),  iso, readOnly);
    init(of(iso, imm, lent, read, readOnly), iso, readOnly);
    init(of(iso, mut, lent, read, readOnly), iso, readOnly);
    init(of(imm, mut, lent, read, readOnly), iso, readOnly);

    init(of(iso, imm, mut, lent, read, readOnly), iso, readOnly);
    
    assert lubMap.size()==63;
    assert glbMap.size()==63;
  }
  public static void main(String[] arg) {
    System.out.println("hello world");
    //Java hates us,
    //if the main class has static initialization it dies silently?
    assert false;
  }
  public static class MMain{
    public static void main(String[] arg) {
      System.out.println("hello world");
      System.out.println(MdfLubGlb.lub(EnumSet.of(mut)));
      //6 wrong in the table + 1 doubt resolved
    }    
  }
}