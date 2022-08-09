interface FearlessBool {
  interface Bool {
  Bool and(Bool b);
  Bool or(Bool b);
  Bool not(Bool b);
  <R> R $if(ThenElse<R> f);
  }

interface True extends Bool {
  default Bool and(Bool b) { return b; }
  default Bool or(Bool b) { return this; }
  default Bool not(Bool b) { return new False(){}; }
  default <R> R $if(ThenElse<R> f) { return f.then(); }
}
interface False extends Bool {
  default Bool and(Bool b) { return b; }
  default Bool or(Bool b) { return this; }
  default Bool not(Bool b) { return new True(){}; }
  default <R> R $if(ThenElse<R> op) { return op.$else(); }
}
interface ThenElse<R> { R then(); R $else(); }

// Debug#((True{} || False{}) ? { .then 5, .else 10 })
public static void main(String[] args) {
  System.out.println(new True(){}.or(new False(){}).$if(new ThenElse<Integer>(){
    public Integer then() { return 5; }
    public Integer $else() { return 10; }
  }));
  }
}
