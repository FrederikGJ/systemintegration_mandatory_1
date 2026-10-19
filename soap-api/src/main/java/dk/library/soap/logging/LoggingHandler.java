package dk.library.soap.logging;

import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Skriver hver SOAP-besked ud i konsollen, både request og response (også faults).
 * SOAP-udgaven af et servlet-filter. Rører ikke ved beskeden.
 */
public class LoggingHandler implements SOAPHandler<SOAPMessageContext> {

    @Override
    public boolean handleMessage(SOAPMessageContext ctx) {
        boolean outbound = (Boolean) ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        System.out.println(outbound
                ? "\n===== SOAP >>> OUTBOUND response ====="
                : "\n===== SOAP <<< INBOUND request =====");
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ctx.getMessage().writeTo(out);
            System.out.println(out.toString(StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.out.println("(kunne ikke skrive beskeden ud: " + e.getMessage() + ")");
        }
        return true;
    }

    @Override public boolean handleFault(SOAPMessageContext ctx) { return handleMessage(ctx); }
    @Override public void close(MessageContext ctx) { }
    @Override public Set<QName> getHeaders() { return null; }
}
