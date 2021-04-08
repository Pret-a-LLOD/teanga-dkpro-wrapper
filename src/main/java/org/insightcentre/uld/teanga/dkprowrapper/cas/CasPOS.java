package org.insightcentre.uld.teanga.dkprowrapper.cas;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.insightcentre.uld.teanga.dkprowrapper.pojos.DKProPOS;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.uima.fit.util.JCasUtil.select;

/**
 *
 * @author John McCrae
 */
public class CasPOS extends EmptyCas {
    public List<DKProPOS> pos;

    public List<DKProPOS> getPos() {
        return pos;
    }

    public void setPos(List<DKProPOS> pos) {
        this.pos = pos;
    }
    
    
    public static CasPOS fromUIMA(CAS cas) throws CASException {
        CasPOS c = new CasPOS();
        c.setDocumentText(cas.getDocumentText());
        c.setLanguage(cas.getDocumentLanguage());
        c.setPos(
                select(cas.getJCas(), POS.class).stream().map(
                        x -> x != null ? DKProPOS.fromDKPro(x) : null).
                        collect(Collectors.toList()));
        return c;        
    }

    @Override
    public String toString() {
        return "CasPOS{" + "pos=" + pos + '}';
    }
    
}
