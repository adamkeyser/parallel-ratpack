package parallel

import ratpack.exec.Promise
import ratpack.func.Function
import ratpack.rx.RxRatpack
import ratpack.test.exec.ExecHarness
import rx.Observable
import spock.lang.AutoCleanup
import spock.lang.Specification
import toDoInParallel.ParallelThing

import java.util.concurrent.CountDownLatch

class ParallelManagerSpec extends Specification {

  @AutoCleanup
  ExecHarness harness = ExecHarness.harness()

  ParallelManager manager

  ParallelThing parallelThingUno
  ParallelThing parallelThingDos
  ParallelThing parallelThingTres
  ParallelThing parallelThingQuattro
  ParallelThing parallelThingCinco


  def setup() {
    parallelThingUno = Mock(ParallelThing)
    parallelThingDos = Mock(ParallelThing)
    parallelThingTres = Mock(ParallelThing)
    parallelThingQuattro = Mock(ParallelThing)
    parallelThingCinco = Mock(ParallelThing)
    manager = new ParallelManager(thingA: parallelThingUno,
        thingB: parallelThingDos,
        thingC: parallelThingTres,
        thingD: parallelThingQuattro,
        thingE: parallelThingCinco)
    RxRatpack.initialize()
  }

  def 'fetches item'() {
    setup:
    def latch = new CountDownLatch(5)
    def c = { v -> latch.countDown() }

    when:
    List result = harness.yield {
      manager.fetchValue()
          .doOnNext { v -> latch.await() }
          .promiseSingle()
    }.valueOrThrow

    then:
    1 * parallelThingUno.fetchValue() >> Observable.just("mocked uno").doOnNext(c).doOnSubscribe {
      println "I'm on thread ${Thread.currentThread().name}"
    }
    1 * parallelThingDos.fetchValue() >> Observable.just("mocked dos").doOnNext(c).doOnSubscribe {
      println "I'm on thread ${Thread.currentThread().name}"
    }
    1 * parallelThingTres.fetchValue() >> Observable.just("mocked tres").doOnNext(c).doOnSubscribe {
      println "I'm on thread ${Thread.currentThread().name}"
    }
    1 * parallelThingQuattro.fetchValue() >> Observable.just("mocked quattro").doOnNext(c).doOnSubscribe {
      println "I'm on thread ${Thread.currentThread().name}"
    }
    1 * parallelThingCinco.fetchValue() >> Observable.just("mocked cinco").doOnNext(c).doOnSubscribe {
      println "I'm on thread ${Thread.currentThread().name}"
    }

    result.each {
      println it
    }
    result.size() == 5

  }

  public <T> T sync(Promise<T> promise) {
    harness.yield(Function.constant(promise)).valueOrThrow
  }

  public <T> T sync(Observable<T> observable) {
    harness.yield(Function.constant(RxRatpack.promiseSingle(observable))).valueOrThrow
  }

}
