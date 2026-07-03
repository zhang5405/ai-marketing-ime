/** JNI: schema list item */
package com.osfans.trime.core;

@SuppressWarnings("unused")
public class SchemaItem {
    public String schemaId, name;
    public SchemaItem(String schemaId, String name) {
        this.schemaId = schemaId; this.name = name;
    }
}
