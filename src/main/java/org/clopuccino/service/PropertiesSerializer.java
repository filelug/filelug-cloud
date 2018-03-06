package org.clopuccino.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * <code>PropertiesSerializer</code> extends JsonSerializer to customize serializing class java.util.Properties.
 * The default serializer for java.util.Properties in Jackson Json serializes the java.lang.Long with java.lang.String,
 * which leads to JsonParsingException when connecting to computer from device.
 * <br>
 * The following error message shows if we use the default serializer:<br>
 * [ERROR]03/01/2017 13:25:15 ConnectToComputerServlet: Login failed. error: com.fasterxml.jackson.databind.JsonMappingException: java.lang.Long cannot be cast to java.lang.String (through reference chain: java.util.Properties["computer-id"])
 * com.fasterxml.jackson.databind.JsonMappingException: java.lang.Long cannot be cast to java.lang.String (through reference chain: java.util.Properties["computer-id"])
 * at com.fasterxml.jackson.databind.JsonMappingException.wrapWithPath(JsonMappingException.java:388) ~[jackson-databind-2.8.6.jar:2.8.6]
 * at com.fasterxml.jackson.databind.JsonMappingException.wrapWithPath(JsonMappingException.java:348) ~[jackson-databind-2.8.6.jar:2.8.6]
 * at com.fasterxml.jackson.databind.ser.std.StdSerializer.wrapAndThrow(StdSerializer.java:343) ~[jackson-databind-2.8.6.jar:2.8.6]
 * at com.fasterxml.jackson.databind.ser.std.MapSerializer.serializeFieldsUsing(MapSerializer.java:742) ~[jackson-databind-2.8.6.jar:2.8.6]
 * at com.fasterxml.jackson.databind.ser.std.MapSerializer.serialize(MapSerializer.java:534) ~[jackson-databind-2.8.6.jar:2.8.6]
 * at com.fasterxml.jackson.databind.ser.std.MapSerializer.serialize(MapSerializer.java:30) ~[jackson-databind-2.8.6.jar:2.8.6]
 * at com.fasterxml.jackson.databind.ser.DefaultSerializerProvider.serializeValue(DefaultSerializerProvider.java:292) ~[jackson-databind-2.8.6.jar:2.8.6]
 * at com.fasterxml.jackson.databind.ObjectMapper._configAndWriteValue(ObjectMapper.java:3681) ~[jackson-databind-2.8.6.jar:2.8.6]
 * at com.fasterxml.jackson.databind.ObjectMapper.writeValueAsString(ObjectMapper.java:3057) ~[jackson-databind-2.8.6.jar:2.8.6]
 * at org.clopuccino.device.servlet.ConnectToComputerServlet.doPost(ConnectToComputerServlet.java:272) ~[classes/:na]
 * at javax.servlet.http.HttpServlet.service(HttpServlet.java:707) [servlet-api-3.1.jar:3.1.0]
 * at javax.servlet.http.HttpServlet.service(HttpServlet.java:790) [servlet-api-3.1.jar:3.1.0]
 * :
 * Caused by: java.lang.ClassCastException: java.lang.Long cannot be cast to java.lang.String
 * at com.fasterxml.jackson.databind.ser.std.StringSerializer.serialize(StringSerializer.java:49) ~[jackson-databind-2.8.6.jar:2.8.6]
 * at com.fasterxml.jackson.databind.ser.std.MapSerializer.serializeFieldsUsing(MapSerializer.java:736) ~[jackson-databind-2.8.6.jar:2.8.6]
 * ... 39 common frames omitted
 *
 * @author masonhsieh
 * @version 1.0
 */
public class PropertiesSerializer extends JsonSerializer<Properties> {

    public PropertiesSerializer() {
        super();
    }

    public void addToObjectMapper(ObjectMapper objectMapper) {
        SimpleModule propertiesModule = new SimpleModule("PropertiesModule", new com.fasterxml.jackson.core.Version(1, 0, 0, "", null, null));

        propertiesModule.addSerializer(Properties.class, this);

        objectMapper.registerModule(propertiesModule);
    }

    @Override
    public void serialize(Properties properties, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        Set<Map.Entry<Object, Object>> entrySet = properties.entrySet();

        gen.writeStartObject();

        for (Map.Entry<Object, Object> entry : entrySet) {
            String key = entry.getKey().toString();

            Object value = entry.getValue();

            if (value != null) {
                if (Integer.class.isInstance(value)) {
                    gen.writeNumberField(key, (Integer) value);
                } else if (Long.class.isInstance(value)) {
                    gen.writeNumberField(key, (Long) value);
                } else if (Float.class.isInstance(value)) {
                    gen.writeNumberField(key, (Float) value);
                } else if (Double.class.isInstance(value)) {
                    gen.writeNumberField(key, (Double) value);
                } else if (BigDecimal.class.isInstance(value)) {
                    gen.writeNumberField(key, (BigDecimal) value);
                } else if (Boolean.class.isInstance(value)) {
                    gen.writeBooleanField(key, (Boolean) value);
                } else if (String.class.isInstance(value)) {
                    gen.writeStringField(key, (String) value);
                } else {
                    gen.writeObjectField(key, value);
                }
            }
        }

        gen.writeEndObject();
    }
}
