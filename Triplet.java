

public class Triplet{
    public int i,j,l;
    public double a;  //mean of all the values from index i to j
    double score;

    public Triplet (double a, int i, int j,double score,int l)
    {
        this.i = i;
        this.j = j;
        this.a =a;
        this.score = score;
        this.l = l;
    }

}