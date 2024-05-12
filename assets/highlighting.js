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
			'read/imm',
		],
	},
	contains: [
		hljs.COMMENT(/\/\//, /\n/),
	],
}));
hljs.highlightAll();
