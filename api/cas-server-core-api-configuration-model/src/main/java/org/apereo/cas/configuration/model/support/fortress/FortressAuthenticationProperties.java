package org.apereo.cas.configuration.model.support.fortress;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link FortressAuthenticationProperties}.
 *
 * @author Yudhi Karunia Surtan
 * @since 5.2.0
 * @deprecated since 6.6 and scheduled for removal.
 */
@RequiresModule(name = "cas-server-support-fortress")
@Getter
@Setter
@Accessors(chain = true)
@Deprecated(since = "6.6")
public class FortressAuthenticationProperties implements Serializable {

    private static final long serialVersionUID = 9068259944327425315L;

    /**
     * Use this setting to set the tenant id onto function call into Fortress which allows segregation of data by customer.
     * The context is used for multi-tenancy to isolate data sets within a particular sub-tree within DIT.
     * Setting contextId into this object will render this class' implementer thread unsafe.
     */
    @RequiredProperty
    private String rbaccontext = "HOME";
}
