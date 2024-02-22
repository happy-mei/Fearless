package baseφrt

import "main/userCode/base"

func ConvertBool(b bool) base.ΦBool_0 {
	if b {
		return base.ΦTrue_0Impl{}
	} else {
		return base.ΦFalse_0Impl{}
	}
}

func Pow[N int64 | uint64](base N, exp uint64) N {
	var res = base
	if exp == 0 { return 1 }
	for ; exp > 1; exp-- {
		res *= base
	}
	return res
}

func Abs[N int64 | float64](n N) N {
	if n < 0 { return -n }
	return n
}
