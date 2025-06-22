ClickCrystals Utils
---
Useful terminal commands for ClickCrystals development.

### Commands
There will be more added in the future.

```yml
Name: module-table
Usage: module-table
Description: Generates and copies to your clipboard a markdown table of modules and their 
  descriptions by reading your local file system for 
  ClickCrystals module .java files.
```
```yml
Name: packets
Usage: packets -minecraftVersion
Description: Generates and copies to your clipboard a Java HashMap of Packet classes
  mapped to their respective names. This util requests data from maven.fabricmc.net
  with the specified minecraftVersion to get all possible packet names from that 
  Minecraft Version.
```
```yml
Name: packet-table
Usage: packet-table
Description: Generates and copies to your clipboard a markdown table of packets and their 
  scripting ID
```


### How to Use?
(Optional, but it's recommended to create a batch file for this)

In the `.bat` file:
```
"%JAVA_HOME%\bin\java" -jar downloadedReleaseFilePath.jar %*
```

Then in terminal: 
```
./yourBatchFileName modules
./yourBatchFileName packets -1.21
```

Happy ClickCrystals coding!


| What You Have | What to get                                                                                                                   |
|:--------------|:------------------------------------------------------------------------------------------------------------------------------|
| higher..      | how is that even possible?                                                                                                    |
| 1.21.6        | not supported                                                                                                                 |
| 1.21.5        | [ClickCrystals-1.21.5-1.3.0.jar](https://github.com/clickcrystals-development/ClickCrystals/releases/tag/v1.3.0) *recommended |
| 1.21.4        | not supported                                                                                                                 |
| 1.21.3        | not supported                                                                                                                 |
| 1.21.2        | not supported                                                                                                                 |
| 1.21.1        | [ClickCrystals-1.21.5-1.3.0.jar](https://github.com/clickcrystals-development/ClickCrystals/releases/tag/v1.3.0) *recommended |
| 1.21          | [ClickCrystals-1.21.5-1.3.0.jar](https://github.com/clickcrystals-development/ClickCrystals/releases/tag/v1.3.0) *recommended |
| 1.20.6        | [ClickCrystals-1.20.6-1.2.9.jar](https://github.com/clickcrystals-development/ClickCrystals/releases/tag/v1.2.9)              |
| 1.20.5        | not supported                                                                                                                 |
| 1.20.4        | [ClickCrystals-1.20.4-1.2.9.jar](https://github.com/clickcrystals-development/ClickCrystals/releases/tag/v1.2.9)              |
| 1.20.3        | [ClickCrystals-1.20.4-1.2.9.jar](https://github.com/clickcrystals-development/ClickCrystals/releases/tag/v1.2.9)              |
| 1.20.2        | [ClickCrystals-1.20.2-1.2.9.jar](https://github.com/clickcrystals-development/ClickCrystals/releases/tag/v1.2.9)              |
| 1.20.1        | [ClickCrystals-1.20-1.2.9.jar](https://github.com/clickcrystals-development/ClickCrystals/releases/tag/v1.2.9)                |
| 1.20          | [ClickCrystals-1.20-1.2.9.jar](https://github.com/clickcrystals-development/ClickCrystals/releases/tag/v1.2.9)                |
| ..lower       | cry                                                                                                                           |


