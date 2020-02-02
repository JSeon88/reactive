import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class DelegateSubGeneric<T,R> implements Subscriber<T> {
    Subscriber sub;

    public DelegateSubGeneric(Subscriber<? super R> sub){
        this.sub = sub;
    }
    @Override
    public void onSubscribe(Subscription sub) {
        this.sub.onSubscribe(sub);
    }

    @Override
    public void onNext(T i) {
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
