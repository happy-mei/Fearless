package main

var baseφrtφGlobalLaunchArgs φbaseφLList_1 = φbaseφLList_1Impl{}

func baseφrtφConvertBool(b bool) φbaseφBool_0 {
	if b {
		return φbaseφTrue_0Impl{}
	} else {
		return φbaseφFalse_0Impl{}
	}
}

func baseφrtφPow[N int64 | uint64](base N, exp uint64) N {
	var res = base
	if exp == 0 {
		return 1
	}
	for ; exp > 1; exp-- {
		res *= base
	}
	return res
}

func baseφrtφAbs[N int64 | float64](n N) N {
	if n < 0 {
		return -n
	}
	return n
}
