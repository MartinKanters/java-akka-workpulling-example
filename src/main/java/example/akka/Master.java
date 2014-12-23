package example.akka;

import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import example.akka.messages.MasterWorkerProtocol;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * This actor is responsible for taking up work and dividing that over Child actors.
 */
public class Master extends UntypedActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private Map<ActorRef, WorkerStatus> workers = new HashMap<>();
    private Queue<WorkDescription> workQueue = new LinkedList<>();

    @Override
    public void onReceive(Object message) throws Exception {


        if (message instanceof MasterWorkerProtocol.WorkerCreated) {
            //////////////////////////////////
            //// A NEW WORKER JOINS THE MASTER
            //////////////////////////////////

            log.info("Yay! A new worker: {}", getSender());

            ActorRef worker = getSender();
            if (!workers.containsKey(worker)) {
                workers.put(worker, new WorkerStatus());
                getContext().watch(worker);
                log.info("Added to the workerslist");
            }




        } else if (message instanceof MasterWorkerProtocol.WorkerRequestsWork) {
            ///////////////////////////
            //// A WORKER REQUESTS WORK
            ///////////////////////////

            ActorRef worker = getSender();
            log.info("Worker requests work: {}", worker);
            if (workers.containsKey(worker)) {
                if (workers.get(worker).getStatus() == WorkerStatus.Status.IDLE) {
                    if (!workQueue.isEmpty()) {
                        WorkDescription workDescription = workQueue.poll();
                        // Sending new work to the worker
                        log.info("--> Sending new work to Worker");
                        worker.tell(new MasterWorkerProtocol.WorkToBeDone(workDescription.getWork()), workDescription.getWorkGiver());
                        workers.get(worker).setWorking(workDescription.getWorkGiver(), workDescription.getWork());
                    } else {
                        // No work left in the queue to be sent
                        worker.tell(new MasterWorkerProtocol.NoWorkToBeDone(), ActorRef.noSender());
                    }
                } else {
                    log.error("Worker requested work while having work");
                }
            }





        } else if (message instanceof MasterWorkerProtocol.WorkIsDone) {
            ////////////////////////////////
            //// A WORKER HAS COMPLETED WORK
            ////////////////////////////////

            log.info("Yay! A worker finished his work: {}", getSender());

            if (workers.containsKey(getSender())) {
                log.info("Setting worker idle...");
                workers.get(getSender()).setIdle();
            }




        } else if (message instanceof Terminated) {
            /////////////////////////
            //// A WORKER HAS STOPPED
            /////////////////////////

            Terminated terminated = (Terminated) message;
            log.info("Noo! One of our workers got killed: {}", terminated.getActor());

            if (workers.containsKey(terminated.getActor())) {
                WorkerStatus workerStatus = workers.get(getSender());
                if (workerStatus.getStatus() == WorkerStatus.Status.WORKING) {
                    getSelf().tell(workerStatus.getWork(), workerStatus.getWorkGiver());
                }

                log.info("Removing from workerlist...");
                workers.remove(terminated.getActor());
            }



        } else {
            ////////////////////
            //// WE'VE GOT WORK!
            ////////////////////

            log.info("Yay! We've got new work. Adding it to the queue...");
            workQueue.add(new WorkDescription(getSender(), message));
            notifyWorkers();
        }
    }

    /**
     * This functions notifies all idle workers that there is new work.
     */
    private void notifyWorkers() {
        for (Map.Entry<ActorRef, WorkerStatus> entry : workers.entrySet()) {
            if (!workQueue.isEmpty()) {
                ActorRef worker = entry.getKey();
                WorkerStatus status = entry.getValue();

                if (status.getStatus() == WorkerStatus.Status.IDLE) {
                    log.info("--> Sending work requests to worker");
                    worker.tell(new MasterWorkerProtocol.WorkIsReady(), getSelf());
                }
            }
        }
    }
}
