# configuration-reader

If you have a property file to configure your java program and you think, that reading and casting each property by hand is tedious, than this is the tool for you.

You simply provide a class that will store your configuration and the ConfigurationReader does its job.
Each field in your class will be filled with the regarding property.

`YourConfiguration instance = ConfigurationReader().getInstance().read("path-to-properties", YourConfiguration.class);`     

## Features

- Class mappings: The property from the file will be mapped to the type of the field in your class. 
- Name mappings: No need to change the names of properties or fields. Map names with `@ConfigurationProperty` and `@NameSpace`.
- Default values: initialize the fields of your class, which will behave as your default values.
- Error handling: throws Exception for mandatory fields if no value in properties file and no default value is provided.

Examples (simple and complex ones) can be found in the wiki.

## TODOs

- Avoid public Fields. Use getter and setter instead if possible.
- Validation of input values (min, max, pattern, ...)
