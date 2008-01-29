// -*- mode: Java; c-basic-offset: 3; tab-width: 8; indent-tabs-mode: nil -*-

import java.io.FileReader;
import gloop.*;

public class u {
   public static void main (String [] a) {
      try {
         Tokenizer t = new Tokenizer (new FileReader ("test.in"));
         Parser p = new Parser (t);
         p.parse (new GlobalScope (), Tokenizer.EOF);
      } catch (java.io.IOException e) {
         System.out.println ("IOEx: " + e);
         e.printStackTrace ();
      } catch (Tokenizer.TokEx e) {
         System.out.println ("TokEx: " + e);
         e.printStackTrace ();
      }
    }
}

// Local variables:
// compile-command: "javac -Xlint:unchecked u.java gloop/*.java && java u"
// End:
