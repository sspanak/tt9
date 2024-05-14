const { basename } = require('path');
const { createReadStream, existsSync } = require('fs');
const { createInterface } = require('readline');
const { print, printError } = require('./_printers.js');


function printHelp() {
	print(`Usage ${basename(process.argv[1])} LOCALE FILENAME.txt [--prefer-lowercase]`);
	print('Removes repeating words from a word list');
	print("If --prefer-lowercase is set, the lowercase variants will be preserved, otherwise capitalized or uppercase variants will remain.")
	print('\nLocale could be any valid JS locale, for example: en, en-US, etc...');
}



function validateInput() {
	if (process.argv.length < 4) {
		printHelp();
		process.exit(1);
	}


	if (!existsSync(process.argv[3])) {
		printError(`Failure! Could not find file "${process.argv[3]}".`);
		process.exit(2);
	}

	return {
		fileName: process.argv[3],
		locale: process.argv[2],
		preferLowercase: !!process.argv[4]
	};
}


/*
const GEO_NAME = /[A-Z]\w+-[^\n]+/;

function getRegularWordKey(locale, word) {
	if (typeof word !== 'string' || word.length === 0) {
		return '';
	}

	return GEO_NAME.test(word) ? word : word.toLocaleLowerCase(locale);
}
*/


function getLowercaseWordKey(locale, word) {
	if (typeof word !== 'string' || word.length === 0) {
		return '';
	}

	return word.toLocaleLowerCase(locale);
}



async function removeRepeatingWords({ fileName, locale, preferLowercase }) {
	const wordMap = new Map();

	let lineReader = createInterface({ input: createReadStream(fileName) });
	for await (const line of lineReader) {
		const lowercaseKey = getLowercaseWordKey(locale, line);
		if (lowercaseKey === '') {
			continue;
		}

		if (wordMap.has(lowercaseKey) && wordMap.get(lowercaseKey) !== line) {
			if (preferLowercase && lowercaseKey === line) {
				wordMap.set(lowercaseKey, line);
			} else if (!preferLowercase && lowercaseKey !== line) {
				wordMap.set(lowercaseKey, line);
			}
		}

		if (!wordMap.has(lowercaseKey)) {
			wordMap.set(lowercaseKey, line);
		}
	}

	return Array.from(wordMap.values(wordMap)).sort();
}



function printWords(wordList) {
	if (!Array.isArray(wordList)) {
		return;
	}

	wordList.forEach(w => print(w));
}



/** main **/
removeRepeatingWords(validateInput())
	.then(words => printWords(words))
	.catch(e => printError(e));
