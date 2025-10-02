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
        Hooks.onEachOperator("MDC", Operators.lift((sc, sub) -> new MDCContextLifter<>(sub)));
        log.info("Reactor → MDC bridge installed");
    }

    static class MDCContextLifter<T> implements CoreSubscriber<T> {
        private final CoreSubscriber<? super T> delegate;

        MDCContextLifter(CoreSubscriber<? super T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void onSubscribe(Subscription s) {
            delegate.onSubscribe(s);
        }

        @Override
        public void onNext(T t) {
            copyToMdc(delegate.currentContext());
            delegate.onNext(t);
        }

        @Override
        public void onError(Throwable t) {
            copyToMdc(delegate.currentContext());
            delegate.onError(t);
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

        private void copyToMdc(Context ctx) {
            if (ctx.hasKey("traceId")) {
                MDC.put("traceId", ctx.get("traceId"));
            } else {
                MDC.remove("traceId");
            }
        }
    }
}

