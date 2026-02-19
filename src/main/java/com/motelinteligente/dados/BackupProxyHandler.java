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

    private static final Set<String> EXCLUDED_TABLES = new HashSet<>(Arrays.asList(
            "ALARMES", "ANTECIPADO", "CAIXA", "CONFIGURACOES", "DESISTENCIA",
            "FUNCIONARIO", "IMAGENS", "JUSTIFICATIVA", "PREVENDIDOS", "PRODUTOS", "QUARTOS",
            "REGISTRALIMPEZA", "REGISTRALOCADO", "REGISTRAMANUTENCAO", "REGISTRARESERVA", "REGISTRAVENDIDO",
            "STATUS", "VALORCARTAO", "PORTOES", "RESERVAS", "LOG_SINCRONIZACAO"));

    public static boolean shouldSkipTable(String sql) {
        if (sql == null)
            return false;
        String s = sql.trim().toUpperCase();
        // Normaliza espaços para facilitar split
        s = s.replaceAll("\\s+", " ");
        String[] words = s.split(" ");

        String tableName = null;

        if (s.startsWith("INSERT INTO")) {
            if (words.length > 2)
                tableName = words[2];
        } else if (s.startsWith("UPDATE")) {
            if (words.length > 1)
                tableName = words[1];
        } else if (s.startsWith("DELETE FROM")) {
            if (words.length > 2)
                tableName = words[2];
        }

        if (tableName != null) {
            // Remove crases, aspas ou parenteses que podem estar colados no nome
            tableName = tableName.replaceAll("[`'\"]", "");
            int parenIndex = tableName.indexOf('(');
            if (parenIndex > 0)
                tableName = tableName.substring(0, parenIndex);

            return EXCLUDED_TABLES.contains(tableName);
        }
        return false;
    }

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
                    new Class[] { PreparedStatement.class },
                    new PreparedStatementProxyHandler(preparedStatement, sql));
        }
        // 2. Intercepta a criação de Statement
        if ("createStatement".equals(method.getName())) {
            Statement statement = (Statement) method.invoke(originalConnection, args);

            return Proxy.newProxyInstance(
                    statement.getClass().getClassLoader(),
                    new Class[] { Statement.class },
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

        // 1. Executa o comando original primeiro
        Object result = method.invoke(originalStatement, args);

        // 2. Após sucesso, verifica necessidade de backup/sync
        if ("execute".equals(methodName) || "executeQuery".equals(methodName) || "executeUpdate".equals(methodName)) {
            if (args.length > 0 && args[0] instanceof String) {
                String sql = (String) args[0];

                if (isWriteOperation(sql)) {
                    // SE for tabela crítica (CheckSincronia)
                    if (BackupProxyHandler.shouldSkipTable(sql)) {
                        // Aciona a sincronia inteligente imediatamente
                        if (CheckSincronia.getInstance() != null) {
                            CheckSincronia.getInstance().solicitarSincroniaImediata();
                        }
                    } else {
                        // Senão, usa o backup "cego" (fila de SQL)
                        BackupQueueManager.getInstance().addTask(new BackupTask(sql, null));
                    }
                }
            }
        }
        return result;
    }

    private boolean isWriteOperation(String sql) {
        // Agora verificamos apenas se é escrita, a decisão de skiipar é feita no invoke
        String trimmedSql = sql.trim().toUpperCase();
        return trimmedSql.startsWith("INSERT") || trimmedSql.startsWith("UPDATE") || trimmedSql.startsWith("DELETE");
    }
}

class PreparedStatementProxyHandler implements InvocationHandler {

    private final PreparedStatement originalPreparedStatement;
    private final String sql;
    private final Object[] parameters = new Object[100]; // Aumentado para 100 para evitar erro em queries grandes
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(BackupProxyHandler.class);

    public PreparedStatementProxyHandler(PreparedStatement originalPreparedStatement, String sql) {
        this.originalPreparedStatement = originalPreparedStatement;
        this.sql = sql;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().startsWith("set") && args.length >= 2) {
            try {
                int parameterIndex = (Integer) args[0] - 1;
                if (parameterIndex >= 0 && parameterIndex < parameters.length) {
                    parameters[parameterIndex] = args[1];
                }
            } catch (Exception e) {
                // Ignora erro de captura de parâmetro para não travar a aplicação principal
            }
        }

        // 1. Executa o comando original primeiro
        Object result = method.invoke(originalPreparedStatement, args);

        // 2. Após sucesso, verifica necessidade de backup/sync
        if ("execute".equals(method.getName()) || "executeQuery".equals(method.getName())
                || "executeUpdate".equals(method.getName())) {

            String completeSql = fillPlaceholders(sql, parameters);

            if (isWriteOperation(completeSql)) {
                // SE for tabela crítica (CheckSincronia)
                if (BackupProxyHandler.shouldSkipTable(completeSql)) {
                    // Aciona a sincronia inteligente imediatamente
                    if (CheckSincronia.getInstance() != null) {
                        CheckSincronia.getInstance().solicitarSincroniaImediata();
                    }
                } else {
                    // Senão, usa o backup "cego"
                    BackupQueueManager.getInstance().addTask(new BackupTask(completeSql, originalPreparedStatement));
                }
            }
        }
        return result;
    }

    // Método auxiliar não alterado...
    private String fillPlaceholders(String sql, Object[] parameters) {
        StringBuilder completeSql = new StringBuilder();
        int paramIndex = 0;
        for (char ch : sql.toCharArray()) {
            if (ch == '?' && paramIndex < parameters.length) {
                Object param = parameters[paramIndex++];
                if (param instanceof Date) {
                    completeSql.append("'").append(DateUtils.formatTimestamp((Date) param)).append("'");
                } else if (param instanceof String) {
                    completeSql.append("'").append(param).append("'");
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
