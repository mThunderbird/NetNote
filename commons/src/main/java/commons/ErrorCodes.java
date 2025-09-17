package commons;

public enum ErrorCodes
{
    INVALID_NOTE_TITLE("Note title already exists in this collection.", 460),
    EMPTY_NOTE_TITLE("Note title cannot be empty.", 461),
    COLLECTION_DOES_NOT_EXIST("Collection does not exist.", 462),
    SERVER_UNREACHABLE("Server is unreachable.", 500);

    private final String message;
    private final int code;

    ErrorCodes(String message, int code)
    {
        this.message = message;
        this.code = code;
    }

    public String getMessage()
    {
        return message;
    }

    public int getCode()
    {
        return code;
    }
}
