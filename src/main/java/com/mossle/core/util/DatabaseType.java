package com.mossle.core.util;

public enum DatabaseType {
	ORACLE(0), SQLSERVER(1), MYSQL(2), DB2(3), H2(4);

	private int value;

	public int getValue() {
		return value;
	}

	DatabaseType(int value) {
		this.value = value;
	}

	public String toLowerCase() {
		switch (value) {
		case 0:
			return "oracle";
		case 1:
			return "mssql";
		case 2:
			return "mysql";
		case 3:
			return "db2";
		case 4:
			return "h2";
		}
		return null;
	}

	public static DatabaseType getDatabaseType(String databaseProductName) {
		if (databaseProductName.equals("Oracle")) {
			return ORACLE;
		} else if (databaseProductName.equals("Microsoft SQL Server")) {
			return SQLSERVER;
		} else if (databaseProductName.equals("MySQL")) {
			return MYSQL;
		} else if (databaseProductName.equals("Db2")) {
			return DB2;
		} else if (databaseProductName.equals("H2")) {
			return H2;
		} else if (databaseProductName.equals("HSQL Database Engine")) {
			return H2;
		} 
		return null;
	}

}