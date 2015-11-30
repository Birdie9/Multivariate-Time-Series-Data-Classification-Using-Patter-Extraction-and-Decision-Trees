import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.StringTokenizer;

public class TrainingSet {
ArrayList<Tuple> tuples  ;
public static int TIME=100;
BufferedWriter bw;
TrainingSet()
{
	tuples = new ArrayList<Tuple>();
	char[] name = "mHealth_subject1.log".toCharArray();
	for(int i=1;i<=9;i++)
	{
		boolean[] done =new boolean[11];
		name[15] = (char) (i+48) ;
		try {
			Scanner in = new Scanner(new File(new String(name)));
			
			while(in.hasNextLine())
			{
				String line = in.nextLine();
				StringTokenizer st = new StringTokenizer(line,"\t");
				String[] temp =new String[24];
				if(st.countTokens()<24) continue;
				for(int k=0;k<24;k++)
				{
					temp[k]= st.nextToken();
				}
				int classlabel =  Integer.parseInt(temp[23]);
				if(classlabel==0 || classlabel ==12) continue;
				classlabel--;
				if(!done[classlabel])
				{
					done[classlabel]=true;
					Tuple tup = new Tuple();
					tup.classLabel=classlabel;
					for(int j=0;j<TIME;j++)
					{
						String line1 = in.nextLine();
						StringTokenizer st1 = new StringTokenizer(line1,"\t");
						//System.out.println(new String(name)+classlabel);
						if(st1.countTokens()<24) continue;
						for(int k=0;k<23;k++)
						{
							Double temp1 = Double.parseDouble(st1.nextToken());
							tup.attributes[k][j]=temp1;
						}
					}
					tuples.add(tup);
				}
				
			}
			for(int j=0;j<11;j++)
			{
				if(!done[j]) System.err.println(j+" not present in file "+ new String(name) );
			}
			in.close();
		} catch (FileNotFoundException e) {
			System.out.println(new String(name));
			e.printStackTrace();
		}
	}
}

public double[] Score1(ArrayList<Tuple> s,Test test)
{
	int n1=0,n2=0;
	ArrayList<Tuple> s1 =new ArrayList<Tuple>();
	ArrayList<Tuple> s2 =new ArrayList<Tuple>();
	
	for(int i=0;i<s.size();i++)
	{
		if(test(test.pattern,s.get(i).attributes[test.attr], test.threshold)){ n1++; s1.add(s.get(i));}
		else { n2++; s2.add(s.get(i));}
	}
	int n=n1+n2;
	double ict= Hc(s) - ((double)n1/(double)n) * Hc(s1) -((double)n2/(double)n) *Hc(s2);
	//System.out.println(ict);
	double ht =  - ((double)n1/(double)n) *Math.log(((double)n1/(double)n))/ Math.log(2) -((double)n2/(double)n) *Math.log(((double)n2/(double)n))/ Math.log(2);
	//System.out.println(ht);
	//System.out.println("score returned" + ict/ht);
	double[] a1 = new double[2];
	a1[0]=ict;
	a1[1]=ht;
	return a1;
}

public double Hc(ArrayList<Tuple> s)
{
	int[] numClas = new int[11];
	int n= s.size();
	
	for(int i=0;i<s.size();i++)
	{
		numClas[s.get(i).classLabel]++;
	}
	double sum=0;
	for(int i=0;i<11; i++)
	{
		if(numClas[i]==0) continue;
		else sum+= ((double) numClas[i]/(double) n) * Math.log(((double)numClas[i]/(double)n) )/Math.log(2);
	}
	//System.out.println(-sum);
	return -sum;
}
public double Score(ArrayList<Tuple> s, Test t)
{
	double[] a= Score1(s,t);
	return 2*a[0]/(a[1]+Hc(s));
}
public double dist(double[] p, double[] atr)
{
	int n=atr.length;
	int pn=p.length;
	double err=Double.MAX_VALUE;
	for(int i=0;i<=n-pn;i++)
	{
		double temp=0;
		for(int j=0;j<pn;j++)
		{
			temp+= Math.pow(p[j]-atr[i+j], 2);
		}
		if(err> temp) err =temp;
	}
	return err;
}
public boolean test(double[] p, double[] atr,double thresh)
{
	double err= dist( p,  atr);
	if(err<thresh) return true;
	return false;
}
public Test Pattern_Search(ArrayList<Tuple> ls,int attr, double[] a)
{System.out.println("Pattern search..");
	double[] maxpat = null;
	double maxScore = 0;
	double maxd = 0;
	for(int i=0;i<TIME;i++)
	{System.out.println(i);
		for(int len=2;i+len-1<TIME;len++)
		{
			double[] pat =new double[len];
			for(int k=0;k<len;k++) pat[k]=a[i+k];
			
			double[] d = new double[ls.size()];
			for(int k=0;k<d.length;k++)
			{
				d[k]= dist(pat,ls.get(k).attributes[attr]);
			}
			Arrays.sort(d);
			double tmaxScore=0;
			double tmaxd=d[0];
			for( int k=0;k<d.length-1;k++)
			{
				double tempd= (d[k]+d[k+1])/2;
				Test t = new Test(attr,tempd,pat,0);
				double tempScore = Score(ls,t);
				if(tempScore>tmaxScore){ tmaxd = tempd;tmaxScore = tempScore;}
				
			}
			if(tmaxScore>maxScore)
			{
				maxScore=tmaxScore;
				maxd=tmaxd;
				maxpat=pat;
			}
			//System.out.println(maxScore);
		}
	}
	System.out.println("finished pattern search");
	return new Test(attr,maxd,maxpat,maxScore);
}
public double[] convertToDouble(pcModel pc)
{
	double[] ret = new double[TIME];
	for(Segment seg:pc.model)
	{
		for( int i=0;i<seg.d;i++)
		{
			ret[i]=seg.a;
		}
	}
	return ret;
}
public Test SearchBestTest(ArrayList<Tuple> ls)
{System.out.println("Seacrhing Best test");
	PCM pcm=new PCM();
	Test maxt=null;
	ArrayList<ArrayList<Tuple>> clasList = new ArrayList<ArrayList<Tuple>> ();
	for(int i=0;i<11;i++)
	{
		clasList.add(null);
	}
	for(Tuple t: ls)
	{
		if(clasList.get(t.classLabel)==null) clasList.set(t.classLabel,new ArrayList<Tuple>());
		clasList.get(t.classLabel).add(t);
	}
	for(int attr =0;attr<23;attr++)
	{
		for(int cls=0;cls<11;cls++)
		{
			if(clasList.get(cls)!=null)
			{
				int size= clasList.get(cls).size();
				int rand =(int) ((double) size* Math.random());
				Tuple tup =clasList.get(cls).get(rand);
				pcModel model = pcm.pcmWithPruning(tup.attributes[attr]);
				double[] a1 =convertToDouble(model);
				Test t = Pattern_Search(ls,attr,a1);
				if(maxt==null || t.score>maxt.score) maxt=t;
			}
		}
	}
	return maxt;
} 
public int pure(ArrayList<Tuple> ls)
{
	int ret =ls.get(0).classLabel;
	
	for(Tuple t: ls)
	{
		if(t.classLabel!= ret) {ret =-1 ;
		break;}
	}
	for(Tuple t:ls)
	{
		System.out.print(t.classLabel + " ");
	}
	System.out.println();
	return ret;
}
public Node makeTree(ArrayList<Tuple> ts,String str)
{
	Node node = new Node();
	node.child=str;
	int clasNum= pure(ts);
	System.out.println(clasNum+ "ret from pure");
	if(clasNum== -1)
	{
		ArrayList<Tuple>  l1= new ArrayList<Tuple>();
		ArrayList<Tuple> l2 =new ArrayList<Tuple>();
		Test test = SearchBestTest(ts);
		for(Tuple t: ts)
		{
			if(test(test.pattern,t.attributes[test.attr],test.threshold)) l1.add(t);
			else l2.add(t);
		}
		node.leaf=false;
		node.decision=-1;
		node.test= test;
		writeToFile("<Node id: "+node.child+">\n");
		writeToFile(node.toString());
		node.left = makeTree(l1,"left");
		node.right =makeTree(l2,"right");
		writeToFile("<\\Node>\n");
		return node;
	}
	else 
	{
		node.leaf=true;
		node.decision= clasNum;
		node.test= null;
		node.left = null;
		node.right =null;
		writeToFile("<Node id: "+node.child+">\n");
		writeToFile(node.toString());
		writeToFile("<\\Node>\n");
		return node;
	}
}
private void writeToFile(String string) {
	// TODO Auto-generated method stub
	try {	
		bw.write(string);
		System.out.println("Done");

	} catch (IOException e) {
		e.printStackTrace();
	}
}

public static void main(String[] args) throws IOException
{
	TrainingSet ts = new TrainingSet();
	File file = new File("Decision_Tree.txt");

	// if file doesnt exists, then create it
	if (!file.exists()) {
		file.createNewFile();
	}

	FileWriter fw = new FileWriter(file.getAbsoluteFile());
	ts.bw = new BufferedWriter(fw);
	
	Node decisionTreeRoot= ts.makeTree(ts.tuples,"root");
	ts.bw.close();
	ts.printTree(decisionTreeRoot);
	ArrayList<Tuple> testTuples = ts.loadData("mHealth_subject10.log".toCharArray());
	for(int i=0;i<testTuples.size();i++)
	{
		System.out.println("Tuple "+i+ " belongs to class with Label: " + ts.traverseTree(decisionTreeRoot, testTuples.get(i)));
	}
}
private void printTree(Node n) {
	// TODO Auto-generated method stub
	if(n.leaf) System.out.println("class: "+n.decision);
	else
	{
		System.out.println("printing kids");
		printTree(n.left);
		printTree(n.right);
	}
}
private  int traverseTree(Node node, Tuple tuple) {
	// TODO Auto-generated method stub
	if(node.leaf) return node.decision;
	else if (test(node.test.pattern,tuple.attributes[node.test.attr],node.test.threshold)) return traverseTree(node.left, tuple);
	else return traverseTree(node.right, tuple);
	
}
private  ArrayList<Tuple> loadData(char[] name) {
	// TODO Auto-generated method stub
	boolean[] done =new boolean[11];
	 ArrayList<Tuple> tuples1 = new ArrayList<Tuple>();
	try {
		Scanner in = new Scanner(new File(new String(name)));
		
		while(in.hasNextLine())
		{
			String line = in.nextLine();
			StringTokenizer st = new StringTokenizer(line,"\t");
			String[] temp =new String[24];
			if(st.countTokens()<24) continue;
			for(int k=0;k<24;k++)
			{
				temp[k]= st.nextToken();
			}
			int classlabel =  Integer.parseInt(temp[23]);
			if(classlabel==0 || classlabel ==12) continue;
			classlabel--;
			if(!done[classlabel])
			{
				done[classlabel]=true;
				Tuple tup = new Tuple();
				tup.classLabel=classlabel;
				for(int j=0;j<TIME;j++)
				{
					String line1 = in.nextLine();
					StringTokenizer st1 = new StringTokenizer(line1,"\t");
					for(int k=0;k<23;k++)
					{
						Double temp1 = Double.parseDouble(st1.nextToken());
						tup.attributes[k][j]=temp1;
					}
				}
				tuples1.add(tup);
			}
			
		}
		
		in.close();
		return tuples1;
	} catch (FileNotFoundException e) {
		System.out.println(new String(name));
		e.printStackTrace();
	}
	return null;
}
}
