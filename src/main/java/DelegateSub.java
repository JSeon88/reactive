import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class DelegateSub implements Subscriber<Integer> {
    Subscriber sub;

    public DelegateSub(Subscriber sub){
        this.sub = sub;
    }
    @Override
    public void onSubscribe(Subscription sub) {
        this.sub.onSubscribe(sub);
    }

    @Override
    public void onNext(Integer i) {
        this.onNext(i);
    }

    @Override
    public void onError(Throwable t) {
        this.sub.onError(t);
    }

    @Override
    public void onComplete() {
        this.sub.onComplete();
    }
}
