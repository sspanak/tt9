const { basename } = require('path');
const { createReadStream, existsSync } = require('fs');



function printHelp() {
	console.log(`Usage ${basename(process.argv[1])} LOCALE FILENAME.txt `);
	console.log('Removes repeating words from a word list');
	console.log('\nLocale could any valid JS locale, for exmaple: en, en-US, etc...');
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



async function removeRepeatingWords({ fileName, locale }) {
	const lineReader = require('readline').createInterface({
	  input: createReadStream(fileName)
	});

	const geographicalName = /[A-Z]\w+\-[^\n]+/;
	const wordMap = {};

	for await (const line of lineReader) {
		const wordKey = geographicalName.test(line) ? line : line.toLocaleLowerCase(locale);
		wordMap[wordKey] = true
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
removeRepeatingWords(validateInput()).then(words => printWords(words));
