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
			'readOnly',
			'lent',
			'recMdf',
		],
	},
	contains: [
		hljs.COMMENT(/\/\//, /\n/),
	],
}));
hljs.highlightAll();
