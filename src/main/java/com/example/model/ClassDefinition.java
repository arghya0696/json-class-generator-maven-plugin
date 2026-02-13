package com.example.model;

import java.util.List;

public class ClassDefinition {
    public String classPath; // e.g., "src/main/java/model" or package name
    public String className; // e.g., "Student.java"
    public List<FieldDefinition> fieldData;
}