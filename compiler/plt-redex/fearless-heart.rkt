#lang racket
(require redex)

(define-language F
  (e ::= x
         (e m Ts (e ...))
         L)
  (M ::= (sig \,)
         (sig -> e \,))
  (L ::= (IT : (IT ... {\' x M ...})))
  (Ls ::= (L ...))
  (arg ::= (x_!_ T))
  (sig ::= (m Xs Γ : T))
  (T ::= IT X)
  (Ts ::= (T ...))
  (IT ::= (D Ts))
  (m ::= (\. Lid) symbols)
  (D ::= Uid)
  (DXs ::= (D Xs))
  (X ::= (side-condition variable-not-otherwise-mentioned_1
                           (regexp-match? #rx"^[A-Z][A-Za-z0-9]*$" (~a (term variable-not-otherwise-mentioned_1)))))
  (Xs ::= (X ...))
  (l ::= variable-not-otherwise-mentioned)
  (x ::= variable-not-otherwise-mentioned)
  (Lid ::= (side-condition variable-not-otherwise-mentioned_1
                           (regexp-match? #rx"^[a-z][A-Za-z0-9]*'?$" (~a (term variable-not-otherwise-mentioned_1)))))
  (Uid ::= (side-condition variable-not-otherwise-mentioned_1
                           (regexp-match? #rx"^[A-Z][A-Za-z0-9]*'?$" (~a (term variable-not-otherwise-mentioned_1)))))
  ; note: not enforcing the ban on //
  (symbols ::= (side-condition variable-not-otherwise-mentioned_1
                           (regexp-match? #rx"^[-+*/\\\\|!@#$%^&?~<>=][-+*\\\\/|!@#$%^&?~<>=]+$|^[-+*/\\\\|!@#$%^&?~<>]+$" (~a (term variable-not-otherwise-mentioned_1)))))

  ; reduction
  (ctxL ::= hole
            (ctxL m Ts (e ...))
            (L m Ts (L ... ctxL e ...)))

  ; type system
  (maybeD D #f)
  (maybeT T #f)
  (maybeCM CM #f)
  (MetaBool #t #f) ; not for use in code, just useful for predicate meta-functions
  [Γ ((x_!_ T) ...)]
  (DM ::= (IT M))
  (DMs ::= (DM ...))
  (mtype ::= (m Xs Ts -> T))
  (mtypes ::= (mtype ...)))

(define Ctx? (redex-match? F ctxL))
(module+ test
  (test-equal (Ctx? (term (hole +()()))) #t)
  (test-equal (term (in-hole (hole +()()) ((A ()) : ((A ()) {\' this })))) (term (((A ()) : ((A ()) {\' this })) +()()))))

;; Reduction
(define (-->raw Ls)
  (reduction-relation
   F
   ; If the method is explicitly on the lit, bind B_0 to selfName.
   ; If the method is not it means it was defined on some trait instead, bind B_0 to "this"
   [--> (L_0 m (T_gens ...) (L_1 ...))
        (subst [(x L) ...] (subst-Xs [(X_gens T_gens) ...] e_res))
        "Call-Lit"
        (where ((D Xs_0) : (IT ... { \' x_0 M ... })) L_0)
        (where #t (in-lit-domain L_0 m))
        (where M_0 (lit-domain L_0 m))
        (where ((m (X_gens ...) ((x_1 T_1) ...) : T_res) -> e_res \,) M_0)
        (where ((x L) ...) ,(apply map list (list (term (x_0 x_1 ...)) (term (L_0 L_1 ...)))))
        ]
   [--> (L_0 m (T_gens ...) (L_1 ...))
        (subst [(x L) ...] (subst-Xs [(X_gens T_gens) ...] e_res))
        "Call-Top"
        (where ((D Xs_0) : (IT ... { \' x_0 M ... })) L_0)
        (where #f (in-lit-domain L_0 m))
        (where [M_fst ... M_0 M_rst ...] (meths ,Ls (D Xs_0)))
        (where ((m (X_gens ...) ((x_1 T_1) ...) : T_res) -> e_res \,) M_0)
        (where ((x L) ...) ,(apply map list (list (term (this x_1 ...)) (term (L_0 L_1 ...)))))
        ]
   ))
(define (-->ctx Ls)
  (context-closure (-->raw Ls) F ctxL))
(module+ test
  ; S:{ +(s: S): S, }
  ; Fresh1:S{ +(s: S): S -> s, } + Fresh2:S{ +(s: S): S -> s, } --> Fresh2:S{ +(s: S): S -> s, }
  (test-equal (apply-reduction-relation (-->ctx (term []))
                                        (term (((Fear1 ()) : ((S ()) {\' fear0N ((+ () ((s (S ()))) : (S ())) -> s \,)})) + () (((S ()) : ((S ()) {\' fear1N ((+ () ((s (S ()))) : (S ())) -> s \,)}))))))
                                        (term [((S ()) : ((S ()) {\' fear1N ((+ () ((s (S ()))) : (S ())) -> s \,)}))]))
  (test-equal (apply-reduction-relation* (-->ctx (term []))
                                        (term (((Fear1 ()) : ((S ()) {\' fear0N ((+ () ((s (S ()))) : (S ())) -> s \,)})) + () (((S ()) : ((S ()) {\' fear1N ((+ () ((s (S ()))) : (S ())) -> s \,)}))))))
                                        (term [((S ()) : ((S ()) {\' fear1N ((+ () ((s (S ()))) : (S ())) -> s \,)}))]))

  ; S:{ +(s: S): S, }
  ; Fresh1:S{ +[R](s: R): R -> s, } +[Res] Res:{} --> Res:{}
  (test-equal (apply-reduction-relation* (-->ctx (term []))
                                         (term (((Fear1 ()) : ((S ()) {\' fear0N ((+ (R) ((s R)) : R) -> s \,)})) + ((Res ())) (((Res ()) : ({\' fear1N }))))))
                                         (term [((Res ()) : ((|'| fear1N)))]))
  (test-equal (apply-reduction-relation* (-->ctx (term []))
                                         (term (((Fear1 ()) : ((S ()) {\' fear0N ((+ (R) ((s R)) : R) -> s \,)})) + ((Res ())) (((Res ()) : ({\' fear1N }))))))
                                         (term [((Res ()) : ((|'| fear1N)))]))

  ; S:{ .make[R]: Res[R], }
  ; Fresh1:S{ .make[R]: Res[R] -> Fresh2:Res[R]{}, }.make[Bar] --> Fresh2:Res[Bar]{}
  (test-equal (apply-reduction-relation* (-->ctx (term []))
                                         (term (((Fear0 ()) : ((S ()) {\' fear0N (((\. make) (R) () : (Res (R))) -> ((Fear1 (R)) : ((Res (R)) {\' fear1N })) \,)})) (\. make) ((Bar ())) ())))
                                         (term [((Fear1 ((Bar ()))) : ((Res ((Bar ()))) (|'| fear1N)))]))

  (test-equal (apply-reduction-relation* (-->ctx (term [((Foo ()) : ( {\' this }))
                                                        ((Box (T)) : ( {\' this (((\. get) () () : T) \,)}))
                                                        ((Fear3 ()) : ((Box ((Foo ()))) {\' fear0N (((\. get) () () : (Foo ())) -> foo \,)}))
                                                        ((Capture ()) : ( {\' this ((+ () ((foo (Foo ()))) : (Foo ())) -> (((Fear3 ()) : ((Box ((Foo ()))) {\' fear0N (((\. get) () () : (Foo ())) -> foo \,)})) (\. get) () ()) \,)}))
                                                        ((Fear4 ()) : ((Capture ()) {\' fear1N }))
                                                        ((Fear5 ()) : ((Foo ()) {\' fear2N }))
                                                        ((Run ()) : ( {\' this ((\# () () : (Foo ())) -> (((Fear4 ()) : ((Capture ()) {\' fear1N })) + () (((Fear5 ()) : ((Foo ()) {\' fear2N })))) \,)}))]))
                                                 (term (((Fear4 ()) : ((Capture ()) {\' fear1N })) + () (((Fear5 ()) : ((Foo ()) {\' fear2N }))))))
              (term [((Fear5 ()) : ((Foo ()) (|'| fear2N)))]))

  (traces (-->ctx (term [((Foo ()) : ( {\' this }))
                                                        ((Box (T)) : ( {\' this (((\. get) () () : T) \,)}))
                                                        ((Fear3 ()) : ((Box ((Foo ()))) {\' fear0N (((\. get) () () : (Foo ())) -> foo \,)}))
                                                        ((Capture ()) : ( {\' this ((+ () ((foo (Foo ()))) : (Foo ())) -> (((Fear3 ()) : ((Box ((Foo ()))) {\' fear0N (((\. get) () () : (Foo ())) -> foo \,)})) (\. get) () ()) \,)}))
                                                        ((Fear4 ()) : ((Capture ()) {\' fear1N }))
                                                        ((Fear5 ()) : ((Foo ()) {\' fear2N }))
                                                        ((Run ()) : ( {\' this ((\# () () : (Foo ())) -> (((Fear4 ()) : ((Capture ()) {\' fear1N })) + () (((Fear5 ()) : ((Foo ()) {\' fear2N })))) \,)}))]))
                                                 (term (((Fear4 ()) : ((Capture ()) {\' fear1N })) + () (((Fear5 ()) : ((Foo ()) {\' fear2N }))))))
  
;  (traces (-->ctx (term []))
;          (term (((Fear1 ()) : ((S ()) {\' fear0N ((+ (R) ((s R)) : R) -> s \,)})) + ((Res ())) (((Res ()) : ({\' fear1N }))))))
  )

;; Utility notations
(define-metafunction F
  sig-of : M -> sig

  [(sig-of (sig \,)) sig]
  [(sig-of (sig -> e \,)) sig])
(define-metafunction F
  m-of : M -> m

  [(m-of M) m (where (m Xs Γ : T) (sig-of M))])
(define-metafunction F
  in-its : (IT ...) D -> MetaBool

  [(in-its (IT_1 ... (D Ts) IT_n ...) D) #t]
  [(in-its (IT_n ...) D) #f])
(define-metafunction F
  different : any any -> MetaBool

  [(different any_1 any_1) #f]
  [(different any_1 any_2) #t])
(define-metafunction F
  x-subseteq : Xs Xs -> MetaBool
  [(x-subseteq (X_1 X_n1 ...) Xs_2) #t
                                    (where (X_fst ... X_1 X_rst ...) Xs_2)
                                    (where #t (x-subseteq (X_n1 ...) Xs_2))]
  [(x-subseteq () Xs_2) #t]
  [(x-subseteq Xs Xs_2) #f])
(define-metafunction F
  x-disjoint : Xs Xs -> MetaBool
  [(x-disjoint (X_1 X_n1 ...) Xs_2) #f
                                    (where (X_fst ... X_1 X_rst ...) Xs_2)]
  [(x-disjoint (X_1 X_n1 ...) Xs_2) #t
                                    (where #t (x-disjoint (X_n1 ...) Xs_2))]
  [(x-disjoint () Xs_2) #t])

(define-metafunction F
  lit-domain : L m -> M
  [(lit-domain ((D Xs) : (IT ... {\' x M_0 ... M M_n ... })) m) M
                                                                   (where m (m-of M))]
  [(lit-domain ((D Xs) : (IT ... {\' x M_n ... })) m) #f])
(define-metafunction F
  in-lit-domain : L m -> MetaBool
  [(in-lit-domain ((D Xs) : (IT ... {\' x M_0 ... M M_n ... })) m) #t
                                                                   (where m (m-of M))]
  [(in-lit-domain ((D Xs) : (IT ... {\' x M_n ... })) m) #f])

(define-metafunction F
  subst : [(x any) ...] any -> any

  [(subst [(x_1 any_1) ... (x any_x) (x_2 any_2) ...] x) any_x]
  [(subst [(x any) ...] (any_0 m_0 Ts (e_n ...))) (any_0res m_0 Ts (any_n ...))
                                                  (where any_0res (subst [(x any) ...] any_0))
                                                  (where (any_n ...) (subst-many [(x any) ...] (e_n ...)))]
  [(subst [(x any) ...] ((D Xs) : (IT ... {\' x_self M_0 ...}))) ((D Xs) : (IT ... {\' x_self2 M_1 ...}))
                                                                 (where x_self2 (subst [(x any) ...] x_self))
                                                                 (where (M_1 ...) (subst-many [(x any) ...] (M_0 ...)))]
  [(subst [(x any) ...] (sig -> e_0 \,)) (sig -> e_1 \,)
                                         (where e_1 (subst [(x any) ...] e_0))]
  [(subst [(x any) ...] (sig \,)) (sig \,)]
  [(subst [(x_1 any_1) ...] any_2) any_2])
(define-metafunction F
  subst-many : [(x any) ...] (any ...) -> (any ...)

  [(subst-many [(x any) ...] (any_0 any_1 ...)) (any_i any_n ...)
                                                (where any_i (subst [(x any) ...] any_0))
                                                (where (any_n ...) (subst-many [(x any) ...] (any_1 ...)))]
  [(subst-many [(x any) ...] ()) ()])
(module+ test
  (test-equal (term (subst [] x))
              (term x))
  (test-equal (term (subst [(y y)] x))
              (term x))
  (test-equal (term (subst [(x y)] x))
              (term y))

  ; (e m Ts (e ...))
  ; no shadowing, this is deep substitution
  (test-equal (term (subst [(recv y)] (recv + () ())))
              (term (y +()())))
  (test-equal (term (subst [(recv y) (arg1 z)] (recv +()(arg1))))
              (term (y +()(z))))
  (test-equal (term (subst [(recv y) (arg1 z)] (recv +()(recv))))
              (term (y +()(y))))
  (test-equal (term (subst [(recv y) (arg1 z)] (recv +(Z)(recv))))
              (term (y +(Z)(y))))
  (test-equal (term (subst [(recv y) (arg1 z)] (recv +()((foo -()(arg1))))))
              (term (y +()((foo -()(z))))))

  ; (Foo {\' this Ms })
  (test-equal (term (subst [(foo y)] ((Foo ()) : ((Foo ()) {\' this ((+ () () : (A ())) -> foo \,)}))))
              (term ((Foo ()) : ((Foo ()) {\' this ((+ () () : (A ())) -> y \,)}))))
  (test-equal (term (subst ((this bSelf)) ((B ()) : ((B ()) {\' this }))))
              (term ((B ()) : ((B ()) {\' bSelf })))))

(define-metafunction F
  subst-Xs : [(X any) ...] any -> any

  [(subst-Xs [(X_1 any_1) ... (X any_x) (X_2 any_2) ...] X) any_x]
  [(subst-Xs [(X any) ...] (any_0 m_0 Ts_0 (e_n ...))) (any_0res m_0 Ts_1 (any_n ...))
                                                       (where any_0res (subst-Xs [(X any) ...] any_0))
                                                       (where (any_n ...) (subst-Xs-many [(X any) ...] (e_n ...)))
                                                       (where Ts_1 (subst-Xs-many [(X any) ...] Ts_0))]
  [(subst-Xs [(X any) ...] ((D Ts_0) : (IT_0 ... {\' x_self M_0 ...}))) (IT_self : (IT_1 ... {\' x_self M_1 ...}))
                                                                        (where IT_self (subst-Xs [(X any) ...] (D Ts_0)))
                                                                        (where (IT_1 ...) (subst-Xs-many [(X any) ...] (IT_0 ...)))
                                                                        (where (M_1 ...) (subst-Xs-many [(X any) ...] (M_0 ...)))]
  [(subst-Xs [(X any) ...] (sig_1 -> e_0 \,)) (sig_2 -> e_1 \,)
                                              (where e_1 (subst-Xs [(X any) ...] e_0))
                                              (where sig_2 (subst-Xs [(X any) ...] sig_1))]
  [(subst-Xs [(X any) ...] (sig_1 \,)) (sig_2 \,)
                                       (where sig_2 (subst-Xs [(X any) ...] sig_1))]
  [(subst-Xs [(X any) ...] (m Ts ((x_p T_p) ...) : T_1)) (m Ts ((x_p T_p2) ...) : T_2)
                                                         (where (T_p2 ...) (subst-Xs-many [(X any) ...] (T_p ...)))
                                                         (where T_2 (subst-Xs [(X any) ...] T_1))]
  [(subst-Xs [(X any) ...] (m Ts (T_p ...) -> T_1)) (m Ts (T_p2 ...) -> T_2)
                                                    (where (T_p2 ...) (subst-Xs-many [(X any) ...] (T_p ...)))
                                                    (where T_2 (subst-Xs [(X any) ...] T_1))]
  [(subst-Xs [(X_1 any) ...] (D Ts_1)) (D Ts_2)
                                       (where Ts_2 (subst-Xs-many [(X_1 any) ...] Ts_1))]
  [(subst-Xs [(X_1 any_1) ...] any_2) any_2])
(define-metafunction F
  subst-Xs-many : [(X any) ...] (any ...) -> (any ...)

  [(subst-Xs-many [(X any) ...] (any_0 any_1 ...)) (any_i any_n ...)
                                                   (where any_i (subst-Xs [(X any) ...] any_0))
                                                   (where (any_n ...) (subst-Xs-many [(X any) ...] (any_1 ...)))]
  [(subst-Xs-many [(X any) ...] ()) ()])
(module+ test
  (test-equal (term (subst-Xs [] X))
              (term X))
  (test-equal (term (subst-Xs [(Y Y)] X))
              (term X))
  (test-equal (term (subst-Xs [(X Y)] X))
              (term Y))
  (test-equal (term (subst-Xs [(X Y)] (Foo [X])))
              (term (Foo [Y])))
  (test-equal (term (subst-Xs [(X (Bar [(Baz [])]))] (Foo [X])))
              (term (Foo [(Bar [(Baz [])])])))

  ; (e m Ts (e ...))
  ; no shadowing, this is deep substitution
  (test-equal (term (subst-Xs [(X Y)] (recv + (X) ())))
              (term (recv +(Y)())))

  ; subst D[Xs]
  (test-equal (term (subst-Xs [(X A)] (Foo (X))))
              (term (Foo (A))))
  
  ; (Foo[X] {'this .foo[Y](a: X, b: Y): Y -> this.foo[Foo[X]] })
  (test-equal (term (subst-Xs [(X A)] ((Foo (X)) : ({\' this }))))
              (term ((Foo (A)) : ({\' this }))))

  ; sig
  (test-equal (term (subst-Xs [(X A) (Y B)] ((\. foo) (Y) ((a X) (b Y)) : Y)))
              (term ((|.| foo) (Y) ((a A) (b B)) : B)))
  
  ; abs M
  (test-equal (term (subst-Xs [(X A) (Y B)] (((\. foo) (Y) ((a X) (b Y)) : Y) \,)))
              (term (((|.| foo) (Y) ((a A) (b B)) : B) |,|)))
  
  (test-equal (term (subst-Xs [(X A) (Y B)] ((Foo (X)) : ((Bar (X)) {\' this (((\. foo) (Y) ((a X) (b Y)) : Y) -> (this (\. foo) ((Foo (X))) ()) \,)}))))
              (term ((Foo (A)) : ((Bar (A)) {\' this (((\. foo) (Y) ((a A) (b B)) : B) -> (this (\. foo) ((Foo (A))) ()) \,)}))))
  )

(define-metafunction F
  sig-types : sig -> Ts

  [(sig-types (m Xs ((x T_1) arg_n ...): T_res)) (T_1 T_n ...)
                                                 (where (T_n ...) (sig-types (m Xs (arg_n ...): T_res)))]
  [(sig-types (m Xs (): T)) ()])
(module+ test
  (test-equal (term (sig-types (+ () () : A)))
              (term ()))
  (test-equal (term (sig-types (+ () ((a A)) : A)))
              (term (A)))
  (test-equal (term (sig-types (+ () ((a A) (b A)) : A)))
              (term (A A)))
  (test-equal (term (sig-types (+ () ((a A) (b B)) : A)))
              (term (A B))))

(define-metafunction F
  trait-lookup : Ls D -> L
  [(trait-lookup (L_0 ... ((D Xs) : (IT ... {\' x M ...})) L_n ...) D) ((D Xs) : (IT ... {\' x M ...}))])
(define-metafunction F
  trait-lookup-ts : Ls D Ts -> L
  [(trait-lookup-ts (L_0 ... ((D (X_1 ..._1)) : (IT ... {\' x M ...})) L_n ...) D (T_1 ..._1)) L_res
                                                                                               (where L_res (subst-Xs [(X_1 T_1) ...] ((D (X_1 ...)) : (IT ... {\' x M ...}))))])
(module+ test
  (test-equal (term (trait-lookup-ts [((DMeth2 (A B)) : ((DMeth1 (A B)) {\' this (((\. m2) (X) () : (DMeth1 (X (DMeth2 ())))) \,)}))
                                      ((Fear0 (X Y Z)) : ((DMeth1 (X Z)) {\' fear0N }))
                                      ((DMeth1 (X Y)) : ( {\' this (((\. m1) (Z) ((x (DMeth1 (X Y)))) : (DMeth1 (X Z))) -> ((Fear0 (X Y Z)) : ((DMeth1 (X Z)) {\' fear0N })) \,)}))]
                                     DMeth1 [F1 F2]))
              (term ((DMeth1 (F1 F2)) : ((|'| this (((|.| m1) (Z) ((x (DMeth1 (F1 F2)))) : (DMeth1 (F1 Z))) ->
                                                                                                            ((Fear0 (F1 F2 Z)) : ((DMeth1 (F1 Z)) (|'| fear0N))) |,|)))))))

(define-metafunction F
  all-Ls : any -> Ls
  ; all-Ls(Ls)
  [(all-Ls (L_1 L_n ...)) (L_fst ... L_rst ...)
                          (where (L_fst ...) (all-Ls L_1))
                          (where (L_rst ...) (all-Ls (L_n ...)))]
  
  ; all-Ls(e)
  [(all-Ls ((D_0 Xs_0) : (IT_0 ... {\' x_0 M_0 ...}))) (((D_0 Xs_0) : (IT_0 ... {\' x_0 M_0 ...})) L_meths ...)
                                                       (where (L_meths ...) (all-Ls (M_0 ...)))]
  [(all-Ls ((D_0 Xs_0) : (IT_0 ... {\' x_0 }))) (((D_0 Xs_0) : (IT_0 ... {\' x_0 })))]
  ; Technically part of all-Ls(L) but split out for clarity. Just collecting all the Ls within methods in an L.
  [(all-Ls (M_0 M_n ...)) (L_m0 ... L_rest ...)
                                 (where (L_m0 ...) (all-Ls M_0))
                                 (where (L_rest ...) (all-Ls (M_n ...)))]
  
  [(all-Ls x) ()]
  [(all-Ls (e_0 m Ts (e_1 e_n ...))) (L_e1 ... L_rest ...)
                                            (where (L_e1 ...) (all-Ls e_1))
                                            (where (L_rest ...) (all-Ls (e_0 m Ts (e_n ...))))]
  [(all-Ls (e_0 m Ts ())) (all-Ls e_0)]
  
  ; all-Ls(M)
  [(all-Ls (sig \,)) ()]
  [(all-Ls (sig -> e \,)) (all-Ls e)]

  [(all-Ls ()) ()]
  )
(module+ test
  (test-equal (term (all-Ls ((A ()) : ((A ()) {\' this ((+ () ((a (A ()))) : (A ())) \,)}))))
              (term [((A ()) : ((A ()) {\' this ((+ () ((a (A ()))) : (A ())) \,)}))]))
  (test-equal (term (all-Ls ((B ()) : ((B ()) (A ()) {\' this ((+ () ((a (A ()))) : (A ())) -> ((Fear1 ()) : ((A ()) {\' fear0N })) \,)}))))
              (term [((B ()) : ((B ()) (A ()) {\' this ((+ () ((a (A ()))) : (A ())) -> ((Fear1 ()) : ((A ()) {\' fear0N })) \,)}))
                     ((Fear1 ()) : ((A ()) {\' fear0N }))])))
(define-metafunction F
  flatten-Ls : (Ls ...) -> Ls
  [(flatten-Ls ((L ...) Ls_n ...)) (L ... L_rest ...)
                                   (where (L_rest ...) (flatten-Ls (Ls_n ...)))]
  [(flatten-Ls ()) ()])

(define-metafunction F
  dmeths : Ls any -> DMs
  [(dmeths Ls (D Ts)) (DM_ms ... DM_trn ...)
                      (where (L_0 ... ((D Xs) : (IT ... {\' x M ...})) L_n ...) Ls)
                      (where (DM_ms ...) (dmeths-m D Xs Ts (M ...)))
                      (where (DM_trn ...) (dmeths Ls (IT ...)))]
  [(dmeths Ls (IT_1 IT_n ...)) (DM_1 ... DM_n ...)
                               (where (DM_1 ...) (dmeths Ls IT_1))
                               (where (DM_n ...) (dmeths Ls (IT_n ...)))]

  [(dmeths Ls ()) ()])
(define-metafunction F
  dmeths-m : D Xs Ts (M ...) -> DMs
  [(dmeths-m D (X_1 ..._1) (T_1 ..._1) (M_1 M_n ...)) (((D (T_1 ...)) M_subst) DM_n ...)
                                                      (where M_subst (subst-Xs [(X_1 T_1) ...] M_1))
                                                      (where (DM_n ...) (dmeths-m D (X_1 ...) (T_1 ...) (M_n ...)))]
  
  [(dmeths-m D Xs Ts ()) ()])
(module+ test
  ;DMeth1[X,Y]:{ .m1[Z](x: DMeth1[X,Y]): DMeth1[X,Z], }
  ;DMeth2{A,B]:DMeth1[A,B]{ .m2[X]: DMeth1[X, DMeth2[A,B]], }
  (test-equal (term (dmeths
                     [((DMeth2 (A B)) : ((DMeth1 [A B]) {\' this (((\. m2) (X) () : (DMeth1 (X (DMeth2 ())))) \,)}))
                      ((DMeth1 (X Y)) : ( {\' this (((\. m1) (Z) ((x (DMeth1 (X Y)))) : (DMeth1 (X Z))) \,)}))]
                     (DMeth1 [Z (Foo [(Bar [Baz])])])))
              (term [((DMeth1 (Z (Foo [(Bar [Baz])])))
                      (((|.| m1) (Z) ((x (DMeth1 (Z (Foo ((Bar (Baz)))))))) : (DMeth1 (Z Z))) |,|))]))
  (test-equal (term (dmeths
                     [((DMeth2 (A B)) : ((DMeth1 [A B]) {\' this (((\. m2) (X) () : (DMeth1 (X (DMeth2 (A B))))) \,)}))
                      ((DMeth1 (X Y)) : ( {\' this (((\. m1) (Z) ((x (DMeth1 (X Y)))) : (DMeth1 (X Z))) \,)}))]
                     (DMeth2 [A B])))
              (term [((DMeth2 (A B)) (((|.| m2) (X) () : (DMeth1 (X (DMeth2 (A B))))) |,|))
                     ((DMeth1 (A B)) (((|.| m1) (Z) ((x (DMeth1 (A B)))) : (DMeth1 (A Z))) |,|))]))

  ;DMeth1[X,Y]:{ .m1[Z](x: DMeth1[X,Y]): DMeth1[X,Z] -> {}, }
  ;DMeth2[A,B]:DMeth1[A,B]{ .m2[X]: DMeth1[X, DMeth2], }
  (test-equal (term (dmeths
                     [((DMeth2 (A B)) : ((DMeth1 (A B)) {\' this (((\. m2) (X) () : (DMeth1 (X (DMeth2 ())))) \,)}))
                      ((Fear0 (X Y Z)) : ((DMeth1 (X Z)) {\' fear0N }))
                      ((DMeth1 (X Y)) : ( {\' this (((\. m1) (Z) ((x (DMeth1 (X Y)))) : (DMeth1 (X Z))) ->
                                                                                                        ((Fear0 (X Y Z)) : ((DMeth1 (X Z)) {\' fear0N })) \,)}))]
                     (DMeth2 [A B])))
              (term [((DMeth2 (A B)) (((|.| m2) (X) () : (DMeth1 (X (DMeth2 ())))) |,|))
                     ((DMeth1 (A B)) (((|.| m1) (Z) ((x (DMeth1 (A B)))) : (DMeth1 (A Z))) ->
                                                                                           ((Fear0 (A B Z)) : ((DMeth1 (A Z)) (|'| fear0N))) |,|))]))
  )


(define-metafunction F
  alternative : DM DM -> MetaBool
  [(alternative (T_1 M_1) (T_2 M_2)) #t
                                     (where #t (different T_1 T_2))
                                     (where m_1 (m-of M_1))
                                     (where m_2 (m-of M_2))
                                     (where #f (different m_1 m_2))]
  [(alternative DM_1 DM_2) #f])
(define-metafunction F
  conflict : Ls DM DM -> MetaBool
  [(conflict Ls (T_1 M_1) (T_2 M_2)) #t
                                     (where #t (alternative (T_1 M_1) (T_2 M_2)))
                                     (where (sig -> e \,) M_2)
                                     (where #f (DM-sub? Ls (T_1 M_1) (T_2 M_2)))]
  [(conflict Ls DM_1 DM_2) #f])
(define-metafunction F
  DM-sub? : Ls DM DM -> MetaBool
  [(DM-sub? Ls (T_1 M_1) (T_2 M_2)) ,(judgment-holds (≤ Ls T_1 T_2))]
  [(DM-sub? Ls DM_1 DM_2) #f])

(define-metafunction F
  implement-ok : Ls T -> MetaBool
  [(implement-ok Ls T) (implement-ok-aux1 Ls T DMs DMs)
                       (where DMs (dmeths Ls T))])
(define-metafunction F
  implement-ok-aux1 : Ls T DMs DMs -> MetaBool
  [(implement-ok-aux1 Ls T (DM_1 DM_n ...) DMs) #t
                                                (where #t (implement-ok-aux2 Ls T DM_1 DMs))
                                                (where #t (implement-ok-aux1 Ls T (DM_n ...) DMs))]
  [(implement-ok-aux1 Ls T () DMs) #t]
  [(implement-ok-aux1 Ls T DMs_1 DMs_2) #f])
(define-metafunction F
  implement-ok-aux2 : Ls T DM DMs -> MetaBool
  [(implement-ok-aux2 Ls T DM_1 (DM_2 DM_n ...)) #t
                                                 (where #t (implement-ok-check Ls T DM_1 DM_2))
                                                 (where #t (implement-ok-aux2 Ls T DM_1 (DM_n ...)))]
  [(implement-ok-aux2 Ls T DM ()) #t]
  [(implement-ok-aux2 Ls T DM DMs) #f])
(define-metafunction F
  implement-ok-check : Ls T DM DM -> MetaBool
  [(implement-ok-check Ls T DM_1 DM_2) #t
                                       (where #f (conflict Ls DM_1 DM_2))]
  [(implement-ok-check Ls T DM_1 DM_2) #t
                                       (where #t (conflict Ls DM_1 DM_2))
                                       (where (DM_h ... DM_3 DM_t ...) (dmeths Ls T))
                                       (where #t (alternative DM_3 DM_1))
                                       (where #t (DM-sub? Ls DM_3 DM_1))
                                       (where #t (DM-sub? Ls DM_3 DM_2))]
  [(implement-ok-check Ls T DM_1 DM_2) #f])
(module+ test
  ; implementOk(B[]) --> A[] and B[]
  (test-equal (term (implement-ok-check [((B ()) : ((A ()) {\' this (((\. foo) () () : (A ())) \,)}))
                                         ((A ()) : ( {\' this (((\. foo) () () : (A ())) \,)}))
                                         ((C ()) : ((B ()) {\' this }))]
                                        (B ())
                                        ((A ()) (((\. foo) () () : (A ())) \,))
                                        ((B ()) (((\. foo) () () : (A ())) \,))))
              #t))
(module+ test
  (test-equal (term (implement-ok [((B ()) : ((A ()) {\' this (((\. foo) () () : (A ())) \,)}))
                                    ((A ()) : ( {\' this (((\. foo) () () : (A ())) \,)}))
                                    ((C ()) : ((B ()) {\' this }))]
                                   (B ())))
               #t)
  (test-equal (term (implement-ok [((B ()) : ((A ()) (C ()) {\' this }))
                                   ((A ()) : ( {\' this (((\. foo) () () : (A ())) \,)}))
                                   ((C ()) : ( {\' this (((\. foo) () () : (C ())) \,)}))]
                                   (B ())))
               #t) ; OK because both abstract
  (test-equal (term (implement-ok [((B ()) : ((A ()) (C ()) {\' this }))
                                   ((Fear0 ()) : ((A ()) {\' fear0N }))
                                   ((A ()) : ( {\' this (((\. foo) () () : (A ())) -> ((Fear0 ()) : ((A ()) {\' fear0N })) \,)}))
                                   ((Fear1 ()) : ((C ()) {\' fear1N }))
                                   ((C ()) : ( {\' this (((\. foo) () () : (C ())) -> ((Fear1 ()) : ((C ()) {\' fear1N })) \,)}))]
                                   (B ())))
               #f)
  ;(parameterize ([current-traced-metafunctions '(implement-ok-check alternative conflict DM-sub?)]))
  )

(define-metafunction F
  override-ok : Ls T -> MetaBool
  [(override-ok Ls T) #t;(override-ok-aux1 Ls T mtypes mtypes)
                      (where mtypes (get-mtypes Ls T))])
(define-metafunction F
  override-ok-aux1 : Ls T mtypes mtypes -> MetaBool
  [(override-ok-aux1 Ls T (mtype_1 mtype_n ...) mtypes) #t
                                                        (where #t (override-ok-aux2 Ls T mtype_1 mtypes))
                                                        (where #t (override-ok-aux1 Ls T (mtype_n ...) mtypes))]
  [(override-ok-aux1 Ls T () mtypes) #t]
  [(override-ok-aux1 Ls T mtypes_1 mtypes_2) #f])
(define-metafunction F
  override-ok-aux2 : Ls T mtype mtypes -> MetaBool
  [(override-ok-aux2 Ls T mtype_1 (mtype_2 mtype_n ...)) #t
                                                         (where #t (override-ok-check Ls T mtype_1 mtype_2))
                                                         (where #t (override-ok-aux2 Ls T mtype_1 (mtype_n ...)))]
  [(override-ok-aux2 Ls T mtype ()) #t]
  [(override-ok-aux2 Ls T mtype mtypes) #f])
(define-metafunction F
  override-ok-check : Ls T mtype mtype -> MetaBool
  [(override-ok-check Ls T mtype_1 mtype_2) #t
                                            (where (m_1 Xs_1 Ts_1 -> T_1) mtype_1)
                                            (where (m_2 Xs_2 Ts_2 -> T_2) mtype_2)
                                            (where #t (different m_1 m_2))]
  [(override-ok-check Ls T mtype_1 mtype_2) #t
                                            (where (m_same Xs_1 Ts_1 -> T_1) mtype_1)
                                            (where (m_same Xs_2 Ts_2 -> T_2) mtype_2)
                                            (where #t (≃ mtype_1 mtype_2))]
  [(override-ok-check Ls T mtype_1 mtype_2) #f])

(define-metafunction F
  get-mtypes : Ls T -> mtypes
  [(get-mtypes Ls T) (mtype ...)
                     (where (DM ...) (dmeths Ls T))
                     (where (mtype ...) (get-mtypes-aux (DM ...)))])

(define-metafunction F
  get-mtypes-aux : (DM ...) -> mtypes
  [(get-mtypes-aux ((T_1 M_1) DM_n ...)) (mtype_1 mtype_n ...)
                                         (where (m Xs ((x_arg T_arg) ...) : T_0) (sig-of M_1))
                                         (where mtype_1 (m Xs (T_arg ...) -> T_0))
                                         (where (mtype_n ...) (get-mtypes-aux (DM_n ...)))]
  [(get-mtypes-aux ()) ()])
                  
(define-metafunction F
  ≃ : mtype mtype -> MetaBool
  [(≃ (m_1 (X_1 ..._1) Ts_1 -> T_1) (m_1 (X_2 ..._1) Ts_2 -> T_2)) #t
                                                                   (where (X_fresh ...) (fresh-Xs (X_1 ...)))
                                                                   (where mtype_1 (subst-Xs [(X_1 X_fresh) ...] (m_1 (X_1 ...) Ts_1 -> T_1)))
                                                                   (where mtype_2 (subst-Xs [(X_2 X_fresh) ...] (m_1 (X_2 ...) Ts_2 -> T_2)))
                                                                   (where (m_same Xs_ignored1 Ts_same -> T_same) mtype_1)
                                                                   (where (m_same Xs_ignored2 Ts_same -> T_same) mtype_1)]
  [(≃ mtype_1 mtype_2) #f])
(define-metafunction F
  fresh-Xs : Xs -> Xs
  [(fresh-Xs (X_1 X_n ...)) (X_fresh X_t ...)
                            (where X_fresh ,(gensym 'Fresh))
                            (where (X_t ...) (fresh-Xs (X_n ...)))]
  [(fresh-Xs ()) ()])
(module+ test
  (test-equal (term (≃ (+[X Y](Y X) -> Y)
                        (+[A B](B A) -> B)))
               #t)
  )

(define-metafunction F
  meths : Ls T -> (M ...)
  [(meths Ls T) (M ...)
                (where DMs (dmeths Ls T))
                (where (M ...) (meths-aux Ls T DMs DMs))])
(define-metafunction F
  meths-aux : Ls T DMs DMs -> (M ...)
  [(meths-aux Ls T (DM_1 DM_n ...) DMs) (M_1 M_res ...)
                                        (where #t (no-DMs-conflicts Ls T DM_1 DMs))
                                        (where (T_1 M_1) DM_1)
                                        (where (M_res ...) (meths-aux Ls T (DM_n ...) DMs))]
  [(meths-aux Ls T (DM_1 DM_n ...) DMs) (M_res ...)
                                        (where #f (no-DMs-conflicts Ls T DM_1 DMs))
                                        (where (M_res ...) (meths-aux Ls T (DM_n ...) DMs))]
  [(meths-aux Ls T () DMs) ()])
(define-metafunction F
  no-DMs-conflicts : Ls T DM DMs -> MetaBool
  [(no-DMs-conflicts Ls T DM (DM_1 DM_n ...)) #t
                                              (where #f (conflict Ls DM DM_1))
                                              (where #t (no-DMs-conflicts Ls T DM (DM_n ...)))]
  [(no-DMs-conflicts Ls T DM (DM_1 DM_n ...)) #f]
  [(no-DMs-conflicts Ls T DM ()) #t])
(module+ test
  (test-equal (term (meths [((B ()) : ((A ()) (C ()) {\' this }))
                            ((Fear0 ()) : ((A ()) {\' fear0N }))
                            ((A ()) : ( {\' this (((\. foo) () () : (A ())) -> ((Fear0 ()) : ((A ()) {\' fear0N })) \,)}))
                            ((Fear1 ()) : ((C ()) {\' fear1N }))
                            ((C ()) : ( {\' this (((\. foo) () () : (C ())) -> ((Fear1 ()) : ((C ()) {\' fear1N })) \,)}))]
                           (B ())))
              (term []))
  (test-equal (term (meths [((B ()) : ((A ()) (C ()) {\' this }))
                            ((Fear0 ()) : ((A ()) {\' fear0N }))
                            ((A ()) : ( {\' this (((\. foo) () () : (A ())) -> ((Fear0 ()) : ((A ()) {\' fear0N })) \,)}))
                            ((C ()) : ( {\' this (((\. foo) () () : (C ())) \,)}))]
                           (B ())))
              (term [(((|.| foo) () () : (A ())) -> ((Fear0 ()) : ((A ()) (|'| fear0N))) |,|)]))
  )

(define-metafunction F
  no-abs-ms : (M ...) -> MetaBool
  [(no-abs-ms (M_1 M_n ...)) #t
                             (where (sig -> e \,) M_1)
                             (where #t (no-abs-ms (M_n ...)))]
  [(no-abs-ms ()) #t]
  [(no-abs-ms (M ...)) #f])

;; Type system
(define-metafunction F
  lookup : Γ x -> T
  
  [(lookup ((x_0 T_0) ... (x_i T_i) (x_i+1 T_i+1) ...) x_i) T_i])
(module+ test
  (test-equal (term (lookup ((foo Bar)) foo)) (term Bar))
  (test-equal (term (lookup ((boo Baz) (foo Bar)) foo)) (term Bar)))

(define-judgment-form F
  #:mode (≤ I I I)
  #:contract (≤ Ls T T)

  [(where (DXs : (T_impls ... {\' x M ...})) (trait-lookup-ts Ls D Ts))
   (where (T_1 ... T_2 T_n ...) ((D Ts) T_impls ...))
   ----------------------------------------------------------------"lit-sub"
    (≤ Ls (D Ts) T_2)]

  [(where (DXs : (T_impls ... {\' x M ...})) (trait-lookup-ts Ls D Ts))
   (where (T_1 ... T_2 T_n ...) ((D Ts) T_impls ...))
   (where #t (different (D Ts) T_2))
   (≤ Ls T_2 T_3)
   ----------------------------------------------------------------"trn-sub"
    (≤ Ls (D Ts) T_3)]

  [----------------------------------------------------------------"X-sub"
    (≤ Ls X_1 X_1)])
(module+ test
  (test-judgment-holds (≤ [((G (X Y)) : ( {\' this }))
                           ((G2 ()) : ((G ((A ()) (B ()))) {\' this }))
                           ((B ()) : ((A ()) {\' this }))
                           ((A ()) : ( {\' this }))]
                          (A ())
                          (A ())))
  (test-judgment-holds (≤ [((G (X Y)) : ( {\' this }))
                           ((G2 ()) : ((G ((A ()) (B ()))) {\' this }))
                           ((B ()) : ((A ()) {\' this }))
                           ((A ()) : ( {\' this }))]
                          (B ())
                          (A ())))
  (test-judgment-holds (≤ [((G (X Y)) : ( {\' this }))
                           ((G2 ()) : ((G ((A ()) (B ()))) {\' this }))
                           ((B ()) : ((A ()) {\' this }))
                           ((A ()) : ( {\' this }))]
                          (B ())
                          (B ())))
  (test-equal (judgment-holds (≤ [((G (X Y)) : ( {\' this }))
                           ((G2 ()) : ((G ((A ()) (B ()))) {\' this }))
                           ((B ()) : ((A ()) {\' this }))
                           ((A ()) : ( {\' this }))]
                          (A ())
                          (B ())))
              #f)
  (test-equal (judgment-holds (≤ [((G (X Y)) : ( {\' this }))
                           ((G2 ()) : ((G ((A ()) (B ()))) {\' this }))
                           ((B ()) : ((A ()) {\' this }))
                           ((A ()) : ( {\' this }))]
                          (G2 ())
                          (G (X Y))))
              #f)
  (test-equal (judgment-holds (≤ [((G (X Y)) : ( {\' this }))
                           ((G2 ()) : ((G ((A ()) (B ()))) {\' this }))
                           ((B ()) : ((A ()) {\' this }))
                           ((A ()) : ( {\' this }))]
                          (G2 ())
                          (G ((A ()) (B ())))))
              #t)

  ; transitivity
  (test-judgment-holds (≤ [((B ()) : ((A ()) {\' this }))
                      ((A ()) : ( {\' this }))
                      ((C ()) : ((B ()) {\' this }))]
                     (C ())
                     (A ())))
  )
(define-judgment-form F
  #:mode (istype I I I I I)
  #:contract (istype Ls Xs Γ e T)

  [(typeof Ls Xs Γ e T_1)
   (≤ Ls T_1 T_res)
   ------------------------------------ "subs-t"
   (istype Ls Xs Γ e T_res)])

(define-judgment-form F
  #:mode (typeof I I I I O)
  #:contract (typeof Ls Xs Γ e T)

  [(typeof Ls Xs Γ x (lookup Γ x)) "var-t"]

  [(where ((D Xs_0) : (IT_impls ... {\' x M ...})) L)
   (where #t (x-subseteq Xs_1 Xs_0))
   (where (M_all ...) (meths Ls (D Xs_0)))
   (where #t (no-abs-ms (M_all ...)))
   (lit-ok Ls Γ L)
   ------------------------------------------------- "lit-t"
   (typeof Ls Xs_1 Γ L (D Xs_0))]

  [(typeof Ls Xs_1 Γ_1 e_0 T_0)
   (where [M_fst ... M M_rst ...] (meths Ls T_0))
   (where (m (X_rawgens ..._1) ((x_m T_rawm) ...) : T_rawres) (sig-of M))
   (where (X_gens ..._1) (fresh-Xs (X_rawgens ...)))
   (where (m (X_rawgens ..._1) ((x_m T_m) ...) : T_res) (subst-Xs [(X_rawgens X_gens) ...] (m (X_rawgens ...) ((x_m T_rawm) ...) : T_rawres)))
   (where [(X T) ...] [(X_gens T_gens) ...])
   (istype Ls Xs_1 Γ_1 e_1 (subst-Xs [(X_gens T_gens) ...] T_m)) ...
   (where T_sres (subst-Xs [(X_gens T_gens) ...] T_res))
   (where #t (x-disjoint Xs_1 (X_gens ...)))
   ------------------------------------------------- "call-t"
   (typeof Ls Xs_1 Γ_1 (e_0 m (T_gens ..._1) (e_1 ...)) T_sres)])

(define-judgment-form F
  #:mode (all-ok I)
  #:contract (all-ok Ls)

  ;[(all-ok Ls ()) "all-ok-base"]
  
  [(where (Ls ...) ((all-Ls L) ...))
   (where (L_all ...) (flatten-Ls ((all-Ls L) ...)))
   (lit-ok (L_all ...) () L) ...
   ---------------------------------------- "all-ok"
   (all-ok (L ...))])
(define-judgment-form F
  #:mode (lit-ok I I I)
  #:contract (lit-ok Ls Γ L)
  
  [(where Γ ((x_self (D Xs)) (x T) ...))
   (ms-ok Ls Xs Γ (M ...))
   (where #t (override-ok Ls (D Xs)))
   (where #t (implement-ok Ls (D Xs)))
   -------------------------------- "lit-ok"
   (lit-ok Ls ((x T) ...) ((D Xs) : (IT ... {\' x_self M ...})))])
(define-judgment-form F
  #:mode (ms-ok I I I I)
  #:contract (ms-ok Ls Xs Γ (M ...))

  [(ms-ok Ls Xs Γ ()) "ms-ok-base"]
  
  [(m-ok Ls Xs Γ M_0)
   (ms-ok Ls Xs Γ (M_n ...))
   -------------------------------- "ms-ok"
   (ms-ok Ls Xs Γ (M_0 M_n ...))])
(define-judgment-form F
  #:mode (m-ok I I I I)
  #:contract (m-ok Ls Xs Γ M)

  [(m-ok Ls Xs Γ (sig \,)) "abs-ok"]

  [(where (m (X_2 ...) (arg_0 ...) : T_0) sig)
   (where Γ_1 (arg_0 ... arg_1 ...))
   (istype Ls (X_1 ... X_2 ...) Γ_1 e T_0)
   ---------------------------------------------- "impl-ok"
   (m-ok Ls (X_1 ...) (arg_1 ...) (sig -> e \,))])

(module+ test
  (test-judgment-holds (all-ok [((B ()) : ((A ()) {\' this }))
                               ((A ()) : ( {\' this }))
                               ((C ()) : ((B ()) {\' this }))]))
  (test-equal (judgment-holds (all-ok [((B ()) : ((A ()) (C ()) {\' this }))
                                       ((Fear0 ()) : ((A ()) {\' fear0N }))
                                       ((A ()) : ( {\' this (((\. foo) () () : (A ())) -> ((Fear0 ()) : ((A ()) {\' fear0N })) \,)}))
                                       ((C ()) : ( {\' this (((\. foo) () () : (C ())) \,)}))]))
              #f)
  (test-judgment-holds (all-ok [((B ()) : ((A ()) {\' this (((\. foo) () () : (A ())) -> this \,)}))
                                ((A ()) : ( {\' this (((\. foo) () () : (A ())) \,)}))]))

  (test-judgment-holds (all-ok [((Fear0 ()) : ((B ()) {\' fear0N }))
                                ((B ()) : ((A ()) {\' this (((\. foo) () () : (A ())) -> ((Fear0 ()) : ((B ()) {\' fear0N })) \,)}))
                                ((A ()) : ( {\' this (((\. foo) () () : (A ())) \,)}))]))
  (test-equal (judgment-holds (all-ok [((Fear0 ()) : ((A ()) {\' fear0N }))
                                ((B ()) : ((A ()) {\' this (((\. foo) () () : (A ())) -> ((Fear0 ()) : ((A ()) {\' fear0N })) \,)}))
                                ((A ()) : ( {\' this (((\. foo) () () : (A ())) \,)}))]))
              #f) ; Fails because Fear0 needs to implement A
  (test-judgment-holds (all-ok [((Fear1 ()) : ((A ()) {\' self (((\. foo) () () : (A ())) -> self \,)}))
                                ((B ()) : ((A ()) {\' this (((\. foo) () () : (A ())) -> ((Fear1 ()) : ((A ()) {\' self (((\. foo) () () : (A ())) -> self \,)})) \,)}))
                                ((A ()) : ( {\' this (((\. foo) () () : (A ())) \,)}))]))

  (test-judgment-holds (all-ok [((B ()) : ((A ()) {\' this (((\. foo) () () : (A ())) -> (this (\. foo) () ()) \,)}))
                                ((A ()) : ( {\' this (((\. foo) () () : (A ())) \,)}))]))
  (test-judgment-holds (all-ok [((B ()) : ((A ()) {\' this (((\. foo) () ((a (A ()))) : (A ())) -> (this (\. foo) () (a)) \,)}))
                                ((A ()) : ( {\' this (((\. foo) () ((a (A ()))) : (A ())) \,)}))]))

  (test-judgment-holds (all-ok [((Ex ()) : ( {\' this (((\. m1) (R) ((r R)) : R) -> r \,)}))]))
  (test-judgment-holds (all-ok [((Ex ()) : ( {\' this (((\. m1) (R) ((r R)) : (Ex ())) -> (this (\. m1) ((Ex ())) (this)) \,)}))]))

  ; Optionals from the paper
  (test-judgment-holds (all-ok [((Fear2 (T)) : ((Opt (T)) {\' fear0N }))
                                ((FOpt ()) : ( {\' this ((\# (T) ((t T)) : (Opt (T))) -> ((Fear2 (T)) : ((Opt (T)) {\' fear0N })) \,)}))
                                ((OptMatch (T R)) : ( {\' this (((\. empty) () () : R) \,) (((\. some) () ((t T)) : R) \,)}))
                                ((Opt (T)) : ( {\' this (((\. m) (R) ((m (OptMatch (T R)))) : R) -> (m (\. empty) () ()) \,)}))]))

;  (test-judgment-holds (all-ok [((Fear6 ()) : ((FOpt ()) {\' fear0N }))
;                                                        ((Fear7 ()) : ((Foo ()) {\' fear1N }))
;                                                        ((Fear8 ()) : ((OptMatch ((Foo ()) (Foo ()))) {\' fear2N (((\. some) () ((f (Foo ()))) : (Foo ())) -> f \,) (((\. empty) () () : (Foo ())) -> (this (\. test) () ()) \,)}))
;                                                        ((Ex ()) : ( {\' this (((\. test) () () : (Foo ())) -> ((((Fear6 ()) : ((FOpt ()) {\' fear0N })) (\. make) ((Foo ())) (((Fear7 ()) : ((Foo ()) {\' fear1N })))) (\. m) ((Foo ())) (((Fear8 ()) : ((OptMatch ((Foo ()) (Foo ()))) {\' fear2N (((\. some) () ((f (Foo ()))) : (Foo ())) -> f \,) (((\. empty) () () : (Foo ())) -> (this (\. test) () ()) \,)})))) \,)}))
;                                                        ((Fear9 (T)) : ((Opt (T)) {\' fear3N }))
;                                                        ((Fear10 ()) : ((Ex ()) {\' fear3N }))
;                                                        ((FOpt ()) : ( {\' this (((\. make) (T) ((t T)) : (Opt (T))) -> ((Fear9 (T)) : ((Opt (T)) {\' fear3N })) \,)}))
;                                                        ((Foo ()) : ( {\' this }))
;                                                        ((OptMatch (T R)) : ( {\' this (((\. empty) () () : R) \,) (((\. some) () ((t T)) : R) \,)}))
;                                                        ((Opt (T)) : ( {\' this (((\. m) (R) ((m (OptMatch (T R)))) : R) -> (m (\. empty) () ()) \,)}))]))

  (test-judgment-holds (all-ok [((Opt (T)) : ( {\' this (((\. m) (R) ((m (OptMatch (T R)))) : R) -> (m (\. empty) () ()) \,)}))
                                ((Foo ()) : ( {\' this }))
                                ((OptMatch (T R)) : ( {\' this (((\. empty) () () : R) \,) (((\. some) () ((t T)) : R) \,)}))
                                ((Ex ()) : ( {\' this (((\. test) () () : (Foo ())) -> ((((Fear7 ()) : ((FOpt ()) {\' fear0N })) (\. make) ((Foo ())) (((Fear8 ()) : ((Foo ()) {\' fear1N })))) (\. m) ((Foo ())) (((Fear9 ()) : ((OptMatch ((Foo ()) (Foo ()))) {\' fear2N (((\. some) () ((f (Foo ()))) : (Foo ())) -> f \,) (((\. empty) () () : (Foo ())) -> (this (\. test) () ()) \,)})))) \,)}))
                                ((FOpt ()) : ( {\' this (((\. make) (T) ((t T)) : (Opt (T))) -> ((Fear10 (T)) : ((Opt (T)) {\' fear3N })) \,)}))
                                ((Run ()) : ( {\' this (((\. run) () () : (Foo ())) -> (((Fear11 ()) : ((Ex ()) {\' fear4N })) (\. test) () ()) \,)}))]))
  
  (test-judgment-holds (m-ok [((Foo ()) : ( {\' this }))
                                ((Box (T)) : ( {\' this (((\. get) () () : T) \,)}))
                                ((Fear2 ()) : ((Box ((Foo ()))) {\' fear0N (((\. get) () () : (Foo ())) -> foo \,)}))
                                ((Capture ()) : ( {\' this ((+ () ((foo (Foo ()))) : (Foo ())) -> (((Fear2 ()) : ((Box ((Foo ()))) {\' fear0N (((\. get) () () : (Foo ())) -> foo \,)})) (\. get) () ()) \,)}))]
                             ()
                             ()
                             ((+ () ((foo (Foo ()))) : (Foo ())) -> (((Fear2 ()) : ((Box ((Foo ()))) {\' fear0N (((\. get) () () : (Foo ())) -> foo \,)})) (\. get) () ()) \,)
                             ))

  (test-judgment-holds (all-ok [((Opt (T)) : ( {\' this (((\. m) (R) ((m (OptMatch (T R)))) : R) -> (m (\. empty) () ()) \,)}))
                                ((Foo ()) : ( {\' this }))
                                ((OptMatch (T R)) : ( {\' this (((\. empty) () () : R) \,) (((\. some) () ((t T)) : R) \,)}))
                                ((Ex ()) : ( {\' this (((\. test) () () : (Foo ())) -> ((((Fear8 ()) : ((FOpt ()) {\' fear0N })) (\. make) ((Foo ())) (((Fear9 ()) : ((Foo ()) {\' fear1N })))) (\. m) ((Foo ())) (((Fear10 ()) : ((OptMatch ((Foo ()) (Foo ()))) {\' fear2N (((\. some) () ((f (Foo ()))) : (Foo ())) -> f \,) (((\. empty) () () : (Foo ())) -> (this (\. test) () ()) \,)})))) \,)}))
                                ((FOpt ()) : ( {\' this (((\. make) (T) ((t T)) : (Opt (T))) -> ((Fear11 (T)) : ((Opt (T)) {\' fear3N (((\. m) (X1Dth0N) ((m (OptMatch (T X1Dth0N)))) : X1Dth0N) -> (m (\. some) () (t)) \,)})) \,)}))
                                ((Run ()) : ( {\' this (((\. run) () () : (Foo ())) -> (((Fear12 ()) : ((Ex ()) {\' fear4N })) (\. test) () ()) \,)}))]))

;  (show-derivations (build-derivations (all-ok [((Opt (T)) : ( {\' this (((\. m) (R) ((m (OptMatch (T R)))) : R) -> (m (\. empty) () ()) \,)}))
;                                ((Foo ()) : ( {\' this }))
;                                ((OptMatch (T R)) : ( {\' this (((\. empty) () () : R) \,) (((\. some) () ((t T)) : R) \,)}))
;                                ((Ex ()) : ( {\' this (((\. test) () () : (Foo ())) -> ((((Fear8 ()) : ((FOpt ()) {\' fear0N })) (\. make) ((Foo ())) (((Fear9 ()) : ((Foo ()) {\' fear1N })))) (\. m) ((Foo ())) (((Fear10 ()) : ((OptMatch ((Foo ()) (Foo ()))) {\' fear2N (((\. some) () ((f (Foo ()))) : (Foo ())) -> f \,) (((\. empty) () () : (Foo ())) -> (this (\. test) () ()) \,)})))) \,)}))
;                                ((FOpt ()) : ( {\' this (((\. make) (T) ((t T)) : (Opt (T))) -> ((Fear11 (T)) : ((Opt (T)) {\' fear3N (((\. m) (X1Dth0N) ((m (OptMatch (T X1Dth0N)))) : X1Dth0N) -> (m (\. some) () (t)) \,)})) \,)}))
;                                ((Run ()) : ( {\' this (((\. run) () () : (Foo ())) -> (((Fear12 ()) : ((Ex ()) {\' fear4N })) (\. test) () ()) \,)}))])))
  
;  (parameterize ([current-traced-metafunctions '(istype)])
;    
;  )
  (show-derivations (build-derivations (all-ok [((B ()) : ((A ()) {\' this }))
                                                ((A ()) : ( {\' this ((+[](): (A ())) \,) }))
                                                ((C ()) : ((B ()) {\' this }))])))
  )

(module+ test
  (test-results))
