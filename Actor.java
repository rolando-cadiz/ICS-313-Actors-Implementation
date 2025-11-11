import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class Actor {
    Queue<Message> recieved = new LinkedList<>();
    Queue<Message> toSend = new LinkedList<>();

    final class NumericActor extends Actor {
        public void incrementInt(int intData) {
        int incrementedValue = intData + 1;
        int randID = new Random().nextInt(100000);
        String newID = Integer.toString(randID);
        Message incrementedMessage = new Message(newID, incrementedValue);
        toSend.add(incrementedMessage);
        }

    }

    final class StringActor extends Actor {
        public void swtichUp(String[] strData) { 
            Message switchIDWithStrVal = new Message(strData[1], strData[0]);
            toSend.add(switchIDWithStrVal);
        }
    }

    final class MakeAnotherActor extends Actor {
        public Actor[] makeActor(int intData) {
            return new Actor[intData];
        }
    }
    public void run() {
        while (true) {
            Message currentMessage = recieved.poll();
            if (currentMessage != null) {
                processMessage(currentMessage);
            }
        }
    }

    public void processMessage(Message message) {
        if (message.getStrValue() != null) {
            proccessString(message);
        } 
        else {
            proccessInt(message);
        }
    }

    public String[] proccessString(Message message) { //generic processor
        String strVal = message.getStrValue();
        String strID = message.getID();
        return new String[] {strVal, strID};
    }

    public int proccessInt(Message message) { //generic processor
        int intVal = message.getIntValue();
        return intVal;
    }

    public Message sendMessage() {
        return toSend.poll();
    }

}
