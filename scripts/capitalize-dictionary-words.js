const { basename } = require('path');
const { createReadStream, existsSync } = require('fs');
const { print, printError } = require('./_printers.js');


function printHelp() {
	print(`Usage ${basename(process.argv[1])} DICTIONARY-FILE-NAME.txt LIST-OF-CAPITALIZED-WORDS.txt MIN-WORD-LENGTH LOCALE`);
	print('Capitalizes a word list using capitalized words in another list.');
	print('\nMIN-WORD-LENGTH must be a positive number.');
	print('LOCALE could be any valid JS locale, for exmaple: en, en-US, etc...');
}



function validateInput() {
	if (process.argv.length < 6) {
		printHelp();
		process.exit(1);
	}

	if (!existsSync(process.argv[3])) {
		printError(`Failure! Could not find list-of-capitals file "${process.argv[3]}".`);
		process.exit(2);
	}

	if (!existsSync(process.argv[2])) {
		printError(`Failure! Could not find dictionary file "${process.argv[2]}".`);
		process.exit(2);
	}

	const minWordLength = Number.parseInt(process.argv[4]);
	if (Number.isNaN(minWordLength) || minWordLength < 0) {
		printError(`Failure! The minimum word length must be a positive number.`);
		process.exit(2);
	}

	return { capitalsFileName: process.argv[3], dictionaryFileName: process.argv[2], locale: process.argv[5], minWordLength };
}


async function capitalize({ dictionaryFileName, capitalsFileName, locale, minWordLength }) {
	// read the dictionary
	let lineReader = require('readline').createInterface({
	  input: createReadStream(dictionaryFileName)
	});

	const words = {};
	for await (const line of lineReader) {
		words[line] = true;
	}


	// convert the dictionary words using the second file
	lineReader = require('readline').createInterface({
	  input: createReadStream(capitalsFileName)
	});

	for await (const capitalizedWord of lineReader) {
		if (capitalizedWord.length < minWordLength) {
			continue;
		}

		const lowercaseWord = capitalizedWord.toLocaleLowerCase(locale);
		if (words[lowercaseWord]) {
			delete words[lowercaseWord];
			words[capitalizedWord] = true;
		}

		const possessiveLowercaseWord = `${lowercaseWord}'s`;
		if (words[possessiveLowercaseWord]) {
			delete words[possessiveLowercaseWord];
			words[`${capitalizedWord}'s`] = true;
		}
	}

	return Object.keys(words);
}



function printWords(wordList) {
	if (!Array.isArray(wordList)) {
		return;
	}

	wordList.forEach(w => print(w));
}



/** main **/
capitalize(validateInput())
	.then(words => printWords(words))
	.catch(e => printError(e));
