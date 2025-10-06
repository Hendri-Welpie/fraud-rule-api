package org.project.fraudruleapi.shared.filter;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

@Slf4j
@Component
public class ReactorMdcBridge {

    @PostConstruct
    public void setup() {
        Hooks.onEachOperator("MDC", Operators.lift((scannable, coreSubscriber) -> new MDCContextLifter<>(coreSubscriber)));
        log.info("Reactor → MDC bridge installed");
    }

    static class MDCContextLifter<T> implements CoreSubscriber<T> {
        private final CoreSubscriber<? super T> delegate;

        MDCContextLifter(CoreSubscriber<? super T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void onSubscribe(Subscription subscription) {
            delegate.onSubscribe(subscription);
        }

        @Override
        public void onNext(T value) {
            copyToMdc(delegate.currentContext());
            delegate.onNext(value);
        }

        @Override
        public void onError(Throwable throwable) {
            copyToMdc(delegate.currentContext());
            delegate.onError(throwable);
        }

        @Override
        public void onComplete() {
            copyToMdc(delegate.currentContext());
            delegate.onComplete();
        }

        @Override
        public Context currentContext() {
            return delegate.currentContext();
        }

        private void copyToMdc(Context context) {
            if (context.hasKey("traceId")) {
                MDC.put("traceId", context.get("traceId"));
            } else {
                MDC.remove("traceId");
            }
        }
    }
}

