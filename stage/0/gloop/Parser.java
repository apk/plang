// -*- mode: Java; c-basic-offset: 3; tab-width: 8; indent-tabs-mode: nil -*-
// Copyright (C) 2007 Andreas Krey, Ulm, Germany <a.krey@gmx.de>

package gloop;

import java.io.IOException;
import java.util.Vector;

import static gloop.Tokenizer.*;

public class Parser {
   final Tokenizer tk;
   Tokenizer.Token tok;

   public Parser (Tokenizer t) throws IOException, Tokenizer.TokEx {
      tk = t;
      tok = tk.get ();
   }

   public Token get () throws IOException, TokEx{
      return tok = tk.get ();
   }

   public void parse (Scope sc, String endt) throws IOException, TokEx {
      while (!tok.is (endt)) {
         // Handle special case of stmt-macro
         // (XXX But let is later going to be an expr-macro!)
         if (tok.is (SYM)) {
            Scope.Ent e = sc.get (tok.val);
            if (e != null) {
               get ();
               Vector<Tokenizer.Token> toklist = e.macstmt (this, sc);
               if (toklist != null) {
                  // Ok, this is actually some kind of macro;
                  // either the list is empty (-> internally done),
                  // or it is a replacement.
                  tk.push (toklist);
                  continue;
               }
            }
            pexpr (sc);
         }
      }
   }

   public void pexpr (Scope sc) throws IOException, Tokenizer.TokEx {
      if (tok.is (NUM)) {
         System.out.println ("NUM");
         get ();
         return;
      }
      if (tok.is (STR)) {
         System.out.println ("STR");
         get ();
         return;
      }
      throw new IllegalArgumentException ("in pexpr (" + tok.tok + ")");
   }

   public void chk (String t) throws IOException, Tokenizer.TokEx {
      if (!tok.is (t)) {
         throw new IllegalArgumentException ("not a '" + t + "'");
      }
      get ();
   }

   public void sym (Scope sc) throws IOException, Tokenizer.TokEx {
      if (!tok.is (SYM)) {
         throw new IllegalArgumentException ("not a sym");
      }
      String s = tok.val;
      get ();
   }

   public void opttype (Scope sc) throws IOException, Tokenizer.TokEx {
      if (tok.is (TYP)) {
         get ();
         // return Type;
      }
      // return null
   }
}
