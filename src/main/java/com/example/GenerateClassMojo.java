package com.example;

import com.example.model.ClassDefinition;
import com.example.model.FieldDefinition;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.javapoet.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Mojo(name = "generate-json-model", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class GenerateClassMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    // We no longer need a single jsonFileName parameter since we scan the directory

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    // ^ Important: This allows us to safely skip JSON files that aren't our models

    public void execute() throws MojoExecutionException {
        File resourceDir = new File(project.getBasedir(), "src/main/resources");

        if (!resourceDir.exists()) {
            getLog().info("src/main/resources does not exist. Skipping generation.");
            return;
        }

        // 1. Find all .json files
        File[] jsonFiles = resourceDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

        if (jsonFiles == null || jsonFiles.length == 0) {
            getLog().info("No JSON files found in src/main/resources.");
            return;
        }

        getLog().info("Found " + jsonFiles.length + " JSON files. Checking for models...");

        // 2. Loop through each file
        for (File jsonFile : jsonFiles) {
            processJsonFile(jsonFile);
        }
    }

    private void processJsonFile(File jsonFile) throws MojoExecutionException {
        try {
            // Parse JSON
            ClassDefinition def = objectMapper.readValue(jsonFile, ClassDefinition.class);

            // Validation: Is this actually a model definition file?
            if (def.className == null || def.fieldData == null) {
                getLog().debug("Skipping " + jsonFile.getName() + " - Missing className or fieldData.");
                return;
            }

            getLog().info("Processing Model: " + def.className);
            generateJavaClass(def);

        } catch (IOException e) {
            // We log a warning but don't fail the build, in case it's a non-model JSON that Jackson couldn't parse
            getLog().warn("Skipping " + jsonFile.getName() + ": Could not parse as ClassDefinition (" + e.getMessage() + ")");
        }
    }

    private void generateJavaClass(ClassDefinition def) throws IOException {
        String simpleClassName = def.className.replace(".java", "");

        // Determine package
        String rawPath = def.classPath.replace("\\", "/");
        String packageName;
        if (rawPath.contains("java/")) {
            packageName = rawPath.substring(rawPath.indexOf("java/") + 5).replace("/", ".");
        } else {
            packageName = rawPath.replace("/", ".");
        }

        // Define Lombok Annotations
        ClassName lombokData = ClassName.get("lombok", "Data");
        ClassName lombokNoArgs = ClassName.get("lombok", "NoArgsConstructor");
        ClassName lombokAllArgs = ClassName.get("lombok", "AllArgsConstructor");

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(simpleClassName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(lombokData)
                .addAnnotation(lombokNoArgs)
                .addAnnotation(lombokAllArgs);

        if (def.fieldData != null) {
            for (FieldDefinition field : def.fieldData) {
                TypeName typeName = parseType(field.fieldType);
                FieldSpec.Builder fieldBuilder = FieldSpec.builder(typeName, field.fieldName, Modifier.PRIVATE);

                if (field.defaultValue != null) {
                    if (field.fieldType.equalsIgnoreCase("String")) {
                        fieldBuilder.initializer("$S", field.defaultValue);
                    } else if (field.fieldType.equalsIgnoreCase("Long")) {
                        fieldBuilder.initializer("$LL", field.defaultValue);
                    } else {
                        fieldBuilder.initializer("$L", field.defaultValue);
                    }
                }
                classBuilder.addField(fieldBuilder.build());
            }
        }

        // Write file
        File outputDir = new File(project.getBasedir(), "src/main/java");
        JavaFile javaFile = JavaFile.builder(packageName, classBuilder.build())
                .indent("    ")
                .build();

        javaFile.writeTo(outputDir);
        getLog().info("Generated " + simpleClassName + ".java");
    }

    private TypeName parseType(String typeStr) {
        if (typeStr.startsWith("List<")) {
            String innerType = typeStr.substring(5, typeStr.length() - 1);
            return ParameterizedTypeName.get(ClassName.get(List.class), getSimpleType(innerType));
        }
        return getSimpleType(typeStr);
    }

    private TypeName getSimpleType(String type) {
        return switch (type) {
            case "String" -> ClassName.get(String.class);
            case "Integer" -> ClassName.get(Integer.class);
            case "int" -> TypeName.INT;
            case "Long" -> ClassName.get(Long.class);
            case "long" -> TypeName.LONG;
            case "Boolean" -> ClassName.get(Boolean.class);
            case "boolean" -> TypeName.BOOLEAN;
            case "Double" -> ClassName.get(Double.class);
            case "double" -> TypeName.DOUBLE;
            default -> ClassName.bestGuess(type);
        };
    }
}