# language: en

Feature: Simple parser feature
    Could execute simple actions, i.e. ones without target, strategy, inputs nor outputs

    Scenario: Simple Success/Debug
        Given Do: debug
        And Run success direct success
        When something is good
            Execute debug substep debug
        And it is very good
            Run success
        Then it is very good
            it is very good
                Do debug
        And it is very good
            it is very good
                Run! success subsubstep success

    Scenario: Deep SubStep fail
        When There is a first step
            I'm a sub step
                I'm a sub sub step
                    Run fail
