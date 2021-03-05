package org.insightcentre.uld.teanga.dkprowrapper.pojos;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem;
import org.apache.uima.jcas.JCas;

/**
 *
 * @author John McCrae
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class DKProStem extends DKProAnnotation {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    

    public static DKProStem fromDKPro(Stem stem) {
        DKProStem s = new DKProStem();
        s.annoFromDKPro(stem);
        s.setValue(stem.getValue());
        return s;
    }

    public Stem toDKPro(JCas cas) {
        Stem s = new Stem(cas);
        annoToDKPro(s);
        s.setValue(value);
        return s;
    }

    @Override
    public String toString() {
        return "DKProStem{" + "value=" + value + '}';
    }
    
}
