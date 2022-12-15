const { basename } = require('path');
const { createReadStream, existsSync } = require('fs');


const GEO_NAME = /[A-Z]\w+\-[^\n]+/;


function printHelp() {
	console.log(`Usage ${basename(process.argv[1])} LOCALE FILENAME.txt `);
	console.log('Removes repeating words from a word list');
	console.log('\nLocale could be any valid JS locale, for exmaple: en, en-US, etc...');
}



function validateInput() {
	if (process.argv.length < 4) {
		printHelp();
		process.exit(1);
	}


	if (!existsSync(process.argv[3])) {
		console.error(`Failure! Could not find file "${process.argv[3]}."`);
		process.exit(2);
	}

	return { fileName: process.argv[3], locale: process.argv[2] };
}



function getRegularWordKey(locale, word) {
	if (typeof word !== 'string' || word.length === 0) {
		return '';
	}

	return GEO_NAME.test(word) ? word : word.toLocaleLowerCase(locale);
}



function getWordKeyPreservingCaptialization(locale, word, wordMap) {
	if (typeof word !== 'string' || word.length === 0 || typeof wordMap !== 'object') {
		return '';
	}

	let wordKey = word.toLocaleLowerCase(locale);

	if (GEO_NAME.test(word) || word.toLocaleLowerCase(locale) !== word) {
		wordKey = word;
		if (wordMap[word.toLocaleLowerCase(locale)]) {
			delete wordMap[word.toLocaleLowerCase(locale)];
		}
	}

	return wordKey;
}



async function removeRepeatingWords({ fileName, locale }) {
	const lineReader = require('readline').createInterface({
	  input: createReadStream(fileName)
	});

	const wordMap = {};

	for await (const line of lineReader) {
		wordMap[getWordKeyPreservingCaptialization(locale, line, wordMap)] = true;
	}

	return Object.keys(wordMap);
}



function printWords(wordList) {
	if (!Array.isArray(wordList)) {
		return;
	}

	wordList.forEach(w => console.log(w));
}



/** main **/
removeRepeatingWords(validateInput())
	.then(words => printWords(words))
	.catch(e => console.error(e));
