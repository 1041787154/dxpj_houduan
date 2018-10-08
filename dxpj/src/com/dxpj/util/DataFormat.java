package com.dxpj.util;

import java.math.BigDecimal;

public class DataFormat {

	public double doubleToTwo(double rr) {
		 
		BigDecimal bigDecimal = new BigDecimal(rr);  
		//这里的 2 就是你要保留几位小数。  
		double f1 = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		return f1;  
		
	}
	 
}
