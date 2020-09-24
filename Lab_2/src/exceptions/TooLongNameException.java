package exceptions;

public class TooLongNameException extends Exception{
    private final String MESSAGE;
    public TooLongNameException(long errorSize){
        MESSAGE = "The size of the File Name up the limit! Curr Size:" + errorSize + "bytes > 4096 bytes (limit)";
    }

    @Override
    public String getMessage() {
        return MESSAGE;
    }
}
