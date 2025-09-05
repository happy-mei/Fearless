package base;
public interface _Opt_1 extends base.Sealed_0{
Object $exclamation$imm();

Object $exclamation$read();

Object $exclamation$mut();

Object $pipe$pipe$imm(base.MF_1 $default);

Object $pipe$pipe$read(base.MF_1 $default);

Object $pipe$pipe$mut(base.MF_1 $default);

Object $pipe$imm(Object $default);

Object $pipe$read(Object $default);

Object $pipe$mut(Object $default);

Object match$imm(base.OptMatch_2 m_m$);

Object match$read(base.OptMatch_2 m_m$);

Object match$mut(base.OptMatch_2 m_m$);

base.flows.Flow_1 flow$imm();

base.flows.Flow_1 flow$read();

base.flows.Flow_1 flow$mut();

base.Opt_1 map$imm(base.OptMap_2 f_m$);

base.Opt_1 map$read(base.OptMap_2 f_m$);

base.Opt_1 map$mut(base.OptMap_2 f_m$);

base.Void_0 ifSome$imm(base.MF_2 f_m$);

base.Void_0 ifSome$read(base.MF_2 f_m$);

base.Void_0 ifSome$mut(base.MF_2 f_m$);

base.Opt_1 flatMap$imm(base.OptFlatMap_2 f_m$);

base.Opt_1 flatMap$read(base.OptFlatMap_2 f_m$);

base.Opt_1 flatMap$mut(base.OptFlatMap_2 f_m$);
}