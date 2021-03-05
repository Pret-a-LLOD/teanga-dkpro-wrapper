package org.insightcentre.uld.teanga.dkprowrapper.cas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import static org.apache.uima.fit.util.JCasUtil.select;
import org.insightcentre.uld.teanga.dkprowrapper.pojos.DKProSentence;
import org.insightcentre.uld.teanga.dkprowrapper.pojos.DKProToken;

/**
 *
 * @author John McCrae
 */
public class CasSentenceToken extends EmptyCas {
    public List<DKProToken> token;
    public List<DKProSentence> sentence;

    public List<DKProToken> getToken() {
        return token;
    }

    public void setToken(List<DKProToken> token) {
        this.token = token;
    }

    public List<DKProSentence> getSentence() {
        return sentence;
    }

    public void setSentence(List<DKProSentence> sentence) {
        this.sentence = sentence;
    }
        
    public static CasSentenceToken fromUIMA(CAS cas) throws CASException {
        CasSentenceToken c = new CasSentenceToken();
        c.setDocumentText(cas.getDocumentText());
        c.setLanguage(cas.getDocumentLanguage());
        c.setSentence(
                select(cas.getJCas(), Sentence.class).stream().map(
                        x -> x != null ? DKProSentence.fromDKPro(x) : null).
                        collect(Collectors.toList()));
        c.setToken(select(cas.getJCas(), Token.class).stream().map(
                        x -> x != null ? DKProToken.fromDKPro(x) : null).
                        collect(Collectors.toList()));
        return c;        
    }

    @Override
    public String toString() {
        return "CasSentenceToken{" + "token=" + token + ", sentence=" + sentence + '}';
    }
    
    
}
