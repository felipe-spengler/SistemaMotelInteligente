package com.motelinteligente.dados;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.LayoutBase;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HtmlLayout extends LayoutBase<ILoggingEvent> {

    @Override
    public String getFileHeader() {
        return "<html>\n<head>\n" +
                "<meta charset=\"UTF-8\">\n" +
                "<title>Logs do Sistema</title>\n" +
                "<style>\n" +
                "  body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8fafc; color: #334155; margin: 20px; }\n" +
                "  h2 { color: #1e293b; }\n" +
                "  table { width: 100%; border-collapse: collapse; box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1); background-color: white; border-radius: 8px; overflow: hidden; }\n" +
                "  th { background-color: #475569; color: white; padding: 12px 16px; text-align: left; font-size: 14px; }\n" +
                "  td { padding: 10px 16px; border-bottom: 1px solid #e2e8f0; font-size: 13px; word-break: break-word; }\n" +
                "  .level-info { background-color: #f0fdf4; color: #166534; }\n" + // Verde
                "  .level-warn { background-color: #fef9c3; color: #854d0e; }\n" + // Amarelo
                "  .level-error { background-color: #fef2f2; color: #991b1b; }\n" + // Vermelho
                "  .level-other { background-color: #ffffff; color: #475569; }\n" +
                "  .stacktrace { font-family: monospace; white-space: pre-wrap; font-size: 11px; color: #dc2626; margin-top: 4px; }\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<h2>Logs de Execução</h2>\n" +
                "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "  <th style=\"width: 15%\">Data/Hora</th>\n" +
                "  <th style=\"width: 8%\">Level</th>\n" +
                "  <th style=\"width: 12%\">Thread</th>\n" +
                "  <th style=\"width: 20%\">Logger</th>\n" +
                "  <th>Mensagem</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n";
    }

    @Override
    public String getFileFooter() {
        return "</tbody>\n</table>\n</body>\n</html>";
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        StringBuilder sb = new StringBuilder();
        String level = event.getLevel().toString().toLowerCase();
        String rowClass = "level-other";
        if (level.contains("info")) {
            rowClass = "level-info";
        } else if (level.contains("warn")) {
            rowClass = "level-warn";
        } else if (level.contains("error")) {
            rowClass = "level-error";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String timestamp = sdf.format(new Date(event.getTimeStamp()));

        sb.append("<tr class=\"").append(rowClass).append("\">\n");
        sb.append("  <td>").append(timestamp).append("</td>\n");
        sb.append("  <td><strong>").append(event.getLevel()).append("</strong></td>\n");
        sb.append("  <td>").append(event.getThreadName()).append("</td>\n");
        sb.append("  <td>").append(event.getLoggerName()).append("</td>\n");
        sb.append("  <td>").append(event.getFormattedMessage());

        // Exception/Stacktrace printing
        IThrowableProxy tp = event.getThrowableProxy();
        if (tp != null) {
            sb.append("<div class=\"stacktrace\">");
            sb.append(tp.getClassName()).append(": ").append(tp.getMessage()).append("\n");
            for (StackTraceElementProxy step : tp.getStackTraceElementProxyArray()) {
                sb.append("  at ").append(step.toString()).append("\n");
            }
            sb.append("</div>");
        }

        sb.append("</td>\n");
        sb.append("</tr>\n");

        return sb.toString();
    }
}
