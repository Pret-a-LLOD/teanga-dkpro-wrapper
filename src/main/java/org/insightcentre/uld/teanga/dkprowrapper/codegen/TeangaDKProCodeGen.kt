package org.insightcentre.uld.teanga.dkprowrapper.codegen

import org.apache.uima.fit.descriptor.TypeCapability
import java.util.stream.Collectors
import java.lang.ClassNotFoundException
import kotlin.Throws
import kotlin.jvm.JvmStatic
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.apache.uima.fit.descriptor.ConfigurationParameter
import org.apache.uima.fit.descriptor.ResourceMetaData
import org.apache.uima.jcas.tcas.Annotation
import org.dkpro.core.opennlp.*
import java.io.File
import java.io.PrintWriter
import java.lang.Exception
import java.util.*

object TeangaDKProCodeGen {
    private fun extractServiceDescriptor(clazz: Class<*>): ServiceDescriptor {
        val desc = ServiceDescriptor()
        desc.classShortName = clazz.simpleName
        desc.classFullName = clazz.canonicalName
        for (anno in clazz.annotations) {
            if (anno is ResourceMetaData) {
                desc.name = anno.name
                desc.description = anno.description
                desc.copyright = anno.copyright
                desc.vendor = anno.vendor
                desc.version = anno.version
            } else if (anno is TypeCapability) {
                if (Arrays.equals(anno.inputs, arrayOf(TypeCapability.NO_DEFAULT_VALUE))) {
                    desc.inputs = arrayOf()
                } else {
                    desc.inputs = anno.inputs
                }

                if (Arrays.equals(anno.outputs, arrayOf(TypeCapability.NO_DEFAULT_VALUE))) {
                    desc.outputs = arrayOf()
                } else {
                    desc.outputs = anno.outputs
                }
            }
        }
        for (field in clazz.declaredFields) {
            for (anno in field.annotations) {
                if (anno is ConfigurationParameter) {
                    desc.parameters.add(ServiceDescriptor.Parameter(
                            anno.name,
                            field.type,
                            anno.mandatory,
                            anno.defaultValue,
                            anno.description
                    ))
                }
            }
        }
        return desc
    }

    private fun makeOpenApiDescription(descriptorList: List<ServiceDescriptor>): HashMap<String, Any> {
        val root = HashMap<String, Any>()
        root["openapi"] = "3.0.0"
        root["info"] = HashMap<Any, Any>()
        (root["info"] as HashMap<String, Any?>?)!!["version"] = "1.0"
        (root["info"] as HashMap<String, Any?>?)!!["title"] = "DKPro Teanga Wrapper"
        val paths = HashMap<String, Any>()
        root["paths"] = paths
        val components = HashMap<String, Any>()
        root["components"] = components
        val schemas = HashMap<String, Any>()
        components["schemas"] = schemas
        for (desc in descriptorList) {
            val m1 = HashMap<String, Any>()
            paths["/" + desc.classShortName] = m1
            val m2 = HashMap<String, Any?>()
            m1["post"] = m2
            m2["summary"] = desc.description
            val m3 = HashMap<String, Any>()
            m2["requestBody"] = m3
            val m4 = HashMap<String, Any>()
            m3["content"] = m4
            val m5 = HashMap<String, Any>()
            m4["application/json"] = m5
            val m6 = HashMap<String, Any>()
            m5["schema"] = m6
            m6["\$ref"] = schemaObject(desc.inputs, schemas)
            val m7 = HashMap<String, Any>()
            m2["parameters"] = desc.parameters.stream().map { x: ServiceDescriptor.Parameter -> x.toOpenAPI() }.collect(Collectors.toList())
            val m8 = HashMap<String, Any>()
            m2["responses"] = m8
            val m9 = HashMap<String, Any>()
            m8["200"] = m9
            m9["description"] = "Success"
            val m10 = HashMap<String, Any>()
            m9["content"] = m10
            val m11 = HashMap<String, Any>()
            m10["application/json"] = m11
            val m12 = HashMap<String, Any>()
            m11["schema"] = m12
            m12["\$ref"] = schemaObject(desc.outputs, schemas)
        }
        return root
    }

    private fun casName(inputs: Array<String>?): String {
        if(inputs?.size == 0)
            return "EmptyCas";
        return "Cas" + Arrays.stream(inputs).map { c: String? ->
            try {
                return@map Class.forName(c).simpleName
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
                return@map ""
            }
        }.sorted().collect(Collectors.joining())
    }

    private fun schemaObject(inputs: Array<String>?, schemas: HashMap<String, Any>): Any {
        return if (inputs == null || inputs.size == 0) {
            if (!schemas.containsKey("EmptyCas")) {
                val emptyCas = HashMap<String, Any>()
                emptyCas["type"] = "object"
                emptyCas["required"] = Arrays.asList("documentText", "language")
                val properties = HashMap<String, Any>()
                val documentText = HashMap<String, Any>()
                documentText["type"] = "string"
                properties["documentText"] = documentText
                val language = HashMap<String, Any>()
                language["type"] = "string"
                properties["language"] = language
                emptyCas["properties"] = properties
                schemas["EmptyCas"] = emptyCas
            }
            "#/components/schemas/EmptyCas"
        } else {
            val name = casName(inputs)
            if (!schemas.containsKey(name)) {
                val cas = HashMap<String, Any>()
                cas["type"] = "object"
                cas["required"] = Arrays.asList("documentText", "language")
                val properties = HashMap<String, Any>()
                val documentText = HashMap<String, Any>()
                documentText["type"] = "string"
                properties["documentText"] = documentText
                val language = HashMap<String, Any>()
                language["type"] = "string"
                properties["language"] = language
                cas["properties"] = properties
                for (input in inputs) {
                    val inputMap = HashMap<String, Any>()
                    inputMap["type"] = "array"
                    val items = HashMap<String, Any>()
                    inputMap["items"] = items
                    try {
                        val clazz = Class.forName(input)
                        items["\$ref"] = "#/components/schemas/" + clazz.simpleName
                        schemaAnnoType(clazz, schemas)
                        properties[clazz.simpleName.substring(0, 1).toLowerCase() + clazz.simpleName.substring(1)] = inputMap
                    } catch (e: ClassNotFoundException) {
                        e.printStackTrace()
                    }
                }
                schemas[name] = cas
            }
            "#/components/schemas/$name"
        }
    }

    private fun schemaAnnoType(clazz: Class<*>, schemas: HashMap<String, Any>) {
        if (!schemas.containsKey(clazz.simpleName)) {
            val m = HashMap<String, Any>()
            schemas[clazz.simpleName] = m
            m["type"] = "object"
            m["required"] = Arrays.asList("begin", "end")
            val properties = HashMap<String, Any>()
            val begin = HashMap<String, Any>()
            properties["begin"] = begin
            begin["type"] = "integer"
            val end = HashMap<String, Any>()
            properties["end"] = end
            end["type"] = "integer"
            for (method in clazz.methods) {
                if (method.name.startsWith("get") && method.declaringClass == clazz) {
                    val name = method.name.substring(3, 4).toLowerCase() + method.name.substring(4)
                    properties[name] = typeToOpenAPI(method.returnType, schemas)
                }
            }
            m["properties"] = properties
        }
    }

    @JvmStatic
    fun typeToOpenAPI(clazz: Class<*>, schemas: HashMap<String, Any>?): Any {
        val m = HashMap<String, Any?>()
        if (clazz == String::class.java) {
            m["type"] = "string"
        } else if (clazz == Boolean::class.javaPrimitiveType || clazz == Boolean::class.java) {
            m["type"] = "boolean"
        } else if (clazz == Int::class.javaPrimitiveType || clazz == Int::class.java) {
            m["type"] = "integer"
        } else if (Annotation::class.java.isAssignableFrom(clazz)) {
            m["\$ref"] = "#/components/schemas/" + clazz.simpleName
            if (schemas != null) schemaAnnoType(clazz, schemas) else System.err.println("Could not register " + clazz.simpleName)
        } else {
            System.err.println("Unsupported type: " + clazz.simpleName)
        }
        return m
    }

    fun generateJavaCode(descriptors: List<ServiceDescriptor>) {
        val dkProJava = PrintWriter("generated/DKPro.java")
        dkProJava.use {
            dkProJava.println("package org.insightcentre.uld.teanga.dkprowrapper;\n" +
                    "\n" +
                    "import java.util.Collections;\n" +
                    "import java.util.List;\n" +
                    "import javax.ws.rs.*;\n" +
                    "import javax.ws.rs.core.Response;\n" +
                    "import org.apache.uima.analysis_engine.AnalysisEngineProcessException;\n" +
                    "import org.apache.uima.cas.CASException;\n" +
                    "import org.apache.uima.resource.ResourceInitializationException;\n" +
                    "import org.insightcentre.uld.teanga.dkprowrapper.cas.*;\n" +
                    "\n" +
                    "/**\n" +
                    " * Automatically generated methods for calling DKPro\n" +
                    " * @author John McCrae\n" +
                    " */\n" +
                    "@Path(\"/\")\n" +
                    "public class DKPro {\n");
            for (descriptor in descriptors) {
                dkProJava.print("\n" +
                        "    @POST\n" +
                        "    @Consumes({\"application/json\"})\n" +
                        "    @Produces({\"application/json\"})\n" +
                        "    @Path(\"/${descriptor.classShortName}\")\n" +
                        "    public Response ${descriptor.classShortName}(" +
                        "         ${casName(descriptor.inputs)} body\n");
                for (parameter in descriptor.parameters) {
                    dkProJava.print(",\n            @QueryParam(\"${parameter.name}\") ${parameter.type?.name} ${parameter.name}")
                }
                dkProJava.print(") {\n" +
                        "");
                // TODO: Default values
                dkProJava.print("        try {\n" +
                        "            DKProInstance instance = new DKProInstance(\n" +
                        "                ${descriptor.classFullName}.class\n")
                for (parameter in descriptor.parameters) {
                    dkProJava.print(",\n                \"${parameter.name}\", ${parameter.name}")
                }
                dkProJava.print("            );\n" +
                        "            instance.process${casName(descriptor.inputs)}(body);\n" +
                        "            return Response.ok().entity(${casName(descriptor.outputs)}.fromUIMA(instance.cas)).build();\n" +
                        "        } catch(AnalysisEngineProcessException | CASException | ResourceInitializationException x) {\n" +
                        "            x.printStackTrace();\n" +
                        "            return Response.serverError().entity(x).build();\n" +
                        "        }\n" +
                        "    }")
            }
            dkProJava.print("\n}")
        }

        val dkProInstance = PrintWriter("generated/DKProInstance.java")
        dkProInstance.use {
            dkProInstance.print("package org.insightcentre.uld.teanga.dkprowrapper;\n" +
                    "\n" +
                    "import org.dkpro.core.io.text.StringReader;\n" +
                    "import static java.util.Arrays.asList;\n" +
                    "import org.apache.uima.UIMAFramework;\n" +
                    "import org.apache.uima.analysis_component.AnalysisComponent;\n" +
                    "import org.apache.uima.analysis_engine.AnalysisEngine;\n" +
                    "import org.apache.uima.analysis_engine.AnalysisEngineDescription;\n" +
                    "import org.apache.uima.analysis_engine.AnalysisEngineProcessException;\n" +
                    "import org.apache.uima.cas.CAS;\n" +
                    "import org.apache.uima.cas.CASException;\n" +
                    "import org.apache.uima.collection.CollectionReader;\n" +
                    "import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;\n" +
                    "import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;\n" +
                    "import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;\n" +
                    "import org.apache.uima.fit.internal.ResourceManagerFactory;\n" +
                    "import static org.apache.uima.fit.util.JCasUtil.select;\n" +
                    "import org.apache.uima.resource.ResourceInitializationException;\n" +
                    "import org.apache.uima.resource.ResourceManager;\n" +
                    "import org.apache.uima.util.CasCreationUtils;\n" +
                    "import org.insightcentre.uld.teanga.dkprowrapper.cas.*;\n" +
                    "import org.insightcentre.uld.teanga.dkprowrapper.pojos.*;\n" +
                    "\n" +
                    "/**\n" +
                    " * Automatically generated wrapper for handling Cas objects\n" +
                    " * @author John McCrae\n" +
                    " */\n" +
                    "public class DKProInstance {\n" +
                    "\n" +
                    "    private final AnalysisEngine aae;\n" +
                    "    public final CAS cas;\n" +
                    "\n" +
                    "    public DKProInstance(Class<? extends AnalysisComponent> clazz, Object... params) throws ResourceInitializationException {\n" +
                    "        final ResourceManager resMgr = ResourceManagerFactory.newResourceManager();\n" +
                    "\n" +
                    "        final CollectionReader reader = UIMAFramework.produceCollectionReader(\n" +
                    "                createReaderDescription(StringReader.class,\n" +
                    "                        StringReader.PARAM_DOCUMENT_TEXT, \"this is an example document\",\n" +
                    "                        StringReader.PARAM_LANGUAGE, \"en\",\n" +
                    "                        StringReader.PARAM_COLLECTION_ID, \"collection\",\n" +
                    "                        StringReader.PARAM_DOCUMENT_ID, \"document\",\n" +
                    "                        StringReader.PARAM_DOCUMENT_URI, \"foo:bar\"), resMgr, null);\n" +
                    "\n" +
                    "        // Create AAE\n" +
                    "        final AnalysisEngineDescription aaeDesc = createEngineDescription(\n" +
                    "                createEngineDescription(clazz, params));\n" +
                    "\n" +
                    "        // Instantiate AAE\n" +
                    "        aae = createEngine(aaeDesc);\n" +
                    "\n" +
                    "        // Create CAS from merged metadata\n" +
                    "        cas = CasCreationUtils.createCas(asList(reader.getMetaData(), aae.getMetaData()),\n" +
                    "                null, reader.getResourceManager());\n" +
                    "        reader.typeSystemInit(cas.getTypeSystem());\n" +
                    "    }\n" +
                    "    \n" +
                    "    public void processEmpty(EmptyCas userCas) throws AnalysisEngineProcessException {\n" +
                    "        cas.reset();\n" +
                    "        cas.setDocumentText(userCas.documentText);\n" +
                    "        cas.setDocumentLanguage(userCas.language);\n" +
                    "        \n" +
                    "        aae.process(cas);\n" +
                    "    }\n" +
                    "    \n")

            for (casType in getCasTypes(descriptors)) {
                dkProInstance.print("    public void process${casType.name}(${casType.name} userCas) throws AnalysisEngineProcessException, CASException {\n" +
                        "        cas.reset();\n" +
                        "        cas.setDocumentText(userCas.documentText);\n" +
                        "        cas.setDocumentLanguage(userCas.language);\n")
                if (casType.members != null) {
                    for (clazz in casType.members) {
                        val varName = clazz.simpleName.substring(0, 1).toLowerCase() + clazz.simpleName.substring(1)
                        dkProInstance.print("        if(userCas.${varName} != null) {\n" +
                                "            for(DKPro${clazz.simpleName} s : userCas.${varName}) {\n" +
                                "                cas.addFsToIndexes(s.toDKPro(cas.getJCas()));\n" +
                                "            }\n" +
                                "        }\n")
                    }
                }
                dkProInstance.print("        aae.process(cas);\n" +
                        "    }\n\n")
            }
            dkProInstance.print("}")
        }
        File("generated/cas").mkdirs()
        val casEmptyOut = PrintWriter("generated/cas/EmptyCas.java")
        casEmptyOut.use {
            casEmptyOut.print("package org.insightcentre.uld.teanga.dkprowrapper.cas;\n" +
                    "\n" +
                    "import org.apache.uima.cas.CAS;\n" +
                    "import org.apache.uima.cas.CASException;\n" +
                    "\n" +
                    "/**\n" +
                    " *\n" +
                    " * @author John McCrae\n" +
                    " */\n" +
                    "public class EmptyCas {\n" +
                    "    public String documentText;\n" +
                    "    public String language;\n" +
                    "    \n" +
                    "    \n" +
                    "    public String getDocumentText() {\n" +
                    "        return documentText;\n" +
                    "    }\n" +
                    "\n" +
                    "    public void setDocumentText(String documentText) {\n" +
                    "        this.documentText = documentText;\n" +
                    "    }\n" +
                    "\n" +
                    "    public String getLanguage() {\n" +
                    "        return language;\n" +
                    "    }\n" +
                    "\n" +
                    "    public void setLanguage(String language) {\n" +
                    "        this.language = language;\n" +
                    "    }\n" +
                    "    \n" +
                    "    public static EmptyCas fromUIMA(CAS cas) throws CASException {\n" +
                    "        EmptyCas c = new EmptyCas();\n" +
                    "        c.setDocumentText(cas.getDocumentText());\n" +
                    "        c.setLanguage(cas.getDocumentLanguage());\n" +
                    "        return c;        \n" +
                    "    }\n" +
                    "\n" +
                    "    @Override\n" +
                    "    public String toString() {\n" +
                    "        return \"EmptyCas{\" + \"documentText=\" + documentText + \", language=\" + language + '}';\n" +
                    "    }\n" +
                    "    \n" +
                    "}\n")
        }
        for (casType in getCasTypes(descriptors)) {
            val casOut = PrintWriter("generated/cas/${casType.name}.java")
            casOut.use {
                casOut.print("package org.insightcentre.uld.teanga.dkprowrapper.cas;\n" +
                        "\n" +
                        "import org.apache.uima.cas.CAS;\n" +
                        "import org.apache.uima.cas.CASException;\n" +
                        "import org.insightcentre.uld.teanga.dkprowrapper.pojos.*;\n" +
                        "\n" +
                        "import java.util.List;\n" +
                        "import java.util.stream.Collectors;\n" +
                        "\n" +
                        "import static org.apache.uima.fit.util.JCasUtil.select;\n" +
                        "\n" +
                        "/**\n" +
                        " * Automatically generated cas Object for ${casType.name}\n" +
                        " * @author John McCrae\n" +
                        " */\n" +
                        "public class ${casType.name} extends EmptyCas {\n")
                if (casType.members != null) {
                    for (clazz in casType.members) {
                        val varName = clazz.simpleName.substring(0, 1).toLowerCase() + clazz.simpleName.substring(1)
                        casOut.print(
                                "    public List<DKPro${clazz.simpleName}> ${varName};\n" +
                                        "\n" +
                                        "    public List<DKPro${clazz.simpleName}> get${clazz.simpleName}() {\n" +
                                        "        return ${varName};\n" +
                                        "    }\n" +
                                        "\n" +
                                        "    public void set${clazz.simpleName}(List<DKPro${clazz.simpleName}> ${varName}) {\n" +
                                        "        this.${varName} = ${varName};\n" +
                                        "    }\n" +
                                        "    \n")
                    }
                }
                casOut.print("    public static Cas${casType.name} fromUIMA(CAS cas) throws CASException {\n" +
                        "        Cas${casType.name} c = new Cas${casType.name}();\n" +
                        "        c.setDocumentText(cas.getDocumentText());\n" +
                        "        c.setLanguage(cas.getDocumentLanguage());\n")
                if (casType.members != null) {
                    for (clazz in casType.members) {
                        casOut.print(
                                "        c.set${clazz.simpleName}(\n" +
                                        "                select(cas.getJCas(), ${clazz.canonicalName}.class).stream().map(\n" +
                                        "                        x -> x != null ? DKPro${clazz.simpleName}.fromDKPro(x) : null).\n" +
                                        "                        collect(Collectors.toList()));\n")
                    }
                }
                casOut.print("        return c;\n" +
                        "    }\n" +
                        "}\n")
            }
        }
        File("generated/pojos").mkdirs()
        val dkProAnnotationJava = PrintWriter("generated/pojos/DKProAnnotation.java")
        dkProAnnotationJava.use {
            dkProAnnotationJava.print("package org.insightcentre.uld.teanga.dkprowrapper.pojos;\n" +
                    "\n" +
                    "import org.apache.uima.jcas.tcas.Annotation;\n" +
                    "\n" +
                    "/**\n" +
                    " *\n" +
                    " * @author John McCrae\n" +
                    " */\n" +
                    "public class DKProAnnotation {\n" +
                    "    public int begin, end;\n" +
                    "\n" +
                    "    public int getBegin() {\n" +
                    "        return begin;\n" +
                    "    }\n" +
                    "\n" +
                    "    public void setBegin(int begin) {\n" +
                    "        this.begin = begin;\n" +
                    "    }\n" +
                    "\n" +
                    "    public int getEnd() {\n" +
                    "        return end;\n" +
                    "    }\n" +
                    "\n" +
                    "    public void setEnd(int end) {\n" +
                    "        this.end = end;\n" +
                    "    }\n" +
                    "    \n" +
                    "    protected void annoFromDKPro(Annotation anno) {\n" +
                    "        this.setBegin(anno.getBegin());\n" +
                    "        this.setEnd(anno.getEnd());\n" +
                    "    }\n" +
                    "    \n" +
                    "    protected void annoToDKPro(Annotation anno) {\n" +
                    "        anno.setBegin(getBegin());\n" +
                    "        anno.setEnd(getEnd());\n" +
                    "    }\n" +
                    "\n" +
                    "    @Override\n" +
                    "    public String toString() {\n" +
                    "        return \"DKProAnnotation{\" + \"begin=\" + begin + \", end=\" + end + '}';\n" +
                    "    }\n" +
                    "    \n" +
                    "}\n")
        }
        for(pojo in getPojos(descriptors)) {
            val pojoOut = PrintWriter("generated/pojos/DKPro${pojo.simpleName}.java")
            pojoOut.use {
                pojoOut.print("package org.insightcentre.uld.teanga.dkprowrapper.pojos;\n" +
                        "\n" +
                        "import com.fasterxml.jackson.annotation.JsonInclude;\n" +
                        "import org.apache.uima.jcas.JCas;\n" +
                        "\n" +
                        "/**\n" +
                        " * Automatically generated POJO for ${pojo.simpleName}\n" +
                        " * @author John McCrae\n" +
                        " */\n" +
                        "@JsonInclude(JsonInclude.Include.NON_NULL)\n" +
                        "class DKPro${pojo.simpleName} extends DKProAnnotation {\n")

                for (method in pojo.methods) {
                    if (method.name.startsWith("get") && method.declaringClass == pojo) {
                        val name = method.name.substring(3)
                        pojoOut.print("    private ${method.returnType.canonicalName} ${name};\n" +
                                "\n" +
                                "    public ${method.returnType.canonicalName} get${name}() {\n" +
                                "        return ${name};\n" +
                                "    }\n" +
                                "\n" +
                                "    public void set${name}(${method.returnType.canonicalName} ${name}) {\n" +
                                "        this.${name} = ${name};\n" +
                                "    }\n" +
                                "    \n")
                    }
                }
                pojoOut.print("    public static DKPro${pojo.simpleName} fromDKPro(${pojo.canonicalName} dkproObj) {\n" +
                        "        DKPro${pojo.simpleName} s = new DKPro${pojo.simpleName}();\n" +
                        "        s.annoFromDKPro(dkproObj);\n")
                for (method in pojo.methods) {
                    if (method.name.startsWith("get") && method.declaringClass == pojo) {
                        val name = method.name.substring(3)
                        pojoOut.print("        s.set${name}(dkproObj.get${name}());\n")
                    }
                }
                pojoOut.print("        return s;\n" +
                        "    }\n" +
                        "\n" +
                        "    public Stem toDKPro(JCas cas) {\n" +
                        "        ${pojo.canonicalName} s = new ${pojo.canonicalName}(cas);\n" +
                        "        annoToDKPro(s);\n")

                for (method in pojo.methods) {
                    if (method.name.startsWith("get") && method.declaringClass == pojo) {
                        val name = method.name.substring(3)
                        pojoOut.print("        s.set${name}(${name});\n")
                    }
                }
                pojoOut.print(
                        "        return s;\n" +
                        "    }\n" +
                        "}\n")
            }
        }
    }

    private fun getCasTypes(descriptors: List<ServiceDescriptor>): Collection<CasType> {
        val map = mutableMapOf<String, CasType>()
        for (descriptor in descriptors) {
            val nameIn = casName(descriptor.inputs)
            if (!map.containsKey(nameIn) && nameIn != "EmptyCas") {
                map[nameIn] = CasType(nameIn, descriptor.inputs?.map { x -> Class.forName(x) })
            }
            val nameOut = casName(descriptor.outputs)
            if (!map.containsKey(nameOut) && nameOut != "EmptyCas") {
                map[nameOut] = CasType(nameOut, descriptor.outputs?.map { x -> Class.forName(x) })
            }
        }
        return map.values
    }

    private fun getPojos(descriptors: List<ServiceDescriptor>): Collection<Class<*>> {
        val map = mutableMapOf<String, Class<*>>()
        for (descriptor in descriptors) {
            for (className in descriptor.inputs!! + descriptor.outputs!!) {
                val clazz = Class.forName(className)
                buildPojos(clazz, map)
            }
        }
        return map.values
    }

    private fun buildPojos(clazz: Class<*>, map: MutableMap<String, Class<*>>) {
        if(!map.contains(clazz.canonicalName) && Annotation::class.java.isAssignableFrom(clazz) && clazz.simpleName != "Annotation") {
            map[clazz.canonicalName] = clazz
            for (method in clazz.methods) {
                if (method.name.startsWith("get") && method.declaringClass == clazz) {
                    buildPojos(method.returnType, map)
                }
            }
        }
    }

    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        File("generated").mkdirs()
        val serviceDescriptors = mutableListOf<ServiceDescriptor>()
        serviceDescriptors.add(extractServiceDescriptor(OpenNlpChunker::class.java))
        serviceDescriptors.add(extractServiceDescriptor(OpenNlpLemmatizer::class.java))
        serviceDescriptors.add(extractServiceDescriptor(OpenNlpParser::class.java))
        serviceDescriptors.add(extractServiceDescriptor(OpenNlpPosTagger::class.java))
        serviceDescriptors.add(extractServiceDescriptor(OpenNlpSegmenter::class.java))
        serviceDescriptors.add(extractServiceDescriptor(OpenNlpSnowballStemmer::class.java))
        val m: Any = makeOpenApiDescription(serviceDescriptors)
        val mapper = ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
        val openapiFile = PrintWriter("generated/openapi.json")
        openapiFile.use {
            mapper.writeValue(openapiFile, m)
        }
        generateJavaCode(serviceDescriptors)
    }
}

data class CasType(val name: String, val members: List<Class<*>>?)