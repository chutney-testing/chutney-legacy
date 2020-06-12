# Contributing

## Summary

* [How to contribute to the documentation](#doc)
* [How to make a Pull Request](#pr)
* [Code convention](#code)
* [Test convention](#test)
* [Branch convention](#branch)
* [Commit message](#commit)
* [Dependency management](#dep)
* [Build Process](#build)
* [Release Management](#release)
* [Add a new Task](#task)
* [Licensing](#oss)


## <a name="doc"></a> How to contribute to the documentation

To contribute to this documentation (README, CONTRIBUTING, etc.), we conforms to the [CommonMark Spec](http://spec.commonmark.org/0.27/)

* [https://www.makeareadme.com/#suggestions-for-a-good-readme](https://www.makeareadme.com/#suggestions-for-a-good-readme)
* [https://help.github.com/en/articles/setting-guidelines-for-repository-contributors](https://help.github.com/en/articles/setting-guidelines-for-repository-contributors)


## <a name="pr"></a> How to make a Pull Request

1. Fork the repository and keep active sync on our repo
2. Create your working branches as you like
   * **WARNING** - Do not modify the master branch nor any of our branches since it will break the automatic sync
3. When you are done, fetch all and rebase your branch onto our master or any other of ours
   * ex. on your branch, do : 
     * `git fetch --all --prune`
     * `git rebase --no-ff origin/master`
4. Test your changes and make sure everything is working
5. Submit your Pull Request
   * Do not forget to add reviewers ! Check out the last authors of the code you modified and add them.
   * In case of doubts, here are active contributors :

     
## <a name="code"></a> Code convention

### Naming

Whenever an acronym is included as part of a type name or method name, keep the first
letter of the acronym uppercase and use lowercase for the rest of the acronym. Otherwise,
it becomes _impossible_ to perform camel-cased searches in IDEs, and it becomes
potentially very difficult for mere humans to read or reason about the element without
reading documentation (if documentation even exists).

Consider for example a use case needing to support an HTTP URL. Calling the method
`getHTTPURL()` is absolutely horrible in terms of usability; whereas, `getHttpUrl()` is
great in terms of usability. The same applies for types `HTTPURLProvider` vs
`HttpUrlProvider`, etc.

Whenever an acronym is included as part of a field name or parameter name:

* If the acronym comes at the start of the field or parameter name, use lowercase for the
  entire acronym
  * for example, `String url;`.
  
* Otherwise, keep the first letter of the acronym uppercase and use lowercase for the
  rest of the acronym
  * for example, `String defaultUrl;`.

### Formatting

We use an [.editorconfig file](http://editorconfig.org/), please use a tool accepting it and do not override rules

#### Imports

It is forbidden to use _wildcard imports_ (e.g., `import static org.assertj.core.api.Assertions.*;`) in Java code.

##### Ordering rules

* import static _all other imports_
* blank line
* import _all other imports_
* blank line

### Javadoc

* Javadoc comments should be wrapped after 80 characters whenever possible.
* This first paragraph must be a single, concise sentence that ends with a period (".").
* Place `<p>` on the same line as the first line in a new paragraph and precede `<p>` with a blank line.
* Insert a blank line before at-clauses/tags.
* Favor `{@code foo}` over `<code>foo</code>`.
* Favor literals (e.g., `{@literal @}`) over HTML entities.
* Use `@since 5.0` instead of `@since 5.0.0`.

### Packages

Adding a new task capabilities should be packaged in a maven submodule named chutney-task-\[task-name\]

## <a name="test"></a> Test convention

### Naming

* All test classes must end with a `Test` suffix.
* Example test classes that should not be picked up by the build must end with a `TestCase` suffix.

### Assertions

* Use `org.assertj.core.api.Assertions` wherever possible.
* Use `org.junit.Assert` if sufficient.

### Mocking

* Use either Mockito or hand-written test doubles.
* Use `org.springframework.test.web.servlet.MockMvc` to mock REST HTTP endpoints
* **Do not use PowerMock**
  * We consider it to be sign of a code-smell
  
## <a name="branch"></a> Branch convention

* **wip/** unstable code, to share between developers working on the same task
* **feat/** stable code of new feature, to be merged if validated
* **bugfix/** stable code of correction (PROD / VALID)
* **tech/** stable code, purely technical modification like refactoring, log level change or documentation


## <a name="commit"></a> Commit message

As a general rule, the style and formatting of commit messages should follow the guidelines in
[How to Write a Git Commit Message](http://chris.beams.io/posts/git-commit/).

* Separate subject from body with a blank line
* Limit the subject line to 50 characters
* Capitalize the subject line
* Do not end the subject line with a period
* Use the imperative mode in the subject line
* Wrap the body at 72 characters
* Use the body to explain what and why vs. how


**Alternative**:

* http://karma-runner.github.io/0.10/dev/git-commit-msg.html


## <a name="build"></a> Build Process

We use travis to build and release Chutney.
[![Build Status](https://travis-ci.org/chutney-testing/chutney.svg?branch=master)](https://travis-ci.org/chutney-testing/chutney)

## <a name="release"></a> Release Management

### Update Changelog file

Do it first, because changelog updates should be part of the release being made

- Install [github-changelog-generator](https://github.com/github-changelog-generator/github-changelog-generator#installation)
- Generate the changelog with https://github.com/github-changelog-generator/github-changelog-generator

```shell
github_changelog_generator -u chutney-testing -p chutney --token <YOUR_TOKEN> --since-tag <previous RELEASE_VERSION>
```
- Copy-paste the generated content and use it to update [CHANGELOG.md](https://github.com/chutney-testing/chutney/blob/master/CHANGELOG.md)

### Releasing

```shell
  mvn versions:set -DnewVersion=<RELEASE_VERSION> -DgenerateBackupPoms=false && mvn versions:set-scm-tag -DnewTag=<RELEASE_VERSION> -DgenerateBackupPoms=false
  git diff HEAD
  git add . && git commit -m "chore: Release <RELEASE_VERSION>"
  git push origin
  git tag <TAG_VERSION>
  git push origin <TAG_VERSION>
```

### Prepare next development

```shell
  mvn versions:set -DnewVersion=<NEXT_DEV_VERSION> -DgenerateBackupPoms=false && mvn versions:set-scm-tag -DnewTag=HEAD -DgenerateBackupPoms=false
  git diff HEAD
  git add . && git commit -m "chore: Prepare next development <NEXT_DEV_VERSION>"
  git push origin
```

### Update Github release

- Update [Release <RELEASE_VERSION>](https://github.com/chutney-testing/chutney/releases)

## <a name="task"></a> Adding a task

 Create a new maven module with _chutney-parent_ as parent.
  And name your module such as _chutney-task-\[task-name\]_
  ```xml
  <parent>
      <artifactId>chutney-parent</artifactId>
      <groupId>com.chutneytesting</groupId>
  </parent>
  ```

* Add a dependency on _chutney-task-spi_
  * Transitive dependencies are :
    * com.google.guava

  ```xml
  <dependency>
      <groupId>com.chutneytesting</groupId>
      <artifactId>task-spi</artifactId>
  </dependency>
  ```


* Create a class which implements `com.chutneytesting.task.spi.Task` interface
* Name your task in CamelCase. It will be converted in spinal-case such as `camel-case` for use in 
when writing scenarios and to tell the engine which task to pick for execution

* Create a constructor with your task parameters annoted with `com.chutneytesting.task.spi.injectable.Input`
  * You can also use `com.chutneytesting.task.spi.injectable.Target` and `com.chutneytesting.task.spi.injectable.Logger`

* Override the `execute()` method
  * This is where your task logic starts

* Feel free to decoupled your code and add any other classes, entities and services up to your needs

* Add a file `META-INF/extension/chutney.tasks`
  * Add the canonical class name of your Task implementation, ex. `com.chutneytesting.task.implementation.http.HttpClientTask`
  * If you have multiple `Task` implementations, add them one by line

* When you are done, in order to use your newly created task, add it as a dependency to your _packaging_ module and build it

## <a name="oss"></a> Licensing

We choose to apply the Apache License 2.0 (ALv2) : [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

As for any project, license compatibility issues may arise and should be taken care of.

Concrete instructions and tooling to keep Chutney ALv2 compliant and limit licensing issues are to be found below.

However, we acknowledge topic's complexity, mistakes might be done and we might not get it 100% right.

Still, we strive to be compliant and be fair, meaning, we do our best in good faith.

As such, we welcome any advice and change request.


To any contributor, we strongly recommend further reading and personal research :
* [http://www.apache.org/licenses/](http://www.apache.org/licenses/)
* [http://www.apache.org/legal/](http://www.apache.org/legal/)
* [http://apache.org/legal/resolved.html](http://apache.org/legal/resolved.html)
* [http://www.apache.org/dev/apply-license.html](http://www.apache.org/dev/apply-license.html)
* [http://www.apache.org/legal/src-headers.html](http://apache.org/legal/src-headers.html)
* [http://www.apache.org/legal/release-policy.html](http://www.apache.org/legal/release-policy.html)
* [http://www.apache.org/dev/licensing-howto.html](http://www.apache.org/dev/licensing-howto.html)

* [Why is LGPL not allowed](https://issues.apache.org/jira/browse/LEGAL-192)
* https://issues.apache.org/jira/projects/LEGAL/issues/

* General news : [https://opensource.com/tags/law](https://opensource.com/tags/law)

### How to manage license compatibility

When adding a new dependency, **one should check its license and all its transitive dependencies** licenses.

ALv2 license compatibility as defined by the ASF can be found here : [http://apache.org/legal/resolved.html](http://apache.org/legal/resolved.html)

3 categories are defined :
   * [Category A](https://www.apache.org/legal/resolved.html#category-a) : Contains all compatibles licenses.
   * [Category B](https://www.apache.org/legal/resolved.html#category-b) : Contains compatibles licenses under certain conditions.
   * [Category X](https://www.apache.org/legal/resolved.html#category-x) : Contains all incompatibles licenses which must be avoid at all cost.

__As far as we understand :__

If, by any mean, your contribution should rely on a Category X dependency, then you must provide a way to modularize it 
and make it's use optional to Chutney, as a plugin.

You may distribute your plugin under the terms of the Category X license.

Any distribution of Chutney bundled with your plugin will probably be done under the terms of the Category X license.

But _"you can provide the user with instructions on how to obtain and install the non-included"_ plugin.

__References :__
- [Optional](https://www.apache.org/legal/resolved.html#optional)
- [Prohibited](https://www.apache.org/legal/resolved.html#prohibited)

### How to comply with Redistribution and Attribution clauses

Lots of licenses place conditions on redistribution and attribution, including ALv2.

__References :__
* http://mail-archives.apache.org/mod_mbox/www-legal-discuss/201502.mbox/%3CCAAS6%3D7gzsAYZMT5mar_nfy9egXB1t3HendDQRMUpkA6dqvhr7w%40mail.gmail.com%3E
* http://mail-archives.apache.org/mod_mbox/www-legal-discuss/201501.mbox/%3CCAAS6%3D7jJoJMkzMRpSdJ6kAVSZCvSfC5aRD0eMyGzP_rzWyE73Q%40mail.gmail.com%3E

#### LICENSE file
##### In Source distribution

This file contains :
* the complete ALv2 license.
* list dependencies and points to their respective license file
  * Example :
    _This product bundles SuperWidget 1.2.3, which is available under a
    "3-clause BSD" license.  For details, see deps/superwidget/_
* do not list dependencies under the ALv2

#### NOTICE file

##### In source distribution

_The NOTICE file is not for conveying information to downstream consumers -- it
is a way to *compel* downstream consumers to *relay* certain required notices._

#### Examples

_Apache Lens:_
* [bin-dist-files](https://git-wip-us.apache.org/repos/asf?p=lens.git;a=tree;f=bin-dist-files;h=0aac54867841dc8644c9d03ee4c8e1ab78db6070;hb=HEAD)
* [Licensing](https://cwiki.apache.org/confluence/display/LENS/Licensing+in+Apache+Lens)

_Apereo:_
* https://www.apereo.org/licensing
* https://www.apereo.org/licensing/practices

_Docassemble:_
* https://docassemble.org/docs/license.html

### Tooling

#### nexB
* https://github.com/nexB

nexB provides a set of tools to manage licenses and dependencies.

#### Licensed
* https://github.com/github/licensed
  * based on https://github.com/benbalter/licensee
* https://githubengineering.com/improving-your-oss-dependency-workflow-with-licensed/

Licensed helps to alert when a dependency license needs review

#### license-maven-plugin

* http://www.mojohaus.org/license-maven-plugin/index.html

_This plugin helps to :_
* generate LICENCE file,
* eventually generate NOTICE file (third-party-licenses).
* bundle external licenses
* generate license headers

#### maven-notice-plugin

* https://github.com/Jasig/maven-notice-plugin

_This plugin helps to :_
* generate NOTICE file (third-party-licenses).

#### Maven-License-Verifier-Plugin

* https://github.com/AyoyAB/Ayoy-Maven-License-Verifier-Plugin

_This plugin helps to :_
* fail build if licensing requirements are not met

Requirements are based on 4 license categories :
* Forbidden
* Valid
* Missing
* Unknown

**Usage example**

Valid and Forbidden licenses are defined in _licenses.xml_ based on license name.
Multiples names can be used to match a license.

Dependencies missing license are accepted by adding them in xml _allowedMissingLicense.xml_ 
based on groupId and artifactId of the dependency.

Plugin goal can be used on the command line : 
```text
mvn se.ayoy.maven-plugins:ayoy-license-verifier-maven-plugin:verify
```

Or in a pom file :
**Parent pom**
```xml
<plugin>
    <groupId>se.ayoy.maven-plugins</groupId>
    <artifactId>ayoy-license-verifier-maven-plugin</artifactId>
    <version>1.0.5</version>
    <executions>
        <execution>
            <phase>compile</phase>
            <goals>
                <goal>verify</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <licenseFile>${project.parent.basedir}/licenses.xml</licenseFile>
        <excludedMissingLicensesFile>${project.parent.basedir}/allowedMissingLicense.xml</excludedMissingLicensesFile>
        <failOnForbidden>false</failOnForbidden>
        <failOnMissing>false</failOnMissing>
        <failOnUnknown>false</failOnUnknown>
    </configuration>
</plugin>
```
**Child pom**

```xml
<plugin>
    <groupId>se.ayoy.maven-plugins</groupId>
    <artifactId>ayoy-license-verifier-maven-plugin</artifactId>
</plugin>
```

#### license-compatibility-checker

* https://github.com/HansHammel/license-compatibility-checker

Helps to check dependencies licenses present in node_modules.
It outputs compatibility level with the current project, based on a matrix.

#### tldrlegal

* https://github.com/eladnava/tldrlegal
  * based on https://github.com/franciscop/legally/

Helps to give an overview of dependencies licenses.

#### apache2-license-checker

* https://github.com/bbc/apache2-license-checker

Helps to give an overview of dependencies licenses and compatibility with ALv2.

### Unresolved questions - HELP WANTED -
* Should test dependencies be taken into account for source distribution ?
  * It appears to be YES
* Should build time dependencies be taken into account ?
  * It appears to be NO but might depend on the actual stuff done by this dependency
