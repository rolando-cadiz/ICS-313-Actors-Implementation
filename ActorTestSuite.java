import java.util.List;

public class ActorTestSuite {
    public static void main(String[] args) throws InterruptedException {

        System.out.println("=== Testing addValues() ===");

        //simple actor to call addValues()
        Actor dummy = new Actor() {
            @Override protected void onMessage(Message m) { }
        };

        Message m1 = new Message("m1", 10);
        Message m2 = new Message("m2", "hello");
        int sum = dummy.addValues(m1, m2);
        System.out.println("addValues = " + sum);  //expect 15

                // Test searchMessage()
        System.out.println("\n=== Testing searchMessage() ===");
        dummy.tell(m1);
        dummy.tell(m2);
        Thread.sleep(100); 
        Message found1 = dummy.searchMessage("m1");
        Message found2 = dummy.searchMessage("m2");
        System.out.println("Found m1: intVal = " + found1.getIntValue()); //expect 10
        System.out.println("Found m2: strVal = " + found2.getStrValue()); //expect "hello"


        //CALLBACK ACTOR (prints results)
        Actor callback = new Actor() {
            @Override
            protected void onMessage(Message m) {
                System.out.println("Callback got: ID = " + m.getID()
                        + ", intVal = " + m.getIntValue()
                        + ", strVal = " + m.getStrValue());
            }
        };
        callback.start();

        // ---- Test NumericIncrementActor ----
        System.out.println("\n=== Testing NumericIncrementActor ===");

        Actor.NumericIncrementActor incActor = new Actor.NumericIncrementActor();
        incActor.start();

        incActor.tell(new Message("inc", 5, callback)); //expect intVal = 6
        Thread.sleep(200);


        // ---- Test StringIdSwitchActor ----
        System.out.println("\n=== Testing StringIdSwitchActor ===");

        Actor.StringIdSwitchActor swapActor = new Actor.StringIdSwitchActor();
        swapActor.start();

        swapActor.tell(new Message("swap", "hello", callback)); 
        // expect callback: ID = "hello", strVal = "swap"
        Thread.sleep(200);


        // ---- Test ActorCreateActor ----
        System.out.println("\n=== Testing ActorCreateActor ===");

        Actor.ActorCreateActor creator = new Actor.ActorCreateActor();
        creator.start();

        creator.tell(new Message("create", 3)); // makes 3 actors (will be either NumericIncrementActors or StringIdSwitchActors)
        Thread.sleep(300);

        List<Actor> spawned = creator.returnActors();
        System.out.println("Creator produced " + spawned.size() + " actors.");


        // ---- Test created actors ----
        for (Actor a : spawned) {
            if (a instanceof Actor.NumericIncrementActor) {
                System.out.println("Testing spawned NumericIncrementActor...");
                a.tell(new Message("inc", 41, callback)); //expect IntVal = 42
            } 
            else if (a instanceof Actor.StringIdSwitchActor) {
                System.out.println("Testing spawned StringIdSwitchActor...");
                a.tell(new Message("swap", "madeByCreator", callback)); //expect ID = "madeByCreator", strVal = "swap"
            }
        }

        Thread.sleep(500);


        // ---- Stop all actors ----
        dummy.stop();
        callback.stop();
        incActor.stop();
        swapActor.stop();
        creator.stop();
        for (Actor a : spawned) a.stop();

        System.out.println("\n=== Done ===");
    }
}