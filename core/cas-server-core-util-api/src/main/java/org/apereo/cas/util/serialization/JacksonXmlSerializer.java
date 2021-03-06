package org.apereo.cas.util.serialization;

import org.apereo.cas.util.function.FunctionUtils;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.Getter;

/**
 * This is {@link JacksonXmlSerializer}.
 * Serializer that attempts to marshal complex object
 * types into xml, used primarily for attribute values
 * that represent POJOs, etc.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
public class JacksonXmlSerializer {
    private final XmlMapper mapper;

    public JacksonXmlSerializer() {
        this.mapper = initializeAndConfigure();
    }

    /**
     * Configure xml mapper.
     *
     * @return the xml mapper
     */
    protected XmlMapper initializeAndConfigure() {
        return new XmlMapper().setDefaultUseWrapper(true);
    }

    /**
     * Write value as string.
     *
     * @param value the value
     * @return the string
     */
    public String writeValueAsString(final Object value) {
        return FunctionUtils.doUnchecked(() -> mapper.writeValueAsString(value));
    }
}
