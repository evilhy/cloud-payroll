package chain.fxgj.server.payroll.config.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author chain
 * create by chain on 2019-01-30 10:31
 **/
public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p == null || p.getText() == null || p.getText().trim().isEmpty()) {
            return null;
        }
        return Instant.ofEpochMilli(Long.parseLong(p.getText())).atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
