import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.Flow.*;

/**
 * Java9 Flow 사용
 * 간단한 publisher 와 subscriber 예제
 */
public class PubSub {

    public static void main(String[] args){
        Iterable<Integer> iter = Arrays.asList(1,2,3,4,5);

        Publisher p = new Publisher() {
            public void subscribe(Subscriber subscriber) {
                Iterator<Integer> it = iter.iterator();

                subscriber.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {
                        try{
                            while (n-- > 0) {
                                if (it.hasNext()) {
                                    subscriber.onNext(it.next());
                                } else {
                                    subscriber.onComplete();
                                    break;
                                }
                            }
                        }catch (RuntimeException e){    // publisher에서 발생된 에러
                            subscriber.onError(e);
                        }

                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };

        Subscriber<Integer> s = new Subscriber<Integer>() {

            // onNext에서 사용하기 위해
            Subscription subscription;

            // 반드시 호출
            // subscribe 하는 즉시 호출함.
            public void onSubscribe(Subscription subscription) {
                System.out.println("onSubscribe");
                // 몽땅 다 받겠다
                //this.subscription.request(Long.MAX_VALUE);
                this.subscription = subscription;
                // 하나의 값만 받는다.
                this.subscription.request(1);
            }

            // 데이터가 있을 때마다 다음 데이터를 넘김.
            public void onNext(Integer item) {
                System.out.println("onNext " + item);
                // 값을 받은 후 또다시 하나의 값을 요청.
                this.subscription.request(1);

            }

            // 에러 발생 시 어떤식으로 처리 할 것인지. 에러 후의 처리
            // optional
            public void onError(Throwable throwable) {
                System.out.println("onError ");
            }

            // 완료시 호출됨
            // optional
            public void onComplete() {
                System.out.println("onComplete ");
            }
        };

        // 구독 시작.
        p.subscribe(s);
    }
}
