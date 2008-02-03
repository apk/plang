// -*- mode: Java; c-basic-offset: 3; tab-width: 8; indent-tabs-mode: nil -*-
// Copyright (C) 2008 Andreas Krey, Ulm, Germany <a.krey@gmx.de>

package gloop;

import java.util.Vector;
import java.io.ByteArrayOutputStream;

public class CodeStore {
   final public static String rcsid = "$Header$";

   private Vector<String> strings = new Vector<String> ();

   ByteArrayOutputStream bytes = new ByteArrayOutputStream ();

   private int last_take = 0;

   public int take (ByteArrayOutputStream s) {
      // return passfirst (bytes.size(), s.writeTo (bytes));
      int p = bytes.size ();
      try {
         s.writeTo (bytes);
      } catch (java.io.IOException e) {
         throw new IllegalArgumentException ("can't happen");
      }
      last_take = p;
      return p;
   }
      
   public int putStr (String s) {
      int i;
      for (i = 0; i < strings.size (); i ++) {
         if (strings.elementAt (i).equals (s)) return i;
      }
      strings.addElement (s);
      return i;
   }

   public void dump () {
      for (int i = 0; i < strings.size (); i ++) {
         System.out.println ("" + i + ": '" + strings.elementAt (i) + "'");
      }
      System.out.println ("Entry: " + last_take);
      byte [] a = bytes.toByteArray ();
      for (int i = 0; i < a.length; i ++) {
         int v = 255 & a [i];
         if (v >= 240) {
            System.out.println ("" + i + ": " + (v - 240));
         } else {
            String n = Code.getCode (v);
            if (n != null) {
               System.out.println ("" + i + ": " + n);
            } else {
               System.out.println ("" + i + ": ?" + v);
            }
         }
      }
   }
}
