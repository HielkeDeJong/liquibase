package liquibase.database.core;

import liquibase.structure.core.Catalog;
import liquibase.structure.core.Table;
import liquibase.structure.core.UniqueConstraint;

public class DB2ZOSDatabase extends DB2Database {

	@Override
	public String getDB2ProductTypeTestSql() {
		return "SELECT COUNT(*) FROM SYSIBM.SYSTABLES";
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
				.format("SELECT DISTINCT K.CONSTNAME AS CONSTRAINT_NAME, T.TABNAME AS TABLE_NAME FROM SYSIBM.SYSKEYCOLUSE K, SYSIBM.SYSTABCONST T WHERE K.CONSTNAME = T.CONSTNAME AND T.TYPE='U' AND T.TBCREATOR = '%s'",
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

}
