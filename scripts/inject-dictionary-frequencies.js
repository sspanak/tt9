const { basename } = require('path');
const { createReadStream, existsSync } = require('fs');


const DELIMITER = '	';


function printHelp() {
	console.log(`Usage ${basename(process.argv[1])} LOCALE DICTIONARY-FILE-NAME.txt WORDS-WITH-FREQUENCIES.txt`);
	console.log('Matches up the words from DICTIONARY-FILE-NAME with the frequencies in WORDS-WITH-FREQUENCIES file.');
	console.log('LOCALE could be any valid JS locale, for exmaple: en, en-US, etc...');
}


function validateInput() {
	if (process.argv.length < 5) {
		printHelp();
		process.exit(1);
	}


	if (!existsSync(process.argv[4])) {
		console.error(`Failure! Could not find the WORDS-WITH-FREQUENCIES file "${process.argv[4]}."`);
		process.exit(2);
	}


	if (!existsSync(process.argv[3])) {
		console.error(`Failure! Could not find dictionary file "${process.argv[3]}."`);
		process.exit(2);
	}

	return {
		locale: process.argv[2],
		dictionaryFileName: process.argv[3],
		wordsWithFrequenciesFileName: process.argv[4]
	};
}


async function inject({ wordsWithFrequenciesFileName, dictionaryFileName, locale }) {
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
		const word = parts[0].toLocaleLowerCase(locale);
		let frequency = parts.length > 1 ? Number.parseInt(parts[1]) : 0;
		if (Number.isNaN(frequency) || frequency < 0) {
			frequency = 0;
		}

		frequencies.set(word, frequency)
	}

	// read the dictionary words
	lineReader = require('readline').createInterface({
	  input: createReadStream(dictionaryFileName)
	});


	const outputWords = [];
	for await (const word of lineReader) {
		const lowercaseWord = word.toLocaleLowerCase(locale);
		outputWords.push(`${word}${ (frequencies.get(lowercaseWord) || 0) > 0 ? DELIMITER + frequencies.get(lowercaseWord) : '' }`);
	}

	return outputWords;
}


function printWords(wordList) {
	if (Array.isArray(wordList)) {
		wordList.forEach(w => console.log(w));
	}
}



/** main **/
inject(validateInput())
	.then(words => printWords(words))
	.catch(e => console.error(e));