const { basename } = require('path');
const { createReadStream, existsSync } = require('fs');
const { createInterface } = require('readline');


function printHelp() {
	console.log(`Usage ${basename(process.argv[1])} DICTIONARY_LOCALE DICTIONARY.TXT FOREIGN_WORDS_LOCALE FOREIGN-WORD-DICTIONARY.txt`);
	console.log('Removes foreign words from a dictionary');
}



function validateInput() {
	if (process.argv.length < 6) {
		printHelp();
		process.exit(1);
	}

	if (!existsSync(process.argv[3])) {
		console.error(`Failure! Could not find words file "${process.argv[3]}."`);
		process.exit(2);
	}

	if (!existsSync(process.argv[5])) {
		console.error(`Failure! Could not find foreign words file "${process.argv[4]}."`);
		process.exit(2);
	}

	return {
		locale: process.argv[2],
		fileName: process.argv[3],
		foreignWordsLocale: process.argv[4],
		foreignWordsFileName: process.argv[5]
	};
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



async function removeForeignWords({ locale, foreignWordsLocale, fileName, foreignWordsFileName }) {
	const foreignWords = {};

	let lineReader = createInterface({ input: createReadStream(foreignWordsFileName) });
	for await (const line of lineReader) {
		foreignWords[getLowercaseWordKey(foreignWordsLocale, line)] = true;
	}


	const wordMap = {};
	lineReader = createInterface({ input: createReadStream(fileName) });
	for await (const line of lineReader) {
		const word = getWordkey(line);
		const lowercaseWord = getLowercaseWordKey(locale, line);

		if (word === '') {
			continue;
		}


		if (!foreignWords[lowercaseWord]) {
			wordMap[word] = true;
		}
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
removeForeignWords(validateInput())
	.then(words => printWords(words))
	.catch(e => console.error(e));
