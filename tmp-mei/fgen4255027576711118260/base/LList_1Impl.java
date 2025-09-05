package base;
public record LList_1Impl() implements base.LList_1 {
  public base.Opt_1 tryGet$imm(long i_m$) {
  return  base.LList_1.tryGet$imm$fun(i_m$, this);
}

public base.Opt_1 tryGet$read(long i_m$) {
  return  base.LList_1.tryGet$read$fun(i_m$, this);
}

public base.Opt_1 tryGet$mut(long i_m$) {
  return  base.LList_1.tryGet$mut$fun(i_m$, this);
}

public base.iter.Iter_1 iter$read() {
  return  base.LList_1.iter$read$fun(this);
}

public base.iter.Iter_1 iter$imm() {
  return  base.LList_1.iter$imm$fun(this);
}

public base.iter.Iter_1 iter$mut() {
  return  base.LList_1.iter$mut$fun(this);
}

public base.Bool_0 isEmpty$read() {
  return  base.LList_1.isEmpty$read$fun(this);
}

public base.LList_1 tail$imm() {
  return  base.LList_1.tail$imm$fun(this);
}

public base.LList_1 tail$read() {
  return  base.LList_1.tail$read$fun(this);
}

public base.LList_1 tail$mut() {
  return  base.LList_1.tail$mut$fun(this);
}

public base.Opt_1 head$imm() {
  return  base.LList_1.head$imm$fun(this);
}

public base.Opt_1 head$read() {
  return  base.LList_1.head$read$fun(this);
}

public base.Opt_1 head$mut() {
  return  base.LList_1.head$mut$fun(this);
}

public base.List_1 list$mut() {
  return  base.LList_1.list$mut$fun(this);
}

public base.flows.FlowOp_1 _flowread$read() {
  return  base.LList_1._flowread$read$fun(this);
}

public base.LList_1 $plus$plus$imm(base.LList_1 l1_m$) {
  return  base.LList_1.$plus$plus$imm$fun(l1_m$, this);
}

public base.LList_1 $plus$plus$read(base.LList_1 l1_m$) {
  return  base.LList_1.$plus$plus$read$fun(l1_m$, this);
}

public base.LList_1 $plus$plus$mut(base.LList_1 l1_m$) {
  return  base.LList_1.$plus$plus$mut$fun(l1_m$, this);
}

public Object get$imm(long i_m$) {
  return  base.LList_1.get$imm$fun(i_m$, this);
}

public Object get$read(long i_m$) {
  return  base.LList_1.get$read$fun(i_m$, this);
}

public Object get$mut(long i_m$) {
  return  base.LList_1.get$mut$fun(i_m$, this);
}

public Object match$read(base.LListMatchRead_2 m_m$) {
  return  base.LList_1.match$read$fun(m_m$, this);
}

public Object match$mut(base.LListMatch_2 m_m$) {
  return  base.LList_1.match$mut$fun(m_m$, this);
}

public base.flows.Flow_1 flow$imm() {
  return  base.LList_1.flow$imm$fun(this);
}

public base.flows.Flow_1 flow$read() {
  return  base.LList_1.flow$read$fun(this);
}

public base.flows.Flow_1 flow$mut() {
  return  base.LList_1.flow$mut$fun(this);
}

public base.flows.FlowOp_1 _flowimm$imm() {
  return  base.LList_1._flowimm$imm$fun(this);
}

public Long size$read() {
  return  base.LList_1.size$read$fun(this);
}

public base.LList_1 $plus$imm(Object e_m$) {
  return  base.LList_1.$plus$imm$fun(e_m$, this);
}

public base.LList_1 $plus$read(Object e_m$) {
  return  base.LList_1.$plus$read$fun(e_m$, this);
}

public base.LList_1 $plus$mut(Object e_m$) {
  return  base.LList_1.$plus$mut$fun(e_m$, this);
}

public base.LList_1 pushFront$read(Object e_m$) {
  return  base.LList_1.pushFront$read$fun(e_m$, this);
}

public base.LList_1 pushFront$mut(Object e_m$) {
  return  base.LList_1.pushFront$mut$fun(e_m$, this);
}

  
}
