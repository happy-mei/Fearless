package base;
public interface Res_1 extends base.Res_2{
Object $exclamation$mut();

Object $exclamation$read();

Object $exclamation$imm();

base.Opt_1 ok$mut();

base.Opt_1 ok$read();

base.Opt_1 ok$imm();

base.Res_2 mapErr$mut(base.ResMapErr_3 f_m$);

base.Res_2 mapErr$read(base.ResMapErr_3 f_m$);

base.Res_2 mapErr$imm(base.ResMapErr_3 f_m$);

Object match$imm(base.EitherMatch_3 m_m$);

Object match$read(base.EitherMatch_3 m_m$);

Object match$mut(base.EitherMatch_3 m_m$);

Object resMatch$mut(base.ResMatch_3 m_m$);

Object resMatch$read(base.ResMatch_3 m_m$);

Object resMatch$imm(base.ResMatch_3 m_m$);

base.Res_2 map$mut(base.ResMap_3 f_m$);

base.Res_2 map$read(base.ResMap_3 f_m$);

base.Res_2 map$imm(base.ResMap_3 f_m$);

base.Bool_0 isOk$read();

base.Bool_0 isErr$read();

base.Opt_1 err$mut();

base.Opt_1 err$read();

base.Opt_1 err$imm();
}