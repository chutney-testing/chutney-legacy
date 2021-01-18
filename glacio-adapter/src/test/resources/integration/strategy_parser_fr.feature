# language: fr

Fonctionnalité: Parser de stratégie localisé

    Scénario: Echoue doucement avec relance
        Quand une étape échoue (doucement:)
            Fait fail (relance: toutes les 5 ms pendant 15 ms)
        Alors le scénario continue tout de même
            Fait success
