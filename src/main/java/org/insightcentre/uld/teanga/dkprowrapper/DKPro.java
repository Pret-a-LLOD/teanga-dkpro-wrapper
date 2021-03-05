package org.insightcentre.uld.teanga.dkprowrapper;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.resource.ResourceInitializationException;
import org.insightcentre.uld.teanga.dkprowrapper.cas.CasPOS;
import org.insightcentre.uld.teanga.dkprowrapper.cas.CasSentenceToken;
import org.insightcentre.uld.teanga.dkprowrapper.cas.EmptyCas;

/**
 *
 * @author John McCrae
 */
@Path("/")
public class DKPro {

    @POST
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @Path("/opennlp_segmenter")
    public Response openNLPSegmenter(EmptyCas body,
            @QueryParam("language") String language,
            @QueryParam("modelVariant") String modelVariant,
            @QueryParam("segmentationModelLocation") String segmentationModelLocation,
            @QueryParam("strictZoning") @DefaultValue("false") boolean strictZoning,
            @QueryParam("tokenizationModelLocation") String tokenizationModelLocation,
            @QueryParam("writeSentence") @DefaultValue("true") boolean writeSentence,
            @QueryParam("writeToken") @DefaultValue("true") boolean writeToken,
            @QueryParam("zoneTypes") List<String> zoneTypes) {
        if(zoneTypes == null)
            zoneTypes = Collections.singletonList("de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Div");
        try {
            DKProInstance instance = new DKProInstance(
                OpenNlpSegmenter.class,
                OpenNlpSegmenter.PARAM_LANGUAGE, language,
                OpenNlpSegmenter.PARAM_VARIANT, modelVariant,
                OpenNlpSegmenter.PARAM_SEGMENTATION_MODEL_LOCATION, segmentationModelLocation,
                OpenNlpSegmenter.PARAM_STRICT_ZONING, strictZoning,
                OpenNlpSegmenter.PARAM_TOKENIZATION_MODEL_LOCATION, tokenizationModelLocation,
                OpenNlpSegmenter.PARAM_WRITE_SENTENCE, writeSentence,
                OpenNlpSegmenter.PARAM_WRITE_TOKEN, writeToken,
                OpenNlpSegmenter.PARAM_ZONE_TYPES, zoneTypes
            );
            instance.processEmpty(body);
            return Response.ok().entity(CasSentenceToken.fromUIMA(instance.cas)).build();
        } catch(AnalysisEngineProcessException | CASException | ResourceInitializationException x) {
            x.printStackTrace();
            return Response.serverError().entity(x).build();
        }
    }
    
    @POST
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @Path("/opennlp_tagger")
    public Response openNLPTagger(
             CasSentenceToken body,
             @QueryParam("internTags") @DefaultValue("true") boolean internTags,
             @QueryParam("language") String language,
             @QueryParam("modelLocation") String modelLocation,
             @QueryParam("posMappingLocation") String posMappingLocation,
             @QueryParam("printTagset") @DefaultValue("false") boolean printTagset,
             @QueryParam("modelVariant") String variant)
            throws NotFoundException {
        
        try {
            DKProInstance instance = new DKProInstance(
                    OpenNlpPosTagger.class,
                    OpenNlpPosTagger.PARAM_INTERN_TAGS, internTags,
                    OpenNlpPosTagger.PARAM_LANGUAGE, language,
                    OpenNlpPosTagger.PARAM_MODEL_LOCATION, modelLocation,
                    OpenNlpPosTagger.PARAM_POS_MAPPING_LOCATION, posMappingLocation,
                    OpenNlpPosTagger.PARAM_PRINT_TAGSET, printTagset,
                    OpenNlpPosTagger.PARAM_VARIANT, variant);
        
            System.err.println(body);
            instance.processSentenceToken(body);
            return Response.ok().entity(CasPOS.fromUIMA(instance.cas)).build();
        } catch(AnalysisEngineProcessException | CASException | ResourceInitializationException x) {
            x.printStackTrace();
            return Response.serverError().entity(x).build();
        }
    }
}
