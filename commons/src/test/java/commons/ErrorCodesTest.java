package commons;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ErrorCodesTest {

    @Test
    void getMessage()
    {
        String errorMsg1 = ErrorCodes.INVALID_NOTE_TITLE.getMessage();
        assertEquals("Note title already exists in this collection.", errorMsg1);
        String errorMsg2 = ErrorCodes.EMPTY_NOTE_TITLE.getMessage();
        assertEquals("Note title cannot be empty.", errorMsg2);
    }
    @Test
    void getCode()
    {
        int errorCode1 = ErrorCodes.INVALID_NOTE_TITLE.getCode();
        assertEquals(460, errorCode1);
        int errorCode2 = ErrorCodes.EMPTY_NOTE_TITLE.getCode();
        assertEquals(461, errorCode2);
    }
}