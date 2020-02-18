# Change Log

## Edit this file
[Guidelines](https://keepachangelog.com/en/1.0.0/) : https://keepachangelog.com/en/1.0.0/

## Unreleased

## 2019-09-10: v2.0.0-34

* feature(ui): Use a context variable as task input for List or Map
* feature(core/ui): Override scenarios parameters from campaign level
* feature(task): Add selector to basic consume amqp task
* feature(task): Get web element attribute
* feature(task): Resize browser
    
* refactor(ui): Project structure

* bugfix: Fix broken component deletion
    
## 2019-09-10: v2.0.0-34

* feature: Migrate from H2 to PostgreSQL
* feature(ui): Manage campaign scenario execution order
* feature(ui): Scenario raw edition form default to HJSON

* feature(core): Support conversion of scenarios using multiple when steps
* feature(core/ui): Copy scenario
* feature(core/ui): Add tags on composite scenario
* feature(core/ui): Prevent deletion of a component block used somewhere else
* feature(core/ui): Improve execution parameters for composite scenarios

* bugfix(core): Truncate too long error message before saving to DB
* bugfix(task): Send an empty body on http post

* test: Add Mutation Testing

## 2019-06-06: v2.0.0

* removed: Automatic step library is removed in favor of component block creation

* feature(ui): Show execution count on scenario list
* feature(ui): Do not show default step strategy on scenario form edition
* feature(task): Scroll web elements

* feature(core/ui): Scenario creation using component blocks
* feature(core/ui): Avoid creating a cyclic component
* feature(core/ui): Execution on component only for repl-like purpose 
* feature(core/ui): Filter scenarios by type (normal / composite)
* feature(core/ui): Filter scenarios without tag
* feature(core/ui): Improve campaign creation & edition
* feature(core/ui): Improve campaign list
* feature(core/ui): Improve campaign execution
* feature(core/ui): Prevent saving a scenario with format errors
* feature(core/ui): Show "Target not found" error on scenario execution


* refactor(core): Improve logging on scheduled campaign execution
* refactor(core/domain): Remove scenario "blob"
* refactor(core/domain): Scenarios by use case
* refactor(core/api): Improve some error messages
* refactor(core/api): Old scenario format retro-compatibility
* refactor(ui): Project structure

* bugfix:(core): Fix on adding a target preventing scenario execution

* doc: Correct an error

## 2019-04-04: v1.14.1-146

* feature(ui): Confirmation box on closing unsaved scenario
* feature(ui): Confirmation box on campaign deletion 
* feature(ui): Keep scenarios list filter preference
* feature(ui): View scenarios by list or by card
* feature(ui): Start/Stop/Pause/Resume a scenario execution
* feature(core/ui): Schedule daily campaign execution
* feature(core): Scenario deletion is now logical only
* feature(core): List all tasks and their parameters
* feature(task): Selenium clear()

* refactor(ui): Remove pop-up on campaign edition
* refactor(ui): Remove pop-up on scenario edition
* refactor(core/ui): Reduce campaign loading time

* security: LDAP authentication

## 2019-03-05: v1.14.1-104

* feature(ui): Follow each step execution in real time
* feature(task): Enable hovering an element then clicking on another
* feature(task): Selenium tests should run on internet explorer
* feature(task): SeleniumSwitchToTask should handle Ok/Cancel alert box
* feature(task): Validate JSON content against JSON schema
* feature(task): Assert missing JSON field 

* bugfix: Fix routing after creation forms

* security(core): SSL

## 2019-02-14: v1.14.1-86

* feature(task): Add AMQP task
* feature(core/plugin): Run a scenario from Intellij
* feature(core): Start/Stop/Pause/Resume a scenario execution
* feature(core): Notify scenario execution events
* feature(core/ui): Enable/Disable documentation examples

* refactor: Remove SQL datasource cache

* bugfix: Campaign XML reports

## 2019-01-14: v1.14.1-68

* feature: Enable switching to popups in order to perform Selenium actions
* feature(ux): Scenario edition
* feature(ux): Step library
* feature(task): Add keystore for https server task
* feature: CLI for running scenario execution

* refactor: Création d'un plugin Intellij pour l'execution et l'écriture (autocomplétion, ...) de scénario
* refactor: Start execution engine without core module

* bugfix: Edition page performance 
* bugfix: Avoid cyclic step creation
* bugfix: Adding empty dataset
* bugfix(core/ui): Out of Memory on campaign loading
* bugfix(task): Close SQL session
* bugfix(task): Fix selenium getText
* bugfix(ui): Scrollbar problem on technical steps edition

* doc: Document all tasks

## 2018-12-13: v1.14.1-44

* feature(task): Add https server start/stop & https listener
* feature: Add showroom page 

* refactor: Decouple core and engine

* bugfix: Fix resource leak

* doc: Add scenario examples

## 2018-11-28: v1.14.1-23

* feature(core/ui): Add scenario parameters

* refactor: Scenario database backup

* bugfix(ui): Scenario form edition (misc)

* doc: Ajout d'un menu d'administration pour la documentation (Exemples à venir)

## 2018-11-12: v1.14.1-10

* removed: Remove step library pop-up on scenario form edition

* feature(task): Add task for validating XML against an XSD
* feature(ui): Redesign scenario form edition UX/UI

* bugfix(ui): Deleting step description does not remove sub-step 

## 2018-11-07: v1.14.1-7

* removed: Remove scenario parameters management

* feature(ui): Provide scenario edition with a web form (without step strategies)
* feature(ui): Show syntax and content errors when editing a scenario
* feature(task): AMQP Clean Queue task accepts a list of files
* feature(core): Manage git scenarios repository

* refactor(ui): Update scenario execution view
* refactor(core/engine): Improve error management

* bugfix(ui): Tags and scenarios card list

## 2018-10-28: v1.13.1-193

* refactor(core/engine/ui): Show complete exception stack
* refactor(task): JMS task provides complete exception stack

## 2018-10-28: v1.13.1-191

* feature(ui): Group admin features under one menu entry
* feature(ui): Provide read only access for unauthenticated users
* feature(func): XPATH function makes use of document namespaces
* feature(task): XML assert task makes use of document namespaces

* refactor(task): Improve content of Selenium tasks information

* bugfix(engine): Each task input/output is evaluated sequentially and evaluation context is updated accordingly after each
* bugfix(core/ui): View saved execution reports after scenario changes

* chore: Add Weblogic 10.3 client dependency
    
## 2018-10-18: v1.13.1-179

* feature(ui): Add new way to create and edit a scenario
* feature(ui): Update global template
* feature(ui): Update scenarios list view
* feature(core/ui): Keep all execution reports and show 20 last reports
* feature(task): Add Selenium tasks

* refactor(core/engine): Decouple Core and Engine
* refactor(engine): Delete optional attributes on Step and StepDefinition

## 2018-10-02: v1.13.1-162

* feature(task): Kafka Basic Publish
* feature(task): Add timeout on http tasks
* feature(task): Json assert compare Number vs String as numbers

* refactor(perf): Cache local agent when compiling scenario
* refactor(test): Move all cucumber scenarios input to ui api

* bugfix: Fix ssh authentication with private key
* bugfix: Fix optional parameters which were mandatory

## 2018-09-20: v1.13.1-153

* feature(ui): Disable edition of scenario saved outside of the local repository
* feature(ui): Scroll to top on target change
* feature(ui): Activate search in editor

* bugfix(core): Removing a scenario deletes all its references in database
* bugfix(core): Save a scenario even if saving a referenced step fails
* bugfix(core): Fix entity mapper test for Windows
* bugfix(task): HTTP headers cannot be injected
* bugfix(task): JSON assert compare number
* bugfix(task): String or JSON in http body
