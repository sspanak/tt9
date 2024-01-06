const { basename } = require('path');
const { createReadStream, existsSync } = require('fs');
const { createInterface } = require('readline');


function printHelp() {
	console.log(`Usage ${basename(process.argv[1])} word-list.txt`);
	console.log('Breaks dashed words into separate words, puts multiple words on a line on new lines and deletes repeating new lines.');
}



function validateInput() {
	if (process.argv.length < 3) {
		printHelp();
		process.exit(1);
	}

	if (!existsSync(process.argv[2])) {
		console.error(`Failure! Could not find word list file "${process.argv[3]}."`);
		process.exit(2);
	}

	return {
		fileName: process.argv[2]
	}
}


function printWords(wordList) {
	if (Array.isArray(wordList)) {
		wordList.forEach(w => console.log(w));
	}
}


function cleanSpecialChars(line) {
	const spacesOnly = /^\s+$/;
	const digits = /\d+/;

	if (!line || !line.length || spacesOnly.test(line)) {
		return [];
	}

	return line
		.replace(/[\x01-\x20&",:;*\/\[\].?()]+/g, ' ')
		.split(' ')
		.filter(w => w.length > 1 && !digits.test(w));
}


function splitDashedWords(inputWords) {
	if (!Array.isArray(inputWords)) {
		return [];
	}

	const wordsSet = new Set();

	for (const word of inputWords) {
		if (!word.includes('-')) {
			wordsSet.add(word);
			continue;
		}

		const parts = word.split('-');
		let root = '';

		for (let i = 0; i < parts.length - 1; i++) {
			root += `${parts[i]}-`;
			wordsSet.add(root);
		}

		wordsSet.add(parts[parts.length - 1]);
	}

	return Array.from(wordsSet);
}



async function work({ fileName }) {
	const wordsSet = new Set();

	const lineReader = createInterface({ input: createReadStream(fileName) });

	for await (const line of lineReader) {
		const newWords = cleanSpecialChars(line);

		for (let i = 0; i < newWords.length; i++) {
			wordsSet.add(newWords[i]);
		}
	}

	const wordsArray = Array.from(wordsSet);
	const splitWords = splitDashedWords(wordsArray);
	const filteredAndSortedWords = splitWords.filter(word => word.length > 1).sort();

	return filteredAndSortedWords;
}



/** main **/
work(validateInput())
	.then(words => printWords(words))
	.catch(e => console.error(e));
