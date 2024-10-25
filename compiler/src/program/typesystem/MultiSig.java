package program.typesystem;

import ast.T;
import failure.CompileError;
import id.Mdf;
import utils.Range;

import java.util.List;
import java.util.stream.Collectors;

public record MultiSig(List<Mdf> recvMdfs, List<List<T>> tss, List<T> rets, List<String> kind){
  public MultiSig{
    int size= rets.size();
    assert size >= 1: "No, this should become a type error since we filter on the receiver modifier and expected results";
    assert recvMdfs.size() == size: recvMdfs+" "+tss+" "+rets;
    assert tss.stream().allMatch(ts->ts.size() == size): tss+" "+rets;
  }
  ETypeSystem expectedT(ETypeSystem self, int i) {
    return self.withExpectedTs(tss.get(i));      
  }
  @Override public String toString(){
    String res="Attempted signatures:\n";
    for(var i: Range.of(rets)) {
      var ps= tss.stream()
        .map(ts->ts.get(i))
        .map(Object::toString)
        .collect(Collectors.joining(", "));
      res+="("+ps+"):"+rets.get(i)+" kind: "+kind.get(i)+"\n";
    }
    return res;
  }
}
