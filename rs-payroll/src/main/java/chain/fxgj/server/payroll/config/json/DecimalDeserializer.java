package chain.fxgj.server.payroll.config.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.bson.types.Decimal128;

import java.io.IOException;

/**
 * @author chain
 * create by chain on 2018/10/18 下午6:28
 **/
public class DecimalDeserializer extends JsonDeserializer<Decimal128> {
    @Override
    public Decimal128 deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p == null || p.getText() == null || p.getText().trim().isEmpty()) {
            return null;
        }
        return Decimal128.parse(p.getText());
    }
}
