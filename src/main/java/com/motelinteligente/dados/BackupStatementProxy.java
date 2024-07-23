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
import java.sql.SQLException;
import java.sql.Statement;

public class BackupStatementProxy implements InvocationHandler {

    private final Statement originalStatement;
    private final BackupQueueManager queueManager;

    public BackupStatementProxy(Statement originalStatement, BackupQueueManager queueManager) {
        this.originalStatement = originalStatement;
        this.queueManager = queueManager;
    }

    public Statement createProxy() {
        return (Statement) Proxy.newProxyInstance(
                originalStatement.getClass().getClassLoader(),
                new Class[]{Statement.class},
                this
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("execute") || method.getName().equals("executeUpdate") || method.getName().equals("executeQuery")) {
            // Adiciona a tarefa à fila de backup
            queueManager.addTask(new BackupTask((String) args[0]));
        }
        return method.invoke(originalStatement, args);
    }
}