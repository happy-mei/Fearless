grammar Example;
@header {package generated;}
e: mCall | x | lambda;
mCall: 'MCall';
x: 'X';
lambda: 'Lambda';

nudeE : e EOF;