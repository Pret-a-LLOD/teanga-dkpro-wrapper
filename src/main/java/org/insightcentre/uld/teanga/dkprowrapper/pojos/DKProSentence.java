package org.insightcentre.uld.teanga.dkprowrapper.pojos;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import org.apache.uima.jcas.JCas;

/**
 *
 * @author John McCrae
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DKProSentence extends DKProAnnotation {
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    

    public static DKProSentence fromDKPro(Sentence sentence) {
        DKProSentence s = new DKProSentence();
        s.annoFromDKPro(sentence);
        s.setId(sentence.getId());
        return s;
    }
    
    public Sentence toDKPro(JCas cas) {
        Sentence s = new Sentence(cas);
        this.annoToDKPro(s);
        s.setId(id);
        return s;
    }

    @Override
    public String toString() {
        return "DKProSentence{" + "id=" + id + '}';
    }
    
}
