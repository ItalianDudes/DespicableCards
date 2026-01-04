# DespicableCards
L'ennesimo gioco da tavolo dove potrai rivalutare la tua cerchia di amici e la loro morale, e probabilmente anche la tua.
Contiene carte immorali, discutibili, decisamente non politically-correct, e adesso anche in digitale!
Altamente sconsigliato per le persone noiose.
Altamente consigliato per le persone con un umorismo discutibile.
L'applicazione richiede Java 21.
NOTA: Il gioco al momento non dispone di un pacchetto di carte pubblico, per cui sarà necessario crearsene uno proprio!

## Pacchetti di Carte
I pacchetti di carte sono dei file database SQLite3.
I pacchetti validi inseriti nella cartella "despicable_dbs" (auto-generata al primo tentativo di hosting nella stessa cartella del jar del gioco) verranno letti e caricati.
I pacchetti non validi verranno segnalati nei log dell'applicazioni (trovabili nella cartella auto-generata "logs" all'avvio dell'app nella stessa cartella del jar del gioco).
Lo schema per creare manualmente un database per un pacchetto di carte valido si trova in "src/main/resources/it/italiandudes/despicable_cards/resources/sql/database.sql".
