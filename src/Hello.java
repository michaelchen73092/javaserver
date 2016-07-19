import java.lang.Object.*;
import java.util.*;
import IRUtilities.*;
import Pair.Pair;

public class Hello
{

	
	public static  void main(String args[])
    {

		String teststr = "delete.";
		Porter porter = new Porter();
		teststr = porter.stripAffixes(teststr);
		System.out.println(teststr);
		Pair pair1 = new Pair("test1",new Integer(66));
		Pair pair2 = new Pair("test2",new Integer(66));
		Pair[] pairarry = new Pair[2];
		pairarry[0] = pair1;
		pairarry[1] = pair2;
		System.out.println(pair1.compareTo(pair2));

		System.out.println(pairarry[0].compareTo(pairarry[1]));
		Arrays.sort(pairarry);
		for(int i=0;i<pairarry.length;i++){
			System.out.println(pairarry[i].getT());
			System.out.println(pairarry[i].getE());

		}

		return;




    }

    
    
    
}


