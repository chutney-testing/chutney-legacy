# <img src="ui/src/assets/logo/logo.svg" height="60" width="60"/> 
## Spice up your spec , Better `taste` your app !

[![Build Status](https://travis-ci.org/chutney-testing/chutney.svg?branch=master)](https://travis-ci.org/chutney-testing/chutney)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/559893368d134d729b204891e3ce0239)](https://www.codacy.com/gh/chutney-testing/chutney?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=chutney-testing/chutney&amp;utm_campaign=Badge_Grade)
[![Coverage Status](https://codecov.io/gh/chutney-testing/chutney/branch/master/graph/badge.svg)](https://codecov.io/gh/chutney-testing/chutney/)
[![FOSSA Status](https://app.fossa.com/api/projects/custom%2B8595%2Fgit%40github.com%3Achutney-testing%2Fchutney.git.svg?type=shield)](https://app.fossa.com/projects/custom%2B8595%2Fgit%40github.com%3Achutney-testing%2Fchutney.git?ref=badge_shield)
[![Download](https://api.bintray.com/packages/chutney-testing/maven/chutney/images/download.svg) ](https://bintray.com/chutney-testing/maven/chutney/_latestVersion)
[![Zulip chat](https://img.shields.io/badge/zulip-join_chat-brightgreen.svg)](https://chutney-testing.zulipchat.com/)

Chutney aims to test deployed software in order to validate functional requirements.

Chutney is released as a standalone application including a test execution engine, 
a web front end, and an edition server to create and edit your scenarios, consult test reports, and define your environments and test data.

Chutney scenarios are declarative. They provide functional requirements and technical details (needed for automation) in a single view. 

Those technical details are provided by generic [Tasks](https://github.com/chutney-testing/chutney/blob/master/task-spi/src/main/java/com/chutneytesting/task/spi/Task.java) such as HTTP, SOAP, AMQP, MongoDB, SQL, JMS, Kafka, Selenium, JSON, XML, etc.
Those Tasks are extensions and you can easily develop yours, even proprietary or non-generic one, and include them in your own release.

In addition, Chutney provide SpEL evaluation and extensible [Function](https://github.com/chutney-testing/chutney/blob/master/task-spi/src/main/java/com/chutneytesting/task/spi/SpelFunction.java) in order to ease the use of managing scenario data like JSON path or Date comparison.  


## Summary

* [Why another test tool ?](#why)
* [What is it not ?](#whatnot)
* [How to contribute ?](#contrib)
* [Team](#team)
    * [Contributors](#contributors)
* [Roadmap](#road)
    * [Project History](#story)
    * [En cours](#wip)
    * [Horizons](#horizon)


## <a name="why"></a> Why another test tool ?

Chutney is an opinionated test tool based upon the practice of Specification by Example. 

Chutney was inspired by Seb Rose blog post in which he revised the test pyramid according to test readability 
[The Testing Iceberg](http://claysnow.co.uk/the-testing-iceberg/)

Chutney is not exactly what Seb Rose meant by using this metaphore.

But we envisioned a tool allowing multiple levels of readability, providing a single place for business people, 
testers and developers to co-create, share and execute acceptance tests.

Moreover, we needed to :
* Promote and support Specification by Example across multiple teams and offices
* Ease collaboration and shared understanding in a "not so agile" environment
* Provide a single source of truth without hiding details in tests _glue code_
* Ease the automation of thousands of manual tests without writing and maintaining specific code
* Automate end-to-end tests of distributed software across secured networks, including hardware over telco networks 


-------------


## <a name="whatnot"></a> What is it not ?

__Chutney is not a replacement for tools like Cucumber, etc.__

While having some overlap, they all fill different test aspect.

The key difference is the absence of glue and support code.

While we think that having glue code is cumbersome and adds unnecessary levels of indirection between the features and the system under test,
especially for high level tests and distributed softwares.

We also do think that using Cucumber for low level testing is sometimes very handy and useful, 
thanks to the high level of expression provided by Gherkin (and this is part of the Testing Iceberg Seb Rose talked about).


__Chutney is no silver-bullet, it is just a tool which promotes and supports one way of doing software testing.__

As such, to benefit from it, we highly advise you to be proficient or to document yourself about 
Behaviour-Driven-Development (by Dan North), Specification by Example (by Gojko Adzic) and Living Documentation (by Cyrille Martraire).
All of which, however you call it, define the same practices and share the same goals.  

Global understanding of Test Driven Development and knowledge about Ubiquitous Language (from Domain Driven Design, by Eric Evans) 
is also valuable.


-------------


## <a name="contrib"></a> How to contribute ?

![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)

You don't need to be a developer to contribute, nor do much, you can simply:
* Enhance documentation,
* Correct a spelling,
* [Report a bug](#bug)
* [Ask a feature](#feat)
* Give us advices
* etc.

To help you start, we invite you to read:
* [Contributing](CONTRIBUTING.md), which gives you rules and code conventions to respect
* [Getting started](GETTING_STARTED.md), which document :
    * How to install and use Chutney as a User
    * How to install and setup the required environment for developing
* [Help Wanted](HELP_WANTED.md), if you wish to help us, but you don't know where to start, you might find some ideas in here !


To contribute to this documentation (README, CONTRIBUTING, etc.), we conforms to the [CommonMark Spec](https://spec.commonmark.org/)


-------------


## <a name="team"></a> Team

You can write to us at: https://chutney-testing.zulipchat.com/

Core contributors :

  * Loic Ledoyen
  * Mael Besson
  * Matthieu Gensollen
  * Nicolas Brouand

### <a name="contributors"></a> Contributors

We strive to provide a benevolent environment and support any [contribution](#contrib).

Before going open source, Chutney was inner-sourced and received contribution from 30 persons:
// TODO


-------------

## <a name="road"></a> Roadmap

### <a name="story"></a> Project history

Project history can be seen in :
* [Change log file](CHANGELOG.md)

#### <a name="state"></a> Current State

Chutney is production ready, and we use it everyday.
Chutney has been successfully applied to ease the automation of hundreds of manual end-to-end tests without writing code.


### <a name="horizon"></a> Horizons

Chutney original vision strive to ease the collaboration of the 3 amigos (business people, testers and developers).

Over the past 2 years Chutney has diverge from our initial goals due to contextual reasons, but we learnt a lot on the way.

On the horizon we want to provide a custom and seamless experience to each profile of the 3 amigos.


