package org.insightcentre.uld.teanga.dkprowrapper.pojos;

import org.apache.uima.jcas.tcas.Annotation;

/**
 *
 * @author John McCrae
 */
public class DKProAnnotation {
    public int begin, end;

    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
    
    protected void annoFromDKPro(Annotation anno) {
        this.setBegin(anno.getBegin());
        this.setEnd(anno.getEnd());
    }
    
    protected void annoToDKPro(Annotation anno) {
        anno.setBegin(getBegin());
        anno.setEnd(getEnd());
    }

    @Override
    public String toString() {
        return "DKProAnnotation{" + "begin=" + begin + ", end=" + end + '}';
    }
    
}
