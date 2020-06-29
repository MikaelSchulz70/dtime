package se.dtime.service.system;

import org.junit.Test;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;

public class SmsSenderTest {
    private SmsSender smsSender = new SmsSender("https://se-1.cellsynt.net/sms.php",
            "userName", "password");

    @Test
    public void buildUrl() {
    }

    @Test
    public void formatMobileNumberTest() {
        assertNull(smsSender.formatMobileNumber(null));
        assertNull(smsSender.formatMobileNumber(""));
        assertNull(smsSender.formatMobileNumber("004673"));
        assertNull(smsSender.formatMobileNumber("0046733333A"));
        assertEquals("00467333334", smsSender.formatMobileNumber("00467333334"));
        assertEquals("00467333334", smsSender.formatMobileNumber("07333334"));
        assertEquals("00467333334", smsSender.formatMobileNumber("+467333334"));
    }

    @Test
    public void buildPhoneNumberListTest() {
        assertNull(smsSender.buildPhoneNumberList(null));
        assertEquals("", smsSender.buildPhoneNumberList(new String[] { null, null}));
        assertEquals("00467333334", smsSender.buildPhoneNumberList(new String[] { "00467333334", null}));
        assertEquals("00467333334,00467333335", smsSender.buildPhoneNumberList(new String[] { "00467333334", "00467333335"}));
        assertEquals("00467333334,00467333335", smsSender.buildPhoneNumberList(new String[] { "+467333334", "07333335"}));
        assertEquals("00467333334", smsSender.buildPhoneNumberList(new String[] { "+467333334", "0733333A"}));
    }

    @Test
    public void buildUrlTest() {
        assertNull(smsSender.buildUrlPath("DOC", "A test msg", null));
        assertEquals("/sms.php?username=userName&password=password&destination=0046700123123&type=text&charset=UTF-8&text=A test msg&originatortype=alpha&originator=DOC",
                smsSender.buildUrlPath("DOC", "A test msg", new String[] { "0046700123123" }));
        //https://se-1.cellsynt.net/sms.php?username=demo&password=test123&destination=0046700123123&type=text&charset=UTF-8&text=Testing%20123&originatortype=alpha&originator=Demo
    }

    @Test
    public void adjustMessageTest() {
        assertEquals("A test msg", smsSender.adjustMessage("A test msg"));
    }

}