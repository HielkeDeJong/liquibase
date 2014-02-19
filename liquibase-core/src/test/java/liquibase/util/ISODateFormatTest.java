package liquibase.util;

import org.junit.Test;

import java.util.Date;

import static junit.framework.Assert.assertEquals;

public class ISODateFormatTest {
    @Test
    public void isoDateFormatWithNoLeadingZeroFractions() throws Exception {
        ISODateFormat dateFormat = new ISODateFormat();
        String testString = "2012-09-12T09:47:54.664";
        Date date = dateFormat.parse(testString);
        assertEquals(testString, dateFormat.format(date));
    }

    @Test
    public void isoDateFormatWithLeadingZeroFractions() throws Exception {
        ISODateFormat dateFormat = new ISODateFormat();
        String testString = "2011-04-21T10:13:40.044";
        Date date = dateFormat.parse(testString);
        assertEquals(testString, dateFormat.format(date));
    }

    @Test
    public void isoDateFormatWithLeadingNoFractions() throws Exception {
        ISODateFormat dateFormat = new ISODateFormat();
        String testString = "2011-04-21T10:13:40";
        Date date = dateFormat.parse(testString);
        assertEquals(testString, dateFormat.format(date));
    }

    @Test
    public void isoDateFormatWithLeadingNanoFractions() throws Exception {
        ISODateFormat dateFormat = new ISODateFormat();
        String testString = "2011-04-21T10:13:40.01234567";
        Date date = dateFormat.parse(testString);
        assertEquals(testString, dateFormat.format(date));
    }

}
