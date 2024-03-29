package org.insightcentre.uld.teanga.dkprowrapper.codegen

import org.apache.uima.fit.descriptor.TypeCapability
import java.util.stream.Collectors
import java.lang.ClassNotFoundException
import kotlin.Throws
import kotlin.jvm.JvmStatic
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.apache.uima.fit.descriptor.ConfigurationParameter
import org.apache.uima.fit.descriptor.ResourceMetaData
import org.apache.uima.jcas.cas.*
import org.apache.uima.jcas.cas.DoubleArray
import org.apache.uima.jcas.cas.FloatArray
import org.apache.uima.jcas.tcas.Annotation
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.PrintWriter
import java.lang.Exception
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
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
                if (anno.inputs.contentEquals(arrayOf(TypeCapability.NO_DEFAULT_VALUE))) {
                    desc.inputs = arrayOf()
                } else {
                    desc.inputs = anno.inputs
                }

                if (anno.outputs.contentEquals(arrayOf(TypeCapability.NO_DEFAULT_VALUE))) {
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
                            field.genericType,
                            anno.mandatory,
                            anno.defaultValue,
                            anno.description
                    ))
                }
            }
        }
        return desc
    }

    private fun makeOpenApiDescription(jar : String, descriptorList: List<ServiceDescriptor>): HashMap<String, Any> {
        val root = HashMap<String, Any>()
        root["openapi"] = "3.0.0"
        root["info"] = HashMap<Any, Any>()
        (root["info"] as HashMap<String, Any?>?)!!["version"] = "1.0"
        (root["info"] as HashMap<String, Any?>?)!!["title"] = "DKPro Teanga Wrapper for $jar"
        var infoString = StringBuilder("DKPro Teanga Wrapper for $jar consists of the following services\n\n")
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
            infoString.append("* ").append(desc.description?.replace("\n","  \n"))
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
        (root["info"] as HashMap<String, Any?>?)!!["description"] = infoString.toString()
        return root
    }

    private fun casName(inputs: Array<String>?): String {
        if(inputs?.size == 0 || inputs == null)
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
        return if (inputs == null || inputs.isEmpty()) {
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
            for (method in getterMethods(clazz)) {
                val name = method.name.substring(3, 4).toLowerCase() + method.name.substring(4)
                properties[name] = typeToOpenAPI(method.returnType, null, schemas)
            }
            m["properties"] = properties
        }
    }

    @JvmStatic
    fun typeToOpenAPI(clazz: Class<*>, type: Type?, schemas: HashMap<String, Any>?): Any {
        val m = HashMap<String, Any?>()
        if (clazz == String::class.java) {
            m["type"] = "string"
        } else if (clazz == Boolean::class.javaPrimitiveType || clazz == Boolean::class.java) {
            m["type"] = "boolean"
        } else if (clazz == Int::class.javaPrimitiveType || clazz == java.lang.Integer::class.java) {
            m["type"] = "integer"
        } else if (clazz == Long::class.javaPrimitiveType || clazz == java.lang.Long::class.java) {
            m["type"] = "integer"
            m["format"] = "int64"
        } else if (clazz == Float::class.javaPrimitiveType || clazz == Float::class.java) {
            m["type"] = "number"
            m["format"] = "float"
        } else if (clazz == Double::class.javaPrimitiveType || clazz == Double::class.java) {
            m["type"] = "number"
            m["format"] = "double"
        } else if (clazz == Array<String>::class.java) {
            m["type"] = "array"
            m["items"] = mapOf("type" to "string")
        } else if (clazz == FloatArray::class.java || clazz == DoubleArray::class.java || clazz == IntegerArray::class.java  ||
                clazz == java.util.regex.Pattern::class.java || clazz == FSArray::class.java) { // Custom converter
            m["type"] = "string"
        } else if (clazz == java.io.File::class.java) {
            m["type"] = "string"
        } else if (java.util.Collection::class.java.isAssignableFrom(clazz)) {
            if(type is ParameterizedType) {
                val t = type.actualTypeArguments[0]
                m["type"] = "array"
                val map =  HashMap<String, Any>()
                m["items"] = map
                if(t is Class<*>) {
                    typeToOpenAPI(t, null, map)
                } else {
                    System.err.println("Cannot cast Type to Class??")
                }
            } else {
                System.err.println(type)
                System.err.println("Could not infer generic type for " + clazz.simpleName)
            }

        } else if (Annotation::class.java.isAssignableFrom(clazz)) {
            m["\$ref"] = "#/components/schemas/" + clazz.simpleName
            if (schemas != null) schemaAnnoType(
                clazz,
                schemas
            ) else System.err.println("Could not register " + clazz.simpleName)
        } else if (clazz.enumConstants != null) {
            m["type"] = "string"
            m["enum"] = clazz.enumConstants
        } else {
            System.err.println("Unsupported type: " + clazz.name)
        }
        return m
    }

    private fun clazzToJavaName(clazz : Class<*>?): String? {
        return if (clazz == FloatArray::class.java || clazz == DoubleArray::class.java || clazz == IntegerArray::class.java  ||
            clazz == java.util.regex.Pattern::class.java || clazz == FSArray::class.java) { // Custom converter
            "String"
        } else if(clazz != null && clazz.isArray) {
            "String"
        } else {
            clazz?.name?.replace("$",".")
        }
    }

    fun generateJavaCode(descriptors: List<ServiceDescriptor>, jar: String) {
        File("dockers/$jar/src/main/java/org/insightcentre/uld/teanga/dkprowrapper").mkdirs()
        val dkProJava = PrintWriter("dockers/$jar/src/main/java/org/insightcentre/uld/teanga/dkprowrapper/DKPro.java")
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
                    dkProJava.print(",\n            @QueryParam(\"${parameter.name}\") ${clazzToJavaName(parameter.type)} _${parameter.name}")
                }
                dkProJava.print(") {\n" +
                        "");
                for (parameter in descriptor.parameters) {
                    generateConversion(dkProJava, parameter)
                }
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
                        "        } catch(AnalysisEngineProcessException | CASException | ResourceInitializationException exc) {\n" +
                        "            exc.printStackTrace();\n" +
                        "            return Response.serverError().entity(exc).build();\n" +
                        "        }\n" +
                        "    }")
            }
            dkProJava.print("\n}")
        }

        val dkProInstance = PrintWriter("dockers/$jar/src/main/java/org/insightcentre/uld/teanga/dkprowrapper/DKProInstance.java")
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
                    "    public void processEmptyCas(EmptyCas userCas) throws AnalysisEngineProcessException {\n" +
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
        File("dockers/$jar/src/main/java/org/insightcentre/uld/teanga/dkprowrapper/cas").mkdirs()
        val casEmptyOut = PrintWriter("dockers/$jar/src/main/java/org/insightcentre/uld/teanga/dkprowrapper/cas/EmptyCas.java")
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
            val casOut = PrintWriter("dockers/$jar/src/main/java/org/insightcentre/uld/teanga/dkprowrapper/cas/${casType.name}.java")
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
                casOut.print("    public static ${casType.name} fromUIMA(CAS cas) throws CASException {\n" +
                        "        ${casType.name} c = new ${casType.name}();\n" +
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
        File("dockers/$jar/src/main/java/org/insightcentre/uld/teanga/dkprowrapper/pojos").mkdirs()
        val dkProAnnotationJava = PrintWriter("dockers/$jar/src/main/java/org/insightcentre/uld/teanga/dkprowrapper/pojos/DKProAnnotation.java")
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
            val pojoOut = PrintWriter("dockers/$jar/src/main/java/org/insightcentre/uld/teanga/dkprowrapper/pojos/DKPro${pojo.simpleName}.java")
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
                        "public class DKPro${pojo.simpleName}")
                if(Annotation::class.java.isAssignableFrom(pojo)) {
                    pojoOut.print(" extends DKProAnnotation")
                }
                pojoOut.print(" {\n")
                for (method in getterMethods(pojo)) {
                        val name = method.name.substring(3)
                        if(Annotation::class.java.isAssignableFrom(method.returnType)) {
                            pojoOut.print(
                                "    private DKPro${method.returnType.simpleName} ${name};\n" +
                                        "\n" +
                                        "    public DKPro${method.returnType.simpleName} get${name}() {\n" +
                                        "        return ${name};\n" +
                                        "    }\n" +
                                        "\n" +
                                        "    public void set${name}(DKPro${method.returnType.simpleName} ${name}) {\n" +
                                        "        this.${name} = ${name};\n" +
                                        "    }\n" +
                                        "    \n"
                            )

                        } else {
                            pojoOut.print(
                                "    private ${method.returnType.canonicalName} ${name};\n" +
                                        "\n" +
                                        "    public ${method.returnType.canonicalName} get${name}() {\n" +
                                        "        return ${name};\n" +
                                        "    }\n" +
                                        "\n" +
                                        "    public void set${name}(${method.returnType.canonicalName} ${name}) {\n" +
                                        "        this.${name} = ${name};\n" +
                                        "    }\n" +
                                        "    \n"
                            )
                        }
                }
                pojoOut.print("    public static DKPro${pojo.simpleName} fromDKPro(${pojo.canonicalName} dkproObj) {\n" +
                        "        DKPro${pojo.simpleName} s = new DKPro${pojo.simpleName}();\n");
                if(Annotation::class.java.isAssignableFrom(pojo)) {
                    pojoOut.print("        s.annoFromDKPro(dkproObj);\n")
                }
                for (method in getterMethods(pojo)) {
                        val name = method.name.substring(3)
                        if(Annotation::class.java.isAssignableFrom(method.returnType)) {
                            pojoOut.print("        s.set${name}(DKPro${method.returnType.simpleName}.fromDKPro(dkproObj.get${name}()));\n")
                        } else {
                            pojoOut.print("        s.set${name}(dkproObj.get${name}());\n")
                        }
                }
                pojoOut.print("        return s;\n" +
                        "    }\n" +
                        "\n" +
                        "    public ${pojo.canonicalName} toDKPro(JCas cas) {\n" +
                        "        ${pojo.canonicalName} s = new ${pojo.canonicalName}(cas);\n")
                if(Annotation::class.java.isAssignableFrom(pojo)) {
                    pojoOut.print("        annoToDKPro(s);\n")
                }

                for (method in getterMethods(pojo)) {
                        val name = method.name.substring(3)
                    pojoOut.println("        if(${name} != null) {")
                    if(Annotation::class.java.isAssignableFrom(method.returnType)) {
                        pojoOut.print("          s.set${name}(${name}.toDKPro(cas));\n")

                    } else {
                        pojoOut.print("          s.set${name}(${name});\n")
                    }
                    pojoOut.println("        }");
                }
                pojoOut.print(
                        "        return s;\n" +
                        "    }\n" +
                        "}\n")
            }
        }
    }

    private fun generateConversion(dkProJava: PrintWriter, parameter: ServiceDescriptor.Parameter) {
        if(parameter.type == java.util.regex.Pattern::class.java) {
            dkProJava.println("java.util.regex.Pattern ${parameter.name} = java.util.regex.Pattern.compile(_${parameter.name});")
//        } else if (parameter.type == FloatArray::class.java) {
//            dkProJava.println("float[] __${parameter.name} = new com.fasterxml.jackson.databind.ObjectMapper().readValue(_${parameter.name}, float[].class);")
//            dkProJava.println("JCas _jcas_${parameter.name} = org.apache.uima.util.CasCreationUtils.createCas((TypeSystemDescription) null, null, null).getJCas();")
//            dkProJava.println("org.apache.uima.jcas.cas.FloatArray ${parameter.name} = org.apache.uima.jcas.cas.FloatArray.create(_jcas_${parameter.name}, __${parameter.name});")
//        } else if (parameter.type == DoubleArray::class.java) {
//        } else if (parameter.type == IntegerArray::class.java) {
//        } else if (parameter.type == FSArray::class.java) {
        } else if(parameter.type?.isArray == true) {
            dkProJava.println("${parameter.type?.componentType?.name}[] ${parameter.name} = null;\n" +
                    "try {\n" +
                    "  ${parameter.name} = " +
                    "new com.fasterxml.jackson.databind.ObjectMapper().readValue(_${parameter.name}, " +
                    "${parameter.type?.componentType?.name}[].class);\n" +
                    "} catch(java.io.IOException x) { x.printStackTrace(); }")
        } else {
            dkProJava.println("${clazzToJavaName(parameter.type)} ${parameter.name} = _${parameter.name};")
        }
    }

    private fun getCasTypes(descriptors: List<ServiceDescriptor>): Collection<CasType> {
        val map = mutableMapOf<String, CasType>()
        for (descriptor in descriptors) {
            val nameIn = casName(descriptor.inputs)
            if (!map.containsKey(nameIn) && nameIn != "EmptyCas") {
                map[nameIn] = CasType(nameIn, descriptor.inputs?.flatMap { x ->
                    try {
                        listOf(Class.forName(x))
                    } catch(x: ClassNotFoundException) {
                        listOf()
                    }
                })
            }
            val nameOut = casName(descriptor.outputs)
            if (!map.containsKey(nameOut) && nameOut != "EmptyCas") {
                map[nameOut] = CasType(nameOut, descriptor.outputs?.flatMap { x ->
                    try {
                        listOf(Class.forName(x))
                    } catch(x: ClassNotFoundException) {
                        listOf()
                    }
                })
            }
        }
        return map.values
    }

    private fun getPojos(descriptors: List<ServiceDescriptor>): Collection<Class<*>> {
        val map = mutableMapOf<String, Class<*>>()
        for (descriptor in descriptors) {
            for (className in (descriptor.inputs?: arrayOf<String>()) + (descriptor.outputs?: arrayOf<String>())) {
                try {
                    val clazz = Class.forName(className)
                    buildPojos(clazz, map)
                } catch(e: ClassNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
        return map.values
    }

    private fun buildPojos(clazz: Class<*>, map: MutableMap<String, Class<*>>) {
        if(!map.contains(clazz.canonicalName) && AnnotationBase::class.java.isAssignableFrom(clazz) && clazz.simpleName != "Annotation") {
            map[clazz.canonicalName] = clazz
            for (method in getterMethods(clazz)) {
                buildPojos(method.returnType, map)
            }
        }
    }

    private fun getterMethods(clazz: Class<*>): List<Method> {
        return clazz.methods.filter { method -> method.name.startsWith("get") && method.declaringClass == clazz &&
                clazz.methods.any { m2 -> m2.name == method.name.replace("get","set")} &&
                method.parameters.isEmpty() &&
                method.returnType != Annotation::class.java
        }
    }

    private fun generatePOM(jar : String) {
        val pom = PrintWriter("dockers/$jar/pom.xml")
        pom.use {
            pom.print("""<?xml version="1.0" encoding="UTF-8"?>

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.insightcentre</groupId>
    <artifactId>teanga-dkpro-wrapper-$jar</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>

    <name>Teanga DKPro Wrapper for $jar</name>
    <url>http://github.com/pret-a-llod/teanga-dkpro-wrapper</url>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <jetty.version>9.4.38.v20210224</jetty.version>
        <jersey.version>2.28</jersey.version>
        <jackson.version>2.9.8</jackson.version>
        <dkpro.version>2.2.0</dkpro.version>
        <kotlin.version>1.4.32</kotlin.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.11</version>
        </dependency>
		<dependency>
			<groupId>javax.activation</groupId>
			<artifactId>activation</artifactId>
			<version>1.1.1</version>
		</dependency>
		<dependency>
			<groupId>javax.xml.bind</groupId>
			<artifactId>jaxb-api</artifactId>
			<version>2.3.0</version>
		</dependency>
		<dependency>
			<groupId>com.sun.xml.bind</groupId>
			<artifactId>jaxb-core</artifactId>
			<version>2.3.0</version>
		</dependency>
		<dependency>
		   <groupId>com.sun.xml.bind</groupId>
		   <artifactId>jaxb-impl</artifactId>
		   <version>2.3.0</version>
		</dependency>
        <dependency>
            <groupId>org.dkpro.core</groupId>
            <artifactId>$jar</artifactId>
	        <version>${'$'}{dkpro.version}</version>
        </dependency>     
        <dependency>
            <groupId>org.dkpro.core</groupId>
            <artifactId>dkpro-core-io-text-asl</artifactId>
	        <version>${'$'}{dkpro.version}</version>
        </dependency>""")
        if(jar.contains("dictionaryannotator")) {
            pom.print("""               
        <dependency>
            <groupId>org.dkpro.core</groupId>
            <artifactId>dkpro-core-api-ner-asl</artifactId>
            <version>${'$'}{dkpro.version}</version>
        </dependency>""")
        }
        pom.print("""
        <dependency>
            <groupId>org.glassfish.jersey.containers</groupId>
            <artifactId>jersey-container-servlet</artifactId>
            <version>${'$'}{jersey.version}</version>
        </dependency>

        <dependency>
            <groupId>org.glassfish.jersey.core</groupId>
            <artifactId>jersey-server</artifactId>
            <version>${'$'}{jersey.version}</version>
        </dependency>
        <dependency>
            <groupId>org.glassfish.jersey.media</groupId>
            <artifactId>jersey-media-json-jackson</artifactId>
            <version>${'$'}{jersey.version}</version>
        </dependency>
        <dependency>
            <groupId>org.glassfish.jersey.inject</groupId>
            <artifactId>jersey-hk2</artifactId>
            <version>${'$'}{jersey.version}</version>
        </dependency><!-- https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-core</artifactId>
            <version>2.9.8</version>
        </dependency>
    </dependencies>
    
        <build>
            <finalName>teanga-dkpro-wrapper-$jar</finalName>
            <pluginManagement><!-- lock down plugins versions to avoid using Maven defaults (may be moved to parent pom) -->
                <plugins>
                    <plugin>
                        <groupId>org.eclipse.jetty</groupId>
                        <artifactId>jetty-maven-plugin</artifactId>
                        <version>${'$'}{jetty.version}</version>
                    </plugin>

                </plugins>
            </pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <executions>
                        <execution>
                            <id>compile</id>
                            <phase>compile</phase>
                            <goals>
                                <goal>compile</goal>
                            </goals>
                        </execution>
                        <execution>
                            <id>testCompile</id>
                            <phase>test-compile</phase>
                            <goals>
                                <goal>testCompile</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </build>
    </project>""")
        }
    }

    private fun genereteWebXML(jar : String) {
        File("dockers/$jar/src/main/webapp/WEB-INF").mkdirs()
        val webXML = PrintWriter("dockers/$jar/src/main/webapp/WEB-INF/web.xml")
        webXML.use {
            webXML.println("""<!DOCTYPE web-app PUBLIC
 "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN"
 "http://java.sun.com/dtd/web-app_2_3.dtd" >

<web-app>

<servlet>
        <servlet-name>Jersey REST Service</servlet-name>
        <servlet-class>org.glassfish.jersey.servlet.ServletContainer</servlet-class>
        <init-param>
            <param-name>javax.ws.rs.Application</param-name>
            <param-value>org.insightcentre.uld.teanga.dkprowrapper.DKProWrapper</param-value>
        </init-param>
       <init-param>
            <param-name>com.sun.jersey.api.json.POJOMappingFeature</param-name>
            <param-value>true</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>

    <servlet-mapping>
        <servlet-name>Jersey REST Service</servlet-name>
        <url-pattern>/*</url-pattern>
    </servlet-mapping>
</web-app>""")
        }
    }

    private fun generateWrapper(jar : String) {
        val wrapper = PrintWriter("dockers/$jar/src/main/java/org/insightcentre/uld/teanga/dkprowrapper/DKProWrapper.java")
        wrapper.use {
            wrapper.println("package org.insightcentre.uld.teanga.dkprowrapper;\n" +
                    "\n" +
                    "import org.glassfish.jersey.server.ResourceConfig;\n" +
                    "\n" +
                    "/**\n" +
                    " *\n" +
                    " * @author John McCrae\n" +
                    " */\n" +
                    "public class DKProWrapper extends ResourceConfig {\n" +
                    "\n" +
                    "    public DKProWrapper() {\n" +
                    "        packages(\"org.insightcentre.uld.teanga.dkprowrapper\");\n" +
                    "        register(DKPro.class);\n" +
                    "    }\n" +
                    "\n" +
                    "    \n" +
                    "\n" +
                    "    \n" +
                    "}")
        }
    }

    private fun generateDockerFile(jar : String) {
        val dockerfile = PrintWriter("dockers/$jar/Dockerfile")
        dockerfile.use {
            dockerfile.println("FROM tomcat:9.0-jdk14-openjdk-oracle                                            \n" +
                    "LABEL description=\"Teanga DKPro Wrapper for $jar\"\n" +
                    "LABEL version=\"1.0\"\n" +
                    "LABEL maintainer=\"john@mccr.ae\"\n" +
                    "EXPOSE 8080" +
                    "                                                                                \n" +
                    "RUN rm -rf /usr/local/tomcat/webapps/ROOT                                       \n" +
                    "COPY target/teanga-dkpro-wrapper-$jar.war /usr/local/tomcat/webapps/ROOT.war\n" +
                    "COPY openapi.yaml /  ")
        }
    }

    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val dockers = mutableMapOf<String, MutableList<String>>()
        for(line in BufferedReader(FileReader("dockers.csv")).lines()) {
            val elems = line.split(",")
            dockers.getOrPut(elems[0]) { mutableListOf() }.add(elems[1])
        }
        for((jar, clazzes) in dockers) {
            File("dockers/$jar").mkdirs()
            System.err.println(jar)
            val serviceDescriptors = clazzes.map { c -> extractServiceDescriptor(Class.forName(c)) }
            val m: Any = makeOpenApiDescription(jar, serviceDescriptors)
            val mapper = ObjectMapper(YAMLFactory())
            val openapiFile = PrintWriter("dockers/$jar/openapi.yaml")
            openapiFile.use {
                mapper.writeValue(openapiFile, m)
            }
            generateJavaCode(serviceDescriptors, jar)
            generatePOM(jar)
            genereteWebXML(jar)
            generateWrapper(jar)
            generateDockerFile(jar)
        }
    }
}

data class CasType(val name: String, val members: List<Class<*>>?)
