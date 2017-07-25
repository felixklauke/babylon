# Babylon

Caution: Still under heavy development

Babylon is a little experimental not really ready to use config format. It aims to replace
json as a config format in my projects by providing the following features: 
- Highly human readable
- Add comments for all values (Main reason of replacing json)
- Simple and clean API
- Managing the config as a model with annotations

On config initialization babylon will check for an existing config and will create a new one
if the file does not exist or read the known config.

# Installation / Usage

- Install [Maven](http://maven.apache.org/download.cgi)
- Clone this repo
- Instal: ```mvn clean install```

**Maven dependencies**

```xml
<dependency>
    <groupId>de.felix_klauke</groupId>
    <artifactId>babylon</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

# Example

_Model:_
```java
public class TestConfig extends Config {

    private String test;
    private String meh;
    
    @Skip
    private MessageManager messageManager;
    
    public TestConfig(String test, String meh, MessageManager messageManager) {
        this.test = test;
        this.meh = meh;
        this.messageManager = messageManager;
    }

    public TestConfig() {
    }
}
```

_Initialize config:_
```java
Babylon babylon = BabylonFactory.createBabylon();
Config config = new TestConfig();

babylon.initializeConfig(config, new File("config.bbl"));
```

_Comments in the config:_
```java
@Comment( "I am a commented field" )
private String name = "SasukeKawaiiTheKing";
```

Comments will look like this: 
```
{
	# I am a commented field
	name: "SasukeKawaiiTheKing"
}
```

_Renaming fields:_
```java
@Name( "customName" )
private String name = "SasukeKawaiiTheKing";
```

Renaming will look like this:
```
{
	customName: "SasukeKawaiiTheKing"
}
```