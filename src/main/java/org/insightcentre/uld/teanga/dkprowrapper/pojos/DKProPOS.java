package org.insightcentre.uld.teanga.dkprowrapper.pojos;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import org.apache.uima.jcas.JCas;

/**
 *
 * @author John McCrae
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DKProPOS extends DKProAnnotation {
    private String PosValue;

    public String getPosValue() {
        return PosValue;
    }

    public void setPosValue(String PosValue) {
        this.PosValue = PosValue;
    }    

    public static DKProPOS fromDKPro(POS pos) {
        DKProPOS p = new DKProPOS();
        p.annoFromDKPro(pos);
        p.setPosValue(pos.getPosValue());
        return p;
    }

    public POS toDKPro(JCas cas) {
        POS p = new POS(cas);
        annoToDKPro(p);
        p.setPosValue(PosValue);
        return p;
    }

    @Override
    public String toString() {
        return "DKProPOS{" + "PosValue=" + PosValue + '}';
    }
    
}
