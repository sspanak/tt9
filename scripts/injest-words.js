const { basename } = require('path');
const { createReadStream, existsSync } = require('fs');
const { createInterface } = require('readline');


function printHelp() {
	console.log(`Usage ${basename(process.argv[1])} word-list.txt [split-ignore-list.txt]`);
	console.log('Breaks dashed words into separate words, puts multiple words on a line on new lines and deletes repeating new lines.');
	console.log('The split-ignore-list is optional. Allows for not splitting certain words by dashes.');
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

	if (process.argv[3] && !existsSync(process.argv[3])) {
		console.error(`Failure! Could not ignore list file "${process.argv[3]}."`);
		process.exit(2);
	}

	return {
		fileName: process.argv[2],
		ignoreListFileName: process.argv[3]
	};
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


function splitDashedWords(inputWords, ignoreList) {
	const ignoreWords = ignoreList instanceof Set ? ignoreList : new Set();
	if (!(inputWords instanceof Set)) {
		return [];
	}

	const dashedRoots = new Set();
	const repeatingDashedRoots = new Set();
	const outputWords = new Set();

	for (const word of inputWords) {
		if (ignoreWords.has(word)) {
			continue;
		}

		const [root, ...others] = word.split('-');
		if (root === undefined || others.length != 1) {
			continue;
		}

		if (dashedRoots.has(root)) {
			repeatingDashedRoots.add(root);
		} else {
			dashedRoots.add(root);
		}
	}

	for (const word of inputWords) {
		const [root, ...others] = word.split('-');
		if (root && others.length === 1 && repeatingDashedRoots.has(root)) {
			outputWords.add(`${root}-`);
			outputWords.add(others.join('-'));
		} else {
			outputWords.add(word);
		}
	}

	return Array.from(outputWords);
}


async function readWords(fileName) {
	const words = new Set();

	if (!fileName) {
		return words;
	}

	for await (const line of createInterface({ input: createReadStream(fileName) })) {
		cleanSpecialChars(line).forEach(w => words.add(w));
	}

	return words;
}


async function work({ fileName, ignoreListFileName }) {
	const [ words, ignoreList ] = await Promise.all([ readWords(fileName), readWords(ignoreListFileName) ]);
	const splitWords = splitDashedWords(words, ignoreList);
	const filteredAndSortedWords = splitWords.filter(word => word.length > 1).sort();

	return filteredAndSortedWords;
}



/** main **/
work(validateInput())
	.then(words => printWords(words))
	.catch(e => console.error(e));
