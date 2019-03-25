
A tool made for the Java programming language, it allows users to make configurations using only enums.   

#What is EnumToConfiguration?
EnumToConfiguration is a library created by Articdive (Lukas Mansour).
It's an open-source library that allows you to create configurations with Java's enums.
The aim of EnumToConfiguration is to make configurations easy to use, easily accessible and useable with minimal code.

# How can I use EnumToConfiguration?
EnumToConfiguration is primarily available to maven users.  
To add it to your maven project use the following repository and dependency:
```
<repository>
    <id>articdive-repo</id>
    <url>https://nexus.articdive.de/repository/maven-public/</url>
</repository>
    
<dependency>
    <groupId>de.articdive</groupId>
    <artifactId>EnumToConfiguration</artifactId>
    <version>1.0-SNAPSHOT</version>
    <scope>compile</scope>
</dependency>
```
# Usage in Java
EnumToConfiguration will require a file and an enum to work.  
The enum must implement de.articdive.enum_to_configuration.ConfigurationNode.  
The enum might look something like this:
```java
import de.articdive.enum_to_configuration.ConfigurationNode;
import de.articdive.enum_to_configuration.ConfigurationSection;

public enum ConfigEnum implements ConfigurationNode {
    MY_NODE("nodePath", "defaultValue", "Comments"),
    MY_PARENT_NODE("parent", new ConfigurationSection(), "This is a parent Node"),
    MY_CHILD_NODE("parent.node", "My Child Value", "This is a child node");
    private final String path;
    private final Object defaultValue;
    private final String[] comments;
    
    ConfigEnum(String path, Object defaultValue, String... comments) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.comments = comments;
    }
    
    @Override
    public String getPath() {
        return path;
    }
    
    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }
    
    @Override
    public String[] getComments() {
        return comments;
    }
}
```

Then create an EnumConfiguration with the EnumConfigurationBuilder, which requires an output File and the enum's class.
```java
EnumConfiguration enumConfiguration = new EnumConfigurationBuilder(file, ConfigEnum.class).build;
```

EnumToConfiguration supports YAML, HOCON and TOML and you can switch the values by using the
```setType(ConfigurationType type )``` method in the EnumConfigurationBuilder.

To get any values use the ```get(ConfigurationNode node)``` or the ```get(String path)``` methods:
```java
Object value = enumConfiguration.get(ConfigEnum.MY_NODE);
```

To set any values use the ```set(ConfigurationNode node, Object o)``` or the ```set(String path, Object o)``` ,ethods:
```java
enumConfiguration.set(ConfigEnum.MY_NODE, "newValue");
```

# I have a question, issue, request, suggestion or similar.
Please [open an issue here on GitHub](https://github.com/Articdive/EnumToConfiguration/issues/new).

# I want to donate.
I highly appreciaite donations, especially when others make money by using my library.  
I will not force you to donate nor will I treat you otherwise for not.  
Please only donate if you are over the age of 18 or have permission from a legal guardian and use your own money.  

[![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=2GDHSJK2FDDF6)