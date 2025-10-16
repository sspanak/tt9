const { basename } = require('path');
const { createReadStream, existsSync } = require('fs');
const { print, printError } = require('./_printers.js');


const DELIMITER = '	';


function printHelp() {
	print(`Usage ${basename(process.argv[1])} LOCALE DICTIONARY-FILE-NAME.txt WORDS-WITH-FREQUENCIES.txt --transcribed --prefer-higher`);
	print('Matches up the words from DICTIONARY-FILE-NAME with the frequencies in WORDS-WITH-FREQUENCIES file.');
	print('	--transcribed: use for transcribed languages where the second column of the input file contains a transcription');
	print('	--prefer-higher: when there are repeating words, prefer the higher frequency rather than the last encountered');
	print('LOCALE could be any valid JS locale, for exmaple: en, en-US, etc...');
}


function validateInput() {
	if (process.argv.length < 5) {
		printHelp();
		process.exit(1);
	}


	if (!existsSync(process.argv[4])) {
		printError(`Failure! Could not find the WORDS-WITH-FREQUENCIES file "${process.argv[4]}".`);
		process.exit(2);
	}


	if (!existsSync(process.argv[3])) {
		printError(`Failure! Could not find dictionary file "${process.argv[3]}".`);
		process.exit(2);
	}


	let higher = false;
	let transcribed = false;
	for (const arg of process.argv) {
		if (arg === '--prefer-higher') higher = true;
		if (arg === '--transcribed') transcribed = true;
	}

	return {
		locale: process.argv[2],
		dictionaryFileName: process.argv[3],
		higher,
		transcribed,
		wordsWithFrequenciesFileName: process.argv[4]
	};
}


async function inject({ wordsWithFrequenciesFileName, dictionaryFileName, locale, higher, transcribed }) {
	// read the frequencies
	let lineReader = require('readline').createInterface({
	  input: createReadStream(wordsWithFrequenciesFileName)
	});


	const frequencies = new Map();
	for await (const line of lineReader) {
		if (!line.includes(DELIMITER)) {
			continue;
		}

		const parts = line.split(DELIMITER);
		const wordId = transcribed && parts.length >= 2 ? `${parts[0]}${parts[1]}` : parts[0].toLocaleLowerCase(locale);
		let frequency = parts.length > 1 ? Number.parseInt(parts[parts.length - 1]) : 0;
		if (Number.isNaN(frequency) || frequency < 0) {
			frequency = 0;
		}

		if (higher) {
			if (frequencies.get(wordId) === undefined || frequencies.get(wordId) < frequency) {
				frequencies.set(wordId, frequency)
			}
		} else {
			frequencies.set(wordId, frequency)
		}

	}

	// read the dictionary words
	lineReader = require('readline').createInterface({
	  input: createReadStream(dictionaryFileName)
	});


	const outputWords = [];
	for await (const line of lineReader) {
		let wordId = '';

		if (transcribed) {
			const parts = line.split(DELIMITER);
			wordId = parts.length > 1 ? `${parts[0]}${parts[1]}` : parts[0];
		} else {
			wordId = line.toLocaleLowerCase(locale);
		}

		outputWords.push(`${line}${ (frequencies.get(wordId) || 0) > 0 ? DELIMITER + frequencies.get(wordId) : '' }`);
	}

	return outputWords;
}


function printWords(wordList) {
	if (Array.isArray(wordList)) {
		wordList.forEach(w => print(w));
	}
}



/** main **/
inject(validateInput())
	.then(words => printWords(words))
	.catch(e => printError(e));