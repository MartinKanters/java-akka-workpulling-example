package example.akka;

import akka.actor.ActorRef;

/**
 * This class holds information about a Worker, whether its currently idle or working.
 */
public class WorkerStatus {
    public enum Status {
        IDLE,
        WORKING
    }

    private ActorRef workGiver;
    private Status status = Status.IDLE;
    private Object work;

    public void setWorking(ActorRef workGiver, Object work) {
        this.workGiver = workGiver;
        this.work = work;
        this.status = Status.WORKING;
    }

    public void setIdle() {
        this.workGiver = null;
        this.work = null;
        this.status = Status.IDLE;
    }

    public Status getStatus() {
        return status;
    }

    public ActorRef getWorkGiver() {
        return workGiver;
    }

    public Object getWork() {
        return work;
    }
}
