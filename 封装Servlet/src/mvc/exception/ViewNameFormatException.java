package mvc.exception;

public class ViewNameFormatException extends RuntimeException {

    public ViewNameFormatException(){}
    public ViewNameFormatException(String message){
        super(message);
    }
}
