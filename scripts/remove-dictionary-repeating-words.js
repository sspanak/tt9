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
	if (typeof word !== 'string' || word.length === 0) {
		return '';
	}

	return word.toLocaleLowerCase(locale);
}



async function removeRepeatingWords({ fileName, locale }) {
	const wordMap = new Map();

	let lineReader = createInterface({ input: createReadStream(fileName) });
	for await (const line of lineReader) {
		const lowercaseKey = getLowercaseWordKey(locale, line);
		if (lowercaseKey === '') {
			continue;
		}

		if (!wordMap.has(lowercaseKey)) {
			wordMap.set(lowercaseKey, line);
		}

		if (wordMap.has(lowercaseKey) && !wordMap.has(line)) {
			wordMap.set(lowercaseKey, line);
		}
	}

	return Array.from(wordMap.values(wordMap)).sort();
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
