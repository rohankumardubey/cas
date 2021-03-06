package org.apereo.cas.util.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Serializable;

/**
 * This is {@link TriStateBoolean}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiredArgsConstructor
public enum TriStateBoolean implements Serializable {
    /**
     * Constant to represent the true state.
     */
    TRUE(Boolean.TRUE),
    /**
     * Constant to represent the false state.
     */
    FALSE(Boolean.FALSE),
    /**
     * Constant to represent the undefined state.
     */
    UNDEFINED(null);

    private static final long serialVersionUID = -145819796564884951L;

    private final Boolean state;

    /**
     * From boolean.
     *
     * @param value the value
     * @return the tri state boolean
     */
    public static TriStateBoolean fromBoolean(final boolean value) {
        return value ? TriStateBoolean.TRUE : TriStateBoolean.FALSE;
    }

    /**
     * Is true.
     *
     * @return the boolean
     */
    public boolean isTrue() {
        return Boolean.TRUE.equals(this.state);
    }

    /**
     * Is false.
     *
     * @return the boolean
     */
    public boolean isFalse() {
        return Boolean.FALSE.equals(this.state);
    }

    /**
     * Is undefined.
     *
     * @return the boolean
     */
    public boolean isUndefined() {
        return this.state == null;
    }

    /**
     * To boolean.
     *
     * @return the boolean
     */
    public Boolean toBoolean() {
        return state;
    }

    public static class Deserializer extends JsonDeserializer<TriStateBoolean> {

        @Override
        public TriStateBoolean deserialize(final JsonParser jsonParser,
                                  final DeserializationContext deserializationContext) throws IOException {
            val value = jsonParser.getText();
            if (StringUtils.equalsIgnoreCase(value, Boolean.TRUE.toString())) {
                return TriStateBoolean.TRUE;
            }
            if (StringUtils.equalsIgnoreCase(value, Boolean.FALSE.toString())) {
                return TriStateBoolean.FALSE;
            }
            return TriStateBoolean.valueOf(value);
        }
    }
}
