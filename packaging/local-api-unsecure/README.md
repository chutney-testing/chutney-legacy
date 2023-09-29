# Local API unsecure packaging

This section describe the default configuration of this packaging.  
See all configuration files in [resources directory](./src/main/resources).

## Server

The server listens http requests on port 8350.  
No frontend is included.  
SpringBoot Actuator is deactivated.

## Persistence

The database used by default is H2.  
Its data file is located under the `.chutney` directory, relative to the root execution directory.  

Embedded SQLite server could be activated by Spring profile.  

All persistent Chutney repositories' files are located under the `.chutney/conf` directory, relative to the root execution directory.

## Authentication

There is no authentication activated. Anyone could access this server with administrator role.
