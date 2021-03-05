package org.insightcentre.uld.teanga.dkprowrapper.cas;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;

/**
 *
 * @author John McCrae
 */
public class EmptyCas {
    public String documentText;
    public String language;
    
    
    public String getDocumentText() {
        return documentText;
    }

    public void setDocumentText(String documentText) {
        this.documentText = documentText;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
    
    public static EmptyCas fromUIMA(CAS cas) throws CASException {
        EmptyCas c = new EmptyCas();
        c.setDocumentText(cas.getDocumentText());
        c.setLanguage(cas.getDocumentLanguage());
        return c;        
    }

    @Override
    public String toString() {
        return "EmptyCas{" + "documentText=" + documentText + ", language=" + language + '}';
    }
    
}
