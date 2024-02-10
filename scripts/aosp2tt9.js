const { basename } = require('path');
const { createReadStream, existsSync } = require('fs');
const { createInterface } = require('readline');


function printHelp() {
	console.log(`Usage ${basename(process.argv[1])} aosp-dictionary-file.txt [minimum-frequency] [--no-freq]`);
	console.log('Converts an AOSP dictionary to TT9 compatible format. The second parameter must be an integer and allows for filtering words with frequency less than the given number. If "--no-freq" is set, only words without frequencies will be listed.');
}



function validateInput() {
	if (process.argv.length < 3) {
		printHelp();
		process.exit(1);
	}

	if (!existsSync(process.argv[2])) {
		console.error(`Failure! Could not find dictionary file "${process.argv[3]}."`);
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
		wordList.forEach(w => console.log(w));
	}
}


async function convert({ fileName, minFrequency, noFrequencies }) {
	words = [];

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
	.catch(e => console.error(e));