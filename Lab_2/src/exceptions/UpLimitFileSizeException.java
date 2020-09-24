package exceptions;

public class UpLimitFileSizeException extends Exception{
    private final String MESSAGE;
    public UpLimitFileSizeException(long wrongSize){
        double sizeInTb = wrongSize/Math.pow(2, 40);
        MESSAGE = "The size of the File up the limit! CurrSize:" + sizeInTb + "Tb > 1 Tb(limit)";
    }

    @Override
    public String getMessage() {
        return MESSAGE;
    }
}
