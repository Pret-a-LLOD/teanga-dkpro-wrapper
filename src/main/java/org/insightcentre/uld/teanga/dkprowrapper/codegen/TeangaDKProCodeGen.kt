package org.insightcentre.uld.teanga.dkprowrapper.codegen

import org.apache.uima.fit.descriptor.TypeCapability
import org.insightcentre.uld.teanga.dkprowrapper.codegen.TeangaDKProCodeGen
import java.util.stream.Collectors
import java.lang.ClassNotFoundException
import kotlin.Throws
import kotlin.jvm.JvmStatic
import org.dkpro.core.opennlp.OpenNlpChunker
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.apache.uima.fit.descriptor.ConfigurationParameter
import org.apache.uima.fit.descriptor.ResourceMetaData
import org.apache.uima.jcas.tcas.Annotation
import java.lang.Exception
import java.util.*

object TeangaDKProCodeGen {
    private fun codegen(clazz: Class<*>): ServiceDescriptor {
        val desc = ServiceDescriptor()
        desc.clazzName = clazz.simpleName
        for (anno in clazz.annotations) {
            if (anno is ResourceMetaData) {
                desc.name = anno.name
                desc.description = anno.description
                desc.copyright = anno.copyright
                desc.vendor = anno.vendor
                desc.version = anno.version
            } else if (anno is TypeCapability) {
                desc.inputs = anno.inputs
                desc.outputs = anno.outputs
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
            paths["/" + desc.clazzName] = m1
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
            val name = "Cas" + Arrays.stream(inputs).map { c: String? ->
                try {
                    return@map Class.forName(c).simpleName
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                    return@map ""
                }
            }.sorted().collect(Collectors.joining())
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

    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val clazz: Class<*> = OpenNlpChunker::class.java
        val m: Any = makeOpenApiDescription(Arrays.asList(codegen(clazz)))
        val mapper = ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
        mapper.writeValue(System.out, m)
    }
}