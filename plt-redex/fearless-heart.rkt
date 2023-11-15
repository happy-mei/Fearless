#lang racket
(require redex)

(define-language F
  (e ::= x
         (e m Ts (e ...))
         L)
  (M ::= (sig \,)
         (sig -> e \,))
  (L ::= (DXs : (IT ... {\' x M ...})))
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
  (mtype ::= (m Xs Ts -> T)))

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
   [--> (L_0 m Ts (L_1 ...))
        (subst ((x L) ...) e_res)
        "Call-Lit"
        (where ((D Xs_0) : (IT ... { \' x_0 M ... })) L_0)
        (where M_0 (lit-domain L_0 m))
        (where ((m Xs_1 ((x_1 T_1) ...) : T_res) -> e_res \,) M_0)
        (where ((x L) ...) ,(apply map list (list (term (x_0 x_1 ...)) (term (L_0 L_1 ...)))))
        ]
;   [--> (B_0 m(v_1 ...))
;        (subst ((x v) ...) e_res)
;        "Call-Top"
;        (fresh (c C))
;        (where number_0 ,(length (term (v_1 ...))))
;        (where (IT ... { \' x_0 M ... }) B_0)
;        (where #f (in-lit-domain B_0 (m number_0)))
;        (where ((x_1 ...) ((T_0 T_1 ...) -> T_res) e_res) (lookup-meth ,Ls (c : B_0) m number_0))
;        (where ((x v) ...) ,(apply map list (list (term (this x_1 ...)) (term (B_0 v_1 ...)))))]
   ))
(define (-->ctx Ls)
  (context-closure (-->raw Ls) F ctxL))
(module+ test
  (test-equal (apply-reduction-relation (-->ctx (term []))
                                        (term (((Fear1 ()) : ((S ()) {\' fear0N ((+ () ((s (S ()))) : (S ())) -> s \,)})) + () (((S ()) : ((S ()) {\' fear1N ((+ () ((s (S ()))) : (S ())) -> s \,)}))))))
                                        (term [((S ()) : ((S ()) {\' fear1N ((+ () ((s (S ()))) : (S ())) -> s \,)}))]))
  (test-equal (apply-reduction-relation* (-->ctx (term []))
                                        (term (((Fear1 ()) : ((S ()) {\' fear0N ((+ () ((s (S ()))) : (S ())) -> s \,)})) + () (((S ()) : ((S ()) {\' fear1N ((+ () ((s (S ()))) : (S ())) -> s \,)}))))))
                                        (term [((S ()) : ((S ()) {\' fear1N ((+ () ((s (S ()))) : (S ())) -> s \,)}))])))

;; Utility notations
(define-metafunction F
  sig-of : M -> sig

  [(sig-of (sig \,)) sig]
  [(sig-of (sig -> e \,)) sig])
(define-metafunction F
  m-of : M -> m

  [(m-of M) m (where (m Xs Γ : T) (sig-of M))])

(define-metafunction F
  lit-domain : L m -> M
  [(lit-domain ((D Xs) : (IT ... {\' x M_0 ... M M_n ... })) m) M
                                                                   (where m (m-of M))]
  [(lit-domain ((D Xs) : (IT ... {\' x M_n ... })) m) #f])

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
                                                                        (where IT_self (subst [(X any) ...] (D Ts_0)))
                                                                        (where (IT_1 ...) (subst-Xs-many [(X any) ...] (IT_0 ...)))
                                                                        (where (M_1 ...) (subst-Xs-many [(X any) ...] (M_0 ...)))]
  [(subst-Xs [(X any) ...] (sig_1 -> e_0 \,)) (sig_2 -> e_1 \,)
                                              (where e_1 (subst-Xs [(X any) ...] e_0))
                                              (where sig_2 (subst-Xs [(X any) ...] sig_1))]
  [(subst-Xs [(X any) ...] (sig_1 \,)) (sig_2 \,)
                                       (where sig_2 (subst-Xs [(X any) ...] sig_1))]
  [(subst-Xs [(X any) ...] (m Ts Γ_1 : T_2)) (m Ts Γ_2 : T_2)
                                             (where Γ_2 (subst-Xs-many [(X any) ...] Γ_1))
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

  ; (Foo[X] {'this .foo[Y](a: X, b: Y): Y -> this.foo[Foo[X]] })
  (test-equal (term (subst-Xs [(X A) (Y B)] ((Foo (X)) : ((Foo (X)) {\' this (((\. foo) (Y) ((a X) (b Y)) : Y) -> (this (\. foo) ((Foo (X))) ()) \,)}))))
              (term ((Foo (A)) : ((Foo (A)) {\' this (((\. foo) (B) ((a A) (b B)) : B) -> (this (\. foo) ((Foo (A))) ()) \,)}))))
;  (test-equal (term (subst-Xs ((this bSelf)) ((B ()) : ((B ()) {\' this }))))
;              (term ((B ()) : ((B ()) {\' bSelf }))))
  )

(define-metafunction F
  tst-of : T M -> (Ts -> T)

  [(tst-of T_0 M) ((T_0 T_n ...) -> T_ret)
                      (where (m Xs ((x_!_: T) ...): T_ret) (sig-of M))
                      (where (T_n ...) (sig-types (sig-of M)))])
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
  collect-all-L : any -> Ls
  ; collectAllL(e)
  [(collect-all-L ((D_0 Xs_0) : (IT_0 ... {\' x_0 M_0 ...}))) (((D_0 Xs_0) : (IT_0 ... {\' x_0 M_0 ...})) L_meths ...)
                                                              (where (L_meths ...) (collect-all-L (M_0 ...)))]
  [(collect-all-L ((D_0 Xs_0) : (IT_0 ... {\' x_0 }))) (((D_0 Xs_0) : (IT_0 ... {\' x_0 })))]
  ; Technically part of collectAllL(L) but split out for clarity. Just collecting all the Ls within methods in an L.
  [(collect-all-L (M_0 M_n ...)) (L_m0 ... L_rest ...)
                                 (where (L_m0 ...) (collect-all-L M_0))
                                 (where (L_rest ...) (collect-all-L (M_n ...)))]
  
  [(collect-all-L x) ()]
  [(collect-all-L (e_0 m Ts (e_1 e_n ...))) (L_e1 ... L_rest ...)
                                            (where (L_e1 ...) (collect-all-L e_1))
                                            (where (L_rest ...) (collect-all-L (e_0 m Ts (e_n ...))))]
  [(collect-all-L (e_0 m Ts ())) (collect-all-L e_0)]
  
  ; collectAllL(M)
  [(collect-all-L (sig \,)) ()]
  [(collect-all-L (sig -> e \,)) (collect-all-L e)]

  [(collect-all-L ()) ()]
  )
(module+ test
  (test-equal (term (collect-all-L ((A ()) : ((A ()) {\' this ((+ () ((a (A ()))) : (A ())) \,)}))))
              (term [((A ()) : ((A ()) {\' this ((+ () ((a (A ()))) : (A ())) \,)}))]))
  (test-equal (term (collect-all-L ((B ()) : ((B ()) (A ()) {\' this ((+ () ((a (A ()))) : (A ())) -> ((Fear1 ()) : ((A ()) {\' fear0N })) \,)}))))
              (term [((B ()) : ((B ()) (A ()) {\' this ((+ () ((a (A ()))) : (A ())) -> ((Fear1 ()) : ((A ()) {\' fear0N })) \,)}))
                     ((Fear1 ()) : ((A ()) {\' fear0N }))])))

(define-metafunction F
  dmeths : Ls any -> DMs
  [(dmeths Ls (D Ts)) (DM_ms ... DM_trn ...)
                      (where (L_0 ... ((D Xs) : (IT ... {\' x M ...})) L_n ...) Ls)
                      (where (DM_ms ...) (dmeths-m D Xs Ts (M ...)))
                      (where (DM_trn ...) (dmeths (IT ...)))]
  [(dmeths Ls (IT_1 IT_n ...)) (DM_1 ... DM_n ...)
                               (where (DM_1 ...) (dmeths Ls IT_1))
                               (where (DM_n ...) (dmeths Ls (IT_n ...)))]

  [(dmeths Ls ()) ()])
(define-metafunction F
  dmeths-m : D Xs Ts (M ...) -> DMs
  [(dmeths-m D Xs Ts (M_1 M_n ...)) (((D Ts) M_subst) DM_n ...)
                                    (where M_subst (subst-Xs [] M_1))
                                    (where (DM_n ...) (dmeths-M D Xs Ts (M_n ...)))]
  
  [(dmeths-m D Xs Ts ()) ()])

(module+ test
  (test-results))
