package example.akka;

import akka.actor.ActorRef;

/**
 * A tuple of a work message and the sender.
 */
public class WorkDescription {
    private ActorRef workGiver;
    private Object work;

    public WorkDescription(ActorRef workGiver, Object work) {
        this.workGiver = workGiver;
        this.work = work;
    }

    public ActorRef getWorkGiver() {
        return workGiver;
    }

    public Object getWork() {
        return work;
    }

}
