import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.*;
import java.util.concurrent.Flow.*;

public class PubSubThread {

    public static void main(String[] args) throws InterruptedException {
        Iterable<Integer> itr = Arrays.asList(1,2,3,4,5);
        //ExecutorService es = Executors.newSingleThreadExecutor();
        ExecutorService es = Executors.newCachedThreadPool();

        Publisher p = new Publisher() {
            @Override
            public void subscribe(Subscriber subscriber) {
                Iterator<Integer> it = itr.iterator();


                subscriber.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {
                        // 후에 cancel를 사용할 때 사용.
                        // 값을 리턴받기 때문에 가능.
                        //Future<?> f = es.submit();

                        es.execute(() ->{
                            int i = 0;
                            try{
                                while (i++ < n){
                                    if(it.hasNext()){
                                        subscriber.onNext(it.next());
                                    }else{
                                        subscriber.onComplete();
                                        break;
                                    }
                                }
                            }catch (Exception e){
                                subscriber.onError(e);
                            }

                        });

                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };

        Subscriber s = new Subscriber() {

            Subscription subscription;

            @Override
            public void onSubscribe(Subscription subscription) {
                System.out.println(Thread.currentThread().getName() + " onSubscribe");
                this.subscription = subscription;
                this.subscription.request(1);

            }

            @Override
            public void onNext(Object item) {
                System.out.println(Thread.currentThread().getName() + " onNext " + item);
                this.subscription.request(1);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("onError");
            }

            @Override
            public void onComplete() {
                System.out.println("onComplete");
            }
        };

        p.subscribe(s);

        // 지정된 시간 동안 대기하며 모든 작업이 중지 되었는지 체크
        es.awaitTermination(10, TimeUnit.SECONDS);
        es.shutdown();

    }
}
