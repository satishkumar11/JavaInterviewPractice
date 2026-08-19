package JavaBasics;


final class MathematicalConstants {
    public final double PI = 3.14;

    public void display() {
        System.out.println(PI);
    }
}


//Cannot inherit from final 'JavaBasics.MathematicalConstants'

//class Circle extends MathematicalConstants {
//
//}

public class FinalClass {

    public static void main(String[] args) {
        MathematicalConstants obj = new MathematicalConstants();
        obj.display();
    }
}
