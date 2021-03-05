package org.insightcentre.uld.teanga.dkprowrapper;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.io.text.StringReader;
import static java.util.Arrays.asList;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionReader;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import org.apache.uima.fit.internal.ResourceManagerFactory;
import static org.apache.uima.fit.util.JCasUtil.select;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceManager;
import org.apache.uima.util.CasCreationUtils;
import org.insightcentre.uld.teanga.dkprowrapper.cas.CasSentenceToken;
import org.insightcentre.uld.teanga.dkprowrapper.cas.EmptyCas;
import org.insightcentre.uld.teanga.dkprowrapper.pojos.DKProSentence;
import org.insightcentre.uld.teanga.dkprowrapper.pojos.DKProToken;

/**
 *
 * @author John McCrae
 */
public class DKProInstance {

    private final AnalysisEngine aae;
    public final CAS cas;

    public DKProInstance(Class<? extends AnalysisComponent> clazz, Object... params) throws ResourceInitializationException {
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
                createEngineDescription(clazz, params));

        // Instantiate AAE
        aae = createEngine(aaeDesc);

        // Create CAS from merged metadata
        cas = CasCreationUtils.createCas(asList(reader.getMetaData(), aae.getMetaData()),
                null, reader.getResourceManager());
        reader.typeSystemInit(cas.getTypeSystem());
    }
    
    public void processEmpty(EmptyCas userCas) throws AnalysisEngineProcessException {
        cas.reset();
        cas.setDocumentText(userCas.documentText);
        cas.setDocumentLanguage(userCas.language);
        
        aae.process(cas);
    }
    
    public void processSentenceToken(CasSentenceToken userCas) throws AnalysisEngineProcessException, CASException {
        cas.reset();
        cas.setDocumentText(userCas.documentText);
        cas.setDocumentLanguage(userCas.language);
        if(userCas.sentence != null) {
            for(DKProSentence s : userCas.sentence) {
                cas.addFsToIndexes(s.toDKPro(cas.getJCas()));
            }
        }
        if(userCas.token != null) {
            for(DKProToken t : userCas.token) {
                cas.addFsToIndexes(t.toDKPro(cas.getJCas()));
            }
        }
        for(Sentence s : select(cas.getJCas(), Sentence.class)) {
            System.err.println(s);
        }
        System.err.println("#sentences " + select(cas.getJCas(), Sentence.class).size());
        aae.process(cas);
    }
}
