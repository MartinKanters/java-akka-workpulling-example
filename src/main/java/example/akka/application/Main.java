package example.akka.application;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import example.akka.Master;
import example.akka.WorkerImplementation;
import com.typesafe.config.ConfigFactory;

/**
 * Essentially the Java version of a Scala example project about pulling.
 * http://letitcrash.com/post/29044669086/balancing-workload-across-nodes-with-akka-2
 */
public class Main {

    private static ActorSystem system;
    private static ActorRef master;

    public static void main(String... args) throws InterruptedException {
        system = ActorSystem.create("AkkaPullingSystem", ConfigFactory.load());

        // Create the master
        master = system.actorOf(Props.create(Master.class), "Master");

        // Sleep some time before we send jobs
        Thread.sleep(2000);

        // Send work to the master
        sendWork(6);

        // Create the workers
        String masterPath = "/user/Master";
        createWorkers(masterPath, 2);

        // send a non-string to let an actor crash
        // master.tell(10, ActorRef.noSender());
    }

    private static void createWorkers(String masterPath, int workers) {
        for (int i = 1; i <= workers; i++) {
            system.actorOf(Props.create(WorkerImplementation.class, masterPath), "Actor" + i);
        }
    }

    private static void sendWork(int jobs) {
        for (int i = 0; i < jobs; i++) {
            String message = "Job " + i;
            master.tell(message, ActorRef.noSender());
        }
    }

}
