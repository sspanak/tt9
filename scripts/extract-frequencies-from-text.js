const { basename } = require('path');
const { createReadStream, existsSync } = require('fs');
const { createInterface } = require('readline');
const { print, printError } = require('./_printers.js');


function printHelp() {
	print(`Usage ${basename(process.argv[1])} text.txt UnicodeRange [ExcludeRange] [EraseRange]`);
	print('From the given text file, extracts and counts the unique words that belong to a given unicode range. Assumes all punctuation, whitespace and foreign characters as word separators.');
	print('Example UnicodeRange: U+900-U+97FU+200CU+200D');
	print('Example ExcludeRange: U+950-U+957U+966-U+97F');
	print('Example EraseRange: U+964U+965U+970U+971');
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

	if (process.argv[5] && !validateUnicodeRange(process.argv[5])) {
		printError(`Failure! Invalid exclude range(s): "${process.argv[5]}".`);
		process.exit(2);
	}

	return {
		fileName: process.argv[2],
		searchRegexString: process.argv[3],
		excludeRegexString: typeof process.argv[4] === 'string' ? process.argv[4] : '',
		eraseRegexString: typeof process.argv[5] === 'string' ? process.argv[5] : ''
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
		wordList.forEach(w => print(`${w.w}\t${w.f}`));
	}
}


function cleanInvalidChars(line, eraseRegex, excludeRegexString) {
	const spacesOnly = /^\s+$/;

	if (!line || !line.length || spacesOnly.test(line)) {
		return [];
	}

	const invalidWordRegex = excludeRegexString.length > 0 ? new RegExp("[" + URangeToXRange(excludeRegexString) + "]", "u") : null;

	return line
		.replace(eraseRegex, ' ')
		.split(' ')
		.filter(w => w.length > 1 && (invalidWordRegex === null || !invalidWordRegex.test(w)));
}


async function readWords(fileName, eraseRegex, excludeRegexString) {
	const words = new Map();

	if (!fileName) {
		return words;
	}

	for await (const line of createInterface({ input: createReadStream(fileName) })) {
		const parts = cleanInvalidChars(line, eraseRegex, excludeRegexString);
		parts.forEach(w => {
			words.set(w, words.has(w) ? words.get(w) + 1 : 1);
		});
	}

	return words;
}


function sortWords(wordsMap) {
	const words = [];
	for (let [w, f] of wordsMap) {
		words.push({ w, f });
	}

	return words.sort((a, b) => {
		if (a.f > b.f) {
			return -1;
		}

		if (a.f < b.f) {
			return 1;
		}

		if (a.w < b.w) {
			return -1;
		}

		if (a.w > b.w) {
			return 1;
		}

		return 0;
	});
}


async function work({ fileName, searchRegexString, excludeRegexString, eraseRegexString }) {
	const eraseRegex = new RegExp("([^" + URangeToXRange(searchRegexString) + " ]+|[" + URangeToXRange(eraseRegexString) + "])", "gu");
	return sortWords(
		await readWords(fileName, eraseRegex, excludeRegexString)
	);
}



/** main **/
work(validateInput())
	.then(words => printWords(words))
	.catch(e => printError(e));
