package example.akka;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import example.akka.messages.MasterWorkerProtocol.WorkCompleted;
import scala.concurrent.Future;
import java.util.concurrent.Callable;
import static akka.dispatch.Futures.future;

/**
 * An actual worker actor.
 */
public class WorkerImplementation extends Worker {

    public WorkerImplementation(String masterPath) {
        super(masterPath);
    }

    @Override
    protected void doWork(ActorRef workGiver, final Object work) {
        log.info("doWork({}, {})", workGiver, work);
        Future<WorkCompleted> f = future(new Callable<WorkCompleted>() {
            public WorkCompleted call() throws InterruptedException {
                if (work instanceof String) {
                    log.info("<-- Returning work result");
                    Thread.sleep(200);
                    return new WorkCompleted(((String) work).toUpperCase());
                } else {
                    log.error("A non-string was passed");
                    getContext().stop(getSelf());
                    throw new IllegalArgumentException("A non-string was passed in this method");
                }
            }
        }, getContext().dispatcher());

        Patterns.pipe(f, getContext().dispatcher()).to(getSelf());
    }
}
