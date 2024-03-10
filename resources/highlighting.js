hljs.registerLanguage('fearless', _ => ({
	keywords: {
		keyword: [
			'imm',
			'mut',
			'iso',
			'read',
			'readH',
			'mutH',
			'this',
		],
	},
	contains: [
		hljs.COMMENT(/\/\//, /\n/),
	],
}));
hljs.highlightAll();
