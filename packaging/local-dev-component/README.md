# Local DEV component packaging

This section describe the default configuration of this packaging.  
See all configuration files in [resources directory](./src/main/resources).  

**This packaging is deprecated as component edition is history**

## Server

The server listens http requests on port 8080, redirected on https port 8443.  
It includes an Angular frontend with component edition.  
SSL and SpringBoot Actuator are activated.

## Persistence

The database used by default is H2.  
Its data file is located under the `.chutney/data` directory, relative to the root execution directory.  

Embedded SQLite server could be activated by Spring profiles.  

All persistent Chutney repositories' files are located under the `.chutney/conf` directory, relative to the root execution directory.

## Authentication

Memory authentication is activated by default.  
Two users are declared : `user` and `admin`, the latter being an administrator.
