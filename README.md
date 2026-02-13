1. create a spring boot project

2. add .json file in the below format in src/main/resources path :
`   {
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
   }`

   3. Add this to your plugin scetion of the spring boot project :
     ` <plugin>
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
      </plugin>`
