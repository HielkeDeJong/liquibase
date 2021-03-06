package liquibase.database.core;

import junit.framework.TestCase;
import liquibase.database.Database;

public class DB2ZOSDatabaseTest extends TestCase {
    public void testGetDefaultDriver() {
        Database database = new DB2ZOSDatabase();

        assertEquals("com.ibm.db2.jcc.DB2Driver", database.getDefaultDriver("jdbc:db2://localhost:50000/liquibas"));

        assertNull(database.getDefaultDriver("jdbc:oracle://localhost;databaseName=liquibase"));
    }


}
