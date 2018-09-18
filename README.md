# configuration-reader

If you have a property file to configure your java program and you think, that reading and casting each property by hand is tedious, than this is the tool for you.

You simply provide a class that will store your configuration and the ConfigurationReader does its job.
Each field in your class will be filled with the regarding property.

`YourConfiguration instance = new ConfigurationReader().read("path-to-properties", YourConfiguration.class);`     

## Features

- Class mappings: The property from the file will be mapped to the type of the field in your class. 
- Name mappings: No need to change the names of properies or fields. Map names with `@PropertyName`
- Default values: initialize the fields of your class, which will behave as your default values
- throws Exception if no value in properties file and no default value is provided 
- TODO: Validation of input values (min, max, pattern, optional, ...)

## Class Mappings

Build in class mappers:
- Boolean
- Boolean[]
- Double
- Double[]
- Float[]
- Integer
- Integer[]
- Long
- Long[]
- String
- String[]

Custom class mappers can be passed via `addClassMapper`.

`configurationReader.addClassMapper(BigDecimal.class, str -> new BigDecimal(str));`
