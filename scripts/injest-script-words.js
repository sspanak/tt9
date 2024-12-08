const { basename } = require('path');
const { createReadStream, existsSync } = require('fs');
const { createInterface } = require('readline');
const { print, printError } = require('./_printers.js');


function printHelp() {
	print(`Usage ${basename(process.argv[1])} word-list.txt UnicodeRange [ExcludeRange]`);
	print('Extracts words that belong to a given unicode range. Assumes all punctuation, whitespace and foreign characters as word separators.');
	print('Example UnicodeRange: U+900-U+97F');
	print('Example ExcludeRange: U+950-U+954U+964-U+971U+200CU+200D');
}



function validateInput() {
	if (process.argv.length < 3) {
		printHelp();
		process.exit(1);
	}

	if (!existsSync(process.argv[2])) {
		printError(`Failure! Could not find word list file "${process.argv[2]}".`);
		process.exit(2);
	}

	if (!validateUnicodeRange(process.argv[3])) {
		printError(`Failure! Invalid or missing search Unicode range(s): "${process.argv[3]}".`);
		process.exit(2);
	}

	if (process.argv[4] && !validateUnicodeRange(process.argv[4])) {
		printError(`Failure! Invalid exclude range(s): "${process.argv[4]}".`);
		process.exit(2);
	}

	return {
		fileName: process.argv[2],
		searchRegexString: process.argv[3],
		excludeRegexString: typeof process.argv[4] === 'string' ? process.argv[4] : ''
	};
}


function validateUnicodeRange(inputRange) {
	return /^([uU]\+[\da-fA-F]+)(\-*[uU]\+[\da-fA-F]+)*$/.test(inputRange);
}


function URangeToXRange(range) {
	if (range.length === 0) {
		return null;
	}

	return range
		.toUpperCase()
		.replaceAll(/U\+([\dA-F]+)/g, "\\u{$1}");
}


function printWords(wordList) {
	if (Array.isArray(wordList)) {
		wordList.forEach(w => print(w));
	}
}


function cleanInvalidChars(line, searchRegex, excludeRegex) {
	const spacesOnly = /^\s+$/;

	if (!line || !line.length || spacesOnly.test(line)) {
		return [];
	}

	const cleanLine = excludeRegex !== null ? line.replace(excludeRegex, ' ') : line;
	return cleanLine
		.replace(searchRegex, ' ')
		.split(' ')
		.filter(w => w.length > 1);
}


async function readWords(fileName, searchRegex, excludeRegex) {
	const words = new Set();

	if (!fileName) {
		return words;
	}

	for await (const line of createInterface({ input: createReadStream(fileName) })) {
		cleanInvalidChars(line, searchRegex, excludeRegex).forEach(w => words.add(w));
	}

	return words;
}


async function work({ fileName, searchRegexString, excludeRegexString }) {
	const searchRegex = new RegExp("[^" + URangeToXRange(searchRegexString) + "]+", "gu");
	const excludeRegex = excludeRegexString.length > 0 ? new RegExp("[" + URangeToXRange(excludeRegexString) + "]+", "gu") : null;

	const words = Array.from(await readWords(fileName, searchRegex, excludeRegex));
	return words.filter(word => word.length > 1).sort();
}



/** main **/
work(validateInput())
	.then(words => printWords(words))
	.catch(e => printError(e));
