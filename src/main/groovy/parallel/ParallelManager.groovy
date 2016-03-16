package parallel

import ratpack.rx.RxRatpack
import rx.Observable
import rx.functions.Func1
import rx.functions.Func2
import toDoInParallel.ParallelThing

import java.util.concurrent.CopyOnWriteArrayList

class ParallelManager {

  ParallelThing thingA = new ParallelThing()
  ParallelThing thingB = new ParallelThing()
  ParallelThing thingC = new ParallelThing()
  ParallelThing thingD = new ParallelThing()
  ParallelThing thingE = new ParallelThing()

  Observable<List<String>> fetchValue() {
    return thingA.fetchValue().flatMap(this.&fetchDependencies as Func1)
  }

  Observable<List<String>> fetchDependencies(String item) {
    Observable.from([thingB, thingC, thingD, thingE]*.fetchValue())
        .compose(RxRatpack.&forkEach)
        .flatMap { it }
        .reduce(new CopyOnWriteArrayList<String>(item), this.&aggregate as Func2)
  }

  static List<String> aggregate(List<String> aggregate, String current) {
    return aggregate << current
  }
}