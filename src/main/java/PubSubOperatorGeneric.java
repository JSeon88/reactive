
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * PubSubOperator에서 구현했던 걸 제네릭으로 변경
 * T -> R
 */
@Slf4j
public class PubSubOperatorGeneric {

    public static void main(String[] args){

        Iterable<Integer> iter = Stream.iterate(1, a -> a+1).limit(10).collect(Collectors.toList());

        Publisher<Integer> pub = iterPub(iter);

        // 각 하나씩 전달되는 데이터를 가공
        //Publisher<Integer> mapPub = mapPub(pub, s ->  s*10);
        //Publisher<String> mapPub = mapPub(pub, s ->  "[" + s + "]");
        //mapPub.subscribe(logSub());

        // 제레널하게 받는 걸로 변경
        //Publisher<Integer> reducePub = reducePub(pub,"", (a,b) -> a+b);
        Publisher<StringBuilder> reducePub = reducePub(pub,new StringBuilder(), (a,b) -> a.append(b + ","));
        reducePub.subscribe(logSub());
    }

    private static <T,R> Publisher<R> reducePub(Publisher<T> pub, R init, BiFunction<R, T, R> bi) {
        return new Publisher<R>() {
            @Override
            public void subscribe(Subscriber<? super R> subscriber) {
                pub.subscribe(new DelegateSubGeneric<T,R>(subscriber){
                    R result = init;

                    @Override
                    public void onNext(T i) {
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


    private static <T,R> Publisher<R> mapPub(Publisher<T> pub, Function<T, R> fn) {
        return new Publisher<R>(){

            // 중계 역할
            @Override
            public void subscribe(Subscriber<? super R> subscriber) {
                // DelegateSub(super class)를 만들어서 꼭 필요한 메소드만 받도록 함.
                // 예를 들어 onSubscribe는 딱히 정의할 것도 없는건데 코드를 나열하기에는 공간 낭비.
                pub.subscribe(new DelegateSubGeneric<T,R>(subscriber) {
                    @Override
                    public void onNext(T i) {
                        subscriber.onNext(fn.apply(i));
                    }
                });
            }
        };
    }


    private static <T> Subscriber<T> logSub() {
        return new Subscriber<T>() {
            Subscription subscription;

            public void onSubscribe(Subscription subscription) {
                log.info("onSubscribe");
                this.subscription = subscription;
                this.subscription.request(Long.MAX_VALUE);
            }

            public void onNext(T i) {
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

    private static <T> Publisher<T> iterPub(Iterable<T> iter) {
        return new Publisher<T>() {
            public void subscribe(Subscriber<? super T> subscriber) {
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
