package id;

import parser.Parser;

public class Id {
  public static boolean validM(String m){
    assert m!=null && !m.isEmpty();
    return new parser.Parser(Parser.dummy,m).parseM();      
  }
  public static boolean validDecName(String name){
    assert name!=null && !name.isEmpty();
    return new parser.Parser(Parser.dummy,name).parseFullCN();      
  }
  public static boolean validGX(String name){ 
    assert name!=null && !name.isEmpty();
    return new parser.Parser(Parser.dummy,name).parseGX();      
  }
  public record DecId(String name,int gen){
    public DecId{ assert validDecName(name) && gen>=0; }
    @Override public String toString() {
      return String.format("%s/%d", name, gen);
    }
  }
  public record MethName(String name, int num){
    public MethName{ assert validM(name) && num>=0; }
    @Override public String toString(){ return name; }
  }
}
