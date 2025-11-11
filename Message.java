public class Message {
    private final String ID;
    private final Integer intValue;
    private final String strValue;
    private final ActorReference to;

    public Message(String ID, Integer intValue) {
        this.ID = ID;
        this.intValue = intValue;
        this.strValue = null;
        this.to = null;
    }
    public Message(String ID, String strValue) {
        this.ID = ID;
        this.strValue = strValue;
        this.intValue = null;
        this.to = null;
    }
    public Message(String ID, Object value, ActorReference to) {
        this.ID = ID;
        this.to = to;
        if (value instanceof Integer) {
            this.intValue = (Integer) value;
            this.strValue = null;
        } else if (value instanceof String) {
            this.intValue = null;
            this.strValue = (String) value;
        } else {
            this.intValue = null;
            this.strValue = null;
        }
    }

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

}
