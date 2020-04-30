# Changelog

## [1.1.4](https://github.com/chutney-testing/chutney/tree/1.1.4) (2020-04-30)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.1.3...1.1.4)

**Implemented enhancements:**

- Add environment name to context and global variable [\#19](https://github.com/chutney-testing/chutney/issues/19)

**Merged pull requests:**

- bugfix: Task using isPresent [\#121](https://github.com/chutney-testing/chutney/pull/121) ([bessonm](https://github.com/bessonm))
- Feat/add environment name to context and global variables [\#120](https://github.com/chutney-testing/chutney/pull/120) ([rbenyoussef](https://github.com/rbenyoussef))

## [1.1.3](https://github.com/chutney-testing/chutney/tree/1.1.3) (2020-04-24)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.1.2...1.1.3)

**Implemented enhancements:**

- Add number of scenario run on campaign report [\#96](https://github.com/chutney-testing/chutney/issues/96)

**Fixed bugs:**

- Component/Scenario edition : Cannot update step parameter value after precedent step deletion [\#107](https://github.com/chutney-testing/chutney/issues/107)
- Campaign run duration on parallels run [\#86](https://github.com/chutney-testing/chutney/issues/86)
- Execution environment for scenario not displayed for direct access [\#85](https://github.com/chutney-testing/chutney/issues/85)
- Need more labels in campaign report [\#73](https://github.com/chutney-testing/chutney/issues/73)
- No alert when closing component screen [\#70](https://github.com/chutney-testing/chutney/issues/70)
- Target are not sorted in chrome [\#22](https://github.com/chutney-testing/chutney/issues/22)
- Scenario description are truncated [\#21](https://github.com/chutney-testing/chutney/issues/21)

**Closed issues:**

- Add an alert when removing global var [\#91](https://github.com/chutney-testing/chutney/issues/91)

**Merged pull requests:**

- Feat/ui improve campaign ux [\#115](https://github.com/chutney-testing/chutney/pull/115) ([rbenyoussef](https://github.com/rbenyoussef))
- feat\(ui\): Show number of passed/failed scenarios on campaign report [\#114](https://github.com/chutney-testing/chutney/pull/114) ([bessonm](https://github.com/bessonm))
- Bugfix/alert on globalvar deletion [\#109](https://github.com/chutney-testing/chutney/pull/109) ([bessonm](https://github.com/bessonm))
- fix\(ui\): Component edition - Must recreate steps parameter values [\#108](https://github.com/chutney-testing/chutney/pull/108) ([boddissattva](https://github.com/boddissattva))
- Feat/ui improve scenario execution report [\#105](https://github.com/chutney-testing/chutney/pull/105) ([rbenyoussef](https://github.com/rbenyoussef))
- bugfix\(ui\): Sort target and environment names [\#104](https://github.com/chutney-testing/chutney/pull/104) ([bessonm](https://github.com/bessonm))
- bugfix\(ui\): Show scenario description without truncation [\#103](https://github.com/chutney-testing/chutney/pull/103) ([bessonm](https://github.com/bessonm))
- Feat/ui display improvement [\#102](https://github.com/chutney-testing/chutney/pull/102) ([rbenyoussef](https://github.com/rbenyoussef))
- Fix/86 [\#93](https://github.com/chutney-testing/chutney/pull/93) ([boddissattva](https://github.com/boddissattva))
- Tech/clean engine target model [\#89](https://github.com/chutney-testing/chutney/pull/89) ([bessonm](https://github.com/bessonm))
- Fix \#85 [\#88](https://github.com/chutney-testing/chutney/pull/88) ([boddissattva](https://github.com/boddissattva))
- bugfix\(ui\): Add alert when leaving component scenario edition with mo… [\#87](https://github.com/chutney-testing/chutney/pull/87) ([nbrouand](https://github.com/nbrouand))
- Tech/reduce cli coupling [\#84](https://github.com/chutney-testing/chutney/pull/84) ([bessonm](https://github.com/bessonm))
- feat\(ui\): Add stop label for campaign execution report summarize [\#78](https://github.com/chutney-testing/chutney/pull/78) ([nbrouand](https://github.com/nbrouand))
- chore\(ui\): Add a test on scenarii.component.spec.ts [\#77](https://github.com/chutney-testing/chutney/pull/77) ([nbrouand](https://github.com/nbrouand))
- chore\(ui\): Add some ng test [\#76](https://github.com/chutney-testing/chutney/pull/76) ([nbrouand](https://github.com/nbrouand))

## [1.1.2](https://github.com/chutney-testing/chutney/tree/1.1.2) (2020-03-19)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.1.1...1.1.2)

**Implemented enhancements:**

- Add a new task to consume message from a kafka topic [\#38](https://github.com/chutney-testing/chutney/issues/38)
- Display environment execution for scenario [\#20](https://github.com/chutney-testing/chutney/issues/20)

**Fixed bugs:**

- Trim all tags [\#59](https://github.com/chutney-testing/chutney/issues/59)

**Closed issues:**

- Wrong redirection when cancelling campaign edition [\#58](https://github.com/chutney-testing/chutney/issues/58)

**Merged pull requests:**

- Feature/add kafka basic consume task [\#68](https://github.com/chutney-testing/chutney/pull/68) ([bessonm](https://github.com/bessonm))
- feat\(core + ui\): Add environment info to scenario execution history [\#66](https://github.com/chutney-testing/chutney/pull/66) ([nbrouand](https://github.com/nbrouand))
- Avoid NullPointerException with message having null value as header [\#65](https://github.com/chutney-testing/chutney/pull/65) ([GeVa2072](https://github.com/GeVa2072))
- feat\(ui\): In campaign edition, cancel redirect to edited campaign [\#64](https://github.com/chutney-testing/chutney/pull/64) ([nbrouand](https://github.com/nbrouand))
- bugfix\(server\): uppercase and trim tags at saved [\#62](https://github.com/chutney-testing/chutney/pull/62) ([nbrouand](https://github.com/nbrouand))

## [1.1.1](https://github.com/chutney-testing/chutney/tree/1.1.1) (2020-03-10)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.1.0...1.1.1)

**Merged pull requests:**

- bugfix\(ui\): NPE on saveComponent + Add more ui coherence between comp… [\#61](https://github.com/chutney-testing/chutney/pull/61) ([nbrouand](https://github.com/nbrouand))
- chore: Share Intellij run config [\#57](https://github.com/chutney-testing/chutney/pull/57) ([bessonm](https://github.com/bessonm))

## [1.1.0](https://github.com/chutney-testing/chutney/tree/1.1.0) (2020-03-06)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.0.0...1.1.0)

**Implemented enhancements:**

- Font size smaller on campaign page  [\#18](https://github.com/chutney-testing/chutney/issues/18)

**Fixed bugs:**

- Navigation between component sometime broken [\#24](https://github.com/chutney-testing/chutney/issues/24)
- Pop up should not appear on scenario execution page [\#16](https://github.com/chutney-testing/chutney/issues/16)
- Id not reload when creating component [\#15](https://github.com/chutney-testing/chutney/issues/15)

**Merged pull requests:**

- feat\(core\): Parameterized component strategies [\#60](https://github.com/chutney-testing/chutney/pull/60) ([bessonm](https://github.com/bessonm))
- Fix/parameters escaping [\#56](https://github.com/chutney-testing/chutney/pull/56) ([boddissattva](https://github.com/boddissattva))
- bugfix\(ui\): Fix broken redirection on child component [\#55](https://github.com/chutney-testing/chutney/pull/55) ([nbrouand](https://github.com/nbrouand))
- bugfix\(ui\): Remove canDeactivate guard on execution history page [\#54](https://github.com/chutney-testing/chutney/pull/54) ([nbrouand](https://github.com/nbrouand))
- Tech/clean up [\#53](https://github.com/chutney-testing/chutney/pull/53) ([bessonm](https://github.com/bessonm))
- feat\(ui\): Reduce font size on campaign page [\#52](https://github.com/chutney-testing/chutney/pull/52) ([nbrouand](https://github.com/nbrouand))
- Chore/travis zulip hook [\#50](https://github.com/chutney-testing/chutney/pull/50) ([bessonm](https://github.com/bessonm))
- chore\(deps\): Bump picocli from 3.9.0 to 4.2.0 [\#49](https://github.com/chutney-testing/chutney/pull/49) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- chore\(deps-dev\): Bump rabbitmq-mock from 1.0.4 to 1.0.14 [\#48](https://github.com/chutney-testing/chutney/pull/48) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- chore\(deps\): Bump build-helper-maven-plugin from 1.12 to 3.0.0 [\#47](https://github.com/chutney-testing/chutney/pull/47) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- chore\(deps\): Bump maven-clean-plugin from 3.0.0 to 3.1.0 [\#46](https://github.com/chutney-testing/chutney/pull/46) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- chore\(deps-dev\): Bump activemq.version from 5.15.0 to 5.15.11 [\#45](https://github.com/chutney-testing/chutney/pull/45) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- doc: Update release management [\#43](https://github.com/chutney-testing/chutney/pull/43) ([bessonm](https://github.com/bessonm))
- chore: Update project info [\#41](https://github.com/chutney-testing/chutney/pull/41) ([bessonm](https://github.com/bessonm))
- fix\(core+ui\): Set id on newly created component. [\#37](https://github.com/chutney-testing/chutney/pull/37) ([boddissattva](https://github.com/boddissattva))

## [1.0.0](https://github.com/chutney-testing/chutney/tree/1.0.0) (2020-02-26)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/d734d7933351ed031c3c598b0a2de7098153b42f...1.0.0)

**Fixed bugs:**

- Global var character escaped twice [\#9](https://github.com/chutney-testing/chutney/issues/9)

**Closed issues:**

- Add a new task to consume message from a kafka topic [\#39](https://github.com/chutney-testing/chutney/issues/39)

**Merged pull requests:**

- chore\(ui\): Generate sources archive [\#40](https://github.com/chutney-testing/chutney/pull/40) ([boddissattva](https://github.com/boddissattva))
- Bugfix/escaped char [\#36](https://github.com/chutney-testing/chutney/pull/36) ([nbrouand](https://github.com/nbrouand))
- Revert "Ignore IntelliJ root configuration files" [\#35](https://github.com/chutney-testing/chutney/pull/35) ([nbrouand](https://github.com/nbrouand))
- chore: Licence compliance with Fossa [\#34](https://github.com/chutney-testing/chutney/pull/34) ([bessonm](https://github.com/bessonm))
- Add randomLong Generator [\#33](https://github.com/chutney-testing/chutney/pull/33) ([GeVa2072](https://github.com/GeVa2072))
- Fix use of == instead of equals method [\#32](https://github.com/chutney-testing/chutney/pull/32) ([GeVa2072](https://github.com/GeVa2072))
- chore: Add Travis deployment. Fix project version. [\#14](https://github.com/chutney-testing/chutney/pull/14) ([boddissattva](https://github.com/boddissattva))
- Fix typo in Selenium documentation snippet [\#13](https://github.com/chutney-testing/chutney/pull/13) ([ledoyen](https://github.com/ledoyen))
- Fix Finally Action target mapping error [\#12](https://github.com/chutney-testing/chutney/pull/12) ([ledoyen](https://github.com/ledoyen))
- Allow use of external npm installation [\#11](https://github.com/chutney-testing/chutney/pull/11) ([ledoyen](https://github.com/ledoyen))
- Ignore IntelliJ root configuration files [\#10](https://github.com/chutney-testing/chutney/pull/10) ([ledoyen](https://github.com/ledoyen))
- chore: Add to Travis build JDK version and install command [\#4](https://github.com/chutney-testing/chutney/pull/4) ([nbrouand](https://github.com/nbrouand))
- Bump npm from 5.8.0 to 6.13.4 in /ui [\#2](https://github.com/chutney-testing/chutney/pull/2) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump amqp-client from 5.3.0 to 5.8.0 [\#1](https://github.com/chutney-testing/chutney/pull/1) ([dependabot[bot]](https://github.com/apps/dependabot))

# Before open source

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
