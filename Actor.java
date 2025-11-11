import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class Actor implements ActorReference, Runnable {
    private BlockingQueue<Message> recieved = new LinkedBlockingQueue<>();
    private volatile boolean running = false;
    private Thread thread;
///
    public static final class NumericIncrementActor extends Actor {
        private final Random rnd = new Random();
        @Override protected void onMessage(Message m) {
            Integer intVal = m.getIntValue();
            if ("inc".equals(m.getID()) && intVal != null) {
                int out = intVal + 1;              // compute
                String newID = Integer.toString(rnd.nextInt(100000));
                if (m.getTo() != null) m.getTo().tell(new Message(newID, out, null)); // send
            }
        }
    }
///
    public static final class StringIdSwitchActor extends Actor {
        @Override protected void onMessage(Message m) {
            if ("swap".equals(m.getID()) && m.getStrValue() != null) {
                // swap: message name becomes value, value becomes name (simple transform)
                String newName = m.getStrValue();
                String newVal  = m.getID();
                if (m.getTo() != null) m.getTo().tell(new Message(newName, newVal, null));
            }
        }
    }
///
    public static final class StoreActor extends Actor {
        private Object last;  // satisfies "store for later"
        @Override protected void onMessage(Message m) {
            switch (m.getID()) {
                case "store":
                    last = (m.getStrValue() != null) ? m.getStrValue() : m.getIntValue();
                    break;
                case "sendStored":
                    if (m.getTo() != null && last != null) {
                        m.getTo().tell(new Message("stored", last, null));
                    }
                    break;
                default:
                    break;
            }
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
                Message m = recieved.take();   // blocks when empty
                onMessage(m);               // <-- message-driven behavior
            }
        } catch (InterruptedException ignored) {}
    }

    protected abstract void onMessage(Message m); // subclasses must implement

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

    // ---- determine a ----
    if (msg1.getIntValue() != null) {
        // numeric payload
        a = msg1.getIntValue();
    } else if (msg1.getStrValue() != null) {
        // string payload → use length
        a = msg1.getStrValue().length();
    } else {
        // no payload
        a = 0;
    }

    // ---- determine b ----
    if (msg2.getIntValue() != null) {
        b = msg2.getIntValue();
    } else if (msg2.getStrValue() != null) {
        b = msg2.getStrValue().length();
    } else {
        b = 0;
    }

    return a + b;
}

}
