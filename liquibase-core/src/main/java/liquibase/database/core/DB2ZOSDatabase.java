package liquibase.database.core;

import liquibase.database.Database;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.structure.core.UniqueConstraint;
import liquibase.structure.core.View;

public class DB2ZOSDatabase extends DB2Database implements Database {

	@Override
	protected String getDefaultDatabaseProductName() {
		return "DB2 z/OS";
	}

	@Override
	public String getShortName() {
		return "db2z";
	}

	@Override
	public String getDB2ProductTypeTestSql() {
		//return "SELECT COUNT(*) FROM SYSIBM.SYSTABLES";
		return "SELECT COUNT(*) FROM SYSIBM.SYSKEYCOLUSE K, SYSIBM.SYSTABCONST T WHERE K.CONSTNAME = T.CONSTNAME";
	}

	@Override
	public String getSelectSequenceSql(String catalogName) {
		return String.format(
				"SELECT NAME AS SEQUENCE_NAME FROM SYSIBM.SYSSEQUENCES WHERE SEQTYPE='S' AND SCHEMA = '%s'",
				correctObjectName(catalogName, Catalog.class));
	}

	@Override
	public String getUniqueConstraintListSql(String catalogName, String tableName) {
		String sql = String
				.format("SELECT DISTINCT K.CONSTNAME AS CONSTRAINT_NAME, T.TBNAME AS TABLE_NAME FROM SYSIBM.SYSKEYCOLUSE K, SYSIBM.SYSTABCONST T WHERE K.CONSTNAME = T.CONSTNAME AND T.TYPE='U' AND T.TBCREATOR = '%s'",
						correctObjectName(catalogName, Catalog.class));
		if (tableName != null) {
			sql += " AND T.TBNAME = '" + correctObjectName(tableName, Table.class) + "'";
		}
		return sql;
	}

	@Override
	public String getUniqueConstraintColumnListSql(String constraintName) {
		return String
				.format("SELECT K.COLNAME AS COLUMN_NAME FROM SYSIBM.SYSKEYCOLUSE K, SYSIBM.SYSTABCONST T WHERE K.CONSTNAME = T.CONSTNAME AND T.TYPE='U' AND T.CONSTNAME = '%s' ORDER BY K.COLSEQ",
						correctObjectName(constraintName, UniqueConstraint.class));
	}

	@Override
	public String getViewDefinitionSql(String schemaName, String viewName) {
		return String.format(
				"SELECT TEXT AS VIEW_DEFINITION FROM SYSIBM.SYSVIEWS WHERE CREATOR = '%S' and NAME = '%S'",
				correctObjectName(schemaName, Schema.class), correctObjectName(viewName, View.class));
	}

}
