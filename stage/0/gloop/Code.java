// -*- mode: Java; c-basic-offset: 3; tab-width: 8; indent-tabs-mode: nil -*-
// Copyright (C) 2008 Andreas Krey, Ulm, Germany <a.krey@gmx.de>

package gloop;

import java.util.Vector;
import java.io.ByteArrayOutputStream;

public class Code {

   CodeStore store;

   ByteArrayOutputStream bytes = new ByteArrayOutputStream ();

   private static String [] codes = {
      "nop",
      "up",  // Get frame pointer n levels up
      "lstore", // into a local variable (via current fp)
      "store", // into a scoped variable (via fp in acc)
      "lload", // load from local frame
      "load", // load from frame in acc
      "print",
      "nullval",
      "push",
      "mult",
      "numval",
      "strval",
      "push",
      "swap",
      "call",
      "fun",
      "meth",
      "ret",
      "stop"
   };

   public Code (CodeStore s) {
      store = s;
   }

   public Code (Code c) {
      store = c.store;
   }

   public static String getCode (int i) {
      if (i >= 0 && i < codes.length) {
         return codes [i];
      }
      return null;
   }

   public int finish () {
      return store.take (bytes);
   }

   private final void putint (int v) {
      bytes.write ((byte)v);
   }

   private final void putop (String op) {
      for (int i = 0; i < codes.length; i ++) {
         if (codes [i] == op) {
            putint (i);
            return;
         }
      }
      throw new IllegalArgumentException ("undefined op: " + op);
   }

   private final void putpar (int val) {
      if (val > 0) {
         putpar (val / 16);
         putint (240 + (val % 16));
      }
   }

   public void put (String op, String par) {
      Tokenizer.println ("                     (" + op + " " + par + ")");
      putpar (store.putStr (par));
      putop (op);
   }

   public void put (String op, int par) {
      Tokenizer.println ("                     (" + op + " " + par + ")");
      putpar (par);
      putop (op);
   }

   public void put (String op) {
      Tokenizer.println ("                     (" + op + ")");
      putop (op);
   }
}
