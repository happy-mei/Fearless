Require Import Ascii.
Require Import Utf8.
Require Import List.
Axiom ctype : Set.
Inductive cap := read | imm | iso | mut.
Definition tyvar := nat.
Inductive type :=
| qual : cap -> ctype -> type
| tvar : tyvar -> type
| rctvar : cap -> tyvar -> type
| ritvar : tyvar -> type.
Definition env := list (nat*type).

Inductive caplt : cap -> cap -> Prop :=
| miniso : ∀ c, caplt iso c
| maxread : ∀ c, caplt c read
| caprefl : ∀ c, caplt c c.
Infix "<:" := caplt (at level 45).
Definition caplt_dec {x y} : {x <: y} + {~ (x <: y)}.
  destruct x; destruct y; try solve [left; constructor];
    right; intro H; inversion H.
Defined.


Definition rcupd (T : type) (rc' : cap) : type :=
  match T with
  | qual rc D => qual rc' D
  | tvar x => rctvar rc' x
  | rctvar rc x => rctvar rc' x
  (* Not clearly specified: educated guess interpreting RC X[RC']=RC' X for both qualified type vars and read/imm *)
  | ritvar x => rctvar rc' x (* Nick thinks this should indeed just replace  *)
  end.

(* This adapts a type environment for an object literal with capability rc0 for a method with receiver permission rc1.
   It's only valid to call this with rc0 <: rc1. Ideally we'd just have an argument of that type and
   use pattern matching to prove the cases that violate that to be unreachable, but this is Coq,
   where the pattern matching is really impoverished. So instead we allow this to fail, but provide a wrapper
   that takes that argument and uses it to guarantee no failure.
 *)
Fixpoint adapt (Γ : env) (rc0 rc1 : cap) : env :=
  match (Γ,rc0,rc1) with
  | (nil,_,_) => nil
  (* (x : T, Γ) [RC0, RC1] = x : T [imm], Γ [RC0, RC1] with T = iso_ or imm_ *)
  (* In this case the use of rcupd is inlined, because we already used pattern matching to check if T was iso_ or imm_ *)
  (* The restriction on T forces it to be either a full qualified type or a qualified type variable *)
  | ((x,(qual iso D))::Γ',_,_) =>   (x,(qual imm D))::(adapt Γ' rc0 rc1)
  | ((x,(qual imm D))::Γ',_,_) =>   (x,(qual imm D))::(adapt Γ' rc0 rc1)
  | ((x,(rctvar iso D))::Γ',_,_) => (x,(rctvar imm D))::(adapt Γ' rc0 rc1)
  | ((x,(rctvar imm D))::Γ',_,_) => (x,(rctvar imm D))::(adapt Γ' rc0 rc1)
  (* (x : T, Γ) [RC0, RC1] = x : T, Γ [RC0, RC1] with RC0 ∈ {iso, mut} and RC1 ∈ {iso, mut} *)
  | ((x,T)::Γ',iso,iso) => (x,T)::(adapt Γ' rc0 rc1)
  | ((x,T)::Γ',iso,mut) => (x,T)::(adapt Γ' rc0 rc1)
  | ((x,T)::Γ',mut,iso) => (x,T)::(adapt Γ' rc0 rc1)
  | ((x,T)::Γ',mut,mut) => (x,T)::(adapt Γ' rc0 rc1)
  (* (x : T, Γ) [RC0, imm] = x : T [imm], Γ [RC0, imm] with RC0 ∈ {iso, mut, read} *)
  | ((x,T)::Γ',iso,imm) =>  (x,(rcupd T imm))::(adapt Γ' rc0 imm)
  | ((x,T)::Γ',mut,imm) =>  (x,(rcupd T imm))::(adapt Γ' rc0 imm)
  | ((x,T)::Γ',read,imm) => (x,(rcupd T imm))::(adapt Γ' rc0 imm)
  (* (x : T, Γ) [RC0, read] = x : T [read], Γ[RC0, read] with RC0 ∈ {iso, mut, read} and T = mut _ or T = read _ *)
  | ((x,(qual mut T))::Γ',iso,read) =>  (x,(qual read T))::(adapt Γ' rc0 read)
  | ((x,(qual mut T))::Γ',mut,read) =>  (x,(qual read T))::(adapt Γ' rc0 read)
  | ((x,(qual mut T))::Γ',read,read) => (x,(qual read T))::(adapt Γ' rc0 read)
  | ((x,(qual read T))::Γ',iso,read) => (x,(qual read T))::(adapt Γ' rc0 read)
  | ((x,(qual read T))::Γ',mut,read) => (x,(qual read T))::(adapt Γ' rc0 read)
  | ((x,(qual read T))::Γ',read,read) =>(x,(qual read T))::(adapt Γ' rc0 read)
  | ((x,(rctvar mut T))::Γ',iso,read) =>  (x,(rctvar read T))::(adapt Γ' rc0 read)
  | ((x,(rctvar mut T))::Γ',mut,read) =>  (x,(rctvar read T))::(adapt Γ' rc0 read)
  | ((x,(rctvar mut T))::Γ',read,read) => (x,(rctvar read T))::(adapt Γ' rc0 read)
  | ((x,(rctvar read T))::Γ',iso,read) => (x,(rctvar read T))::(adapt Γ' rc0 read)
  | ((x,(rctvar read T))::Γ',mut,read) => (x,(rctvar read T))::(adapt Γ' rc0 read)
  | ((x,(rctvar read T))::Γ',read,read) =>(x,(rctvar read T))::(adapt Γ' rc0 read)
  (* (x : T, Γ) [RC0, read] = x : T [read/imm], Γ [RC0, read] with RC0 ∈ {iso, mut, read} and T = read/imm_ or T = X *)
  (* Inlining the T[read/imm] here because we've already got the structure open for T *)
  | ((x,(ritvar X))::Γ',iso,read) =>  (x,(ritvar X))::(adapt Γ' rc0 read)
  | ((x,(ritvar X))::Γ',mut,read) =>  (x,(ritvar X))::(adapt Γ' rc0 read)
  | ((x,(ritvar X))::Γ',read,read) => (x,(ritvar X))::(adapt Γ' rc0 read)
  | ((x,(tvar X))::Γ',iso,read) =>    (x,(ritvar X))::(adapt Γ' rc0 read)
  | ((x,(tvar X))::Γ',mut,read) =>    (x,(ritvar X))::(adapt Γ' rc0 read)
  | ((x,(tvar X))::Γ',read,read) =>   (x,(ritvar X))::(adapt Γ' rc0 read)

  (* The following cases were missing from the paper when initially formalized, but after
     discussion with Nick and Marco, this is a case where the binding should simply be dropped
     (this was in some earlier version of the formalism) because the reference may have
     mutable aliases).
     This is one place where bounds could be useful, allowing some type variable uses to be
     retained.
   *)
  | ((x,qual read D)::Γ', imm, _) => adapt Γ' rc0 rc1    (* (x:read D,Γ)[imm,_] *)
  | ((x,qual mut D)::Γ', imm, _) => adapt Γ' rc0 rc1    (* (x:mut D,Γ)[imm,_] *)
  | ((x,tvar X)::Γ', imm, _) => adapt Γ' rc0 rc1    (* (x:X,Γ)[imm,_] *)
  | ((x,rctvar read X)::Γ', imm, _) => adapt Γ' rc0 rc1    (* (x:read X,Γ)[imm,_] *)
  | ((x,rctvar mut X)::Γ', imm, _) => adapt Γ' rc0 rc1    (* (x:mut X,Γ)[imm,_] *)
  | ((x,ritvar X)::Γ', imm, _) => adapt Γ' rc0 rc1    (* (x:read/imm X,Γ)[imm,_] *)
  (* The following are not defined according to page 41, as exhaustiveness checking forces them to be added.
     The read,iso and read,mut cases correspond to instances where the method's receiver capability
     is actually incompatible with the object literal's capability. E.g., if the object literal is read,
     it's not possible to call a mut method on it (or iso method on it).
     Requiring an extra parameter here (implicit in the paper def) that rc0 <: rc1 would make these cases impossible.
   *)
  | ((x,qual read D)::Γ', read, iso) => nil (* (x:read D,Γ)[read,iso] *)
  | ((x,qual read D)::Γ', read, mut) => nil (* (x:read D,Γ)[read,mut] *)
  | ((x,qual mut D)::Γ', read, iso) => nil (* (x:mut D,Γ)[read,iso] *)
  | ((x,qual mut D)::Γ', read, mut) => nil (* (x:mut D,Γ)[read,mut] *)
  | ((x,tvar X)::Γ', read, iso) => nil (* (x:X,Γ)[read,iso] *)
  | ((x,tvar X)::Γ', read, mut) => nil (* (x:X,Γ)[read,mut] *)
  | ((x,rctvar read X)::Γ', read, iso) => nil (* (x:read X,Γ)[read,iso] *)
  | ((x,rctvar read X)::Γ', read, mut) => nil (* (x:read X,Γ)[read,mut] *)
  | ((x,rctvar mut X)::Γ', read, iso) => nil (* (x:mut X,Γ)[read,iso] *)
  | ((x,rctvar mut X)::Γ', read, mut) => nil (* (x:mut X,Γ)[read,mut] *)
  | ((x,ritvar X)::Γ', read, iso) => nil (* (x:read/imm X,Γ)[read,iso] *)
  | ((x,ritvar X)::Γ', read, mut) => nil (* (x:read/imm X,Γ)[read,mut] *)
  end.

(* bindings that should be dropped when capturing for an immutable literal *)
Inductive droppable_binding : (nat * type) -> Prop :=
| qual_read_drop : ∀ x D, droppable_binding (x,qual read D)
| qual_mut_drop : ∀ x D, droppable_binding (x,qual mut D)
| tvar_drop : ∀ x X, droppable_binding (x,tvar X)
| qualvar_read_drop : ∀ x D, droppable_binding (x,rctvar read D)
| qualvar_mut_drop : ∀ x D, droppable_binding (x,rctvar mut D)
| tivar_drop : ∀ x X, droppable_binding (x,ritvar X).

(* adapt only produces the empty env if given the empty env or every binding
   base read or mut and the object literal is marked imm *)
Lemma adapt_valid : ∀ Γ rc0 rc1, rc0 <: rc1 -> adapt Γ rc0 rc1 = nil <-> (((Γ = nil \/ rc0 = imm) /\ forall x, In x Γ -> droppable_binding x)).
Proof.
  intro Γ. induction Γ; try firstorder.
  - destruct a. specialize (IHΓ rc0 rc1 H).
    right.
    destruct t; try destruct c; destruct rc0; destruct rc1; inversion H; subst; try solve[inversion H0]; solve[reflexivity].
  - destruct a. subst. specialize (IHΓ rc0 rc1 H).
    destruct t; try destruct c; destruct rc0; destruct rc1; inversion H; subst; try solve[inversion H0];
      rewrite IHΓ in H0; try constructor.
  - destruct a. subst. specialize (IHΓ rc0 rc1 H).
    destruct t; try destruct c; destruct rc0; destruct rc1; inversion H; subst; try solve[inversion H0];
      rewrite IHΓ in H0; destruct H0 as [Hqual Hin]; try constructor; try auto.
  - destruct a. subst. specialize (IHΓ rc0 rc1 H).
    destruct t; try destruct c; destruct rc0; destruct rc1; inversion H; subst; try solve[inversion H0];
      rewrite IHΓ in H0; destruct H0 as [Hqual Hin]; try constructor; try auto.
  (* Now the other direction *)
  - subst. specialize (IHΓ _ _ H).
    assert (droppable_binding a). {
      apply H1. constructor. reflexivity.
    }
    assert (adapt (a :: Γ) imm rc1 = adapt Γ imm rc1). {
      destruct H0; reflexivity.
    }
    rewrite H2. rewrite IHΓ. split. right; auto.
    intros.  apply H1. apply in_cons. assumption.
Qed.    
        
    
  
(* Overloading parenthes in Coq usually goes *very* poorly (read: breaks everything), and having notations with no leading
   character is very brittle so we're triggering this notation with
   ⟨ (\langle) and ⟩ (\rangle), i.e.
   ⟨Γ⟩[rc0,rc1]
   The binding precedence is lower than function application, so function applications in the Γ position will be parsed correctly.
*)
Notation "'⟨' Γ '⟩[' rc0 ',' rc1 ']'" := (adapt Γ rc0 rc1) (at level 55).


Definition adapt_classic (x y : cap) : option cap :=
  match (x,y) with
  | (imm,_) => Some imm
  | (iso,_) => None
  | (_,iso) => None
  | (_,imm) => Some imm
  | (mut,y) => Some y
  | (read,_) => Some read
  end.
(* \rhd *)
Infix "▷" := adapt_classic (at level 45).


(*
       There are 4 cases where where the M# prediction would be to see a certain capability,
       but Fearless instead simply doesn't capture the variable:
       - rc0 = imm, rc1 = read, considering capture of read
       - rc0 = imm, rc1 = read, considering capture of mut 
       - rc0 = imm, rc1 = imm, considering capture of read 
       - rc0 = imm, rc1 = imm, considering capture of mut 

       In M# and Pony, captures are flattened into fields, so captures are restricted by field behavior.
       In Fearless, fields now show up as captures, meaning fields are lifted to capture restrictions, so some awkwardness arises when imm literals are declared.

       In particular, this is a case where the object literal is imm, so any read fields of the object in M# or Pony would end up as imm, because external factors
       (the paths to making an immutable object) would ensure that any such readable field would be safe to call immutable. Here the path to immutability
       is largely via the capture mechanism, so rather than M# "make sure everything captured by imm is really imm" approach, Fearless has an
       "only capture things we can locally be sure are imm when constructing imm" approach.
       This is rooted in the context management in M#; things that would not be safe to capture as immutable get framed out by other mechanisms (i.e., recovery).
       Fearless has direct object literals that can simply be declared imm off the bat, and are checked locally rather than triggering recovery, so must be
       more conservative in this way.

       This may be more of a difference in object model than capture, we'll need to figure out the details.
       An M#/Pony/L42-like recovery/promotion based on blocks would allow behaviors like their capture (creating a <i>non</i>-immutable object and recovering
       it to immutable based on contextual constraints), and the method overloading in Fearless can encode that. So this is not really a loss of expressiveness,
       but more addressing a side of capture that was never addressed before by taking imm objects as primitive rather than derived.

       The divergence is due, at least partly, to the fact that capture is used in Fearless in situations where
       viewpoint adaptation wasn't, either because of difference in design philosophy (second-class closures) or
       because the situations simply didn't exist there.

       Formalizing this relationship would require formalizing typing derivations as well, not just algebraic operations on types and contexts.
*)

(** viewpoint adaptation predicts everything except captures into imm object literals
    TODO: Generalize this for other types (e.g., type variables, etc.)
 *)
Lemma viewpoint_agrees_non_imm_literals : forall x q rc0 rc1 qa qb D,
    rc0 ▷ rc1 = Some qa -> qa ▷ q = Some qb -> rc0 <: rc1 ->
    ⟨(x,qual q D)::nil⟩[rc0,rc1] = ((x,qual qb D)::nil) \/ (rc0=imm /\ (rc1=read \/ rc1=imm) /\ (q=mut \/ q = read)).
Proof.
  intros.
  destruct rc0; destruct rc1; compute [adapt_classic] in H; inversion H; subst; eauto;
  destruct q; compute [adapt_classic] in H0; inversion H0; subst; eauto; try solve[left; reflexivity];
    inversion H1 (* most cases at this point violate the constraint that the object literal capability is less than the receiver capability for the method *);
    solve [right; auto]. (* remaining cases don't exist in M# *)
Qed.

(** When it doesn't drop a binding, adapt predicts with viewpoint adaptation as long as nothing is iso. *)
Lemma adapt_agrees_with_non_iso_viewpoint : forall x q rc0 rc1 qb D,
    ⟨(x,qual q D)::nil⟩[rc0,rc1] = ((x,qual qb D)::nil) -> rc0 <: rc1 -> q ≠ iso -> rc0 ≠ iso -> rc1 ≠ iso -> ∃ qa, rc0 ▷ rc1 = Some qa ∧ qa ▷ q = Some qb.
Proof.
  intros.
  destruct rc0; destruct rc1; destruct q; compute [adapt_classic] in H; inversion H; subst;
    try solve [exists read; eauto]; try solve [exists imm; eauto]; try solve [exists mut; eauto];
    inversion H0 (* most cases at this point violate the constraint that the object literal capability is less than the receiver capability for the method *);
    match goal with (* At this point, remaining cases deal with iso *)
    | [ H: iso ≠ iso |- _ ] => exfalso; apply H; auto
    | _ => idtac
    end.
Qed.


  
(* See line page ~45 *)
Definition tvarsubst (T : type) (X : tyvar) (T' : type) : type :=
  match T with
  | qual c D => qual c D (* In general this would be more complex, right now we're not modeling class type parameters, or we'd recur into D's type arguments *)
  | tvar X' => if (Nat.eqb X X') then T' else tvar X'
  | rctvar c X' => if (Nat.eqb X X') then rcupd T' c else T
  | ritvar X' => if (Nat.eqb X X') then (
                    match T' with
                    (* read/imm X[X=X'] = read/imm X' *)
                    | tvar Z => ritvar Z
                    (* read/imm X [X = T ] = T with T = imm _ or T = read/imm _,
                       paper def is inconsistent about whether T is the target of substitution or the type being inserted,
                       so some of this uses T' below  *)
                    | qual imm D => qual imm D
                    | rctvar imm _ => T'
                    | ritvar _ => T'
                    (* read/imm X [X = T ] = T [read] with T not of form {imm _ , read/imm _} *)
                    | _ => rcupd T' read
                    end)
                (* read/imm X'[X=T] = read/imm X' with X ≠ X'*)
                else ritvar X' (* X ≠ X' *)
  end
.
Notation "'@' T '[' X ':=' U ']'" := (tvarsubst T X U) (at level 45).

(* TODO: Prove that capture and type variable substitution commute *)
