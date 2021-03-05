package org.insightcentre.uld.teanga.dkprowrapper.pojos;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;
import org.apache.uima.jcas.JCas;

/**
 *
 * @author John McCrae
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class DKProMorphologicalFeatures extends DKProAnnotation {
    private String gender, number, _case, degree, verbForm, tense, mood, voice,
            definiteness, value, person, aspect, animacy, negative,
            numType, possessive, pronType, reflex;

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCase() {
        return _case;
    }

    public void setCase(String _case) {
        this._case = _case;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getVerbForm() {
        return verbForm;
    }

    public void setVerbForm(String verbForm) {
        this.verbForm = verbForm;
    }

    public String getTense() {
        return tense;
    }

    public void setTense(String tense) {
        this.tense = tense;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public String getDefiniteness() {
        return definiteness;
    }

    public void setDefiniteness(String definiteness) {
        this.definiteness = definiteness;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getAspect() {
        return aspect;
    }

    public void setAspect(String aspect) {
        this.aspect = aspect;
    }

    public String getAnimacy() {
        return animacy;
    }

    public void setAnimacy(String animacy) {
        this.animacy = animacy;
    }

    public String getNegative() {
        return negative;
    }

    public void setNegative(String negative) {
        this.negative = negative;
    }

    public String getNumType() {
        return numType;
    }

    public void setNumType(String numType) {
        this.numType = numType;
    }

    public String getPossessive() {
        return possessive;
    }

    public void setPossessive(String possessive) {
        this.possessive = possessive;
    }

    public String getPronType() {
        return pronType;
    }

    public void setPronType(String pronType) {
        this.pronType = pronType;
    }

    public String getReflex() {
        return reflex;
    }

    public void setReflex(String reflex) {
        this.reflex = reflex;
    }

    public static DKProMorphologicalFeatures fromDKPro(MorphologicalFeatures morph) {
        DKProMorphologicalFeatures m = new DKProMorphologicalFeatures();
        m.annoFromDKPro(morph);
        m.setAnimacy(morph.getAnimacy());
        m.setAspect(morph.getAspect());
        m.setCase(morph.getCase());
        m.setDefiniteness(morph.getDefiniteness());
        m.setDegree(morph.getDegree());
        m.setGender(morph.getGender());
        m.setMood(morph.getMood());
        m.setNegative(morph.getNegative());
        m.setNumType(morph.getNumType());
        m.setNumber(morph.getNumber());
        m.setPerson(morph.getPerson());
        m.setPossessive(morph.getPossessive());
        m.setPronType(morph.getPronType());
        m.setReflex(morph.getReflex());
        m.setTense(morph.getTense());
        m.setValue(morph.getValue());
        m.setVerbForm(morph.getVerbForm());
        m.setVoice(morph.getVoice());
        return m;
    }
    
     public MorphologicalFeatures toDKPro(JCas cas) {
        MorphologicalFeatures m = new MorphologicalFeatures(cas);
        annoToDKPro(m);
        m.setAnimacy(this.getAnimacy());
        m.setAspect(this.getAspect());
        m.setCase(this.getCase());
        m.setDefiniteness(this.getDefiniteness());
        m.setDegree(this.getDegree());
        m.setGender(this.getGender());
        m.setMood(this.getMood());
        m.setNegative(this.getNegative());
        m.setNumType(this.getNumType());
        m.setNumber(this.getNumber());
        m.setPerson(this.getPerson());
        m.setPossessive(this.getPossessive());
        m.setPronType(this.getPronType());
        m.setReflex(this.getReflex());
        m.setTense(this.getTense());
        m.setValue(this.getValue());
        m.setVerbForm(this.getVerbForm());
        m.setVoice(this.getVoice());
        return m;
    }

    @Override
    public String toString() {
        return "DKProMorphologicalFeatures{" + "gender=" + gender + ", number=" + number + ", _case=" + _case + ", degree=" + degree + ", verbForm=" + verbForm + ", tense=" + tense + ", mood=" + mood + ", voice=" + voice + ", definiteness=" + definiteness + ", value=" + value + ", person=" + person + ", aspect=" + aspect + ", animacy=" + animacy + ", negative=" + negative + ", numType=" + numType + ", possessive=" + possessive + ", pronType=" + pronType + ", reflex=" + reflex + '}';
    }

}
