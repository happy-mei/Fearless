package base;
public interface Str_0 extends base.Sealed_0,base.Stringable_0,base.ToHash_0,base.flows.Joinable_1{
base.Hasher_0 hash$read(base.Hasher_0 hasher_m$);

rt.Str str$read();

base.Action_1 float$imm();

rt.Str join$imm(base.flows.Flow_1 flow_m$);

base.Bool_0 $equals$equals$imm(rt.Str other_m$);

rt.Str substring$imm(long start_m$, long end_m$);

base.Bool_0 $exclamation$equals$imm(rt.Str other_m$);

base.List_1 utf8$imm();

rt.Str charAt$imm(long index_m$);

base.flows.Flow_1 flow$imm();

base.Void_0 assertEq$imm(rt.Str message_m$, rt.Str other_m$);

base.Void_0 assertEq$imm(rt.Str other_m$);

base.Bool_0 isEmpty$read();

base.Void_0 clear$mut();

base.Void_0 append$mut(base.Stringable_0 other_m$);

rt.Str normalise$imm();

base.Bool_0 startsWith$imm(rt.Str other_m$);

rt.Str $plus$mut(base.Stringable_0 other_m$);

rt.Str $plus$imm(base.Stringable_0 other_m$);

Long size$imm();
}