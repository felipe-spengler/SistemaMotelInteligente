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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class BackupProxyHandler implements InvocationHandler {

    private final Connection originalConnection;
    private final BackupQueueManager queueManager;

    public BackupProxyHandler(Connection originalConnection, BackupQueueManager queueManager) {
        this.originalConnection = originalConnection;
        this.queueManager = queueManager;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = method.invoke(originalConnection, args);
        
        // Intercepta as operações de banco de dados que precisam ser enfileiradas para backup
        if (method.getName().equals("createStatement")) {
            return new BackupStatementProxy((Statement) result, queueManager).createProxy();
        } else if (method.getName().equals("prepareStatement")) {
            return new BackupPreparedStatementProxy((PreparedStatement) result, (String) args[0], queueManager).createProxy();
        }
        
        return result;
    }
}