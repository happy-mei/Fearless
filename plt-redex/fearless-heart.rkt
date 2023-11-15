#lang racket
(require redex)

(define-language F
  (e ::= x
         (e m Ts (e ...))
         L)
  (M ::= (sig \,)
         (sig -> e \,))
  (L ::= ((D Xs) : (IT ... {\' x M ...})))
  (Ls ::= (L ...))
  (arg ::= (x_!_ T))
  (sig ::= (m Xs Γ : T))
  (T ::= IT X)
  (Ts ::= (T ...))
  (IT ::= (D Ts))
  (m ::= (\. Lid) symbols)
  (D ::= Uid)
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
  (DM ::= (D M))
  (DMs ::= (DM ...))
  (MId ::= (m number))
  (DId ::= (C number)))

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
  domain-subset-eq : (M ...) (M ...) -> MetaBool

  [(domain-subset-eq (M_i M_n ...) (M_1 ... M_j M_m ...)) #t
                                                          (where MId_i (MId-of (sig-of M_i)))
                                                          (where MId_j (MId-of (sig-of M_j)))
                                                          (where MId_j MId_i)
                                                          (where #t (domain-subset-eq (M_n ...) (M_1 ... M_j M_m ...)))]
  [(domain-subset-eq (M_0 M_n ...) (M_m ...)) #f]
  [(domain-subset-eq () (M_n ...)) #t])

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

(module+ test
  (test-results))
