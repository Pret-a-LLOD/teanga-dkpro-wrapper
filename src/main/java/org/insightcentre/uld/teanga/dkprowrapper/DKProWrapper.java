package org.insightcentre.uld.teanga.dkprowrapper;

import org.glassfish.jersey.server.ResourceConfig;

/**
 *
 * @author John McCrae
 */
public class DKProWrapper extends ResourceConfig {

    public DKProWrapper() {
        packages("org.insightcentre.uld.teanga.dkprowrapper");
        register(DKPro.class);
    }

    

    
}
