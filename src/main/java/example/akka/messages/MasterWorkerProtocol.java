package example.akka.messages;

import akka.actor.ActorRef;

public class MasterWorkerProtocol {
    /////////// These messages will be sent by the master
    public static class WorkToBeDone {
        private Object work;

        public WorkToBeDone(Object work) {
            this.work = work;
        }

        public Object getWork() {
            return work;
        }
    }

    public static class WorkIsReady {

    }

    public static class NoWorkToBeDone {

    }


    //////////// These messages will be sent by workers
    public static class WorkerCreated {

    }

    public static class WorkerRequestsWork {

    }

    public static class WorkIsDone {

    }

    public static class WorkCompleted {
        private Object result;

        public WorkCompleted(Object result) {
            this.result = result;
        }

        public Object getResult() {
            return result;
        }
    }
}
