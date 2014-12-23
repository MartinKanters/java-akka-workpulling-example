package example.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Procedure;
import static example.akka.messages.MasterWorkerProtocol.*;

/**
 * The abstract class which handles work from the master.
 * The actual work would be done in a subclass derived from this class.
 */
public abstract class Worker extends UntypedActor {
    protected LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private ActorSelection master;

    public Worker(String masterPath) {
        this.master = getContext().actorSelection(masterPath);
    }

    @Override
    public void preStart() throws Exception {
        master.tell(new WorkerCreated(), getSelf());
        master.tell(new WorkerRequestsWork(), getSelf());
    }

    /**
     * This is the Working Procedure.
     * This will be the behavior when the actor is working.
     */
    private Procedure<Object> workingProcedure = new Procedure<Object>() {
        @Override
        public void apply(Object message) {
            if (message instanceof WorkToBeDone) {
                // We are already working
            } else if (message instanceof WorkIsReady) {
                // We are already working
            } else if (message instanceof NoWorkToBeDone) {
                // We shouldn't have got this message anyway.
            } else if (message instanceof WorkCompleted) {
                log.info("Work is completed");
                master.tell(new WorkIsDone(), getSelf());
                master.tell(new WorkerRequestsWork(), getSelf());

                //Getting the idle procedure again.
                getContext().unbecome();
            } else {
                unhandled(message);
            }
        }
    };

    /**
     * This is the default Idle Procedure.
     * This is the behavior when the Actor is looking for work.
     */
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof WorkToBeDone) {
            WorkToBeDone workToBeDone = (WorkToBeDone) message;
            log.info("Got work {}", workToBeDone);

            doWork(getSender(), workToBeDone.getWork());
            getContext().become(workingProcedure);

        } else if (message instanceof WorkIsReady) {
            log.info("<-- Requesting work");
            master.tell(new WorkerRequestsWork(), getSelf());
        } else if (message instanceof NoWorkToBeDone) {
            log.info("No work to do");
            // No work to be done, apparently someone else got the job.
        } else {
            unhandled(message);
        }
    }

    abstract protected void doWork(ActorRef workGiver, Object work);
}
