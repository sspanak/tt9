const { basename } = require('path');
const { createReadStream, existsSync } = require('fs');
const { createInterface } = require('readline');

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



function getLowercaseWordKey(locale, word) {
	return getWordkey(word).toLocaleLowerCase(locale);
}



function getWordkey(word) {
	if (typeof word !== 'string' || word.length === 0) {
		return '';
	}

	return word;
}



async function removeRepeatingWords({ fileName, locale }) {

	const wordMap = {};

	let lineReader = createInterface({ input: createReadStream(fileName) });
	for await (const line of lineReader) {
		wordMap[getLowercaseWordKey(locale, line)] = true;
	}


	lineReader = createInterface({ input: createReadStream(fileName) });
	for await (const line of lineReader) {
		const word = getWordkey(line);
		const lowercaseWord = getLowercaseWordKey(locale, line);

		if (word === '') {
			continue;
		}


		if (word !== lowercaseWord) {
			delete wordMap[lowercaseWord];
			wordMap[word] = true;
		}
	}

	return Object.keys(wordMap).sort();
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
