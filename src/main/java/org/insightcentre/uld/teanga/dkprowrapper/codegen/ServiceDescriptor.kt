package org.insightcentre.uld.teanga.dkprowrapper.codegen

import org.insightcentre.uld.teanga.dkprowrapper.codegen.TeangaDKProCodeGen.typeToOpenAPI
import java.lang.reflect.Type

class ServiceDescriptor {
    var name: String? = null
    var copyright: String? = null
    var vendor: String? = null
    var description: String? = null
    var version: String? = null
    var classShortName: String? = null
    var classFullName: String? = null
    var inputs: Array<String>? = null
    var outputs: Array<String>? = null
    var parameters: MutableList<Parameter> = ArrayList()

    class Parameter {
        var name: String? = null
        var type: Class<*>? = null
        var genericType: Type? = null
        var mandatory = false
        var defaultValue: Array<String>? = null
        var description: String? = null

        constructor() {}
        constructor(name: String?, type: Class<*>?, genericType: Type?, mandatory: Boolean, defaultValue: Array<String>?, description: String?) {
            this.name = name
            this.type = type
            this.genericType = genericType
            this.mandatory = mandatory
            this.defaultValue = defaultValue
            this.description = description
        }

        fun toOpenAPI(): Any {
            val m = HashMap<String, Any?>()
            m["name"] = name
            if (description != null) m["description"] = description
            m["in"] = "query"
            m["required"] = mandatory
            val schemas = HashMap<String, Any?>()
            m["schema"] = typeToOpenAPI(type!!, genericType, null)
            if (defaultValue != null) {
                (m["schema"] as HashMap<String, Any?>?)!!["default"] = defaultValue
            }
            return m
        }
    }
}