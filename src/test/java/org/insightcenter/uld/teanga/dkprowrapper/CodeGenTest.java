package org.insightcenter.uld.teanga.dkprowrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_ADJ;
import org.apache.uima.cas.*;
import org.apache.uima.cas.admin.CASAdminException;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.LowLevelCAS;
import org.apache.uima.cas.impl.LowLevelIndexRepository;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.cas.Sofa;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import java.io.InputStream;
import java.util.Iterator;
import java.util.ListIterator;

public class CodeGenTest {

    @Test
    public void testPOS() throws ResourceInitializationException, CASException, JsonProcessingException {
       /* JCas jcas = JCasFactory.createJCas();
        POS pos = new POS(jcas);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValueAsString(pos);*/
    }
}
