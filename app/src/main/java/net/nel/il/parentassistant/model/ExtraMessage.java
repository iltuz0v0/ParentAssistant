package net.nel.il.parentassistant.model;

public class ExtraMessage {

    private String message;

    private int position;

    private int finishPosition;

    public ExtraMessage(String message, int position, int finishPosition) {
        this.message = message;
        this.position = position;
        this.finishPosition = finishPosition;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getFinishPosition() {
        return finishPosition;
    }

    public void setFinishPosition(int finishPosition) {
        this.finishPosition = finishPosition;
    }
}
