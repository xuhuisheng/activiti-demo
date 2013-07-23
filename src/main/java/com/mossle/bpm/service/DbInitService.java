package com.mossle.bpm.service;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.PostConstruct;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mossle.core.util.DatabaseType;

@Service
@Transactional
public class DbInitService {
	private Logger logger = LoggerFactory.getLogger(DbInitService.class);
	@Autowired
	private JdbcTemplate jdbcTemplate;
	private DatabaseType databaseType = DatabaseType.H2;
	
	@PostConstruct
	public void initData(){
		initDatabaseType();
		if(!existTable("ACT_GE_PROPERTY")){
			runScript(getActivitiResourceForDbOperation(databaseType.toLowerCase(), "drop", "drop", "engine"));
			runScript(getActivitiResourceForDbOperation(databaseType.toLowerCase(), "drop", "drop", "history"));
			runScript(getActivitiResourceForDbOperation(databaseType.toLowerCase(), "drop", "drop", "identity"));
			runScript(getActivitiResourceForDbOperation(databaseType.toLowerCase(), "create", "create", "engine"));
			runScript(getActivitiResourceForDbOperation(databaseType.toLowerCase(), "create", "create", "history"));
			runScript(getActivitiResourceForDbOperation(databaseType.toLowerCase(), "create", "create", "identity"));
			runScript("ddl/V1_0_0__data.sql");
		}
		if(!existTable("bpm_delegate_info")){
			runScript("ddl/"+databaseType.toLowerCase()+"/bpm_sql.sql");
		}
	}
	
	protected String getActivitiResourceForDbOperation(String databaseType, String directory, String operation, String component) {
		return "org/activiti/db/" + directory + "/activiti." + databaseType + "." + operation + "." + component + ".sql";
	}
	
	/**
	 * 执行sql文件
	 */
	protected boolean runScript(String sqlFile){
		boolean b = false;
		try {
			Resources.setCharset(Charset.forName("UTF-8"));
			b = runScript(Resources.getResourceAsReader(sqlFile));
			if(b){
				logger.info("执行SQL文件:["+sqlFile+"] 成功!!!");
			}else{
				logger.info("执行SQL文件:["+sqlFile+"] 失败!!!");
			}
		} catch (IOException e) {
			logger.error("sql文件执行出错:"+e.getMessage());
		}
		return b;
	}
	
	
	
	protected boolean runScript(Reader reader){
		boolean result = false;
		Connection conn = null;
		try {
			conn = jdbcTemplate.getDataSource().getConnection();
			ScriptRunner runner = new ScriptRunner(conn);  
			runner.setAutoCommit(true);
			runner.setStopOnError(false);
			runner.setErrorLogWriter(null);  
			runner.setLogWriter(null);  
			runner.runScript(reader);  
			result = true;
		}catch(Exception ex){
			logger.error(ex.getMessage());
		}finally{
			try {
				conn.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
			}
		}
		return result;
	}
	
	protected boolean existTable(String tablename){
		boolean result = false;
		Connection conn = null;
		DatabaseMetaData dbmd = null;
		ResultSet rs = null;
		try {
			conn = jdbcTemplate.getDataSource().getConnection();
			dbmd = conn.getMetaData();
			String schemaName = getSchemaName(dbmd);
			rs = dbmd.getTables(null , schemaName ,  tablename, new String[]{"TABLE"});
			if(rs.next()){
				result = true;
			}
		}catch(Exception ex){
			logger.error(ex.getMessage());
		}finally{
			try {
				dbmd = null;
				rs.close();
				conn.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
			}
		}
		return result;
	}
	
	private String getSchemaName(DatabaseMetaData dbmd) throws SQLException{
		String schemaName;
		switch (databaseType.getValue()) {
		case 1://mssql
			schemaName = "dbo";
			break;
		case 4://h2
			schemaName = null;
			break;
		default:
			schemaName = dbmd.getUserName();
			break;
		}
		return schemaName;
	}
	
	private void initDatabaseType(){
		Connection conn = null;
		DatabaseMetaData dbmd = null;
		try {
			conn = jdbcTemplate.getDataSource().getConnection();
			dbmd = conn.getMetaData();
			databaseType = DatabaseType.getDatabaseType(dbmd.getDatabaseProductName());
		}catch(Exception ex){
			logger.error(ex.getMessage());
		}finally{
			try {
				dbmd = null;
				conn.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
			}
		}
	}
}
