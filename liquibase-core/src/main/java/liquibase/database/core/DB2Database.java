package liquibase.database.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.DateParseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.structure.core.UniqueConstraint;
import liquibase.util.JdbcUtils;

public abstract class DB2Database extends AbstractJdbcDatabase {

	public DB2Database() {
		super.setCurrentDateTimeFunction("CURRENT TIMESTAMP");
		super.sequenceNextValueFunction = "NEXT VALUE FOR %s";
		super.sequenceCurrentValueFunction = "PREVIOUS VALUE FOR %s";
	}

	@Override
	public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
		if (!conn.getDatabaseProductName().startsWith("DB2")) {
			return false;
		}

		if (!isCorrectDB2ProductType(conn)) {
			return false;
		}

		return true;
	}

	@Override
	public String getDefaultDriver(String url) {
		if (url.startsWith("jdbc:db2")) {
			return "com.ibm.db2.jcc.DB2Driver";
		}
		return null;
	}

	@Override
	public int getPriority() {
		return PRIORITY_DEFAULT;
	}

	@Override
	public Integer getDefaultPort() {
		return 446;
	}

	@Override
	public boolean supportsSchemas() {
		return false;
	}

	@Override
	public boolean supportsCatalogs() {
		return true;
	}

	@Override
	public String getDefaultCatalogName() {

		if (defaultCatalogName != null) {
			return defaultCatalogName;
		}

		if (defaultSchemaName != null) {
			return defaultSchemaName;
		}

		if (getConnection() == null) {
			return null;
		}
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = ((JdbcConnection) getConnection()).createStatement();
			rs = stmt.executeQuery("select current schema from sysibm.sysdummy1");
			if (rs.next()) {
				String result = rs.getString(1);
				if (result != null) {
					this.defaultSchemaName = result;
				} else {
					this.defaultSchemaName = super.getDefaultSchemaName();
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not determine current schema", e);
		} finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeStatement(stmt);
		}

		return defaultSchemaName;
	}

	@Override
	public String correctObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
		if (objectName == null) {
			return null;
		}
		return objectName.toUpperCase();
	}

	@Override
	public boolean supportsInitiallyDeferrableColumns() {
		return false;
	}

	/**
	 * Return an DB2 date literal with the same value as a string formatted
	 * using ISO 8601.
	 * <p/>
	 * Convert an ISO8601 date string to one of the following results:
	 * to_date('1995-05-23', 'YYYY-MM-DD') to_date('1995-05-23 09:23:59',
	 * 'YYYY-MM-DD HH24:MI:SS')
	 * <p/>
	 * Implementation restriction: Currently, only the following subsets of
	 * ISO8601 are supported: YYYY-MM-DD hh:mm:ss YYYY-MM-DDThh:mm:ss
	 */
	@Override
	public String getDateLiteral(String isoDate) {
		String normalLiteral = super.getDateLiteral(isoDate);

		if (isDateOnly(isoDate)) {
			StringBuffer val = new StringBuffer();
			val.append("DATE(");
			val.append(normalLiteral);
			val.append(')');
			return val.toString();
		} else if (isTimeOnly(isoDate)) {
			StringBuffer val = new StringBuffer();
			val.append("TIME(");
			val.append(normalLiteral);
			val.append(')');
			return val.toString();
		} else if (isDateTime(isoDate)) {
			StringBuffer val = new StringBuffer();
			val.append("TIMESTAMP(");
			val.append(normalLiteral);
			val.append(')');
			return val.toString();
		} else {
			return "UNSUPPORTED:" + isoDate;
		}
	}

	@Override
	public boolean supportsTablespaces() {
		return true;
	}

	@Override
	public String getViewDefinition(CatalogAndSchema schema, String name) throws DatabaseException {
		return super.getViewDefinition(schema, name).replaceFirst("CREATE VIEW \\w+ AS ", ""); // db2
																								// returns
																								// "create
																								// view....as
																								// select
	}

	@Override
	public java.util.Date parseDate(String dateAsString) throws DateParseException {
		try {
			if (dateAsString.indexOf(' ') > 0) {
				return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateAsString);
			} else if (dateAsString.indexOf('.') > 0 && dateAsString.indexOf('-') > 0) {
				return new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSSSSS").parse(dateAsString);

			} else {
				if (dateAsString.indexOf(':') > 0) {
					return new SimpleDateFormat("HH:mm:ss").parse(dateAsString);
				} else if (dateAsString.indexOf('.') > 0) {
					return new SimpleDateFormat("HH.mm.ss").parse(dateAsString);
				} else {
					return new SimpleDateFormat("yyyy-MM-dd").parse(dateAsString);
				}
			}
		} catch (ParseException e) {
			throw new DateParseException(dateAsString);
		}
	}

	@Override
	public String generatePrimaryKeyName(String tableName) {
		if (tableName.equals(getDatabaseChangeLogTableName())) {
			tableName = "DbChgLog".toUpperCase();
		} else if (tableName.equals(getDatabaseChangeLogLockTableName())) {
			tableName = "DbChgLogLock".toUpperCase();
		}

		String pkName = super.generatePrimaryKeyName(tableName);
		if (pkName.length() > 18) {
			pkName = pkName.substring(0, 17);
		}
		return pkName;
	}

	@Override
	public String escapeIndexName(String catalogName, String schemaName, String indexName) {
		// does not support the schema name for the index -
		return super.escapeIndexName(null, null, indexName);
	}

	@Override
	public CatalogAndSchema getSchemaFromJdbcInfo(String rawCatalogName, String rawSchemaName) {
		return this.correctSchema(new CatalogAndSchema(rawSchemaName, null));
	}

	@Override
	public String getJdbcCatalogName(CatalogAndSchema schema) {
		return null;
	}

	@Override
	public String getJdbcSchemaName(CatalogAndSchema schema) {
		return correctObjectName(schema.getCatalogName(), Catalog.class);
	}

	@Override
	public boolean jdbcCallsCatalogsSchemas() {
		return true;
	}

	// DB2 LUW and z/OS separation
	public abstract String getDB2ProductTypeTestSql();
	
	public abstract String getSelectSequenceSql(String catalogName);

	public abstract String getUniqueConstraintListSql(String catalogName, String tableName);

	public abstract String getUniqueConstraintColumnListSql(String constraintName);

	public abstract String getViewDefinitionSql(String schemaName, String viewName);

	protected boolean isCorrectDB2ProductType(DatabaseConnection conn) {
		String sql = getDB2ProductTypeTestSql();
		Statement stmt = null;
		try {
			stmt = ((JdbcConnection) conn).getUnderlyingConnection().createStatement();
			stmt.execute(sql);
			return true;
		} catch (SQLException e) {
			// No DB2 LUW
			return false;
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					// Do nothing
				}
			}
		}
	}

}
