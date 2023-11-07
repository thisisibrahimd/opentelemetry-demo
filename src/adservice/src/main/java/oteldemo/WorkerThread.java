package oteldemo;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;


public class WorkerThread implements Runnable {

    private static final Tracer tracer = GlobalOpenTelemetry.getTracer("adservice");
    private String command;

    public WorkerThread(String s){
        this.command=s;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName()+" Start. Command = "+command);
        processCommand();
        System.out.println(Thread.currentThread().getName()+" End.");
    }

    private void processCommand() {
        Span span = tracer.spanBuilder("processCommand").startSpan();

        // Make the span the current span
        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("sleep.second", 5000);
            span.addEvent(
                    "Slept", Attributes.of(AttributeKey.booleanKey("sleep.status"), true));
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch(Throwable t) {
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
    }

    @Override
    public String toString(){
        return this.command;
    }
}
