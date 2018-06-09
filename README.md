# IntegrityShowSuspectDetails
This Utility shows the author and the changes caused the Suspect Flag to appear.

## Use Cases
- As a document author, I'd like to see which change has caused the Suspect Flag to appear. 

## Possible Import Layout
![CustomImport](doc/ShowSuspectDetails.PNG)

## Important
- This tool has been created back in 2015 and is in production then
- The usabilty should be improved

## Tested with
- Integrity LM 10.4
- Integrity LM 10.9
- Integrity LM 11.0

## Installation
- Provide the following files locally:

```
#ClientFolder#/IntegrityShowSuspectDetails.jar (new)
#ClientFolder#/lib/IntegrityAPI.jar (new)
#ClientFolder#/lib/daisydiff.jar (new)
#ClientFolder#/lib/jfxmessagebox-1.1.0.jar (new)
#ClientFolder#/lib/mksapi.jar (existing)
```
- Create a Custom Menu Entry as follows:
```
Name:  Show Suspect Details
Program: ..\jre\bin\javaw
Parameter: -jar ..\IntegrityShowSuspectDetails.jar
```

## How to Run
- Select a suspect node in any document
- Choose Custom > Show Suspect Details
- The form for Suspect Details should open
- Review the outcome
