package org.insightcentre.uld.teanga.dkprowrapper.codegen;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpChunker;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;

import java.lang.annotation.Annotation;

public class TeangaDKProCodeGen {
    private static void codegen(Class<?> clazz) {
        for(Annotation anno : clazz.getAnnotations()) {
            if(anno instanceof ResourceMetaData) {
                System.err.println(((ResourceMetaData) anno).name());
                System.err.println(((ResourceMetaData) anno).copyright());
            } else if(anno instanceof TypeCapability) {
                System.err.println(((TypeCapability) anno).inputs());
            } else {
                System.err.println(anno.annotationType().getName());
            }
        }
    }

    public static void main(String[] args) {
        Class<?> clazz = OpenNlpChunker.class;
        codegen(clazz);
    }
}
