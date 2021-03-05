package org.insightcentre.uld.teanga.dkprowrapper.pojos;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.jcas.JCas;

/**
 *
 * @author John McCrae
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DKProToken extends DKProAnnotation {

    private String id;
    private DKProLemma lemma;
    private DKProStem stem;
    private DKProPOS pos;
    private DKProMorphologicalFeatures morph;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public DKProLemma getLemma() {
        return lemma;
    }

    public void setLemma(DKProLemma lemma) {
        this.lemma = lemma;
    }

    public DKProStem getStem() {
        return stem;
    }

    public void setStem(DKProStem stem) {
        this.stem = stem;
    }

    public DKProPOS getPos() {
        return pos;
    }

    public void setPos(DKProPOS pos) {
        this.pos = pos;
    }

    public DKProMorphologicalFeatures getMorph() {
        return morph;
    }

    public void setMorph(DKProMorphologicalFeatures morph) {
        this.morph = morph;
    }

    public static DKProToken fromDKPro(Token token) {
        DKProToken t = new DKProToken();
        t.annoFromDKPro(token);
        if (token.getLemma() != null) {
            t.setLemma(DKProLemma.fromDKPro(token.getLemma()));
        }
        if (token.getStem() != null) {
            t.setStem(DKProStem.fromDKPro(token.getStem()));
        }
        if (token.getPos() != null) {
            t.setPos(DKProPOS.fromDKPro(token.getPos()));
        }
        if (token.getMorph() != null) {
            t.setMorph(DKProMorphologicalFeatures.fromDKPro(token.getMorph()));
        }
        t.setId(token.getId());
        return t;
    }

    public Token toDKPro(JCas cas) {
        Token t = new Token(cas);
        annoToDKPro(t);
        if (this.getLemma() != null) {
            t.setLemma(this.getLemma().toDKPro(cas));
        }
        if (this.getStem() != null) {
            t.setStem(this.getStem().toDKPro(cas));
        }
        if (this.getPos() != null) {
            t.setPos(this.getPos().toDKPro(cas));
        }
        if (this.getMorph() != null) {
            t.setMorph(this.getMorph().toDKPro(cas));
        }
        t.setId(this.getId());
        return t;
    }

    @Override
    public String toString() {
        return "DKProToken{" + "id=" + id + ", lemma=" + lemma + ", stem=" + stem + ", pos=" + pos + ", morph=" + morph + '}';
    }
    
}
