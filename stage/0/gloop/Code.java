// -*- mode: Java; c-basic-offset: 3; tab-width: 8; indent-tabs-mode: nil -*-
// Copyright (C) 2008 Andreas Krey, Ulm, Germany <a.krey@gmx.de>

package gloop;

import java.util.Vector;
import java.io.ByteArrayOutputStream;

public class Code {
   final public static String rcsid = "$Header$";

   CodeStore store;

   ByteArrayOutputStream bytes = new ByteArrayOutputStream ();

   private static String [] codes = {
      "nop",
      "deflocal",
      "varlocal",
      "print",
      "arg",
      "begin",
      "nullval",
      "push",
      "mult",
      "load",
      "numval",
      "strval",
      "push",
      "call",
      "fun",
      "meth",
      "ret"
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
      System.out.println ("                     (" + op + " " + par + ")");
      putpar (store.putStr (par));
      putop (op);
   }

   public void put (String op, int par) {
      System.out.println ("                     (" + op + " " + par + ")");
      putpar (par);
      putop (op);
   }

   public void put (String op) {
      System.out.println ("                     (" + op + ")");
      putop (op);
   }
}
