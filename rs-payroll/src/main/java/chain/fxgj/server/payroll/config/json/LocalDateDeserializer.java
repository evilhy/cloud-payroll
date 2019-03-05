package chain.fxgj.server.payroll.config.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * @author chain
 * create by chain on 2019-01-30 10:31
 **/
public class LocalDateDeserializer extends JsonDeserializer<LocalDate> {
    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p == null || p.getText() == null || p.getText().trim().isEmpty()) {
            return null;
        }
        return Instant.ofEpochMilli(Long.parseLong(p.getText())).atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}
