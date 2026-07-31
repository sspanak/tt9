const { basename } = require('path');
const { createReadStream, existsSync } = require('fs');
const { createInterface } = require('readline');
const { print, printError, printWordsWithFrequencies } = require('./_printers.js');


function printHelp() {
	print(`Usage ${basename(process.argv[1])} LOCALE WORD-LIST.txt LANGUAGE-DEFINITION.yml`);
	print('Sorts a dictionary for optimum search speed.');
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

	if (!existsSync(process.argv[4])) {
		printError(`Failure! Could not find language definition file "${process.argv[4]}".`);
		process.exit(2);
	}

	return {
		definitionFile: process.argv[4],
		wordsFile: process.argv[3],
		locale: process.argv[2]
	};
}


async function readWords(fileName, letterWeights, locale) {
	const words = [];

	if (!fileName) {
		return words;
	}

	for await (const line of createInterface({ input: createReadStream(fileName) })) {
		let [word, frequency] = line.split("\t");
		frequency = Number.parseInt(frequency);

		words.push({
			frequency: Number.isNaN(frequency) ? 0 : frequency,
			sortKey: makeSortKey(word, letterWeights, locale),
			word,
		});
	}

	return words;
}


async function readDefinition(fileName) {
	if (!fileName) {
		return new Map();
	}

	let lettersPattern = /^\s+-\s*\[([^\]]+)/;
	let letterWeights = new Map([["'", 1], ['-', 1], ['"', 1], ['.', 1]]);

	let key = 2;
	for await (const line of createInterface({ input: createReadStream(fileName) })) {
		if (line.includes('SPECIAL') || line.includes('PUNCTUATION')) {
			continue;
		}

		const matches = line.match(lettersPattern);
		if (matches && matches[1]) {
			const letters = matches[1].replace(/\s/g, '').split(',');
			for (let l of letters) {
				letterWeights.set(l, key);
			}
			key++;
		}
	}

	return letterWeights;
}


function makeSortKey(word, letterWeights, locale) {
	const lower = word.toLocaleLowerCase(locale);

	const key = new Array(lower.length + 1);
	key[0] = lower.length;

	for (let i = 0; i < lower.length; i++) {
		key[i + 1] = letterWeights.get(lower[i]);
		key[i + 1] = key[i + 1] === undefined ? 0 : key[i + 1];
	}

	return key;
}


function dictionarySort(a, b) {
	const ka = a.sortKey;
	const kb = b.sortKey;

	for (let i = 0; i < ka.length; i++) {
		if (ka[i] !== kb[i]) {
			return ka[i] - kb[i];
		}
	}

	return 0;
}


async function work({ definitionFile, wordsFile, locale }) {
	const letterWeights = await readDefinition(definitionFile);
	const words = await readWords(wordsFile, letterWeights, locale);
	return words.sort((a, b) => dictionarySort(a, b));
}



/** main **/
work(validateInput())
	.then(words => printWordsWithFrequencies(words))
	.catch(e => printError(e));
