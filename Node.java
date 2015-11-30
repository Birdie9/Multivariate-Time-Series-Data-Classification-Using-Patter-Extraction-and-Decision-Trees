
public class Node {
	String child;
	public Test test;
	public int decision;
	public boolean leaf;
	public Node left;
	public Node right;
public String toString(){
	if(leaf) return "class: " +decision +"\n";
	else return test.toString();
}
}
