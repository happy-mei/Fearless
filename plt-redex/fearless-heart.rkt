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
            (ctxV m(e ...))
            (L m(L ... ctxL e ...)))

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

;; Reduction
(define (-->raw Ds)
  (reduction-relation
   F
   ; If the method is explicitly on the lit, bind B_0 to selfName.
   ; If the method is not it means it was defined on some trait instead, bind B_0 to "this"
   [--> (L_0 m Ts (L_1 ...))
        (subst ((x L) ...) e_res)
        "Call-Lit"
        (where ((D Xs) : (IT ... { \' x_0 M ... })) L_0)
        (where #t (in-lit-domain L_0 m))
        (where ((x_1 ...) ((T_0 T_1 ...) -> T_res) e_res) (tst-of (D Xs) M))
        (where ((x L) ...) ,(apply map list (list (term (x_0 x_1 ...)) (term (L_0 L_1 ...)))))]
;   [--> (B_0 m(v_1 ...))
;        (subst ((x v) ...) e_res)
;        "Call-Top"
;        (fresh (c C))
;        (where number_0 ,(length (term (v_1 ...))))
;        (where (IT ... { \' x_0 M ... }) B_0)
;        (where #f (in-lit-domain B_0 (m number_0)))
;        (where ((x_1 ...) ((T_0 T_1 ...) -> T_res) e_res) (lookup-meth ,Ds (c : B_0) m number_0))
;        (where ((x v) ...) ,(apply map list (list (term (this x_1 ...)) (term (B_0 v_1 ...)))))]
   ))
(define (-->ctx Ds)
  (context-closure (-->raw Ds) F ctxV))

;; Utility notations
(define-metafunction F
  sig-of : M -> sig

  [(sig-of (sig \,)) sig]
  [(sig-of (sig -> e \,)) sig])
(define-metafunction F
  m-of : M -> m

  [(m-of M) m (where (m Xs Γ : T) (sig-of M))])

(define-metafunction F
  in-lit-domain : L m -> MetaBool
  [(in-lit-domain ((D Xs) : (IT ... {\' x M_0 ... M M_n ... })) MId) #t
                                                          (where m (m-of M))]
  [(in-lit-domain ((D Xs) : (IT ... {\' x M_n ... })) MId) #f])

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
