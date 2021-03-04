package org.insightcentre.uld.teanga.dkprowrapper;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 *
 * @author John McCrae
 */
@Path("/")
public class TreeTagger {

    @POST

    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response rootPost(@QueryParam("POSMappingLocation") String poSMappingLocation,
             String body,
             @QueryParam("executablePath") String executablePath,
             @QueryParam("language") String language,
             @QueryParam("mappingEnabled") Boolean mappingEnabled,
             @QueryParam("modelArtifactUri") String modelArtifactUri,
             @QueryParam("modelEncoding") String modelEncoding,
             @QueryParam("modelLocation") String modelLocation,
             @QueryParam("modelVariant") String modelVariant,
             @DefaultValue("false") @QueryParam("performanceMode") Boolean performanceMode,
             @DefaultValue("false") @QueryParam("printTagSet") Boolean printTagSet,
             @DefaultValue("true") @QueryParam("writeLemma") Boolean writeLemma,
             @DefaultValue("true") @QueryParam("writePOS") Boolean writePOS)
            throws NotFoundException {
        System.err.println(executablePath);
        return null;
    }
}
