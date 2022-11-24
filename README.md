# <img src="ui/src/assets/logo/logo.svg" width="400"/> 
## Spice up your spec , Better `taste` your app !

[![Build](https://github.com/chutney-testing/chutney/workflows/Build/badge.svg?branch=master)](https://github.com/chutney-testing/chutney/actions)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/559893368d134d729b204891e3ce0239)](https://www.codacy.com/gh/chutney-testing/chutney?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=chutney-testing/chutney&amp;utm_campaign=Badge_Grade)
[![Coverage Status](https://codecov.io/gh/chutney-testing/chutney/branch/master/graph/badge.svg)](https://codecov.io/gh/chutney-testing/chutney/)
[![FOSSA Status](https://app.fossa.com/api/projects/custom%2B8595%2Fgit%40github.com%3Achutney-testing%2Fchutney.git.svg?type=shield)](https://app.fossa.com/projects/custom%2B8595%2Fgit%40github.com%3Achutney-testing%2Fchutney.git?ref=badge_shield)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.chutneytesting/server/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.chutneytesting/server)
[![Zulip chat](https://img.shields.io/badge/zulip-join_chat-brightgreen.svg)](https://chutney-testing.zulipchat.com/)

Chutney aims to test deployed software in order to validate functional requirements.

Chutney is released as a standalone application including a test execution engine, 
a web front end, and an edition server to create and edit your scenarios, consult test reports, and define your environments and test data.

Chutney scenarios are declarative. They provide functional requirements and technical details (needed for automation) in a single view.

Those technical details are provided by generic [Actions](https://github.com/chutney-testing/chutney/blob/master/action-spi/src/main/java/com/chutneytesting/action/spi/Action.java) (such as HTTP, AMQP, MongoDB, Kafka, Selenium, etc.)  
Those Tasks are extensions, and you can easily develop yours, even proprietary or non-generic one, and include them in your own release.

In addition, Chutney provide SpEL evaluation and extensible [Function](https://github.com/chutney-testing/chutney/blob/master/action-spi/src/main/java/com/chutneytesting/action/spi/SpelFunction.java) in order to ease the use of managing scenario data like JSON path or Date comparison.

[Find out more in the documentation !](https://www.chutney-testing.com/)

Still asking yourself ["Why another test tool ?"](https://www.chutney-testing.com/concepts/)

-------------

## Summary

* [How to contribute ?](#contrib)
* [Support](#support)
* [Team](#team)
    * [Contributors](#contributors)
* [Roadmap](#road)
    * [Project History](#story)
    * [Horizons](#horizon)
* [Architecture Overview](#archi)

-------------

## <a name="contrib"></a> How to contribute ?

![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)

You don't need to be a developer to contribute, nor do much, you can simply:
* Enhance documentation,
* Correct a spelling,
* [Report a bug](https://github.com/chutney-testing/chutney/issues/new/choose)
* [Ask a feature](https://github.com/chutney-testing/chutney/issues/new/choose)
* [Give us advices or ideas](https://github.com/chutney-testing/chutney/discussions/categories/ideas),
* etc.

To help you start, we invite you to read:
* [Contributing](CONTRIBUTING.md), which gives you rules and code conventions to respect
* [Getting started](GETTING_STARTED.md), which document :
    * How to install and use Chutney as a User
    * How to install and setup the required environment for developing
* [Help Wanted](HELP_WANTED.md), if you wish to help us, but you don't know where to start, you might find some ideas in here !

To contribute to this documentation (README, CONTRIBUTING, etc.), we conforms to the [CommonMark Spec](https://spec.commonmark.org/)

## <a name="support"></a> Support

We’re using [Discussions](https://github.com/chutney-testing/chutney/discussions) as a place to connect with members of our - slow pace growing - community. We hope that you:
  * Ask questions you’re wondering about,
  * Share ideas,
  * Engage with other community members,
  * Welcome others, be friendly and open-minded !

For a more informal place to chat, if you worry about feeling dumb in the open on Github or feel uncomfortable with English, we can meet on [Zulip](https://chutney-testing.zulipchat.com/) through public or private messages. We will be happy to chat either in English, French, Spanish or Italian as much as we can ! :) https://chutney-testing.zulipchat.com/

## <a name="team"></a> Team

Core contributors :
  * [Mael Besson](https://github.com/bessonm)
  * [Nicolas Brouand](https://github.com/nbrouand)
  * [Matthieu Gensollen](https://github.com/boddissattva)
  * [Karim Goubbaa](https://github.com/KarimGl)
  * [Loic Ledoyen](https://github.com/ledoyen)

### <a name="contributors"></a> Contributors

We strive to provide a benevolent environment and support any [contribution](#contrib).

Before going open source, Chutney was inner-sourced and received contribution from over 30 persons

## <a name="road"></a> Roadmap

### <a name="story"></a> Project history

Project history can be found in the [change log file](CHANGELOG.md)

#### <a name="state"></a> Current State

Chutney is production ready, and we use it every day.  
Chutney has been successfully applied to ease the automation of hundreds of manual end-to-end tests without writing code.

### <a name="horizon"></a> Horizons

Chutney original vision strive to ease the collaboration of the 3 amigos (business people, testers and developers).  
Over the past 2 years Chutney has diverged from our initial goals due to contextual reasons, but we learnt a lot on the way.

On the horizon we want to provide a custom and seamless experience to each profile of the 3 amigos. This is still a **work in progress**

## <a name="archi"></a> Architecture Overview

![archi](https://user-images.githubusercontent.com/7816908/132717205-bb092328-e639-4a80-90d4-f1cdabe7e8a4.jpg)
