# Traditional T9
Questo manuale spiega come configurare e utilizzare Traditional T9 in diversi scenari. Per le istruzioni di installazione e le informazioni sulle versioni "lite" e "full", consultare la [Guida all’installazione](https://github.com/sspanak/tt9/blob/master/docs/installation.md) su GitHub. Infine, è possibile dare un’occhiata alla [pagina principale del repository](https://github.com/sspanak/tt9), che include tutto il codice sorgente, una guida per sviluppatori, la politica sulla privacy e la documentazione supplementare.

## Configurazione iniziale
Dopo l’installazione, per prima cosa bisogna abilitare Traditional T9 come tastiera Android. Per farlo, cliccare sull'icona del launcher. Se è necessaria un'azione, tutte le opzioni tranne Configurazione Iniziale saranno disabilitate e apparirà l'etichetta: "TT9 è disabilitato". Andare su Configurazione Iniziale e abilitarlo.

_Se l'icona non appare subito dopo l’installazione, riavviare il telefono, e dovrebbe apparire. Ciò è dovuto al tentativo di Android di risparmiare batteria non aggiornando la lista delle app appena installate._

### Utilizzo su telefoni solo touchscreen
Sui dispositivi touchscreen, si consiglia vivamente di disabilitare il correttore ortografico di sistema. Non può essere usato quando si digita con i tasti numerici, quindi è possibile risparmiare batteria disabilitandolo.

Un altro problema è che potrebbe mostrare un fastidioso popup "Aggiungi Parola", che aggiunge parole alla tastiera di sistema predefinita (solitamente, Gboard) e non al dizionario di Traditional T9. Di nuovo, per evitare tali situazioni, il correttore ortografico di sistema deve essere disabilitato.

Se è necessario eseguire questo passaggio, l'opzione "Correttore ortografico di sistema" nella schermata Configurazione Iniziale sarà attiva. Cliccarci per disabilitare il componente di sistema. Se tale opzione non è presente, non è necessario fare altro.

Dopo aver completato la configurazione, dare un'occhiata alla sezione [Tastierino a Schermo](#tastierino-a-schermo) per ulteriori suggerimenti e trucchi.

### Abilitare la Modalità Predittiva
La Modalità Predittiva richiede che venga caricato un dizionario di lingua per fornire suggerimenti di parole. È possibile attivare le lingue abilitate e caricare i loro dizionari da Schermata Impostazioni → [Lingue](#opzioni-di-lingua). In caso di dimenticanza, Traditional T9 caricherà automaticamente il dizionario quando si inizia a digitare. Per ulteriori informazioni, [vedi sotto](#opzioni-di-lingua).

#### Note per telefoni di fascia bassa
Il caricamento del dizionario può saturare i telefoni di fascia bassa. Quando si usa la versione "lite" di TT9, ciò può causare l'interruzione dell’operazione da parte di Android. Se il caricamento dura più di 30 secondi, collegare il caricatore o assicurarsi che lo schermo rimanga acceso durante il caricamento.

È possibile evitare quanto sopra utilizzando invece la versione "full".

#### Note per Android 13 o versioni successive
Di default, le notifiche per le nuove app installate sono disabilitate. Si consiglia di abilitarle. In questo modo, si riceverà una notifica quando ci sono aggiornamenti del dizionario e, una volta scelto di installarli, TT9 mostrerà lo stato di caricamento. Gli aggiornamenti vengono rilasciati al massimo una volta al mese, quindi non ci si deve preoccupare di ricevere troppo spam.

È possibile abilitare le notifiche andando su Impostazioni → Lingue e attivando Notifiche del Dizionario.

_Se si decide di tenerle disattivate, TT9 continuerà a funzionare senza problemi, ma sarà necessario gestire i dizionari manualmente._

## Impostazioni
Nella schermata delle impostazioni, puoi scegliere le lingue per la digitazione, configurare i tasti rapidi del tastierino, cambiare l'aspetto dell'applicazione o migliorare la compatibilità con il telefono.

### Come accedere alle Impostazioni?

#### Metodo 1
Clicca sull'icona di avvio di Traditional T9.

#### Metodo 2 (usando un touchscreen)
- Tocca un campo di testo o numero per attivare TT9.
- Usa il pulsante a forma di ingranaggio su schermo.

#### Metodo 3 (usando una tastiera fisica)
- Inizia a digitare in un campo di testo o numero per attivare TT9.
- Apri l'elenco dei comandi utilizzando il pulsante degli strumenti su schermo o premendo il tasto assegnato [Predefinito: Tenere premuto ✱].
- Premi il tasto 2.

### Navigare nelle Impostazioni
Se hai un dispositivo con tastierino fisico, ci sono due modi per navigare nelle Impostazioni.

1. Usa i tasti Su/Giù per scorrere e OK per aprire o attivare un'opzione.
2. Premi i tasti da 1 a 9 per selezionare l'opzione corrispondente e premi due volte per aprirla/attivarla. La doppia pressione funziona ovunque tu sia nella schermata. Per esempio, anche se ti trovi in cima, premendo due volte il tasto 3 si attiverà la terza opzione. Infine, il tasto 0 è una comoda scorciatoia per scorrere fino alla fine ma non apre l'ultima opzione.

### Opzioni di Lingua

#### Caricamento di un Dizionario
Dopo aver abilitato una o più lingue nuove, devi caricare i rispettivi dizionari per la Modalità Predittiva. Una volta caricato, il dizionario resterà disponibile fino a quando non utilizzerai una delle opzioni "cancella". In questo modo, puoi abilitare e disabilitare le lingue senza dover ricaricare i dizionari ogni volta. Basta farlo una sola volta, solo la prima volta.

Significa anche che, se devi iniziare a usare la lingua X, puoi disabilitare tutte le altre lingue, caricare solo il dizionario X (risparmiando tempo!), e poi riattivare tutte le lingue che usavi prima.

Ricorda che ricaricare un dizionario reimposta la popolarità dei suggerimenti ai valori predefiniti. Tuttavia, non c’è nulla di cui preoccuparsi. Di solito, noterai poche o nessuna differenza nell'ordine dei suggerimenti, a meno che tu non usi spesso parole insolite.

#### Caricamento Automatico del Dizionario

Se salti o dimentichi di caricare un dizionario dalla schermata Impostazioni, il caricamento avverrà automaticamente quando apri un'applicazione dove puoi digitare e passi alla Modalità Predittiva. Verrà visualizzato un messaggio che ti chiederà di attendere il completamento del caricamento e poi potrai iniziare a digitare subito.

Se cancelli uno o più dizionari, NON verranno ricaricati automaticamente. Dovrai farlo manualmente. Verranno ricaricati automaticamente solo i dizionari delle nuove lingue abilitate.

#### Eliminazione di un Dizionario
Se smetti di usare le lingue X o Y, puoi disabilitarle e anche usare "Cancella Non Selezionati" per liberare spazio di archiviazione.

Per cancellare tutto, indipendentemente dalla selezione, usa "Cancella Tutto".

In tutti i casi, le parole aggiunte personalizzate verranno conservate e ripristinate una volta ricaricato il dizionario.

#### Parole Aggiunte
L’opzione "Esporta" consente di creare un file CSV con tutte le parole aggiunte per tutte le lingue. Successivamente, puoi utilizzare il file CSV per migliorare Traditional T9! Vai su GitHub e condividi le parole in una [nuova issue](https://github.com/sspanak/tt9/issues) o una [pull request](https://github.com/sspanak/tt9/pulls). Dopo la revisione e l’approvazione, saranno incluse nella prossima versione.

Con "Importa", puoi importare un CSV precedentemente esportato. Tuttavia, ci sono alcune restrizioni:
- È possibile importare solo parole composte da lettere. Apostrofi, trattini, altri segni di punteggiatura o caratteri speciali non sono ammessi.
- Gli emoji non sono ammessi.
- Un file CSV può contenere un massimo di 250 parole.
- È possibile importare fino a 1000 parole, quindi al massimo 4 file da 250 parole ciascuno. Oltre questo limite, è comunque possibile aggiungere parole durante la digitazione.

Con "Elimina", puoi cercare e cancellare parole errate o altre che non vuoi nel dizionario.

## Tasti Rapidi Hardware

Tutti i tasti rapidi possono essere riconfigurati o disabilitati da Impostazioni → Tastierino → Seleziona Tasti Rapidi.

### Tasti di Digitazione

#### Tasto Suggerimento Precedente (Default: D-pad Sinistra):
Seleziona il suggerimento di parola/lettera precedente.

#### Tasto Suggerimento Successivo (Default: D-pad Destra):
Seleziona il suggerimento di parola/lettera successivo.

#### Tasto Filtro Suggerimenti (Default: D-pad Su):
_Solo modalità predittiva._

- **Pressione singola**: Filtra l'elenco dei suggerimenti, lasciando solo quelli che iniziano con la parola corrente. Non importa se è una parola completa o meno. Ad esempio, digitare "remin" e premere Filtro. Lascerà tutte le parole che iniziano con "remin": "remin" stesso, "ricordare", "ricorda", "ricordato", "ricordando" e così via.
- **Pressione doppia**: Espandi il filtro fino al suggerimento completo. Ad esempio, digitare "remin" e premere Filtro due volte. Filtrerà prima "remin", quindi estenderà il filtro a "ricordare". Si può continuare a espandere il filtro fino a ottenere la parola più lunga del dizionario.

Il filtro è utile anche per digitare parole sconosciute. Diciamo che si vuole digitare "Anakin", che non è nel dizionario. Iniziare con "A", quindi premere Filtro per nascondere "B" e "C". Ora premere il tasto 6. Poiché il filtro è attivo, oltre alle parole reali del dizionario, fornirà tutte le combinazioni possibili per 1+6: "A..." + "m", "n", "o". Selezionare "n" e premere Filtro per confermare la selezione e produrre "An". Ora premendo il tasto 2, verranno visualizzati "An..." + "a", "b", e "c". Selezionare "a" e continuare fino a ottenere "Anakin".

Quando il filtro è abilitato, il testo di base diventa grassetto e in corsivo.

#### Tasto Cancella Filtro (Default: D-pad Giù):
_Solo modalità predittiva._

Cancella il filtro di suggerimento, se applicato.

#### D-pad Centro (OK o INVIO):
- Quando sono visualizzati i suggerimenti, digita il suggerimento attualmente selezionato.
- Altrimenti, esegue l'azione predefinita per l'applicazione corrente (ad esempio invia un messaggio, vai a un URL o digita una nuova riga).

_**Nota:** Ogni applicazione decide autonomamente cosa fare quando si preme OK e TT9 non ha alcun controllo su questo._

_**Nota 2:** Per inviare messaggi con OK nelle applicazioni di messaggistica, è necessario abilitare l'impostazione "Invia con INVIO" o una simile. Se l'applicazione non dispone di questa impostazione, probabilmente non supporta l'invio dei messaggi in questo modo. In tal caso, utilizzare l'app KeyMapper dal [Play Store](https://play.google.com/store/apps/details?id=io.github.sds100.keymapper) o da [F-droid](https://f-droid.org/packages/io.github.sds100.keymapper/). KeyMapper può rilevare le app di chat e simulare un tocco sul pulsante di invio del messaggio premendo o tenendo premuto un tasto hardware. Consultare la [guida rapida](https://docs.keymapper.club/quick-start/) per ulteriori informazioni._

#### Tasto 0:
- **In modalità 123:**
  - **Premere:** digita "0".
  - **Tenere premuto:** digita caratteri speciali/matematici.
- **In modalità ABC:**
  - **Premere:** digita spazio, nuova riga o caratteri speciali/matematici.
  - **Tenere premuto:** digita "0".
- **In modalità Predittiva:**
  - **Premere:** digita uno spazio, una nuova linea, "0" o caratteri speciali/matematici.
  - **Premere due volte:** digita il carattere assegnato nelle impostazioni della modalità predittiva. (Default: ".")
  - **Tenere premuto:** digita "0".
- **In modalità Cheonjiin (Coreano):**
  - **Premere:** digita "ㅇ" e "ㅁ".
  - **Tenere premuto:** digita uno spazio, una nuova linea, "0" o caratteri speciali/matematici.

#### Tasto 1:
- **In modalità 123:**
  - **Premere:** digita "1".
  - **Tenere premuto:** digita caratteri di punteggiatura
- **In modalità ABC:**
  - **Premere:** digita caratteri di punteggiatura
  - **Tenere premuto:** digita "1".
- **In modalità Predittiva:**
  - **Premere:** digita caratteri di punteggiatura
  - **Premere più volte:** digita emoji
  - **Tenere premuto:** digita "1".
- **In modalità Cheonjiin (Coreano):**
  - **Premere:** digita la vocale "ㅣ".
  - **Tenere premuto:** digita caratteri di punteggiatura.
  - **Tenere premuto, quindi premere:** digita emoji.

#### Tasti da 2 a 9:
- **In modalità 123:** digita il rispettivo numero
- **In modalità ABC e Predittiva:** digita una lettera o tenere premuto per digitare il numero corrispondente.

### Tasti Funzione

#### Tasto Aggiungi Parola:
Aggiunge una nuova parola al dizionario per la lingua corrente.

#### Tasto Backspace (Indietro, Canc, o Backspace):
Cancella semplicemente il testo.

Se il telefono ha un tasto dedicato "Canc" o "Cancella", non è necessario impostare nulla nelle Impostazioni, a meno che non si desideri avere un altro tasto Backspace. In questo caso, l'opzione vuota: "--" sarà pre-selezionata automaticamente.

Su telefoni che hanno un tasto "Elimina"/"Indietro" combinato, quel tasto sarà selezionato automaticamente. Tuttavia, è possibile assegnare la funzione "Backspace" a un altro tasto, così "Indietro" servirà solo per navigare.

_**NB:** Usare "Indietro" come backspace non funziona in tutte le applicazioni, in particolare Firefox, Spotify e Termux. Possono assumere il pieno controllo del tasto e ridefinirne la funzione, significando che farà ciò che i creatori dell’app hanno deciso. Purtroppo, non è possibile fare nulla, poiché "Indietro" ha un ruolo speciale in Android e il suo utilizzo è limitato dal sistema._

_**NB 2:** Tenendo premuto il tasto "Indietro" si attiverà sempre l'azione predefinita del sistema (cioè mostrare l’elenco delle applicazioni in esecuzione)._

_In questi casi, si potrebbe assegnare un altro tasto (tutti gli altri tasti sono completamente utilizzabili), oppure usare il tasto backspace su schermo._

#### Tasto Modalità di Input Successiva (Default: premere #):
Cicla tra le modalità di input (abc → Predittiva → 123).

_La modalità predittiva non è disponibile nei campi password._

_Nei campi a soli numeri, cambiare modalità non è possibile. In questi casi, il tasto torna alla sua funzione predefinita (cioè digita "#")._

#### Tasto Strumenti degli appunti:
Mostra il pannello degli strumenti degli appunti, che consente di selezionare, tagliare, copiare e incollare testo. È possibile chiudere il pannello premendo di nuovo il tasto "✱" o, nella maggior parte delle applicazioni, il tasto Indietro. Per maggiori dettagli, vedi [sotto](#strumenti-degli-appunti).

#### Tasto Lingua Successiva (Default: tenere premuto #):
Cambiare la lingua di digitazione quando sono state abilitate più lingue nelle impostazioni.

#### Tasto Seleziona Tastiera:
Apre la finestra di dialogo Cambia Tastiera di Android, dove è possibile selezionare una tastiera tra tutte quelle installate.

#### Tasto Maiusc (Default: premere ✱):
- **Quando si digita testo:** Passa tra maiuscole e minuscole.
- **Quando si digitano caratteri speciali con il tasto 0**: Mostra il gruppo di caratteri successivo.

#### Tasto Mostra Impostazioni:
Apre la schermata di configurazione Impostazioni. Qui è possibile scegliere le lingue per digitare, configurare i tasti rapidi del tastierino, cambiare l’aspetto dell’applicazione o migliorare la compatibilità con il proprio telefono.

#### Tasto Annulla:
Annulla l’ultima azione. Equivale a premere Ctrl+Z su un computer o Cmd+Z su un Mac.

_La cronologia delle azioni annullate è gestita dalle app, non da Traditional T9. Ciò significa che l’annullamento potrebbe non essere disponibile in tutte le app._

#### Tasto Ripristina:
Ripete l’ultima azione annullata. Equivale a premere Ctrl+Y o Ctrl+Shift+Z su un computer o Cmd+Y su un Mac.

_Come per Annulla, il comando Ripristina potrebbe non essere disponibile in tutte le app._

#### Tasto Input Vocale:
Attiva l'input vocale sui telefoni che lo supportano. Vedere [sotto](#input-vocale) per ulteriori informazioni.

#### Tasto Elenco Comandi / aka Tavolozza Comandi / (Default: tenere premuto ✱):
Mostra un elenco di tutti i comandi (o funzioni).

Molti telefoni hanno solo due o tre pulsanti "liberi" che possono essere usati come tasti rapidi. Tuttavia, Traditional T9 ha molte più funzioni, il che significa che semplicemente non c’è spazio per tutte sul tastierino. La Tavolozza Comandi risolve questo problema. Permette di invocare le funzioni aggiuntive (o comandi) usando combinazioni di tasti.

Di seguito è riportato un elenco dei comandi possibili:
- **Mostra la schermata delle impostazioni (Combinazione predefinita: tenere premuto ✱, tasto 1).** Uguale a premere [Mostra Impostazioni](#tasto-mostra-impostazioni).
- **Aggiungi una parola (Combinazione predefinita: tenere premuto ✱, tasto 2).** Uguale a premere [Aggiungi Parola](#tasto-aggiungi-parola).
- **Input Vocale (Combinazione predefinita: tenere premuto ✱, tasto 3).** Uguale a premere [Input Vocale](#tasto-input-vocale).
- **Annulla (Combinazione predefinita: tenere premuto ✱, tasto 4).** Uguale a premere [Tasto Annulla](#tasto-annulla).
- **Strumenti degli appunti (Combinazione predefinita: tenere premuto ✱, tasto 5).** Uguale a premere [Strumenti degli appunti](#tasto-strumenti-degli-appunti).
- **Ripristina (Combinazione predefinita: tenere premuto ✱, tasto 6).** Uguale a premere [Tasto Ripristina](#tasto-ripristina).
- **Seleziona una Tastiera Diversa (Combinazione predefinita: tenere premuto ✱, tasto 8).** Uguale a premere [Seleziona Tastiera](#tasto-seleziona-tastiera).

_Questo tasto non fa nulla quando il Layout dello Schermo è impostato su "Tastierino Virtuale" perché tutti i tasti per tutte le funzioni possibili sono già disponibili sullo schermo._

## Tastierino a Schermo
Sui dispositivi dotati esclusivamente di touchscreen è disponibile un tastierino a schermo completamente funzionale, attivato automaticamente. Se il dispositivo non viene rilevato come touchscreen, è possibile abilitarlo manualmente da Impostazioni → Aspetto → Layout a schermo selezionando «Tastierino numerico virtuale».

Sui dispositivi con touchscreen e tastiera hardware, i tasti a schermo possono essere disattivati per liberare spazio sullo schermo. Questa opzione è disponibile in Impostazioni → Aspetto.

Si consiglia inoltre di disattivare il comportamento speciale che associa il tasto «Indietro» alla funzione «Backspace», poiché risulta utile solo quando si utilizza una tastiera hardware. Questa impostazione viene solitamente gestita automaticamente. In caso contrario, accedere a Impostazioni → Tastierino → Seleziona tasti di scelta rapida → Tasto Backspace e selezionare l’opzione «--».

### Layout a schermo Retro e Modern
Sono disponibili due layout di tastierino virtuale: Retro e Modern.

Il layout Retro include un D-pad con un tasto OK centrale nella parte superiore e i tasti numerici sottostanti, ricordando da vicino le tastiere dei telefoni dei primi anni 2000. È adatto agli utenti che cercano un’esperienza tradizionale, ai dispositivi con schermi più piccoli e alle persone con pollici più grandi. Può inoltre risultare familiare a chi ha utilizzato applicazioni T9 ormai dismesse come Old Keyboard o Big Old Keyboard.

Il layout Modern mantiene l’aspetto e il comportamento standard di Android, utilizzando al contempo un layout di digitazione a 12 tasti. Presenta un blocco centrale di tasti numerici (0–9) per l’inserimento del testo, con tasti funzione come Maiusc, Backspace, cambio lingua e OK (Invio) disposti in colonne a sinistra e a destra.

### Panoramica dei tasti virtuali
Il tastierino a schermo funziona allo stesso modo di una tastiera fisica per telefoni. I tasti con una sola funzione mostrano un’etichetta o un’icona centrale. I tasti con una funzione aggiuntiva tramite pressione prolungata mostrano un’etichetta o un’icona secondaria nell’angolo in alto a destra.

#### Tasti 0–9
I tasti numerici vengono utilizzati per digitare parole e inserire cifre. Il layout Retro consente inoltre gesti di scorrimento verso sinistra e verso destra su alcuni tasti. Quando disponibili, tali funzioni sono indicate da icone nell’angolo inferiore sinistro o destro del tasto.

Nella versione Google Play, i gesti di scorrimento possono essere personalizzati o disattivati sia per il layout Retro sia per quello Modern. Questa configurazione è disponibile in Impostazioni → Tastierino → Funzioni dei tasti.

#### Tasti di testo personalizzati («!» e «?»)
Per impostazione predefinita, questi tasti inseriscono i rispettivi segni di punteggiatura. Nei campi di input numerici o telefonici, possono inserire caratteri alternativi come l’asterisco, il cancelletto o il punto decimale.

Nella versione Google Play, questi tasti possono essere personalizzati. È possibile modificare il carattere predefinito e assegnare azioni ai gesti di scorrimento verso l’alto, il basso, sinistra e destra. Questa configurazione è disponibile in Impostazioni → Tastierino → Funzioni dei tasti.

#### Tasto modalità di input
- **Pressione:** Scorre le modalità di input (abc → Predittivo → 123).
- **Pressione prolungata:** Cambia la lingua di digitazione quando sono abilitate più lingue dalle Impostazioni.
- **Scorrimento orizzontale:** Passa all’ultima tastiera utilizzata diversa da TT9.
- **Scorrimento verticale:** Apre la finestra di dialogo Android per il cambio tastiera, consentendo di selezionare tra tutte le tastiere installate.

Il tasto mostra una piccola icona a forma di globo quando sono state abilitate più lingue da Impostazioni → Lingue. L’icona indica che è possibile cambiare lingua tenendo premuto il tasto.

_Nel layout Retro, si trova in basso a destra._

_Nel layout Modern, si trova in basso a sinistra._

#### Backspace
Elimina i caratteri quando viene premuto. Quando Impostazioni → Tastierino → Eliminazione rapida è attivato, è possibile scorrere all’indietro per eliminare la parola precedente.

#### Tasto filtro
- **Pressione:** Filtra l’elenco dei suggerimenti. Vedere [sopra](#tasto-filtro-suggerimenti-default-d-pad-su) per il funzionamento del filtro delle parole.
- **Pressione prolungata:** Cancella il filtro, se attivo.

_Il tasto è disponibile solo nel layout Modern. Posizione: secondo tasto dall’alto._

_Il filtraggio è possibile solo in modalità Predittiva._

#### Strumenti appunti / Tasto input vocale
- **Pressione:** Apre le opzioni di copia, incolla e modifica del testo.
- **Pressione prolungata:** Attiva l’input vocale.

_Il tasto è disponibile solo nel layout Modern. Posizione: terzo tasto dall’alto._

#### Tasto OK
- **Pressione:** Equivale alla pressione del tasto INVIO sulle altre tastiere.

Il layout Retro consente inoltre di abilitare i gesti di scorrimento da Impostazioni → Aspetto → Tasti.

- **Scorrimento verso l’alto senza suggerimenti:** Sposta il cursore verso l’alto (equivalente a D-PAD su).
- **Scorrimento verso il basso senza suggerimenti:** Sposta il cursore verso il basso (equivalente a D-PAD giù).
- **Scorrimento verso l’alto con suggerimenti:** Filtra l’elenco dei suggerimenti. Vedere [sopra](#tasto-filtro-suggerimenti-default-d-pad-su).
- **Scorrimento verso il basso con suggerimenti:** Cancella il filtro dei suggerimenti.

### Ridimensionamento del Pannello Tastiera Durante la Digitazione
A volte potresti trovare che il Tastierino Virtuale occupi troppo spazio, impedendoti di vedere cosa stai digitando o alcuni elementi dell’app. Se è così, puoi ridimensionarlo tenendo premuto e trascinando il tasto Impostazioni/Tavolozza Comandi o trascinando la Barra di Stato (dove è visualizzata la lingua corrente o modalità di digitazione). Quando l’altezza diventa troppo ridotta, il layout cambierà automaticamente a "Tasti Funzione" o "Solo Lista Suggerimenti". Analogamente, ingrandendo il layout tornerà al "Tastierino Virtuale". È anche possibile fare doppio tap sulla barra di stato per minimizzare o massimizzare istantaneamente.

_Il ridimensionamento tramite doppio tocco è disattivato per impostazione predefinita. È possibile abilitarlo in: Impostazioni → Aspetto._

_Ridimensionare Traditional T9 ridimensiona anche l'applicazione corrente, ma questa operazione è molto impegnativa. Potrebbe causare sfarfallii o rallentamenti su molti telefoni, anche di fascia alta._

### Modificare l'Altezza dei Tasti
È anche possibile modificare l'altezza dei tasti su schermo. Vai su Impostazioni → Aspetto → Altezza Tasti su Schermo e regolala come desideri.

L’impostazione predefinita di 100% offre un buon equilibrio tra dimensioni dei tasti e spazio sullo schermo occupato. Se hai dita grandi, puoi aumentarla leggermente, mentre su uno schermo più grande, come un tablet, potresti ridurla.

_Se lo spazio disponibile sullo schermo è limitato, TT9 ignorerà questa impostazione e ridurrà automaticamente l’altezza, per lasciare spazio all'applicazione corrente._

## Strumenti degli appunti
Dal pannello degli strumenti degli appunti puoi selezionare, tagliare, copiare e incollare testo, come su una tastiera del computer. Per uscire dal pannello degli appunti, premi il tasto "✱" o il tasto Indietro (eccetto nei browser, in Spotify e in altre applicazioni). In alternativa, premi un tasto lettera sulla tastiera su schermo.

Di seguito è riportato un elenco dei possibili comandi di testo:
1. Seleziona il carattere precedente (come Shift+Freccia Sinistra su una tastiera per computer)
2. Seleziona nessuno
3. Seleziona il carattere successivo (come Shift+Freccia Destra)
4. Seleziona la parola precedente (come Ctrl+Shift+Freccia Sinistra)
5. Seleziona tutto
6. Seleziona la parola successiva (come Ctrl+Shift+Freccia Destra)
7. Taglia
8. Copia
9. Incolla

Per facilitare la modifica, sono attivi anche i tasti backspace, spazio e OK.

## Input Vocale
La funzione di inserimento vocale consente la conversione del parlato in testo, simile a Gboard. Come tutte le altre tastiere, Traditional T9 non esegue il riconoscimento vocale da solo, ma chiede al telefono di farlo.

_Il pulsante di inserimento vocale è nascosto sui dispositivi che non lo supportano._

### Dispositivi con Google
Nei dispositivi con i Servizi Google, TT9 utilizza l’infrastruttura di Google per convertire la voce in testo. Su Android 12 o versioni precedenti, è necessario essere connessi a una rete Wi-Fi o attivare i dati mobili affinché funzioni. Su Android 13 o versioni successive, TT9 può eseguire il riconoscimento vocale sia online che offline utilizzando i pacchetti lingua del dispositivo. Per l’uso offline, assicurati di scaricare tutte le lingue desiderate da: Impostazioni Android → Sistema → Riconoscimento sul dispositivo → Aggiungi una lingua.

_I pacchetti installati per Google Voice, altri assistenti vocali o tastiere potrebbero non funzionare con Traditional T9. È consigliato installare i pacchetti globali dallo schermo "Riconoscimento sul dispositivo"._

### Dispositivi senza Google
Nei dispositivi senza Google, se è presente un'app di assistente vocale o se la tastiera nativa supporta l’inserimento vocale, verrà utilizzata l’opzione disponibile. Tieni presente che questo metodo è molto meno efficace rispetto a Google. Non funziona in ambienti rumorosi e di solito riconosce solo frasi semplici come "apri calendario" o "riproduci musica".

### Altri dispositivi
Altri telefoni senza Google generalmente non supportano l’inserimento vocale. I telefoni cinesi non hanno capacità di riconoscimento vocale a causa delle politiche di sicurezza cinesi. Su questi dispositivi, è possibile abilitare il supporto vocale installando l’app di Google con nome pacchetto: "com.google.android.googlequicksearchbox". In alternativa, puoi provare a installare Google Go: "com.google.android.apps.searchlite".

## Risoluzione dei problemi
Per alcune applicazioni o dispositivi, è possibile abilitare opzioni speciali, che miglioreranno la compatibilità con Traditional T9. Puoi trovarle alla fine di ciascuna schermata di impostazioni, nella sezione Compatibilità.

### Metodo Alternativo di Scorrimento dei Suggerimenti
_In: Impostazioni → Aspetto._

Su alcuni dispositivi, in Modalità Predittiva, potrebbe non essere possibile scorrere la lista fino alla fine o potrebbe essere necessario scorrere avanti e indietro più volte prima che appaia l'ultimo suggerimento. Il problema si verifica a volte su Android 9 o versioni precedenti. Abilita l'opzione se riscontri questo problema.

### Sempre in Primo Piano
_In: Impostazioni → Aspetto._

Su alcuni telefoni, in particolare Sonim XP3plus (XP3900), Traditional T9 potrebbe non apparire quando inizi a digitare, oppure potrebbe essere parzialmente coperto dai tasti software. In altri casi, potrebbero apparire delle barre bianche attorno alla tastiera. Il problema può verificarsi in un'applicazione specifica o in tutte. Per evitarlo, abilita l'opzione "Sempre in Primo Piano".

### Spazio inferiore (orientamento verticale)
_In: Impostazioni → Aspetto._

Sui dispositivi Samsung con Android 15 o versioni successive, Traditional T9 potrebbe apparire troppo in basso sullo schermo. In tal caso, la barra di navigazione di sistema copre l’ultima riga della tastiera, rendendo i tasti inutilizzabili. Qualsiasi tentativo di digitare uno spazio, premere OK o cambiare la modalità di input provoca la chiusura della tastiera. Aumentare lo «Spazio inferiore» a 48 dp risolve il problema.

In altri casi, può comparire uno spazio vuoto non necessario sotto il blocco dei tasti. Riducendo lo «Spazio inferiore» a 0 dp tale spazio viene eliminato.

_Vedere il bug [#950](https://github.com/sspanak/tt9/issues/950) per ulteriori informazioni._

_In casi molto rari, anche dispositivi non Samsung possono presentare gli stessi problemi. Vedere [#755](https://github.com/sspanak/tt9/issues/755)._

### Protezione dalla Ripetizione dei Tasti
_In: Impostazioni → Tastierino._

I telefoni CAT S22 Flip e Qin F21 sono noti per i loro tastierini di bassa qualità, che si deteriorano rapidamente e iniziano a registrare più clic per una singola pressione. Potresti notarlo mentre scrivi o navighi nei menu del telefono.

Per i telefoni CAT, l'impostazione consigliata è di 50-75 ms. Per Qin F21, prova con 20-30 ms. Se il problema persiste, aumenta il valore leggermente, ma cerca di mantenerlo il più basso possibile.

_**Nota:** Più alto è il valore impostato, più lentamente dovrai digitare. TT9 ignorerà le pressioni dei tasti molto rapide._

_**Nota 2:** Oltre a ciò, i telefoni Qin potrebbero non rilevare le pressioni prolungate dei tasti. Sfortunatamente, in questo caso non c'è nulla che si possa fare._

### Mostra il testo in composizione
_In: Impostazioni → Tastierino._

Se hai problemi a digitare su Deezer o Smouldering Durtles perché i suggerimenti scompaiono rapidamente prima che tu possa vederli, disattiva questa opzione. Questo farà sì che la parola attuale rimanga nascosta fino a quando non premi OK o Spazio, o fino a quando non tocchi la lista dei suggerimenti.

Il problema si verifica perché Deezer e Smouldering Durtles a volte modificano il testo che digiti, causando un malfunzionamento di TT9.

### I pannelli degli sticker ed emoji su Telegram/Snapchat non si aprono
Questo accade se stai utilizzando uno dei layout di dimensioni ridotte. Al momento, non esiste una soluzione definitiva, ma puoi utilizzare la seguente procedura temporanea:
- Vai su Impostazioni → Aspetto e abilita il Tastierino su Schermo.
- Torna alla chat e clicca sul pulsante emoji o sticker. Ora appariranno.
- Puoi ora tornare alle impostazioni e disabilitare il tastierino su schermo. I pannelli degli emoji e degli sticker rimarranno accessibili fino a quando non riavvii l'app o il telefono.

### Traditional T9 non appare immediatamente in alcune applicazioni (solo per telefoni senza touchscreen)
Se hai aperto un'applicazione in cui puoi scrivere, ma TT9 non appare automaticamente, inizia semplicemente a digitare e apparirà. In alternativa, premere i tasti di scelta rapida per cambiare [la modalità di input](#tasto-modalità-di-input-successiva-default-premere) o [la lingua](#tasto-lingua-successiva-default-tenere-premuto) può anche far apparire TT9, quando è nascosto.

Su alcuni dispositivi, TT9 potrebbe rimanere invisibile, indipendentemente da ciò che fai. In questi casi, devi abilitare [Sempre in Primo Piano](#sempre-in-primo-piano).

**Spiegazione lunga.** Il motivo di questo problema è che Android è progettato principalmente per dispositivi touchscreen. Di conseguenza, si aspetta che tu tocchi il campo di testo/numero per mostrare la tastiera. È possibile far apparire TT9 senza questa conferma, ma poi, in alcuni casi, Android dimentica di nasconderlo quando dovrebbe. Ad esempio, potrebbe rimanere visibile dopo aver composto un numero di telefono o dopo aver inviato testo in un campo di ricerca.

Per questi motivi, per seguire gli standard attesi di Android, il controllo è nelle tue mani. Basta premere un tasto per "toccare" lo schermo e continuare a digitare.

### Sul Qin F21 Pro, tenendo premuto il tasto 2 o il tasto 8 si alza o abbassa il volume invece di digitare un numero
Per attenuare questo problema, vai su Impostazioni → Aspetto e abilita "Icona di Stato". TT9 dovrebbe rilevare il Qin F21 e abilitare le impostazioni automaticamente, ma in caso di fallimento del rilevamento automatico, o se hai disabilitato l'icona per qualche motivo, devi attivarla per consentire il corretto funzionamento di tutti i tasti.

**Spiegazione lunga.** Il Qin F21 Pro (e possibilmente anche il F22) ha un'applicazione di tasti rapidi che consente di assegnare le funzioni Volume Su e Volume Giù ai tasti numerici. Di default, il gestore dei tasti rapidi è abilitato, e tenendo premuto il tasto 2 il volume aumenta, mentre tenendo premuto l'8 il volume diminuisce. Tuttavia, quando non c'è un'icona di stato, il gestore presuppone che non sia attiva alcuna tastiera e regola il volume, anziché lasciare che Traditional T9 gestisca il tasto e digiti un numero. Quindi, abilitare l'icona bypassa semplicemente il gestore dei tasti rapidi e tutto funziona correttamente.

### Problemi generali sui telefoni Xiaomi
Xiaomi ha introdotto diverse autorizzazioni non standard sui loro telefoni, che impediscono il corretto funzionamento della tastiera virtuale su schermo di Traditional T9. Più precisamente, i tasti "Mostra Impostazioni" e "Aggiungi Parola" potrebbero non svolgere le rispettive funzioni. Per risolvere questo problema, devi concedere a TT9 i permessi di "Visualizza finestra pop-up" e "Visualizza finestra pop-up in esecuzione in background" dalle impostazioni del telefono. [Questa guida](https://parental-control.flashget.com/how-to-enable-display-pop-up-windows-while-running-in-the-background-on-flashget-kids-on-xiaomi) per un'altra applicazione spiega come fare.

È anche altamente raccomandato concedere l'autorizzazione per la "Notifica permanente". Questa è simile all'autorizzazione per le "Notifiche" introdotta in Android 13. Vedi [sopra](#note-per-android-13-o-versioni-successive) per ulteriori informazioni sul motivo per cui è necessaria.

_I problemi di Xiaomi sono stati discussi in [questa issue su GitHub](https://github.com/sspanak/tt9/issues/490)._

### L'Input Vocale impiega molto tempo per fermarsi
È [un problema noto](https://issuetracker.google.com/issues/158198432) su Android 10 che Google non ha mai risolto. Non è possibile attenuarlo dal lato TT9. Per fermare l'operazione di Input Vocale, resta in silenzio per qualche secondo. Android spegne il microfono automaticamente quando non rileva alcun suono.

### La mia app bancaria non accetta Traditional T9
Ciò non rappresenta un problema di TT9. Le banche spesso limitano l’uso di tastiere non standard o open source, poiché non intendono correre rischi e presumono che tali tastiere possano essere insicure. Alcuni istituti forniscono persino un proprio tastierino, arrivando a bloccare la tastiera standard di Google, Gboard. In questo caso, purtroppo, l’unica soluzione consiste nell’utilizzare la tastiera originale del dispositivo.

### La vibrazione non funziona (solo per dispositivi touchscreen)
Le opzioni di risparmio energetico, ottimizzazione e la funzione "Non disturbare" possono impedire la vibrazione. Controlla nelle Impostazioni di sistema del tuo dispositivo se una di queste opzioni è attivata. Su alcuni dispositivi, è possibile configurare l'ottimizzazione della batteria per ogni singola applicazione da Impostazioni di sistema → Applicazioni. Se il tuo dispositivo lo consente, disattiva l'ottimizzazione per TT9.

Un altro motivo per cui la vibrazione potrebbe non funzionare è che potrebbe essere disabilitata a livello di sistema. Controlla se il tuo dispositivo ha le opzioni "Vibrazione al tocco" o "Vibrazione alla pressione dei tasti" in Impostazioni di sistema → Accessibilità e attivale. I dispositivi Xiaomi e OnePlus offrono un controllo della vibrazione ancora più dettagliato. Assicurati che tutte le impostazioni pertinenti siano attivate.

Infine, la vibrazione non funziona in modo affidabile su alcuni dispositivi. Per risolvere il problema, sarebbero necessarie autorizzazioni aggiuntive e l’accesso a più funzioni del dispositivo. Tuttavia, poiché TT9 è una tastiera che mette la privacy al primo posto, non richiederà tali accessi.

## Domande Frequenti

### Perché non aggiungete la lingua X?
Mi piacerebbe molto, ma ho bisogno del tuo aiuto. Supportare più di 40 lingue da solo è impossibile. Poiché non parlo la tua lingua, è difficile per me trovare risorse affidabili online, e qui i madrelingua come te possono fare la differenza.
In realtà, oltre il 90% delle lingue presenti è stato aggiunto da o con l’aiuto di utenti appassionati.

Per aggiungere una nuova lingua, ho bisogno di una lista di parole corretta ortograficamente, preferibilmente tratta da una fonte ufficiale o accademica (ad esempio, “Grande Dizionario della Lingua X”). Queste liste garantiscono suggerimenti di qualità quando si scrive.

Se non esiste un dizionario del genere, puoi fornire una lista di parole disponibile gratuitamente. Idealmente dovrebbe contenere tra 300.000 e 500.000 parole, ma se la lingua ha molte flessioni (tempo, genere, numero, ecc.), potrebbero servire fino a un milione di parole.

### Nella lingua XYZ ci sono parole errate o mancanti. Perché non vengono corrette?
Come detto sopra, non parlo la tua lingua e potrei non accorgermi di questi errori. Ma con il tuo aiuto, possiamo correggerli e migliorare il dizionario per tutti.

### Non potete aggiungere la funzionalità X?
No.

Ognuno ha le proprie preferenze. Alcuni vogliono tasti più grandi, altri in un ordine diverso, alcuni vogliono un tasto di scelta rapida per digitare ".com" e altri sentono la mancanza del loro vecchio telefono o tastiera. Ma per favore, capisci che sto lavorando a questo progetto nel mio tempo libero e su base volontaria. È impossibile soddisfare migliaia di richieste diverse, alcune delle quali si contraddicono tra loro.

Henry Ford una volta disse: "Il cliente può avere l'auto di qualsiasi colore desideri, purché sia nera." Allo stesso modo, Traditional T9 è essenziale, efficace e gratuito, ma quello che vedi è quello che ottieni.

### Non potete renderlo più simile al mio dispositivo preferito (ad esempio Sony Ericsson, Xperia, Nokia C2, Samsung) o alla mia app di tastiera preferita?
No.

Traditional T9 non è pensato per essere un sostituto o un'app clone. Ha un design unico, ispirato principalmente al Nokia 3310 e 6303i. E sebbene catturi l'essenza dei classici, offre un'esperienza propria che non replica esattamente nessun dispositivo.

### Dovreste copiare TouchPal; era la migliore tastiera!
No. Vedere i punti precedenti.

TouchPal era una tastiera veloce e reattiva, con ampie possibilità di temi, personalizzazione e supporto multilingue. Era popolare intorno al 2015, quando la concorrenza era limitata. Tuttavia, non è mai stata una vera tastiera T9: il layout a 12 tasti era disponibile solo per alcune lingue ed era progettato esclusivamente per touchscreen.

Con il tempo, ha iniziato a perdere l’attenzione sull’aspetto più importante: la digitazione. Sono stati introdotti annunci pubblicitari, le richieste di autorizzazioni sono diventate aggressive e ha iniziato a raccogliere dati sensibili degli utenti. Alla fine, è stata rimossa dal Play Store.

Al contrario, la [filosofia](https://github.com/sspanak/tt9/?tab=readme-ov-file#-philosophy) di TT9 si basa sui principi dell’open source. Il codice sorgente e i dizionari sono disponibili pubblicamente e possono essere esaminati. La privacy degli utenti è rispettata fin dalla progettazione. I contributi della comunità hanno migliorato il progetto, includendo correzioni di bug, nuove lingue e traduzioni. Gli utenti possono inoltre creare le proprie versioni modificate.

TT9 non offre funzionalità come forme dei tasti personalizzabili, ma propone un layout pulito e leggibile, focalizzato su una digitazione efficiente. Non replica lo stile visivo di TouchPal, ma funziona su smartphone moderni con Android 16, su dispositivi con tastiera hardware di ispirazione nostalgica come Qin F21, Cat S22 Flip e Sonim XP3800, e persino su telecomandi TV.

Se non sei d’accordo o desideri spiegare il tuo punto di vista, partecipa alla [discussione aperta](https://github.com/sspanak/tt9/issues/647) su GitHub. Ricorda di mantenere un atteggiamento rispettoso. I messaggi d’odio non saranno tollerati.

### Android mi ha avvisato che la tastiera potrebbe raccogliere i miei dati personali, inclusi numeri di carte di credito e password
Si tratta di un avviso standard di Android che viene mostrato quando si installa e si attiva qualsiasi tastiera, non soltanto Traditional T9. Può essere certo che tutto ciò che digita rimane sul dispositivo. Il motore di digitazione è completamente open source; è quindi possibile esaminarne il codice su GitHub e verificare che la privacy sia adeguatamente tutelata.

_Qualora avesse ulteriori dubbi, La invitiamo a consultare l’Informativa sulla Privacy dell’app._

### Ho bisogno di usare un layout QWERTY (solo dispositivi touchscreen)
Traditional T9 è una tastiera T9 e, in quanto tale, non fornisce un layout simile al QWERTY.

Se stai ancora imparando a usare T9 e hai bisogno di tornare indietro occasionalmente, oppure trovi più conveniente digitare nuove parole usando QWERTY, scorri verso l'alto il tasto modalità di input per passare a una tastiera diversa. Vedi [panoramica dei tasti virtuali](#panoramica-dei-tasti-virtuali) per ulteriori informazioni.

La maggior parte delle altre tastiere permette di tornare a Traditional T9 tenendo premuta la barra spaziatrice o il tasto "cambia lingua". Controlla il rispettivo manuale per ulteriori informazioni.

### Non riesco a cambiare lingua su un telefono touchscreen
Innanzitutto, assicurati di aver abilitato tutte le lingue desiderate in Impostazioni → Lingue. Poi tieni premuto il [tasto modalità di input](#tasto-modalità-di-input) per cambiare lingua.

### Come aggiungere contrazioni come «I've» o «don't» al dizionario?
Tutte le contrazioni in tutte le lingue sono già disponibili come parole separate, quindi non è necessario aggiungere nulla. Questo garantisce la massima flessibilità: puoi combinare qualsiasi parola con qualsiasi contrazione e risparmiare molto spazio di archiviazione.

Ad esempio, puoi digitare 've premendo: 183; oppure 'll con: 155. Questo significa che "I'll" = 4155 e "we've" = 93183. Puoi anche scrivere termini come "google.com" premendo: 466453 (google) 1266 (.com).

Un esempio più complesso in francese: "Qu'est-ce que c'est" = 781 (qu'), 378123 (est-ce), 783 (que), 21378 (c'est).

_Un'eccezione importante alla regola sono "can't" e "don't" in inglese. In questi casi, 't non è una parola separata, ma puoi comunque digitarli come spiegato sopra._