const { basename } = require('path');
const { createReadStream, existsSync } = require('fs');
const { createInterface } = require('readline');
const { print, printError } = require('./_printers.js');


function printHelp() {
	print(`Usage ${basename(process.argv[1])} LOCALE word-list.txt`);
	print('Searches for compound words with that also exsit as separate words and removes the compound variants.');
	print('For example, "fly-by" will be removed, if the word list contains both "fly" and "by".')
	print('LOCALE could be any valid JS locale, for exmaple: en, en-US, etc...')
}


function validateInput() {
	if (process.argv.length < 4) {
		printHelp();
		process.exit(1);
	}

	if (!existsSync(process.argv[3])) {
		printError(`Failure! Could not find word list file "${process.argv[3]}".`);
		process.exit(2);
	}

	return {
		fileName: process.argv[3],
		locale: process.argv[2],
		separator: '-'
	};
}


function printWords(wordList) {
	if (wordList instanceof Set) {
		wordList.forEach(w => print(w));
	}
}


async function readWords(fileName) {
	const words = new Set();

	if (!fileName) {
		return words;
	}

	for await (const line of createInterface({ input: createReadStream(fileName) })) {
		words.add(line);
	}

	return words;
}


function removeCompoundWords(locale, words, lowerCaseWords, separator) {
	if (!(words instanceof Set)) {
		return new Set();
	}

	const uniqueWords = new Set();
	words.forEach(w => {
		// simple words
		if (!w.includes(separator) || w.startsWith(separator) || w.endsWith(separator)) {
			uniqueWords.add(w);
			return;
		}

		// compound words
		let partMissing = false;
		const parts = w.split(separator);
		if (parts.length > 1) {
			for (const splw of parts) {
				if (splw.length === 0) {
					continue;
				}

				if (!lowerCaseWords.has(splw.toLocaleLowerCase(locale))) {
					partMissing = true;
					break;
				}
			}
		}

		if (partMissing) {
			uniqueWords.add(w);
		}
	});

	return uniqueWords;
}


function wordsToLowerCase(locale, words) {
	const lowerWords = new Set();
	if (words instanceof Set) {
		words.forEach(w => lowerWords.add(w.toLocaleLowerCase(locale)))
	}
	return lowerWords;
}


async function work({ fileName, locale, separator }) {
	const words = await readWords(fileName);
	return removeCompoundWords(locale, words, wordsToLowerCase(locale, words), separator);
}



/** main **/
work(validateInput())
	.then(words => printWords(words))
	.catch(e => printError(e));
