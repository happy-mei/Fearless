package base;
public interface ListView_1 extends base.List_1{
base.Opt_1 tryGet$mut(long i_m$);

base.Opt_1 tryGet$read(long i_m$);

base.Opt_1 tryGet$imm(long i_m$);

base.iter.Iter_1 iter$mut();

base.iter.Iter_1 iter$read();

base.iter.Iter_1 iter$imm();

base.Bool_0 isEmpty$read();

base.Opt_1 tryExpose$read(base.List_1 list_m$, long i_m$);

base.List_1 inner$read();

base.Opt_1 takeFirst$mut();

base.flows.FlowOp_1 _flowread$read(long start_m$, long end_m$);

base.Void_0 add$mut(Object e_m$);

base.flows.FlowOp_1 _flowimm$imm(long start_m$, long end_m$);

Object get$mut(long i_m$);

Object get$read(long i_m$);

Object get$imm(long i_m$);

base.Void_0 addAll$mut(base.List_1 other_m$);

base.flows.Flow_1 flow$mut();

base.flows.Flow_1 flow$read();

base.flows.Flow_1 flow$imm();

base.Void_0 clear$mut();

base.List_1 as$read(base.MF_2 f_m$);

base.ListView_1 subList$read(long from_m$, long to_m$);

base.List_1 $plus$mut(Object e_m$);

Long size$read();

Object expose$read(base.List_1 list_m$, long i_m$);
}