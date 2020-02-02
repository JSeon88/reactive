
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Reactive Stream - Operators
 *
 * Publisher -> ( Data 1-> Operator -> Data 2 ) -> Subscriber
 *
 * 데이터 가공
 * 1. map (d1 -> f -> d2)
 * pub -> [Data1] -> mapPub -> [Data2] -> logSub
 *                          <- subscribe(logSub)
 *                          -> onSubscribe(Subscription)
 *                          -> onNext
 *                          -> onNext
 *                          -> onComplete
 *
 * 전에 만든것과의 다른점
 * 1. reactivestreams 사용
 * 2. log4j 추가
 * 3. 람다식으로 변경
 * 4. 메소드 분리
 * 5. Operators 추가
 * 6. delegateSub 추가
 */
@Slf4j
public class PubSubOperator {

    public static void main(String[] args){

        Iterable<Integer> iter = Stream.iterate(1, a -> a+1).limit(10).collect(Collectors.toList());

        Publisher<Integer> pub = iterPub(iter);

        // 각 하나씩 전달되는 데이터를 가공
        //Publisher<Integer> mapPub = mapPub(pub, s ->  s*10);
        //Publisher<Integer> mapPub2 = mapPub(mapPub, s ->  -s);
        //mapPub2.subscribe(logSub());

        // 전달되는 데이터의 총 합 구하기
        //Publisher<Integer> sumPub = sumPub(pub);
        //sumPub.subscribe(logSub());

        // 제레널하게 받는 걸로 변경
        Publisher<Integer> reducePub = reducePub(pub,0, (a,b) -> a+b);
        reducePub.subscribe(logSub());
    }

    private static Publisher<Integer> reducePub(Publisher<Integer> pub, int init, BiFunction<Integer, Integer, Integer> bi) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> subscriber) {
                pub.subscribe(new DelegateSub(subscriber){
                    int result = init;

                    @Override
                    public void onNext(Integer i) {
                        result = bi.apply(result,i);
                    }

                    @Override
                    public void onComplete() {
                        subscriber.onNext(result);
                        subscriber.onComplete();
                    }
                });
            }
        };
    }

    private static Publisher<Integer> sumPub(Publisher<Integer> pub) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> subscriber) {
                pub.subscribe(new DelegateSub(subscriber){
                    int sum = 0;

                    // 넘어오는 데이터를 sum에 담아두기
                    @Override
                    public void onNext(Integer i) {
                        sum += i;
                    }

                    // 끝나면 onComplete로 넘어오기 때문에 이때 최종 값을 전달한다.
                    @Override
                    public void onComplete() {
                        subscriber.onNext(sum);
                        subscriber.onComplete();
                    }
                });

            }
        };

    }

    private static Publisher<Integer> mapPub(Publisher<Integer> pub, Function<Integer, Integer> fn) {
        return new Publisher<Integer>(){

            // 중계 역할
            @Override
            public void subscribe(Subscriber<? super Integer> subscriber) {
                // DelegateSub(super class)를 만들어서 꼭 필요한 메소드만 받도록 함.
                // 예를 들어 onSubscribe는 딱히 정의할 것도 없는건데 코드를 나열하기에는 공간 낭비.
                pub.subscribe(new DelegateSub(subscriber) {
                    @Override
                    public void onNext(Integer i) {
                        subscriber.onNext(fn.apply(i));
                    }
                });
            }
        };
    }


    private static Subscriber<Integer> logSub() {
        return new Subscriber<Integer>() {
            Subscription subscription;

            public void onSubscribe(Subscription subscription) {
                log.info("onSubscribe");
                this.subscription = subscription;
                this.subscription.request(Long.MAX_VALUE);
            }

            public void onNext(Integer i) {
                log.info("onNext:{}" ,i);
            }

            public void onError(Throwable throwable) {
                log.info("onError:{}" +throwable);
            }

            public void onComplete() {
                log.info("onComplete");
            }
        };
    }

    private static Publisher<Integer> iterPub(Iterable<Integer> iter) {
        return new Publisher<Integer>() {
            public void subscribe(Subscriber<? super Integer> subscriber) {
                subscriber.onSubscribe(new Subscription() {
                    public void request(long l) {
                        try{
                            iter.forEach(i -> subscriber.onNext(i));
                            subscriber.onComplete();
                        }catch (Throwable t){
                            subscriber.onError(t);
                        }
                    }

                    public void cancel() {

                    }
                });
            }
        };
    }
}
