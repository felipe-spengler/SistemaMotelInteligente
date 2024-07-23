/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.dados;

/**
 *
 * @author MOTEL
 */
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BackupPreparedStatementProxy implements InvocationHandler {

    private final PreparedStatement originalPreparedStatement;
    private final String sql;
    private final BackupQueueManager queueManager;

    public BackupPreparedStatementProxy(PreparedStatement originalPreparedStatement, String sql, BackupQueueManager queueManager) {
        this.originalPreparedStatement = originalPreparedStatement;
        this.sql = sql;
        this.queueManager = queueManager;
    }

    public PreparedStatement createProxy() {
        return (PreparedStatement) Proxy.newProxyInstance(
                originalPreparedStatement.getClass().getClassLoader(),
                new Class[]{PreparedStatement.class},
                this
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("execute") || method.getName().equals("executeUpdate") || method.getName().equals("executeQuery")) {
            // Adiciona a tarefa à fila de backup
            queueManager.addTask(new BackupTask(sql));
        }
        return method.invoke(originalPreparedStatement, args);
    }
}