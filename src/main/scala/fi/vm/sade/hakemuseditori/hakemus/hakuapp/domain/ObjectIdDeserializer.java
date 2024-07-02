package fi.vm.sade.hakemuseditori.hakemus.hakuapp.domain;

import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;

public class ObjectIdDeserializer extends JsonDeserializer<ObjectId> {

    @Override
    public ObjectId deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        final ObjectCodec codec = jsonParser.getCodec();
        final JsonNode treeNode = codec.readTree(jsonParser);

        if (treeNode.has("time") && treeNode.has("machine") && treeNode.has("inc")) {
            final int time = treeNode.get("time").asInt();
            final int machine = treeNode.get("machine").asInt();
            final int inc = treeNode.get("inc").asInt();

            return ObjectId.createFromLegacyFormat(time, machine, inc);
        }
        return null;
    }
}
