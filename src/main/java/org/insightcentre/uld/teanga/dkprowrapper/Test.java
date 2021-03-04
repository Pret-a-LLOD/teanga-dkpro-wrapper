package org.insightcentre.uld.teanga.dkprowrapper;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2006Writer;
import de.tudarmstadt.ukp.dkpro.core.io.text.StringReader;
import de.tudarmstadt.ukp.dkpro.core.languagetool.LanguageToolLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.maltparser.MaltParser;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.internal.ResourceManagerFactory;
import org.apache.uima.resource.ResourceManager;
import org.apache.uima.util.CasCreationUtils;
import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import org.apache.uima.json.JsonCasSerializer;

import static org.apache.uima.fit.util.JCasUtil.select;

public class Test {
    private static AnalysisEngine aae;
    private static CAS cas;
    private static JsonCasSerializer jcs;

    private static void init() throws Exception {
        final ResourceManager resMgr = ResourceManagerFactory.newResourceManager();

        final CollectionReader reader = UIMAFramework.produceCollectionReader( 
            createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, "this is an example document",
                StringReader.PARAM_LANGUAGE, "en",
                StringReader.PARAM_COLLECTION_ID, "collection",
                StringReader.PARAM_DOCUMENT_ID, "document",
                StringReader.PARAM_DOCUMENT_URI, "foo:bar"), resMgr, null);
     
        // Create AAE
        final AnalysisEngineDescription aaeDesc = createEngineDescription(
                createEngineDescription(OpenNlpSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class),
                createEngineDescription(LanguageToolLemmatizer.class),
                createEngineDescription(MaltParser.class));
  
        // Instantiate AAE
        aae = createEngine(aaeDesc);
  
        // Create CAS from merged metadata
        cas = CasCreationUtils.createCas(asList(reader.getMetaData(), aae.getMetaData()), 
                null, reader.getResourceManager());
        reader.typeSystemInit(cas.getTypeSystem());

        jcs = new JsonCasSerializer();
        jcs.setPrettyPrint(true);
    }

    private static void setCas(String documentText) {
        cas.setDocumentLanguage("en");
        cas.setDocumentText(documentText);
    }

    public static void main(String[] args) throws Exception {
        init();
        setCas("This is a harder example");

        aae.process(cas);

        for(Sentence sentence : select(cas.getJCas(), Sentence.class)) {
            System.err.println(sentence);
        }
        for(POS pos : select(cas.getJCas(), POS.class)) {
            System.err.println(pos);
        }
    }
}
