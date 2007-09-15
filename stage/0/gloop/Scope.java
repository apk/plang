// -*- mode: Java; c-basic-offset: 3; tab-width: 8; indent-tabs-mode: nil -*-
// Copyright (C) 2007 Andreas Krey, Ulm, Germany <a.krey@gmx.de>

package gloop;

import java.util.HashMap;
import java.util.Vector;
import java.io.IOException;

public class Scope {

   public class Ent {
      public Ent (String n) {
         ents.put (n, this);
      }

      public Vector<Tokenizer.Token> macstmt (Parser p, Scope sc)
         throws IOException, Tokenizer.TokEx
      {
         // Macro expanders either return empty list to indicate
         // internal processing of macro, or a token list that
         // is the expansion of a user-level macro.
         return null;
      }
   };

   Scope parent;
   HashMap<String,Ent> ents = new HashMap<String,Ent> ();

   public Ent get (String n) {
      return ents.get (n);
   }
}
