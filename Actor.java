import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class Actor implements ActorReference, Runnable {
    private BlockingQueue<Message> recieved = new LinkedBlockingQueue<>();
    private volatile boolean running = false;
    private Thread thread;
///
    public static final class NumericIncrementActor extends Actor {
        @Override protected void onMessage(Message m) {
            Integer intVal = m.getIntValue();
            if ("inc".equals(m.getID()) && intVal != null) {
                int incrementedVal = intVal + 1;              
                String newID = "postInc";
                if (m.getTo() != null) m.getTo().tell(new Message(newID, incrementedVal, null)); 
            }
        }
    }
///
    public static final class StringIdSwitchActor extends Actor {
        @Override protected void onMessage(Message m) {
            if ("swap".equals(m.getID()) && m.getStrValue() != null) {
                String newName = m.getStrValue();
                String newVal  = m.getID();
                if (m.getTo() != null) m.getTo().tell(new Message(newName, newVal, null));
            }
        }
    }
///
    public static final class ActorCreateActor extends Actor { 
        private final Random rand = new Random();
        List<Actor> actors = new ArrayList<>();

        @Override protected void onMessage(Message m) {
            int startIndex = actors.size(); //prevents double starting of actor threads.
            if (m.getIntValue() == null || m.getIntValue() <= 0) {
                return; //if intValue is null or non-positive, do nothing
            }
            int num = rand.nextInt(1,3); //1 makes a NumericIncrementActor, 2 makes a StringIdSwitchActor
            switch (num) {
                case 1:
                    for (int i = 0; i < m.getIntValue(); i++) {
                        actors.add(new NumericIncrementActor()); 
                        actors.get(startIndex + i).start(); //start the newly created actor thread
                    }
                    break;
                case 2:
                    for (int i = 0; i < m.getIntValue(); i++) {
                        actors.add(new StringIdSwitchActor()); 
                        actors.get(startIndex + i).start(); //start the newly created actor thread
                    }
                    break;
                default:
                    break;
            }
        }

        protected List<Actor> returnActors() {
            return actors; //list of actors created by this actor
        }
    }
///
        @Override
    public void tell(Message message) {
        recieved.offer(message);
    }
    public void start() {
        if (running) return;
        running = true;
        thread = new Thread(this, getClass().getSimpleName());
        thread.start();
    }
    public void stop() {
        running = false;
        if (thread != null) {
            thread.interrupt();
        }
    }
    @Override
    public void run() {
        try {
            while (running) {
                Message m = recieved.take();   
                onMessage(m);               
            }
        } catch (InterruptedException ignored) {}
    }

    protected abstract void onMessage(Message m); 

    public Message searchMessage(String ID) {
        for (Message msg : recieved) {
            if (msg.getID().equals(ID)) {
                return msg;
            }
        }
        System.out.println("Message with ID " + ID + " not found.");
        return null;
    }

    public int addValues(Message msg1, Message msg2) {
    int a, b;
    if (msg1.getIntValue() != null) {
        a = msg1.getIntValue();
    } else if (msg1.getStrValue() != null) {
        a = msg1.getStrValue().length();
    } else {
        a = 0; //both intValue and strValue are null
    }

    if (msg2.getIntValue() != null) {
        b = msg2.getIntValue();
    } else if (msg2.getStrValue() != null) {
        b = msg2.getStrValue().length();
    } else {
        b = 0; //both intValue and strValue are null
    }

    return a + b;
}

}
