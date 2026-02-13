1. clone this repo and  run  mvn clean install 
2. Note : This plugin uses lombok for Getter setter AllArgs and NoArgs constructor . You need to add Lombok in your classpath for spring boot project
3. create a spring boot project

4. add .json file in the below format in src/main/resources path :
   ```{
   "classPath": "src/main/java/com/tw/demo_pulgin/model",
   "className": "College.java",
   "fieldData": [
   {
   "fieldName": "collegeName",
   "fieldType": "String",
   "defaultValue": null
   },
   {
   "fieldName": "address",
   "fieldType": "List<String>",
   "defaultValue": null
   },
   {
   "fieldName": "phoneNumber",
   "fieldType": "Long",
   "defaultValue": null
   }
   ]
   }
   ```

5. Add this to your plugin section of the spring boot project :
   ```
   <plugin>
    <groupId>com.example</groupId>
    <artifactId>json-class-generator-maven-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
    <execution>
    <id>generate-model-classes</id>
    <phase>generate-sources</phase>
    <goals>
    <goal>generate-json-model</goal>
    </goals>
    </execution>
    </executions>
   </plugin>
   ```

6. run mvn clean compile
   
