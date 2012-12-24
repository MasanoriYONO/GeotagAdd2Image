package net.masanoriyono.GeotagAdd2Image;

import android.util.Log;

public class Fraction {
	//分子
	public long numerator = 0;
	//分母
    public long denominator = 1;
    //約数
    public long factor = 1;        
    
//    public long reduced_numerator;
//	//分母
//    public long reduced_denominator;
    
    Fraction(){
        this.numerator = 0;
        this.denominator = 1;
    }

    Fraction(long numerator, long denominator){
        this.numerator = numerator;
        this.denominator = denominator;
    }

	public Fraction reduce(){
		//numerator:分子 denominator:分母
        long min = Math.min(Math.abs(numerator), Math.abs(denominator));
        for(long i=min; i>1 ; i--)
        {
            if((numerator % i ==0) && (denominator % i ==0))
            {
            	numerator = numerator/i;
            	denominator  = denominator/i;
            	factor = factor * i;
            	
            	Log.d("Fraction","約数:" + factor);
            	Log.d("Fraction","約数後: " + numerator + "/" + denominator);
                
//            	reduced_numerator = numerator;
//            	reduced_denominator = denominator;
            	
            	Fraction f = new Fraction(numerator, denominator);
                f.factor = factor;
                return f;
            }
        }
        return this;
    }
	
	public Fraction reduction(){
        return this.reduce(); 
    }
	
	public long getNumerator(){
		return this.numerator;
	}
	
	public long getDenominator(){
		return this.denominator;
	}
	
//	public long getReducedNumerator(){
//		return this.reduced_numerator;
//	}
//	
//	public long getReducedDenominator(){
//		return this.reduced_denominator;
//	}
}
