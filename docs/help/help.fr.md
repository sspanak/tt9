# Traditional T9
Ce manuel explique comment configurer et utiliser Traditional T9 dans différents scénarios. Pour les instructions d'installation et des informations sur les versions "lite" et "complète", consultez le [guide d'installation](https://github.com/sspanak/tt9/blob/master/docs/installation.md) sur GitHub. Enfin, vous pouvez également consulter la [page principale du dépôt](https://github.com/sspanak/tt9), qui inclut tout le code source, un guide du développeur, la politique de confidentialité, et la documentation supplémentaire.

## Configuration initiale
Après l'installation, il est d'abord nécessaire d'activer Traditional T9 comme clavier Android. Pour cela, cliquez sur l'icône de l'application. Si une action est nécessaire, toutes les options à l'exception de "Configuration initiale" seront désactivées, et une étiquette "TT9 est désactivé" sera affichée. Allez dans "Configuration initiale" et activez-le.

_Si l'icône n'apparaît pas immédiatement après l'installation, redémarrez votre téléphone et elle devrait apparaître. Cela est dû à Android qui essaie d'économiser de l'énergie en ne rafraîchissant pas la liste des applications nouvellement installées._

### Utilisation sur un téléphone à écran tactile uniquement
Sur les appareils à écran tactile, il est fortement recommandé de désactiver le correcteur orthographique du système. Il ne peut pas être utilisé lors de la saisie avec les touches numériques, ce qui vous permet de prolonger l'autonomie de la batterie en le désactivant.

Un autre problème est que le correcteur orthographique peut afficher une fenêtre contextuelle "Ajouter un mot" qui ajoute des mots au clavier système par défaut (souvent Gboard) et non au dictionnaire de Traditional T9. Pour éviter cela, le correcteur orthographique du système doit être désactivé.

Si vous devez effectuer cette étape, l'élément "Correcteur orthographique système" dans l'écran de configuration initiale sera activé. Cliquez dessus pour désactiver le composant système. Si cet élément n'est pas visible, vous n'avez rien d'autre à faire.

Une fois la configuration terminée, consultez la section [Pavé numérique à l’écran](#pavé-numérique-à-l-écran) pour des astuces supplémentaires.

### Activation du Mode Prédictif
Le Mode Prédictif nécessite le chargement d’un dictionnaire linguistique pour proposer des suggestions de mots. Vous pouvez activer les langues et charger leurs dictionnaires depuis Écran de Paramètres → [Langues](#options-de-langue). Si vous oubliez de charger un dictionnaire, Traditional T9 le fera automatiquement au début de la saisie. Pour plus d'informations, [voir ci-dessous](#options-de-langue).

#### Remarques pour les téléphones bas de gamme
Le chargement du dictionnaire peut saturer les téléphones bas de gamme. Avec la version "lite" de TT9, cela peut entraîner l'abandon de l'opération par Android. Si le chargement prend plus de 30 secondes, branchez le chargeur ou assurez-vous que l’écran reste allumé pendant le chargement.

Vous pouvez éviter cela en utilisant la version "complète" à la place.

#### Remarques pour Android 13 ou version supérieure
Par défaut, les notifications pour les applications nouvellement installées sont désactivées. Il est recommandé de les activer. Cela vous permet d’être informé des mises à jour des dictionnaires, et une fois que vous choisissez de les installer, TT9 affichera la progression du chargement. Les nouvelles mises à jour sont publiées au maximum une fois par mois, vous n’avez donc pas à craindre un excès de notifications.

Vous pouvez activer les notifications en allant dans Paramètres → Langues et en activant Notifications de Dictionnaire.

_Si vous choisissez de les garder désactivées, TT9 continuera de fonctionner sans problème, mais vous devrez gérer manuellement les dictionnaires._

## Paramètres
Sur l’écran des paramètres, vous pouvez choisir les langues de saisie, configurer les touches de raccourci du pavé numérique, changer l'apparence de l'application ou améliorer la compatibilité avec votre téléphone.

### Comment accéder aux paramètres ?

#### Méthode 1
Cliquez sur l'icône de lancement de Traditional T9.

#### Méthode 2 (avec un écran tactile)
- Appuyez sur un champ de texte ou de numéro pour activer TT9.
- Utilisez le bouton d'engrenage à l'écran.

#### Méthode 3 (avec un clavier physique)
- Commencez à taper dans un champ de texte ou de numéro pour activer TT9.
- Ouvrez la liste de commandes en utilisant le bouton d'outils à l'écran ou en appuyant sur la touche de raccourci attribuée [par défaut : Maintenez ✱].
- Appuyez sur la touche 2.

### Naviguer dans les paramètres
Si vous disposez d'un clavier matériel, il existe deux manières de naviguer dans les paramètres.

1. Utilisez les touches Haut/Bas pour faire défiler et OK pour ouvrir ou activer une option.
2. Appuyez sur les touches de 1 à 9 pour sélectionner l'option correspondante et appuyez deux fois dessus pour l’ouvrir/activer. L'appui double fonctionne peu importe où vous êtes à l'écran. Par exemple, même si vous êtes en haut, appuyer deux fois sur la touche 3 activera la troisième option. Enfin, la touche 0 est un raccourci pratique pour faire défiler jusqu'à la fin mais n'ouvre pas la dernière option.

### Options de langue

#### Chargement d'un dictionnaire
Après avoir activé une ou plusieurs nouvelles langues, vous devez charger les dictionnaires correspondants pour le mode prédictif. Une fois un dictionnaire chargé, il restera jusqu'à ce que vous utilisiez l'une des options de « suppression ». Cela signifie que vous pouvez activer et désactiver les langues sans recharger leurs dictionnaires à chaque fois, seulement une fois au début.

Cela signifie aussi que si vous devez utiliser la langue X, vous pouvez désactiver toutes les autres langues, charger uniquement le dictionnaire X (et gagner du temps !), puis réactiver toutes les langues que vous utilisiez auparavant.

Gardez à l’esprit que le rechargement d'un dictionnaire réinitialisera la popularité des suggestions aux valeurs par défaut. Toutefois, il n'y a pas de quoi s'inquiéter. Dans la plupart des cas, vous verrez peu ou pas de différence dans l'ordre des suggestions, sauf si vous utilisez fréquemment des mots peu courants.

#### Chargement automatique du dictionnaire

Si vous oubliez de charger un dictionnaire depuis l'écran des paramètres, cela se fera automatiquement plus tard, lorsque vous ouvrirez une application dans laquelle vous pouvez taper et que vous passerez en mode prédictif. Vous serez invité à patienter pendant le chargement, et après cela, vous pourrez commencer à taper immédiatement.

Si vous supprimez un ou plusieurs dictionnaires, ils ne se rechargeront PAS automatiquement. Vous devrez le faire manuellement. Seuls les dictionnaires pour les langues nouvellement activées se chargeront automatiquement.

#### Suppression d'un dictionnaire
Si vous avez cessé d'utiliser les langues X ou Y, vous pouvez les désactiver et utiliser l'option « Supprimer les non-sélectionnés » pour libérer de l'espace de stockage.

Pour tout supprimer, indépendamment de la sélection, utilisez « Tout supprimer ».

Dans tous les cas, vos mots ajoutés personnalisés seront préservés et restaurés une fois que vous rechargerez les dictionnaires plus tard.

#### Mots ajoutés
L’option « Exporter » permet de créer un fichier CSV avec tous les mots ajoutés pour toutes les langues. Ensuite, vous pouvez utiliser ce fichier CSV pour améliorer Traditional T9 ! Rendez-vous sur GitHub et partagez les mots dans un [nouveau ticket](https://github.com/sspanak/tt9/issues) ou une [pull request](https://github.com/sspanak/tt9/pulls). Une fois révisés et approuvés, ils seront inclus dans la prochaine version.

Avec "Importer", vous pouvez importer un fichier CSV précédemment exporté. Cependant, il y a certaines restrictions :
- Vous pouvez importer uniquement des mots constitués de lettres. Les apostrophes, tirets, autres signes de ponctuation ou caractères spéciaux ne sont pas autorisés.
- Les émojis ne sont pas autorisés.
- Un fichier CSV peut contenir un maximum de 250 mots.
- Vous pouvez importer jusqu'à 1000 mots, ce qui signifie que vous pouvez importer au maximum 4 fichiers de 250 mots chacun. Au-delà de cette limite, vous pourrez toujours ajouter des mots en les tapant.

L'option "Supprimer" vous permet de rechercher et de supprimer les mots mal orthographiés ou ceux que vous ne souhaitez plus dans le dictionnaire.

## Raccourcis clavier

Tous les raccourcis clavier peuvent être reconfigurés ou désactivés via Paramètres → Clavier → Sélectionner les Raccourcis.

### Touches de saisie

#### Touche de suggestion précédente (par défaut : flèche gauche) :
Sélectionne la suggestion de mot ou de lettre précédente.

#### Touche de suggestion suivante (par défaut : flèche droite) :
Sélectionne la suggestion de mot ou de lettre suivante.

#### Touche de filtrage des suggestions (par défaut : flèche vers le haut) :
_Mode Prédictif uniquement._

- **Appui simple** : Filtre la liste des suggestions, ne laissant que celles qui commencent par le mot actuel. Par exemple, tapez "remin" et appuyez sur Filtrer pour ne garder que les mots commençant par "remin" : "remin", "remind", "reminds", "reminded", "reminding", etc.
- **Double appui** : Élargit le filtre à la suggestion complète. Par exemple, tapez "remin" et appuyez deux fois sur Filtrer pour filtrer par "remind". Vous pouvez continuer à élargir le filtre jusqu'à obtenir le mot le plus long du dictionnaire.

Le filtrage est aussi utile pour taper des mots inconnus. Par exemple, pour taper "Anakin", commencez par "A", puis appuyez sur Filtrer pour masquer "B" et "C". Appuyez ensuite sur la touche 6. Avec le filtre activé, cela proposera toutes les combinaisons possibles pour 1+6 : "A..." + "m", "n", "o". Sélectionnez "n" et appuyez sur Filtrer pour valider votre sélection et obtenir "An". Ensuite, appuyez sur la touche 2 pour obtenir "An..." + "a", "b", "c". Continuez jusqu’à obtenir "Anakin".

Lorsque le filtrage est activé, le texte de base devient en gras et en italique.

#### Touche Effacer le Filtre (par défaut : flèche vers le bas) :
_Mode Prédictif uniquement._

Efface le filtre des suggestions, si appliqué.

#### Touche centrale (OK ou Entrée) :
- Lorsque des suggestions sont affichées, saisit la suggestion sélectionnée.
- Sinon, exécute l’action par défaut de l’application (par exemple, envoyer un message, accéder à une URL, ou insérer un retour à la ligne).

_**Remarque** : Chaque application décide de la fonction à effectuer lorsqu’OK est pressé, et TT9 n'a aucun contrôle sur ce comportement._

_**Remarque 2** : Pour envoyer des messages avec OK dans les applications de messagerie, vous devez activer leur paramètre "Envoyer avec Entrée" ou un paramètre similaire. Si l'application n'a pas de tel paramètre, elle ne supporte probablement pas l'envoi par cette méthode. Dans ce cas, utilisez l'application KeyMapper depuis le [Play Store](https://play.google.com/store/apps/details?id=io.github.sds100.keymapper) ou [F-droid](https://f-droid.org/packages/io.github.sds100.keymapper/). Elle peut détecter les applications de messagerie et simuler une touche sur le bouton d'envoi de message. Consultez le [guide de démarrage rapide](https://docs.keymapper.club/quick-start/) pour plus d'informations._

#### Touche 0 :
- **En mode 123 :**
  - **Appui** : tape "0".
  - **Appui long** : tape des caractères spéciaux/mathématiques.
- **En mode ABC :**
  - **Appui** : tape espace, nouvelle ligne, ou caractères spéciaux/mathématiques.
  - **Appui long** : tape "0".
- **En mode Prédictif :**
  - **Appui** : tape un espace, une nouvelle ligne, ou caractères spéciaux/mathématiques.
  - **Double appui** : tape le caractère attribué dans les paramètres du mode Prédictif (par défaut : ".").
  - **Appui long** : tape "0".
- **En mode Cheonjiin (Coréen) :**
  - **Appui :** tape "ㅇ" et "ㅁ".
  - **Appui long :** tape un espace, une nouvelle ligne, "0" ou des caractères spéciaux/mathématiques.

#### Touche 1 :
- **En mode 123 :**
  - **Appui** : tape "1".
  - **Appui long** : tape des caractères de ponctuation.
- **En mode ABC :**
  - **Appui** : tape des caractères de ponctuation.
  - **Appui long** : tape "1".
- **En mode Prédictif :**
  - **Appui** : tape des caractères de ponctuation.
  - **Appui multiple** : tape des émojis.
  - **Appui long** : tape "1".
- **En mode Cheonjiin (Coréen) :**
  - **Appui :** tape la voyelle "ㅣ".
  - **Appui long :** tape des caractères de ponctuation.
  - **Appui long, puis appui court :** tape des emojis.

#### Touche 2 à 9 :
- **En mode 123** : tape le chiffre correspondant.
- **En mode ABC et Prédictif** : tape une lettre ou appuyez longuement pour taper le chiffre correspondant.

### Touches de fonction

#### Touche Ajouter un Mot :
Ajoute un nouveau mot au dictionnaire pour la langue actuelle.

#### Touche Retour Arrière (Retour, Suppr, ou Retour Arrière) :
Supprime du texte.

Si votre téléphone a une touche "Suppr" ou "Effacer" dédiée, vous n'avez rien à configurer, sauf si vous souhaitez un autre Retour Arrière. Dans ce cas, l’option vide : "--" sera automatiquement présélectionnée.

Sur les téléphones avec une touche combinée "Supprimer"/"Retour", cette touche sera sélectionnée automatiquement. Vous pouvez néanmoins attribuer la fonction "Retour Arrière" à une autre touche pour que "Retour" n’ait qu’une fonction de navigation.

_**NB** : Utiliser "Retour" comme retour arrière ne fonctionne pas dans toutes les applications, notamment Firefox, Spotify, et Termux, car ces applications peuvent redéfinir la fonction de la touche. "Retour" ayant un rôle spécial dans Android, son usage est limité par le système._

_**NB 2** : Maintenir la touche "Retour" déclenche toujours l’action système par défaut (par exemple, afficher la liste des applications en cours)._

_Dans ces cas, vous pouvez attribuer une autre touche (toutes les autres sont utilisables) ou utiliser le retour arrière à l’écran._

#### Touche Changer de Mode de Saisie (par défaut : appui sur #) :
Change le mode de saisie (abc → Prédictif → 123).

_Le mode Prédictif n'est pas disponible dans les champs de mot de passe._

_Dans les champs numériques uniquement, changer de mode est impossible. Dans ce cas, la touche revient à sa fonction par défaut (c’est-à-dire taper "#")._

#### Touche Outils du presse-papiers :
Affiche le panneau avec des outils du presse-papiers, permettant de sélectionner, couper, copier et coller du texte. Vous pouvez fermer le panneau en appuyant à nouveau sur la touche "✱" ou, dans la plupart des applications, en appuyant sur le bouton Retour. Détails disponibles [ci-dessous](#outils-de-presse-papiers).

#### Touche Langue Suivante (par défaut : appui long sur #) :
Changer la langue de saisie lorsque plusieurs langues ont été activées dans les paramètres.

#### Touche Sélectionner le Clavier :
Ouvre la boîte de dialogue de changement de clavier d'Android, où vous pouvez choisir parmi tous les claviers installés.

#### Touche Maj (par défaut : appui sur ✱) :
- **Lors de la saisie de texte** : Alterne entre majuscules et minuscules.
- **Lors de la saisie de caractères spéciaux avec la touche 0** : Affiche le groupe de caractères suivant.

#### Touche Afficher les Paramètres :
Ouvre l’écran de configuration des Paramètres, où vous pouvez choisir les langues pour la saisie, configurer les raccourcis clavier, modifier l’apparence de l’application, ou améliorer la compatibilité avec votre téléphone.

#### Touche Annuler :
Annule la dernière action. Équivaut à appuyer sur Ctrl+Z sur un ordinateur ou Cmd+Z sur un Mac.

_L’historique d’annulation est géré par les applications, et non par Traditional T9. Cela signifie que l’annulation peut ne pas être possible dans toutes les applications._

#### Touche Rétablir :
Répète la dernière action annulée. Équivaut à appuyer sur Ctrl+Y ou Ctrl+Maj+Z sur un ordinateur ou Cmd+Y sur un Mac.

_Comme pour Annuler, la commande Rétablir peut ne pas être disponible dans toutes les applications._

#### Touche Saisie Vocale :
Active la saisie vocale pour les téléphones compatibles. Voir [ci-dessous](#saisie-vocale) pour plus d'informations.

#### Touche Liste des Commandes / Palette de Commandes / (par défaut : appui long sur ✱) :
Affiche une liste de toutes les commandes (ou fonctions).

De nombreux téléphones ont seulement deux ou trois touches "libres" pouvant être utilisées comme raccourcis. Mais, Traditional T9 a beaucoup plus de fonctions, donc il est impossible de toutes les avoir sur le clavier. La Palette de Commandes résout ce problème en permettant d’invoquer des fonctions supplémentaires via des combinaisons de touches.

Voici une liste des commandes possibles :
- **Afficher l’Écran des Paramètres (Combo par défaut : appui long sur ✱, touche 1).** Identique à l’appui sur [Afficher les Paramètres](#touche-afficher-les-paramètres).
- **Ajouter un Mot (Combo par défaut : appui long sur ✱, touche 2).** Identique à l’appui sur [Ajouter un Mot](#touche-ajouter-un-mot).
- **Saisie Vocale (Combo par défaut : appui long sur ✱, touche 3).** Identique à l’appui sur [Saisie Vocale](#touche-saisie-vocale).
- **Annuler (Combo par défaut : appui long sur ✱, touche 4).** Identique à l’appui sur [Touche Annuler](#touche-annuler).
- **Outils du presse-papiers (Combo par défaut : appui long sur ✱, touche 5).** Identique à l’appui sur [Outils du presse-papiers](#touche-outils-du-presse-papiers).
- **Rétablir (Combo par défaut : appui long sur ✱, touche 6).** Identique à l’appui sur [Touche Rétablir](#touche-rétablir).
- **Sélectionner un Clavier Différent (Combo par défaut : appui long sur ✱, touche 8).** Identique à l’appui sur [Sélectionner le Clavier](#touche-sélectionner-le-clavier).

_Cette touche ne fait rien lorsque l'Affichage de l'Écran est réglé sur "Pavé Virtuel", car toutes les touches pour toutes les fonctions possibles sont déjà disponibles à l’écran._

## Pavé numérique à l'écran
Sur les appareils exclusivement tactiles, un clavier à l’écran entièrement fonctionnel est disponible et activé automatiquement. Si l’appareil n’est pas détecté comme tactile, il peut être activé manuellement depuis Paramètres → Apparence → Disposition à l’écran en sélectionnant « Pavé numérique virtuel ».

Sur les appareils disposant à la fois d’un écran tactile et d’un clavier matériel, les touches à l’écran peuvent être désactivées afin de libérer de l’espace. Cette option est disponible dans Paramètres → Apparence.

Il est également recommandé de désactiver le comportement spécial qui associe la touche « Retour » à « Retour arrière », car cela n’est utile que lors de l’utilisation d’un clavier matériel. Cette opération est généralement effectuée automatiquement. Dans le cas contraire, accédez à Paramètres → Clavier → Sélectionner les raccourcis → Touche Retour arrière et sélectionnez l’option « -- ».

### Dispositions à l’écran Retro et Modern
Deux dispositions de clavier virtuel sont disponibles : Retro et Modern.

La disposition Retro comprend un pavé directionnel (D-pad) avec une touche OK centrale en haut et des touches numériques en dessous, rappelant fortement les claviers des téléphones du début des années 2000. Elle convient aux utilisateurs recherchant une expérience traditionnelle, aux appareils dotés de petits écrans et aux personnes ayant de grands pouces. Elle peut également séduire les utilisateurs familiers avec d’anciennes applications de clavier T9 aujourd’hui abandonnées, telles que Old Keyboard ou Big Old Keyboard.

La disposition Modern conserve l’apparence et le comportement standard d’Android tout en utilisant une disposition de saisie à 12 touches. Elle comporte un bloc central de touches numériques (0–9) pour la saisie de texte, avec des touches de fonction telles que Maj, Retour arrière, changement de langue et OK (Entrée) disposées en colonnes à gauche et à droite.

### Aperçu des touches virtuelles
Le clavier à l’écran fonctionne de la même manière qu’un clavier matériel de téléphone. Les touches ayant une seule fonction affichent une étiquette ou une icône centrale. Les touches disposant d’une fonction supplémentaire par appui long affichent une étiquette ou une icône secondaire dans le coin supérieur droit.

#### Touches 0–9
Les touches numériques sont utilisées pour saisir des mots et des chiffres. La disposition Retro permet également des gestes de balayage vers la gauche et vers la droite sur certaines touches. Lorsque ces fonctions sont disponibles, elles sont indiquées par des icônes dans le coin inférieur gauche ou droit de la touche.

Dans la version Google Play, les gestes de balayage peuvent être personnalisés ou désactivés pour les dispositions Retro et Modern. Cela peut être configuré depuis Paramètres → Clavier → Fonctions des touches.

#### Touches de texte personnalisées (« ! » et « ? »)
Par défaut, ces touches insèrent les signes de ponctuation correspondants. Dans les champs de saisie numériques ou téléphoniques, elles peuvent insérer des caractères alternatifs tels qu’un astérisque, un dièse ou un point décimal.

Dans la version Google Play, ces touches peuvent être personnalisées. Il est possible de modifier le caractère par défaut et d’assigner des actions aux balayages vers le haut, le bas, la gauche et la droite. Cette configuration s’effectue depuis Paramètres → Clavier → Fonctions des touches.

#### Touche de mode de saisie
- **Appui :** Fait défiler les modes de saisie (abc → Prédictif → 123).
- **Appui long :** Change la langue de saisie lorsque plusieurs langues sont activées dans les paramètres.
- **Balayage horizontal :** Bascule vers le dernier clavier utilisé autre que TT9.
- **Balayage vertical :** Ouvre la boîte de dialogue Android de changement de clavier, permettant de sélectionner parmi tous les claviers installés.

La touche affiche une petite icône en forme de globe lorsque plusieurs langues ont été activées depuis Paramètres → Langues. Cette icône indique qu’il est possible de changer de langue en maintenant la touche enfoncée.

_Dans la disposition Retro, il s’agit de la touche en bas à droite._

_Dans la disposition Modern, il s’agit de la touche en bas à gauche._

#### Retour arrière
Supprime des caractères lorsqu’elle est pressée. Lorsque Paramètres → Clavier → Suppression rapide est activé, un balayage vers l’arrière permet de supprimer le mot précédent.

#### Touche de filtrage
- **Appui :** Filtre la liste des suggestions. Voir [ci-dessus](#touche-de-filtrage-des-suggestions-par-défaut-flèche-vers-le-haut) pour le fonctionnement du filtrage des mots.
- **Appui long :** Efface le filtre, s’il est actif.

_Cette touche est disponible uniquement dans la disposition Modern. Emplacement : deuxième touche à partir du haut._

_Le filtrage n’est possible qu’en mode prédictif._

#### Outils du presse-papiers / Touche de saisie vocale
- **Appui :** Ouvre les options de copie, collage et d’édition de texte.
- **Appui long :** Active la saisie vocale.

_Cette touche est disponible uniquement dans la disposition Modern. Emplacement : troisième touche à partir du haut._

#### Touche OK
- **Appui :** Équivaut à appuyer sur la touche ENTRÉE des autres claviers.

La disposition Retro permet en outre d’activer des gestes de balayage depuis Paramètres → Apparence → Touches.

- **Balayage vers le haut sans suggestions :** Déplacer le curseur vers le haut (équivalent au D-PAD haut).
- **Balayage vers le bas sans suggestions :** Déplacer le curseur vers le bas (équivalent au D-PAD bas).
- **Balayage vers le haut avec suggestions :** Filtrer la liste des suggestions. Voir [ci-dessus](#touche-de-filtrage-des-suggestions-par-défaut-flèche-vers-le-haut).
- **Balayage vers le bas avec suggestions :** Effacer le filtre des suggestions.

### Redimensionner le panneau du clavier pendant la saisie
Dans certains cas, vous pouvez trouver que le pavé numérique virtuel occupe trop d'espace à l'écran, vous empêchant de voir ce que vous tapez ou certains éléments de l'application. Si c'est le cas, vous pouvez le redimensionner en maintenant enfoncée et en faisant glisser la touche Paramètres/Palette de commandes ou en faisant glisser la barre d'état (elle affiche la langue ou le mode de saisie actuel). Lorsque la hauteur devient trop petite, la disposition sera automatiquement changée en « touches de fonction » ou en « liste de suggestions uniquement ». Respectivement, en redimensionnant vers le haut, la disposition reviendra au « pavé numérique virtuel ». Vous pouvez également double-cliquer sur la barre d'état pour minimiser ou maximiser instantanément.

_Le redimensionnement par double pression est désactivé par défaut. Vous pouvez l’activer ici : Paramètres → Apparence._

_Redimensionner Traditional T9 entraîne également le redimensionnement de l'application en cours. Faire les deux est très exigeant en ressources et peut provoquer un scintillement ou des ralentissements sur de nombreux téléphones, même ceux de gamme supérieure._

### Changer la hauteur des touches
Il est également possible de modifier la hauteur des touches à l'écran. Pour ce faire, allez dans Paramètres → Apparence → Hauteur des touches et ajustez-la selon vos préférences.

La valeur par défaut de 100 % est un bon compromis entre une taille de bouton utilisable et l’espace occupé sur l’écran. Cependant, si vous avez de grands doigts, vous voudrez peut-être augmenter un peu cette valeur, tandis que si vous utilisez TT9 sur un écran plus grand, comme une tablette, vous souhaiterez peut-être la réduire.

_Si l'espace disponible à l'écran est limité, TT9 ignorera ce paramètre et réduira automatiquement sa hauteur, pour laisser suffisamment de place à l'application en cours._

## Outils de presse-papiers
Depuis le panneau des outils du presse-papiers, vous pouvez sélectionner, couper, copier et coller du texte, de la même manière qu'avec un clavier d'ordinateur. Pour quitter les outils de presse-papiers, appuyez sur la touche « ✱ » ou sur la touche Retour (sauf dans les navigateurs Web, Spotify et quelques autres applications). Ou appuyez sur la touche de lettres du clavier à l'écran.

Voici une liste des commandes de texte possibles :
1. Sélectionner le caractère précédent (comme Shift+Gauche sur un clavier d'ordinateur)
2. Ne rien sélectionner
3. Sélectionner le caractère suivant (comme Shift+Droite)
4. Sélectionner le mot précédent (comme Ctrl+Shift+Gauche)
5. Tout sélectionner
6. Sélectionner le mot suivant (comme Ctrl+Shift+Droite)
7. Couper
8. Copier
9. Coller

Pour faciliter l'édition, les touches d'effacement, d'espace et de validation sont également actives.

## Saisie vocale
La fonction de saisie vocale permet de convertir la parole en texte, comme sur Gboard. Comme tous les autres claviers, Traditional T9 ne réalise pas la reconnaissance vocale lui-même ; il demande à votre téléphone de le faire.

_Le bouton de saisie vocale est masqué sur les appareils qui ne la prennent pas en charge._

### Appareils avec Google
Sur les appareils dotés des services Google, TT9 utilise l'infrastructure Google pour convertir votre voix en texte. Sous Android 12 ou version antérieure, vous devez être connecté à un réseau Wi-Fi ou activer les données mobiles pour que cela fonctionne. À partir d’Android 13, TT9 peut effectuer la reconnaissance vocale en ligne et hors ligne à l’aide des paquets linguistiques du système. Pour une utilisation hors ligne, veillez à télécharger les langues souhaitées via : Paramètres Android → Système → Reconnaissance sur l'appareil → Ajouter une langue.

_Les paquets installés pour Google Voice, d'autres assistants vocaux ou claviers ne sont pas garantis pour fonctionner avec Traditional T9. Il est recommandé d’installer les paquets globaux via l’écran "Reconnaissance sur l’appareil"._

### Appareils sans Google
Sur les appareils sans Google, si une application d’assistant vocal est présente ou si le clavier natif prend en charge la saisie vocale, la reconnaissance vocale utilisera ce qui est disponible. Notez que cette méthode est bien moins performante que celle de Google. Elle ne fonctionne pas dans un environnement bruyant et ne reconnaît en général que des phrases simples comme "ouvrir calendrier" ou "jouer musique".

### Autres appareils
Les autres téléphones sans Google ne prennent généralement pas en charge la saisie vocale. Les téléphones chinois n’ont pas de capacités de reconnaissance vocale en raison des politiques de sécurité chinoises. Sur ces téléphones, vous pouvez essayer d’activer la saisie vocale en installant l’application Google, nom du paquet : « com.google.android.googlequicksearchbox ». Vous pouvez aussi essayer d’installer Google Go : « com.google.android.apps.searchlite ».

## Dépannage
Pour certaines applications ou appareils, il est possible d'activer des options spéciales, qui permettront à Traditional T9 de mieux fonctionner avec eux. Vous les trouverez à la fin de chaque écran de paramètres, dans la section Compatibilité.

### Méthode alternative de défilement des suggestions
_Dans : Paramètres → Apparence._

Sur certains appareils, en mode prédictif, il peut être impossible de faire défiler la liste jusqu'à la fin, ou vous devrez faire défiler plusieurs fois en arrière et en avant jusqu'à ce que la dernière suggestion apparaisse. Ce problème survient parfois sur Android 9 ou les versions antérieures. Activez cette option si vous rencontrez ce problème.

### Toujours au premier plan
_Dans : Paramètres → Apparence._

Sur certains téléphones, notamment le Sonim XP3plus (XP3900), Traditional T9 peut ne pas s'afficher lorsque vous commencez à taper, ou il peut être partiellement couvert par les touches virtuelles. Dans d'autres cas, il peut y avoir des bandes blanches autour. Ce problème peut se produire dans une application spécifique ou dans toutes. Pour l’éviter, activez l'option « Toujours au premier plan ».

### Espace inférieur (orientation portrait)
_Dans : Paramètres → Apparence._

Sur les appareils Samsung équipés d’Android 15 ou version ultérieure, Traditional T9 peut apparaître trop bas à l’écran. Dans ce cas, la barre de navigation du système recouvre la dernière rangée du clavier, rendant les touches inutilisables. Toute tentative de saisir un espace, d’appuyer sur OK ou de changer le mode de saisie entraîne la fermeture du clavier. L’augmentation de la valeur « Espace inférieur » à 48 dp résout ce problème.

Dans d’autres situations, un espace vide inutile peut apparaître sous le bloc de touches. La réduction de « Espace inférieur » à 0 dp permet de l’éliminer.

_Voir le bug [#950](https://github.com/sspanak/tt9/issues/950) pour plus d’informations._

_Dans de très rares cas, des appareils non Samsung peuvent présenter les mêmes problèmes. Voir [#755](https://github.com/sspanak/tt9/issues/755)._

### Protection contre la répétition des touches
_Dans : Paramètres → Clavier._

Les téléphones CAT S22 Flip et Qin F21 sont connus pour leurs claviers de mauvaise qualité qui se dégradent rapidement avec le temps et commencent à enregistrer plusieurs clics pour une seule pression. Vous pouvez le remarquer en tapant ou en naviguant dans les menus du téléphone.

Pour les téléphones CAT, la valeur recommandée est de 50 à 75 ms. Pour le Qin F21, essayez entre 20 et 30 ms. Si le problème persiste, augmentez la valeur légèrement, mais essayez de la garder aussi basse que possible.

_**Remarque :** Plus la valeur est élevée, plus vous devrez taper lentement. TT9 ignorera les pressions très rapides._

_**Remarque 2 :** Outre ce qui précède, les téléphones Qin peuvent également ne pas détecter les pressions longues. Malheureusement, dans ce cas, rien ne peut être fait._

### Afficher le texte en cours de saisie
_Dans : Paramètres → Clavier._

Si vous avez des difficultés à taper dans Deezer ou Smouldering Durtles parce que les suggestions disparaissent trop rapidement avant que vous puissiez les voir, désactivez cette option. Cela fera en sorte que le mot actuel reste caché jusqu'à ce que vous appuyiez sur OK ou Espace, ou jusqu'à ce que vous appuyiez sur la liste de suggestions.

Le problème survient parce que Deezer et Smouldering Durtles modifient parfois le texte que vous tapez, ce qui empêche TT9 de fonctionner correctement.

### Les stickers et panneaux d'emoji de Telegram/Snapchat ne s'ouvrent pas
Cela se produit si vous utilisez l'un des petits formats de clavier. Actuellement, il n'existe pas de solution permanente, mais vous pouvez utiliser la solution suivante :
- Allez dans Paramètres → Apparence et activez le Pavé numérique à l'écran.
- Retournez dans le chat et cliquez sur le bouton emoji ou stickers. Ils apparaîtront maintenant.
- Vous pouvez maintenant retourner dans les paramètres et désactiver le pavé numérique à l'écran. Les panneaux d'emoji et de stickers resteront accessibles jusqu'à ce que vous redémarriez l'application ou le téléphone.

### Traditional T9 ne s'affiche pas immédiatement dans certaines applications
Si vous avez ouvert une application dans laquelle vous pouvez taper, mais que TT9 n'apparaît pas automatiquement, commencez simplement à taper et il apparaîtra. Sinon, appuyez sur les touches de raccourci pour changer [le mode de saisie](#touche-changer-de-mode-de-saisie-par-défaut-appui-sur) ou la [langue](#touche-langue-suivante-par-défaut-appui-long-sur) pour faire apparaître TT9 s'il est caché.

Sur certains appareils, TT9 peut rester invisible, peu importe ce que vous faites. Dans ce cas, vous devez activer l'option [Toujours au premier plan](#toujours-au-premier-plan).

**Explication longue.** Ce problème est dû au fait qu'Android est principalement conçu pour les appareils à écran tactile, et attend donc que vous touchiez le champ texte/numéro pour afficher le clavier. Il est possible de faire apparaître TT9 sans cette confirmation, mais dans certains cas, Android oubliera alors de le cacher quand il le devrait, par exemple après avoir composé un numéro de téléphone ou après avoir soumis un texte dans un champ de recherche.

Pour cette raison, et afin de respecter les normes Android, le contrôle est entre vos mains. Appuyez simplement sur une touche pour « toucher » l'écran et continuez à taper.

### Sur le Qin F21 Pro, maintenir enfoncée la touche 2 ou 8 augmente ou diminue le volume au lieu de taper un chiffre
Pour atténuer ce problème, allez dans Paramètres → Apparence et activez « Icône de statut ». TT9 devrait détecter automatiquement le Qin F21 et activer les paramètres, mais si la détection automatique échoue ou si vous avez désactivé l'icône pour une raison quelconque, vous devez l'activer pour que toutes les touches fonctionnent correctement.

**Explication longue.** Le Qin F21 Pro (et peut-être le F22) a une application de raccourcis qui permet d'assigner les fonctions de Volume Haut et Bas aux touches numériques. Par défaut, le gestionnaire de raccourcis est activé et maintenir la touche 2 augmente le volume, tandis que maintenir la touche 8 le diminue. Cependant, lorsque l'icône de statut est absente, le gestionnaire suppose qu'aucun clavier n'est actif et ajuste le volume au lieu de permettre à Traditional T9 de taper un chiffre. En activant l'icône, on contourne simplement le gestionnaire de raccourcis et tout fonctionne correctement.

### Problèmes généraux sur les téléphones Xiaomi
Xiaomi a introduit plusieurs autorisations non standard sur ses téléphones, ce qui empêche le clavier virtuel de Traditional T9 de fonctionner correctement. Plus précisément, les touches « Afficher les paramètres » et « Ajouter un mot » peuvent ne pas fonctionner. Pour corriger cela, vous devez accorder les autorisations « Afficher les fenêtres contextuelles » et « Afficher les fenêtres contextuelles en arrière-plan » à TT9 dans les paramètres de votre téléphone. [Ce guide](https://parental-control.flashget.com/how-to-enable-display-pop-up-windows-while-running-in-the-background-on-flashget-kids-on-xiaomi) pour une autre application explique comment le faire.

Il est également fortement recommandé d'accorder l'autorisation « Notification permanente ». Cela est similaire à l'autorisation de « Notifications » introduite dans Android 13. Voir [ci-dessus](#remarques-pour-android-13-ou-version-supérieure) pour plus d'informations sur son importance.

_Les problèmes Xiaomi ont été discutés dans [ce problème GitHub](https://github.com/sspanak/tt9/issues/490)._

### La saisie vocale prend beaucoup de temps pour s'arrêter
C'est [un problème connu](https://issuetracker.google.com/issues/158198432) sous Android 10 que Google n'a jamais corrigé. Il est impossible de le résoudre côté TT9. Pour arrêter l'opération de saisie vocale, restez silencieux pendant quelques secondes. Android éteint automatiquement le microphone lorsqu'il ne détecte aucune parole.

### Mon application bancaire n’accepte pas Traditional T9
Il ne s’agit pas d’un problème lié à TT9. Les banques restreignent fréquemment les claviers non standard ou open source, car elles ne souhaitent prendre aucun risque et partent du principe qu’ils peuvent être peu sûrs. Certaines vont même jusqu’à fournir leur propre clavier, bloquant parfois le clavier standard de Google, Gboard. Malheureusement, dans ce cas, la seule solution consiste à utiliser le clavier d’origine de l’appareil.

### La vibration ne fonctionne pas (appareils à écran tactile uniquement)
Les options d'économie de batterie, d'optimisation et la fonction "Ne pas déranger" peuvent empêcher la vibration. Vérifiez si l'une de ces options est activée dans les paramètres système de votre appareil. Sur certains appareils, il est possible de configurer l'optimisation de la batterie individuellement pour chaque application via Paramètres système → Applications. Si votre appareil le permet, désactivez l'optimisation pour TT9.

Une autre raison pour laquelle la vibration ne fonctionne pas est qu'elle peut être désactivée au niveau du système. Vérifiez si votre appareil dispose des options "Vibrer au toucher" ou "Vibrer à l'appui des touches" dans Paramètres système → Accessibilité et activez-les. Les appareils Xiaomi et OnePlus offrent un contrôle encore plus précis de la vibration. Assurez-vous que tous les paramètres pertinents sont activés.

Enfin, la vibration ne fonctionne pas de manière fiable sur certains appareils. Pour corriger cela, il faudrait des permissions et un accès à davantage de fonctions du système. Cependant, TT9 étant un clavier qui respecte la confidentialité, il ne demandera pas ces accès.

## Questions Fréquemment Posées

### Pourquoi n’ajoutez-vous pas la langue X ?
J’aimerais beaucoup le faire, mais j’ai besoin de ton aide. Il m’est impossible de gérer plus de 40 langues tout seul. Comme je ne parle pas ta langue, il m’est difficile de trouver des ressources fiables en ligne. C’est là que les locuteurs natifs comme toi peuvent vraiment aider.
En fait, plus de 90 % des langues existantes ont été ajoutées par ou avec l’aide d’utilisateurs enthousiastes.

Pour ajouter une nouvelle langue, j’ai besoin d’une liste de mots vérifiée, de préférence issue d’une source officielle ou académique (par exemple, « Grand dictionnaire de la langue X »). Ce type de liste offre les meilleures suggestions de mots pendant la saisie.

S’il n’existe pas un tel dictionnaire, tu peux fournir une liste de mots téléchargeable gratuitement. L’idéal est une liste de 300 000 à 500 000 mots, mais si la langue a beaucoup de variations (temps, genre, nombre, etc.), environ 1 million de mots peut être nécessaire.

### Il y a des mots mal orthographiés ou manquants dans la langue XYZ. Pourquoi ne sont-ils pas corrigés ?
Comme indiqué ci-dessus, je ne parle pas ta langue et je ne remarque donc pas forcément ces erreurs. Mais avec ton aide, nous pouvons les corriger et améliorer le dictionnaire pour tout le monde.

### Ne pouvez-vous pas ajouter la fonctionnalité X ?
Non.

Chacun a ses préférences. Certains veulent des touches plus grandes, d'autres dans un ordre différent, certains veulent une touche de raccourci pour taper ".com", et d'autres regrettent leur ancien téléphone ou clavier. Mais veuillez comprendre que je fais ce travail bénévolement sur mon temps libre. Il est impossible de satisfaire des milliers de demandes différentes, dont certaines se contredisent même.

Henry Ford a dit un jour : "Le client peut choisir n'importe quelle couleur, tant que c'est noir." De la même manière, Traditional T9 est simple, efficace et gratuit, mais vous obtenez ce qui est proposé.

### Ne pouvez-vous pas le rendre plus similaire à mon appareil préféré (par exemple Sony Ericsson, Xperia, Nokia C2, Samsung) ou à mon application de clavier préférée ?
Non.

Traditional T9 n'est pas conçu pour être un substitut ou une application clonée. Il possède un design unique, principalement inspiré des Nokia 3310 et 6303i. Et bien qu'il capture l'essence des classiques, il offre une expérience propre qui ne répliquera exactement aucun appareil.

### Vous devriez copier TouchPal ; c’était le meilleur clavier !
Non. Voir les points précédents.

TouchPal était un clavier rapide et réactif, offrant de nombreuses options de thèmes, de personnalisation et de prise en charge multilingue. Il était populaire vers 2015, à une époque où la concurrence était limitée. Toutefois, il n’a jamais été un véritable clavier T9 : la disposition à 12 touches n’était disponible que pour certaines langues et était conçue exclusivement pour les écrans tactiles.

Avec le temps, l’application a perdu de vue l’essentiel — la saisie. Des publicités ont été ajoutées, les demandes d’autorisations sont devenues agressives et des données sensibles ont commencé à être collectées. Finalement, TouchPal a été retiré du Play Store.

À l’inverse, la [philosophie](https://github.com/sspanak/tt9/?tab=readme-ov-file#-philosophy) de TT9 repose sur les principes de l’open source. Son code source et ses dictionnaires sont accessibles publiquement et peuvent être examinés. Le respect de la vie privée des utilisateurs est intégré dès la conception. Les contributions de la communauté ont permis d’améliorer le projet, notamment par des corrections de bogues, l’ajout de nouvelles langues et des traductions. Les utilisateurs peuvent également créer leurs propres versions modifiées.

TT9 ne propose pas de fonctionnalités telles que des formes de touches personnalisables, mais offre une disposition claire et lisible axée sur une saisie efficace. Il ne reproduit pas le style visuel de TouchPal, mais fonctionne sur les smartphones modernes sous Android 16, sur des appareils à clavier matériel d’inspiration nostalgique comme le Qin F21, le Cat S22 Flip et le Sonim XP3800, ainsi que sur des télécommandes de télévision.

Si vous n’êtes pas d’accord ou souhaitez exposer votre point de vue, rejoignez la [discussion ouverte](https://github.com/sspanak/tt9/issues/647) sur GitHub. Merci de rester respectueux envers les autres. Les messages haineux ne seront pas tolérés.

### Android m’a averti que le clavier pouvait collecter mes données personnelles, y compris les numéros de carte de crédit et les mots de passe
Il s’agit d’un avertissement standard d’Android affiché lors de l’installation et de l’activation de tout clavier, pas uniquement Traditional T9. Soyez assuré que toutes vos saisies demeurent sur votre appareil. Le moteur de saisie est entièrement open source, ce qui vous permet d’en examiner le code sur GitHub et de vérifier que votre confidentialité est préservée.

_Si vous avez encore des préoccupations, veuillez consulter la politique de confidentialité de l’application._

### J'ai besoin d'utiliser un clavier QWERTY (uniquement pour les appareils tactiles)
Traditional T9 est un clavier T9 et en tant que tel, il ne propose pas de disposition de type QWERTY.

Si vous apprenez encore à utiliser T9 et que vous devez parfois revenir en arrière, ou si vous trouvez plus pratique de taper de nouveaux mots en utilisant QWERTY, glisser vers le haut la touche de mode de saisie pour passer à un autre clavier. Pour plus d'informations, voir [l'aperçu des touches virtuelles](#aperçu-des-touches-virtuelles).

La plupart des autres claviers permettent de revenir à Traditional T9 en maintenant la barre d'espace ou la touche « changer de langue ». Consultez la documentation ou le manuel respectif pour plus d'informations.

### Je ne peux pas changer de langue sur un téléphone tactile
Tout d'abord, assurez-vous d'avoir activé toutes les langues souhaitées dans Paramètres → Langues. Ensuite, maintenez la [touche de mode de saisie](#touche-de-mode-de-saisie) pour changer de langue.

### Comment ajouter des contractions telles que « I've » ou « don't » au dictionnaire ?
Toutes les contractions dans toutes les langues sont déjà disponibles sous forme de mots séparés, donc vous n'avez rien à ajouter. Cela offre une flexibilité maximale : vous pouvez combiner n'importe quel mot avec n'importe quelle contraction, et cela permet également de gagner beaucoup d'espace de stockage.

Par exemple, vous pouvez taper 've en appuyant sur : 183 ; ou 'll avec : 155. Cela signifie que "I'll" = 4155 et "we've" = 93183. Vous pouvez également taper des choses comme "google.com" en appuyant sur : 466453 (google) 1266 (.com).

Un exemple plus complexe en français : "Qu'est-ce que c'est" = 781 (qu'), 378123 (est-ce), 783 (que), 21378 (c'est).

_Les exceptions notables à cette règle sont "can't" et "don't" en anglais. Ici, 't n'est pas un mot séparé, mais vous pouvez tout de même les taper comme expliqué ci-dessus._