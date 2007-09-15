// -*- mode: Java; c-basic-offset: 3; tab-width: 8; indent-tabs-mode: nil -*-
// Copyright (C) 2007 Andreas Krey, Ulm, Germany <a.krey@gmx.de>

package gloop;

import java.util.HashMap;
import java.util.Vector;

import java.io.IOException;
import static gloop.Tokenizer.*;

public class GlobalScope extends Scope {
   Vector<Tokenizer.Token> empty_tokens = new Vector<Tokenizer.Token> ();

   public GlobalScope () {
      new Ent ("let") {
         public Vector<Tokenizer.Token> macstmt (Parser p, Scope sc)
            throws IOException, Tokenizer.TokEx
         {
            /* Type tp = */ p.opttype (sc);
            /* String sym = */ p.sym (sc);
            p.chk (ASGN);
            p.pexpr (sc);
            p.chk (SEMI);
            return empty_tokens;
         }
      };
      new Ent ("var") {
         public Vector<Tokenizer.Token> macstmt (Parser p, Scope sc)
            throws IOException, Tokenizer.TokEx
         {
            /* Type tp = */ p.opttype (sc);
            /* String sym = */ p.sym (sc);
            p.chk (ASGN);
            p.pexpr (sc);
            p.chk (SEMI);
            return empty_tokens;
         }
      };
   }
}
