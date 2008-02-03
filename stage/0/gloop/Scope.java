// -*- mode: Java; c-basic-offset: 3; tab-width: 8; indent-tabs-mode: nil -*-
// Copyright (C) 2007, 2008 Andreas Krey, Ulm, Germany <a.krey@gmx.de>

package gloop;

import java.util.HashMap;
import java.util.Vector;
import java.io.IOException;

public class Scope {
   final public static String rcsid = "$Header$";

   abstract public class Ent {
      public Ent (String n) {
         ents.put (n, this);
      }

      public Vector<Tokenizer.Token> macstmt (Parser p, Code c, Scope sc)
         throws IOException, Tokenizer.TokEx
      {
         // Macro expanders either return empty list to indicate
         // internal processing of macro, or a token list that
         // is the expansion of a user-level macro.
         return null;
      }

      abstract public String desc ();

      public String toString () { return "<ent " + desc () + ">"; }
   };

   Scope parent;
   HashMap<String,Ent> ents = new HashMap<String,Ent> ();

   public Scope () {
      this (null);
   }

   public Scope (Scope par) {
      parent = par;
   }

   public Ent getRec (String n) {
      Ent e = ents.get (n);
      if (e != null) return e;
      if (parent != null) return parent.getRec (n);
      return null;
   }

   public void putDef (final String n) {
      // XXX Duplicate names are only an error within argument lists,
      // or within single let statements.
      new Ent (n) {
         public String desc () { return "<def " + n + ">"; }
      };
   }
   public void putVar (final String n) {
      new Ent (n) {
         public String desc () { return "<var " + n + ">"; }
      };
   }
}
