
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;


public class PCM {

    public double Variance(double[] a,int lowerindex, int upperindex)
    {
        double var=0;
        double mean = Mean(a,lowerindex,upperindex);

        for(int l=lowerindex;l<upperindex;l++)
            var = var + ((a[l] - mean)*(a[l] - mean));

        return var/(upperindex - lowerindex);

    }
    public double Score(double[] a,int lower, int upper, int l)
    {
        return   (upper-lower)*Variance(a,lower,upper) - (upper-l)*Variance(a,l,upper) -(l-lower)*Variance(a,lower,l);
    }

    public double Mean(double[] a,int lowerindex, int upperindex)
    {
        double answer=0;
        for(int l=lowerindex;l <upperindex;l++)
            answer = answer + a[l];

        return answer/(upperindex-lowerindex);
    }

    public ArrayList<Segment> Build_PCM(double []a, int S)
    {
        int i = 0, j = a.length, l=0;
        double val = Mean(a,i,j);
        double score=0;

        for(int k=0;k<j;k++)
        {
            double temp = Score(a,i, j, k);
            if (score <temp)
            {
                l = k;
                score = temp;
            }
        }

        Triplet t = new Triplet(val,i,j,score,l);
        ArrayList <Triplet> list = new ArrayList<Triplet>();
        list.add(t);

        while(list.size()<S)
        {
            double maxscore =-1;
            int maxindex =Integer.MIN_VALUE;

            for(i=0;i<list.size();i++)
            {
                if (list.get(i).score > maxscore)
                {
                    maxscore = list.get(i).score;
                    maxindex = i;
                }
            }
            int l1 = list.get(maxindex).l;
            i = list.get(maxindex).i;
            j = list.get(maxindex).j;
            list.remove(maxindex);
            score =0;
            for(int k=i+1;k<l1;k++)
            {
                double temp = Score(a,i,l1,k);
                if (score<temp)
                {
                    l=k;
                    score=temp;
                }
            }
            val = Mean(a,i,l1);
            Triplet t1 = new Triplet(val,i,l1,score,l);
            list.add(t1);
            score =0;
            for(int k=l1+1;k<j;k++)
            {
                double temp = Score(a,l1,j,k);
                if (score<temp)
                {
                    l=k;
                    score=temp;
                }
            }
            val = Mean(a,l1,j);
            Triplet t2 = new Triplet(val,l1,j,score,l);
            list.add(t2);
        }
        Triplet[] ar =new Triplet[list.size()];
        ar=list.toArray(ar);
        Arrays.sort(ar, new Comparator<Triplet>(){
			@Override
			public int compare(Triplet arg0, Triplet arg1) {
				return (arg0.i-arg1.i);
			}
        });
        ArrayList <Segment> list1 = new ArrayList<Segment>();
        for(i=0;i<S;i++)
        {
            Segment s = new Segment(list.get(i).j - list.get(i).i, list.get(i).a);
            list1.add(s);
        }
    return list1;
    }

    public pcModel pcmWithPruning(double[] atr )
    {
    	System.out.println("pruning...");
        double[] atrEv = new double[atr.length/2];
        double[] atrOd = new double[atr.length/2];
        int j=0;
        for(int i=0;i<atr.length;i+=2)
        {
            atrEv[j] =atr[i];
            atrOd[j]=atr[i+1];
            j++;
        }
        ArrayList<ArrayList<Segment>> models = new  ArrayList<ArrayList<Segment>>();
        for(int i=0;i<=atr.length/2;i++ ) models.add(null);
        for(int i=1;i<=atr.length/2;i++)
        {
            models.set(i, Build_PCM(atrEv,i));
        }

        double error=Double.MAX_VALUE;
        int s=1;
        for(int i=1;i<=atr.length/2;i++)
        {
            double temp= findError(models.get(i),atrOd);
            if(error>temp){ error=temp; s=i;}
        }

        return new pcModel(Build_PCM(atr,s));
    }
    public double[] convertToDouble(ArrayList<Segment > p)
    {
    	double[] ret = new double[TrainingSet.TIME/2];
    	for(Segment seg:p)
    	{
    		for( int i=0;i<seg.d;i++)
    		{
    			ret[i]=seg.a;
    		}
    	}
    	return ret;
    }

    private double findError(ArrayList<Segment> mod, double[] atr) {
    	double[] a= convertToDouble(mod);
    	double error=0;
       for(int i=0;i<a.length;i++)
       {
    	   error+= (a[i]-atr[i])*(a[i]-atr[i]); 
       }
        return error;
    }

}
