package chain.fxgj.server.payroll.config.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bson.types.Decimal128;

import java.io.IOException;

/**
 * @author chain
 * create by chain on 2018/10/18 下午6:27
 **/
public class DecimalSerializer extends JsonSerializer<Decimal128> {
    @Override
    public void serialize(Decimal128 o, JsonGenerator j, SerializerProvider s) throws IOException {
        if (o == null) {
            j.writeNull();
        } else {
            j.writeNumber(o.bigDecimalValue());
        }
    }
}

