package org.insightcentre.uld.teanga.dkprowrapper.pojos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import org.apache.uima.jcas.JCas;

/**
 *
 * @author John McCrae
 */
@JsonInclude(Include.NON_NULL)
public class DKProLemma extends DKProAnnotation {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
        
    public static DKProLemma fromDKPro(Lemma lemma) {
        DKProLemma l = new DKProLemma();
        l.annoFromDKPro(lemma);
        l.setValue(lemma.getValue());
        return l;
    }
    
    public Lemma toDKPro(JCas cas) {
        Lemma l = new Lemma(cas);
        annoToDKPro(l);
        l.setValue(value);
        return l;
    }

    @Override
    public String toString() {
        return "DKProLemma{" + "value=" + value + '}';
    }

}
