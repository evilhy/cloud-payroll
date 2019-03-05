package chain.fxgj.server.payroll.config.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.bson.types.ObjectId;

import java.io.IOException;

/**
 * @author chain
 * create by chain on 2018/10/18 下午6:28
 **/
public class ObjectIdDeserializer extends JsonDeserializer<ObjectId> {
    @Override
    public ObjectId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (p == null || p.getText() == null || p.getText().trim().isEmpty()) {
            return null;
        }
        return new ObjectId(p.getText());
    }
}
