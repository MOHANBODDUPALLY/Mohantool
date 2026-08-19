# Function GAPI XML Generator

Java desktop tool to generate GAPI XML values from Function IDs.

## Processing flow

Excel
→ Function ID
→ Function XML
→ `<GAPI>`
→ GAPI JavaScript
→ GAPI name + value
→ XML output

## Excel columns

The tool reads:

- BRU FUNC ID
- RETURN FUNC ID
- DEO FUNC ID

## Function XML

Example:

```xml
<GAPI>F05030706382</GAPI>