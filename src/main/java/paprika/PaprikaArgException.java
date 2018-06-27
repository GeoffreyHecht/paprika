package paprika;

public class PaprikaArgException extends Exception {

    public PaprikaArgException(String message) {
        super("Invalid args were passed to Paprika:\n" + message);
    }

}
