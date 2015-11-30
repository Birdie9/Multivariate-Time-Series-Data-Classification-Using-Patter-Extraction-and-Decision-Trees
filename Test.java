
public class Test {
int attr;
double threshold;
double[] pattern;
double score;
Test(int a, double th, double[] p,double s)
{
	attr=a;
	threshold=th;
	pattern=p;
	score=s;
}
public String toString()
{
	return "Attribute: "+attr+"\n"+"Threshold: "+threshold+"\n"+"Pattern: "+ printDouble(pattern)+"\n"+"Score: "+score +"\n"; 
}
private String printDouble(double[] a) {
	// TODO Auto-generated method stub
	String ret="";
	for(int i=0;i<a.length;i++) ret+=(a[i]+ " ");
	return ret;
}
}
