package com.rhjf.base;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**使用C3P0连接池
 * 从C3P0连接池ComboPooledDataSource中获得Connection
 * 关闭Connection
 * */

public class OnlineConnectionFactory {

    public static OnlineConnectionFactory connectionFactory = new OnlineConnectionFactory();

    private ComboPooledDataSource ds; ///C3P0连接池

    ///初始化数据库连接池的参数：5个
    ///给C3P0连接池指定DriverClass、JdbcUrl、User、Password、InitialPoolSize、MaxPoolSize、CheckoutTimeout
    private OnlineConnectionFactory() {
        ds = new ComboPooledDataSource();
        try {
        	
            String DriverClass = PropertyUtils.getValue("OnlineDriverClass");
            //log.debug("DriverClass = " + DriverClass);
            if (DriverClass != null) {
                ds.setDriverClass(DriverClass);
            }
        }
        catch (PropertyVetoException e) {
        }
        String JdbcUrl = PropertyUtils.getValue("OnlineJdbcUrl");
        //log.debug("JdbcUrl = " + JdbcUrl);
        if (JdbcUrl != null) {
            ds.setJdbcUrl(JdbcUrl);
        }


        String User = PropertyUtils.getValue("OnlineUser");
        //log.debug("User = " + User);
        if (User != null) {
            ds.setUser(User);
        }

        String Password = PropertyUtils.getValue("OnlinePassword");
        //log.debug("Password = " + Password);
        if (Password != null) {
            ds.setPassword(Password);
        }

        String InitialPoolSize = PropertyUtils.getValue("InitialPoolSize");
        //log.debug("InitialPoolSize = " + InitialPoolSize);
        if (InitialPoolSize != null) {
            ds.setInitialPoolSize(Integer.parseInt(InitialPoolSize));
        }

        String MaxPoolSize = PropertyUtils.getValue("MaxPoolSize");
        //log.debug("MaxPoolSize = " + MaxPoolSize);
        if (MaxPoolSize != null) {
            ds.setMaxPoolSize(Integer.parseInt(MaxPoolSize));
        }

        String CheckoutTimeout = PropertyUtils.getValue("CheckoutTimeout");
        //log.debug("CheckoutTimeout = " + CheckoutTimeout);
        if (CheckoutTimeout != null) {
           ds.setCheckoutTimeout(Integer.parseInt(CheckoutTimeout));
        }
        
        String MaxIdleTime = PropertyUtils.getValue("MaxIdleTime");
        //log.debug("MaxIdleTime = " + MaxIdleTime);
        if(MaxIdleTime!=null){
        	ds.setMaxIdleTime(Integer.parseInt(MaxIdleTime));
        }
        
    }

    public static OnlineConnectionFactory getInstance() {
        return connectionFactory;
    }

    /**
     * 获得数据库连接
     *
     * @return
     */
    public Connection getConnection() {
        try {
            return ds.getConnection();
        } catch (SQLException e) {
        	e.printStackTrace();
            throw new RuntimeException(e.getMessage() + "code = " + e.getErrorCode());
        } 
    }

    /**
     * 关闭数据库连接
     *
     * @param connection
     * @param prepareStatement
     * @param resultSet
     */
    public  void closeConnection(Connection connection, PreparedStatement prepareStatement, ResultSet resultSet) {

        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (prepareStatement != null) {
                prepareStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage() + "code = " + e.getErrorCode());
        }
    }

}
