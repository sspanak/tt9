const { basename } = require('path');
const { createReadStream, existsSync } = require('fs');
const { createInterface } = require('readline');
const { print, printError } = require('./_printers.js');


function printHelp() {
	print(`Usage ${basename(process.argv[1])} aosp-dictionary-file.txt [minimum-frequency] [--no-freq]`);
	print('Converts an AOSP dictionary to TT9 compatible format. The second parameter must be an integer and allows for filtering words with frequency less than the given number. If "--no-freq" is set, only words without frequencies will be listed.');
}



function validateInput() {
	if (process.argv.length < 3) {
		printHelp();
		process.exit(1);
	}

	if (!existsSync(process.argv[2])) {
		printError(`Failure! Could not find dictionary file "${process.argv[2]}".`);
		process.exit(2);
	}

	return {
		fileName: process.argv[2],
		minFrequency: Number.isNaN(Number.parseInt(process.argv[3])) ? 0 : Number.parseInt(process.argv[3]),
		noFrequencies: process.argv[4] === '--no-freq'
	};
}


function printWords(wordList) {
	if (Array.isArray(wordList)) {
		wordList.forEach(w => print(w));
	}
}


async function convert({ fileName, minFrequency, noFrequencies }) {
	const words = [];

	let lineReader = createInterface({ input: createReadStream(fileName) });
	for await (const line of lineReader) {
		let word = line
			.replace(/^dictionary=main.+$/, '')
			.replace(/^\s+/, '')
			.replace(/^shortcut=.+/, '')
			.replace(/^word=([^,]+),f=(\d+)($|,.+$)/, '$1\t$2');

		if (minFrequency && word !== '') {
			const parts = word.split('\t');
			if (!(parts.length > 1 && Number.parseInt(parts[1]) >= minFrequency)) {
				word = '';
			}
		}

		if (noFrequencies) {
			const parts = word.split('\t');
			if (parts.length > 0) {
				word = parts[0];
			}
		}


		if (word !== '') {
			words.push(word);
		}
	}

	return words;
}


/** main **/
convert(validateInput())
	.then(words => printWords(words))
	.catch(e => printError(e));