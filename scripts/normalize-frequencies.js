const { basename } = require('path');
const { createReadStream, existsSync } = require('fs');
const { createInterface } = require('readline');
const { print, printError, printWordsWithFrequencies } = require('./_printers.js');


function printHelp() {
	print(`Usage ${basename(process.argv[1])} WORD-LIST.txt`);
	print('Normalizes dictionary frequencies up to 255.');
}



function validateInput() {
	if (process.argv.length < 3) {
		printHelp();
		process.exit(1);
	}

	if (!existsSync(process.argv[2])) {
		printError(`Failure! Could not find word list file "${process.argv[3]}".`);
		process.exit(2);
	}

	return {
		fileName: process.argv[2],
		maxAllowedFrequency: 255
	};
}


async function normalize({ fileName, maxAllowedFrequency }) {
	const words = [];

	if (!fileName) {
		return words;
	}

	let maxWordFrequency = 0;

	for await (const line of createInterface({ input: createReadStream(fileName) })) {
		let [word, frequency] = line.split("\t");

		frequency = Number.isNaN(Number.parseInt(frequency)) ? 0 : Number.parseInt(frequency)
		maxWordFrequency = Math.max(maxWordFrequency, frequency);

		words.push({word, frequency});
	}

	const normalizationRatio = maxAllowedFrequency / maxWordFrequency;

	for (word of words) {
		word.frequency = Math.ceil(word.frequency * normalizationRatio);
	}

	return words;
}


/** main **/
normalize(validateInput())
	.then(words => printWordsWithFrequencies(words))
	.catch(e => printError(e));
