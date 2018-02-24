package com.rhjf.base;


import java.sql.CallableStatement; 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class BaseDao {
	protected Logger log = Logger.getLogger(this.getClass());

	/**
	 * 执行新增和修改的数据库操作,不用处理返回的ResultSet结果集
	 * 
	 * @param sql
	 *            sql语句
	 * @param params
	 *            参数，若为日期，需要特别处理
	 * @return
	 */
	public int executeSql(String sql, Object[] params) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			connection = ConnectionFactory.getInstance().getConnection();
			preparedStatement = connection.prepareStatement(sql);
			// log.debug("executeSql sql = " + sql);
			// log.debug("params = " + params);
			if (params != null) {
				// 设置sql语句参数
				for (int i = 0; i < params.length; i++) {
					// log.debug("params[i] = " + params[i]);
					if (params[i] != null) {
						if (params[i] instanceof java.util.Date) {
							preparedStatement
									.setTimestamp(i + 1, new Timestamp(
											((Date) params[i]).getTime()));
						} else {
							preparedStatement.setObject(i + 1, params[i]);
						}
					} else {
						preparedStatement.setString(i + 1, "");
					}
				}
			}
			return preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			
			log.error(e.getMessage() + "code = " + e.getErrorCode()+",sql:"+sql + "params:" + Arrays.toString(params));
			// /throw new RuntimeException(e.getMessage() + "code = " +
			// e.getErrorCode());
			return -1;
		} finally {
			ConnectionFactory.getInstance().closeConnection(connection,
					preparedStatement, resultSet);
		}

	}

	/**
	 * 批量执行sql语句 paramsArr是个2维数组，第一维度表示各条记录，第二维度表示各条记录里的各个parameter值
	 */
	public int[] executeBatchSql(String sql, Object[][] paramsArr) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection = ConnectionFactory.getInstance().getConnection();
			preparedStatement = connection.prepareStatement(sql);

			if (paramsArr != null) {
				for (int s = 0; s < paramsArr.length; s++) {
					Object[] params = paramsArr[s];
					if (params != null) {
						// 设置sql语句参数
						for (int i = 0; i < params.length; i++) {
							if (params[i] != null) {
								if (params[i] instanceof java.util.Date) {
									preparedStatement.setTimestamp(
											i + 1,
											new Timestamp(((Date) params[i])
													.getTime()));
								} else {
									preparedStatement.setObject(i + 1,
											params[i]);
								}
							} else {
								preparedStatement.setString(i + 1, "");
							}
						}
						preparedStatement.addBatch();// /批量增加1条
					}
				}
			}
			return preparedStatement.executeBatch();// /批量执行
		} catch (SQLException e) {
			e.printStackTrace();
			log.error(e.getMessage() + "code = " + e.getErrorCode()+",sql:"+sql);
		} finally {
			ConnectionFactory.getInstance().closeConnection(connection,preparedStatement, resultSet);
		}
		return null;
	}
	
	/**
	 *   批量执行不同的sql语句 不包含查询
	 * executeBatchSql
	 * @time 2015年9月23日下午4:23:16
	 * @packageName com.dl.ios6
	 * @param sql  多个sql语句的数组
	 * @return
	 */
	public int[] executeBatchSql(String[] sql){
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		connection = ConnectionFactory.getInstance().getConnection();
		Statement state = null;
		try {
			if(sql!=null&&sql.length>0){
				boolean autoCommit = connection.getAutoCommit();
				connection.setAutoCommit(false);
			    state = connection.createStatement();
				for (int i = 0; i < sql.length; i++) {
					state.addBatch(sql[i]);
				}
				int j[] = state.executeBatch();
				connection.commit();
				connection.setAutoCommit(autoCommit);
				state.close();
				ConnectionFactory.getInstance().closeConnection(connection, preparedStatement, resultSet);
				return j;
			}
		} catch (SQLException e) {
			state = null;
			ConnectionFactory.getInstance().closeConnection(connection, preparedStatement, resultSet);
		}
		return null;
	}
	

	/**
	 * 批量执行sql语句 paramsArr是个2维数组，第一维度表示各条记录，第二维度表示各条记录里的各个parameter值
	 */
	public int[] executeBatchSql(String sql, List<Object[]> paramsList) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {

			connection = ConnectionFactory.getInstance().getConnection();
			preparedStatement = connection.prepareStatement(sql);

			if (paramsList == null){
				return null;
			}
			// /遍历所有记录
			for (int i = 0; i < paramsList.size(); i++) {
				Object[] tObj = paramsList.get(i);
				if (tObj == null) {
					continue;
				}
				// /遍历记录中的每个字段
				for (int j = 0; j < tObj.length; j++) {
					Object curObj = tObj[j];
					if (curObj != null) {
						if (curObj instanceof java.util.Date) {
							preparedStatement.setTimestamp(j + 1,
									new Timestamp(((java.util.Date) curObj).getTime()));
						} else {
							preparedStatement.setObject(j + 1, curObj);
						}
					} else{
						preparedStatement.setString(j + 1, null);
					}
				}// /遍历记录中的每个字段
				preparedStatement.addBatch();// /添加一条记录
			}// /遍历所有记录

			return preparedStatement.executeBatch();// /批量执行
		} catch (SQLException e) {
			e.printStackTrace();
			log.error(e.getMessage() + "code = " + e.getErrorCode()+",sql:"+sql);
			System.exit(1);
		} finally {
			ConnectionFactory.getInstance().closeConnection(connection,
					preparedStatement, resultSet);
		}
		return null;
	}

	/*
	 * 执行sql操作，把sql和params结合成一个sql语句
	 * 执行sql查询的结果集交给sqlExecute这个接口函数处理，处理后封装的对象放到List里
	 */
	public List<Map<String, Object>> queryForList(String sql, Object[] params) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			connection = ConnectionFactory.getInstance().getConnection();
			preparedStatement = connection.prepareStatement(sql);
			// 设置sql语句参数
			if (params != null) {
				for (int i = 0; i < params.length; i++) {
					// log.debug("params[i] = " + params[i]);
					preparedStatement.setObject(i + 1, params[i]);
				}
			}
			resultSet = preparedStatement.executeQuery();
			ResultSetMetaData md = resultSet.getMetaData(); // 得到结果集(rs)的结构信息，比如字段数、字段名等
			int columnCount = md.getColumnCount();
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			Map<String, Object> rowData = new HashMap<String, Object>();
			while (resultSet.next()) {
				rowData = new HashMap<String, Object>(columnCount);
				for (int i = 1; i <= columnCount; i++) {
					rowData.put(md.getColumnLabel(i), resultSet.getString(i));
				}
				list.add(rowData);
			}
			return list;
		} catch (SQLException e) {
			log.error(e.getMessage() + "code = " + e.getErrorCode()+",sql:"+sql);
		} finally {
			ConnectionFactory.getInstance().closeConnection(connection,
					preparedStatement, resultSet);
		}
		return null;
	}
	
	
	public List<Map<String, String>> queryForList(String sql) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			connection = ConnectionFactory.getInstance().getConnection();
			preparedStatement = connection.prepareStatement(sql);
			resultSet = preparedStatement.executeQuery();
			ResultSetMetaData md = resultSet.getMetaData(); // 得到结果集(rs)的结构信息，比如字段数、字段名等
			int columnCount = md.getColumnCount();
			List<Map<String, String>> list = new ArrayList<Map<String, String>>();
			Map<String, String> rowData = new HashMap<String, String>();
			while (resultSet.next()) {
				rowData = new HashMap<String, String>(columnCount);
				for (int i = 1; i <= columnCount; i++) {
					rowData.put(md.getColumnLabel(i), resultSet.getString(i));
				}
				list.add(rowData);
			}

			return list;
		} catch (SQLException e) {
			log.error(e.getMessage() + "code = " + e.getErrorCode()+",sql:"+sql);
		} finally {
			ConnectionFactory.getInstance().closeConnection(connection,
					preparedStatement, resultSet);
		}
		return null;
	}
	
	
	public Map<String, Object> queryForMap(String sql, Object[] params) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			connection = ConnectionFactory.getInstance().getConnection();
			preparedStatement = connection.prepareStatement(sql);
			// 设置sql语句参数
			if (params != null) {
				for (int i = 0; i < params.length; i++) {
					// log.debug("params[i] = " + params[i]);
					preparedStatement.setObject(i + 1, params[i]);
				}
			}
			resultSet = preparedStatement.executeQuery();
			ResultSetMetaData md = resultSet.getMetaData(); // 得到结果集(rs)的结构信息，比如字段数、字段名等
			int columnCount = md.getColumnCount();
			Map<String, Object> rowData = null;
			while (resultSet.next()) {
				rowData = new HashMap<String, Object>(columnCount);
				for (int i = 1; i <= columnCount; i++) {
					rowData.put(md.getColumnLabel(i), resultSet.getObject(i));
				}
				break;
			}

			return rowData;
		} catch (SQLException e) {
//			log.error(e.getMessage());
			log.error(e.getMessage() + "code = " + e.getErrorCode()+",sql:"+sql);
		} finally {
			ConnectionFactory.getInstance().closeConnection(connection,
					preparedStatement, resultSet);
		}
		return null;
	}
	
	
	
	public Map<String, String> queryForMapStr(String sql, Object[] params) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			connection = ConnectionFactory.getInstance().getConnection();
			preparedStatement = connection.prepareStatement(sql);
			// 设置sql语句参数
			if (params != null) {
				for (int i = 0; i < params.length; i++) {
					// log.debug("params[i] = " + params[i]);
					preparedStatement.setObject(i + 1, params[i]);
				}
			}
			resultSet = preparedStatement.executeQuery();
			ResultSetMetaData md = resultSet.getMetaData(); // 得到结果集(rs)的结构信息，比如字段数、字段名等
			int columnCount = md.getColumnCount();
			Map<String, String> rowData = null;
			while (resultSet.next()) {
				rowData = new HashMap<String, String>(columnCount);
				for (int i = 1; i <= columnCount; i++) {
					rowData.put(md.getColumnLabel(i), resultSet.getString(i));
				}
				break;
			}

			return rowData;
		} catch (SQLException e) {
//			log.error(e.getMessage());
			log.error(e.getMessage() + "code = " + e.getErrorCode()+",sql:"+sql);
		} finally {
			ConnectionFactory.getInstance().closeConnection(connection,
					preparedStatement, resultSet);
		}
		return null;
	}
	
	
	
	public int executeProcedure(String name) {
		int seqNo = 0;
		CallableStatement cStmt = null;
		Connection conn = null;
		ResultSet rs = null;
		try {
			conn = ConnectionFactory.getInstance().getConnection();
			cStmt = conn.prepareCall("{call "+name+"}");
			rs = cStmt.executeQuery();
			if(rs.next()){
				seqNo = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return 1;
		} finally {
			cStmt = null;
			ConnectionFactory.getInstance().closeConnection(conn, null, rs);
		}
		return seqNo;
	}
}
