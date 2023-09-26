# Chutney packaging

## Contents

This repository contains some packaging examples of Chutney server :
* [Chutney server without frontend nor security](./local-api-unsecure)
* [Chutney server with frontend and ldap authentication](./local-dev)
* [Chutney server with frontend and component edition](./local-dev-component)

## Build

The build of each packaging results into an executable JAR archive which can be used as it is.  
Only the local-dev packaging is included as assets in each Chutney release.  

## Usage

To run a packaging, just execute : `java -jar <packaging-archive-jar-file>`.  
It will start a chutney server ready for use.  
See each packaging descriptions to know more on what is included.
