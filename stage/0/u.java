// -*- mode: Java; c-basic-offset: 3; tab-width: 8; indent-tabs-mode: nil -*-

import java.io.FileReader;
import gloop.*;

public class u {
   final public static String rcsid = "$Header$";

   public static void main (String [] a) {
      try {
         try {
            Tokenizer t = new Tokenizer (new FileReader ("test.in"));
            Parser p = new Parser (t);
            CodeStore cs = new CodeStore ();
            Code c = new Code (cs);
            p.parse (c, new LocalScope (new GlobalScope ()), Tokenizer.EOF);
            c.put ("stop");
            c.finish ();
            Tokenizer.flush ();
            cs.dump ();

            Runner prg = cs.getProg ();
            prg.run ();
         } finally {
            Tokenizer.flush ();
         }
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
