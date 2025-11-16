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
