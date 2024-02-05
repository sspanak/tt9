const { basename } = require('path');
const { createReadStream, existsSync } = require('fs');
const { createInterface } = require('readline');


function printHelp() {
	console.log(`Usage ${basename(process.argv[1])} LOCALE WORD-LIST.txt LANGUAGE-DEFINITION.yml`);
	console.log('Sorts a dictionary for optimum search speed.');
}



function validateInput() {
	if (process.argv.length < 4) {
		printHelp();
		process.exit(1);
	}

	if (!existsSync(process.argv[3])) {
		console.error(`Failure! Could not find word list file "${process.argv[3]}."`);
		process.exit(2);
	}

	if (!existsSync(process.argv[4])) {
		console.error(`Failure! Could not find language definition file "${process.argv[3]}."`);
		process.exit(2);
	}

	return {
		definitionFile: process.argv[4],
		wordsFile: process.argv[3],
		locale: process.argv[2]
	};
}


function printWords(wordList) {
	if (Array.isArray(wordList)) {
		wordList.forEach(w => console.log(`${w.word}${w.frequency ? '\t' + w.frequency : ''}`));
	}
}


async function readWords(fileName) {
	const words = [];

	if (!fileName) {
		return words;
	}

	for await (const line of createInterface({ input: createReadStream(fileName) })) {
		const [word, frequency] = line.split("\t");
		words.push({ 
			word, 
			frequency: Number.isNaN(Number.parseInt(frequency)) ? 0 : Number.parseInt(frequency) 
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
			letters.forEach(l => letterWeights.set(l, key));
			key++;
		}
	}

	return letterWeights;
}


function dictionarySort(a, b, letterWeights, locale) {
	if (a.word.length !== b.word.length) {
		return a.word.length - b.word.length;
	}

	for (let i = 0, end = a.word.length; i < end; i++) {
		const charA = a.word.toLocaleLowerCase(locale).charAt(i);
		const charB = b.word.toLocaleLowerCase(locale).charAt(i);
		const distance = letterWeights.get(charA) - letterWeights.get(charB);


		if (distance !== 0) {
			return distance;
		}
	}

	return 0;
}


async function work({ definitionFile, wordsFile, locale }) {
	return Promise.all([
		readWords(wordsFile),
		readDefinition(definitionFile)
	]).then(([words, letterWeights]) =>
		words.sort((a, b) => dictionarySort(a, b, letterWeights, locale))
	);
}



/** main **/
work(validateInput())
	.then(words => printWords(words))
	.catch(e => console.error(e));