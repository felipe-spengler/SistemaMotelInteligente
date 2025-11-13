/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.dados;


import java.lang.reflect.Proxy;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class BackupProxyHandler implements InvocationHandler {

    private final Connection originalConnection;
    private final Set<String> sqlWriteOperations = new HashSet<>(Arrays.asList("INSERT", "UPDATE", "DELETE"));
    
    public BackupProxyHandler(Connection originalConnection) {
        this.originalConnection = originalConnection;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = method.invoke(originalConnection, args);

        if ("prepareStatement".equals(method.getName())) {
            String sql = (String) args[0];
            PreparedStatement preparedStatement = (PreparedStatement) result;

            // Return a proxy for the prepared statement
            return Proxy.newProxyInstance(
                    preparedStatement.getClass().getClassLoader(),
                    new Class[]{PreparedStatement.class},
                    new PreparedStatementProxyHandler(preparedStatement, sql)
            );
        }
        // 2. Intercepta a criação de Statement
        if ("createStatement".equals(method.getName())) {
            Statement statement = (Statement) method.invoke(originalConnection, args);
            
            return Proxy.newProxyInstance(
                statement.getClass().getClassLoader(),
                new Class[]{Statement.class},
                new StatementProxyHandler(statement) // Novo handler para Statement
            );
        }
        return result;
    }
}
 class StatementProxyHandler implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(StatementProxyHandler.class);
    private final Statement originalStatement;

    public StatementProxyHandler(Statement originalStatement) {
        this.originalStatement = originalStatement;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        
        // Intercepta os métodos de execução
        if ("execute".equals(methodName) || "executeQuery".equals(methodName) || "executeUpdate".equals(methodName)) {
            if (args.length > 0 && args[0] instanceof String) {
                String sql = (String) args[0];
            
                // Verifica se é uma operação de escrita antes de adicionar à fila
                if (isWriteOperation(sql)) {
                    //logger.info("Query de backup (Statement): \n" + sql);
                    BackupQueueManager.getInstance().addTask(new BackupTask(sql, null)); 
                }
            }

            return method.invoke(originalStatement, args);
        }
        
        return method.invoke(originalStatement, args);
    }

    private boolean isWriteOperation(String sql) {
        String trimmedSql = sql.trim().toUpperCase();
        return trimmedSql.startsWith("INSERT") || trimmedSql.startsWith("UPDATE") || trimmedSql.startsWith("DELETE");
    }
}
class PreparedStatementProxyHandler implements InvocationHandler {

    private final PreparedStatement originalPreparedStatement;
    private final String sql;
    private final Object[] parameters = new Object[10]; // Assuming a max of 10 parameters for simplicity
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(BackupProxyHandler.class);

    public PreparedStatementProxyHandler(PreparedStatement originalPreparedStatement, String sql) {
        this.originalPreparedStatement = originalPreparedStatement;
        this.sql = sql;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().startsWith("set")) {
            int parameterIndex = (Integer) args[0] - 1;
            parameters[parameterIndex] = args[1];
        }

        if ("execute".equals(method.getName()) || "executeQuery".equals(method.getName()) || "executeUpdate".equals(method.getName())) {
            String completeSql = fillPlaceholders(sql, parameters);
            
            if (isWriteOperation(completeSql)) {
                //logger.info(String.format("Query de backup Prepared: " + completeSql));
                BackupQueueManager.getInstance().addTask(new BackupTask(completeSql, originalPreparedStatement));
            }

            return method.invoke(originalPreparedStatement, args);
        }

        return method.invoke(originalPreparedStatement, args);
    }

    private String fillPlaceholders(String sql, Object[] parameters) {
        

        StringBuilder completeSql = new StringBuilder();
        int paramIndex = 0;
        for (char ch : sql.toCharArray()) {
            if (ch == '?' && paramIndex < parameters.length) {
                Object param = parameters[paramIndex++];
                if (param instanceof Date) {
                    completeSql.append("'").append(DateUtils.formatTimestamp((Date) param)).append("'"); // Formata corretamente o timestamp com aspas
                } else if (param instanceof String) {
                    completeSql.append("'").append(param).append("'"); // Coloca aspas para strings
                } else {
                    completeSql.append(param != null ? param.toString() : "NULL");
                }
            } else {
                completeSql.append(ch);
            }
        }
        
        return completeSql.toString();
    }

    private boolean isWriteOperation(String sql) {
        // Check if the SQL starts with a write operation keyword
        String trimmedSql = sql.trim().toUpperCase();
        return trimmedSql.startsWith("INSERT") || trimmedSql.startsWith("UPDATE") || trimmedSql.startsWith("DELETE");
    }
}

class DateUtils {

    private static final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public static String formatTimestamp(Date date) {
        return timestampFormat.format(date);
    }
}
