# ICS-313-Actors-Implementation

This project implements a lightweight actor-based concurrency model in Java.
Each actor runs inside its own thread, receives messages asynchronously through a BlockingQueue, and reacts to incoming messages via an overridden onMessage method.

The system includes three actor sub-types, which inherit from the Actor class types:

NumericIncrementActor – processes numeric "inc" messages and replies with incremented values.

StringIdSwitchActor – processes "swap" messages by swapping message ID and string value. It is expected that the message value will be of type String

ActorCreateActor – dynamically spawns a user-determined number of actors and starts them at runtime.

Together, these classes demonstrate asynchronous message passing, thread-based concurrency, and dynamic actor creation in a manner inspired by systems like Erlang, Akka, or the classic actor model.

# Classes
## Actor.java
This is the base class for all actor subtypes.
 - Each actor runs on its own dedicated thread.
 - Incoming messages are stored in a thread-safe BlockingQueue<Message>.
 - The run() loop continuously takes messages and calls onMessage.
 - tell(Message) is the asynchronous communication primitive.
 - An actor starts with start() and terminates with stop().
 - Each actor, no matter its subtype, can search for a message it has previously received using searchMessage().
 - Each actor, no matter its subtype, can add two message values (a + b) using addValues(). If the message value is numeric, the operand will be considered a primitive integer value. If the message value is a string, the operand will be considered the value of the string length
### Fields

    private BlockingQueue<Message> recieved = new LinkedBlockingQueue<>();
    private volatile boolean running = false;
    private Thread thread;

### Functions

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
  

## NumericIncrementActor
A subtype of an Actor
 - Takes a message with a non-null and non-negative integer value and increments the value by one.
 - Overrides the onMessage() function

### Functions

         @Override protected void onMessage(Message m) {
            Integer intVal = m.getIntValue();
            if ("inc".equals(m.getID()) && intVal != null) {
                int incrementedVal = intVal + 1;              
                String newID = "postInc";
                if (m.getTo() != null) m.getTo().tell(new Message(newID, incrementedVal, null)); 
            }
        }

## StringIdSwitchActor
A subtype of an Actor
 - Takes a message with a string value and swaps the message ID with the string value.
 - Overrides the onMessage() function

### Functions

        @Override protected void onMessage(Message m) {
            if ("swap".equals(m.getID()) && m.getStrValue() != null) {
                String newName = m.getStrValue();
                String newVal  = m.getID();
                if (m.getTo() != null) m.getTo().tell(new Message(newName, newVal, null));
            }
        }

## ActorCreateActor
A subtype of an Actor
 - Takes a message with a non-null and non-negative integer value and creates at random either NumericIncrementActor(s) or StringIdSwitchActor(s).
 - Overrides the onMessage() function

### Fields

        private final Random rand = new Random();
        List<Actor> actors = new ArrayList<>();

### Functions

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

## Message.java

Contains the structure of a message sent between actors.
 - A message is constructed with a string ID paired with either:
   - An integer value.
   - A string value.
   - An Object along with a reference to another actor in which the message itself will be sent to.

### Fields

    private final String ID;
    private final Integer intValue;
    private final String strValue;
    private final ActorReference to;

### Functions

    public String getID() {
        return ID;
    }
    public Integer getIntValue() {
        return intValue;
    }   
    public String getStrValue() {
        return strValue;
    }   
    public ActorReference getTo() {
        return to;
    }


## ActoreReference.java (interface)
A simple file that contains a single method. This allows actors to send messages to each other regardless of subtype

### Functions


  public interface ActorReference {
     void tell(Message message);
 }
 

## ActorTestSuite.java
This is the testing file for all actors and actor subtypes

## Tests
### addValues()
 - Creates two messages and checks that:
 - Integer values are used directly
 - Strings contribute their .length()
 - Mixed types add correctly

### NumericIncrementActor
 - Actor thread is started
 - A message containing an integer value is sent
 - A callback actor is created to print results

 ### StringIdSwitchActor
 - Actor thread is started
 - A message containing a string ID and a string value is sent
 - A callback actor is created to print results

### ActorCreateActor
 - Actor thread is started
 - Sends a "create" message with an integer value of 3
 - The actor creates 3 new actors with random types
 - The test sends each newly created actor an appropriate test message (either "inc" or "swap")

