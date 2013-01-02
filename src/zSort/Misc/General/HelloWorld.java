class HelloWorld {

  public static void main (String args[]) {

    System.out.println("Hello World!");

  }
  
}

class Count {

  public static void main (String args[]) {
  
    int i;

    for (i = 0; i < 50; i=i+1) { 
      System.out.println(i);
    }
    
  }
  
}
class Fibonacci {

  public static void main (String args[]) {
  
    int low = 1;
    int high = 0;
    
    System.out.println(low);
    while (high < 50) {
      System.out.println(high);
      int temp = high;
      high = high + low;
      low = temp;
    }
    
  }
   
}
class printArgs {

  public static void main (String args[]) {

    for (int i = 0; i < args.length; i++) { 
      System.out.println(args[i]);
    }
    
  }
  
}

